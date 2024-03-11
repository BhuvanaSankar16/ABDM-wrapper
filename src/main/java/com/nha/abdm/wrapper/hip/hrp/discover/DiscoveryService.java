/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.discover;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.models.CareContext;
import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.GatewayCallbackResponse;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.HIPClient;
import com.nha.abdm.wrapper.hip.HIPPatient;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.DiscoverRequest;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.OnDiscoverErrorRequest;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.OnDiscoverPatient;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.OnDiscoverRequest;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscoveryService implements DiscoveryInterface {

  private final PatientRepo patientRepo;
  private final RequestManager requestManager;
  private final HIPClient hipClient;
  private final RequestLogService requestLogService;
  private final PatientService patientService;

  JaroWinkler jaroWinkler = new JaroWinkler();

  @Value("${onDiscoverPath}")
  public String onDiscoverPath;

  @Autowired
  public DiscoveryService(
      RequestManager requestManager,
      HIPClient hipClient,
      PatientRepo patientRepo,
      RequestLogService requestLogService,
      PatientService patientService) {
    this.requestManager = requestManager;
    this.hipClient = hipClient;
    this.patientRepo = patientRepo;
    this.requestLogService = requestLogService;
    this.patientService = patientService;
  }

  private static final Logger log = LogManager.getLogger(DiscoveryService.class);

  /**
   * <B>discovery</B>
   *
   * <p>Using the demographic details and abhaAddress fetching careContexts from db.<br>
   * Logic ->step 1: Check for AbhaAddress, if present build discoverRequest and make POST
   * /discover.<br>
   * step 2: fetch list of users with mobileNumber, then check patientIdentifier if present, then
   * return careContexts.<br>
   * if patientIdentifier present and not matched return null/not found.<br>
   * if patientIdentifier not present check for gender, then +-5 years in Year of birth, then name
   * with fuzzy logic, if any of the above demographics fail to match return null/ not matched.<br>
   * build discoverRequest and make POST /on-discover.
   *
   * @param discoverRequest Response from ABDM gateway with patient demographic details and
   *     abhaAddress.
   */
  @Override
  public ResponseEntity<GatewayCallbackResponse> discover(DiscoverRequest discoverRequest) {
    if (Objects.isNull(discoverRequest) || Objects.isNull(discoverRequest.getPatient())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    String abhaAddress = discoverRequest.getPatient().getId();
    String yearOfBirth = discoverRequest.getPatient().getYearOfBirth();
    String gender = discoverRequest.getPatient().getGender();
    String name = discoverRequest.getPatient().getName();

    // First find patient using their abha address.
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress);
    // If there is no match by abha address, then the lookup should be done by mobile number
    // and patient reference number.
    if (Objects.isNull(patient)) {
      Optional<Patient> patientMatch;
      if (!CollectionUtils.isEmpty(discoverRequest.getPatient().getVerifiedIdentifiers())) {
        String patientMobileNumber =
            discoverRequest.getPatient().getVerifiedIdentifiers().get(0).getValue();
        // If mobile number was provided in request as verified identifier.
        if (StringUtils.hasLength(patientMobileNumber)) {
          patientMatch =
              findPatientUsingMobile(
                  discoverRequest, patientMobileNumber, yearOfBirth, gender, name);
          if (patientMatch.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(1000);
            errorResponse.setMessage("HIP -> Patient details mismatch with mobile");
            onDiscoverNoPatientRequest(discoverRequest, errorResponse);
          } else {
            patient = patientMatch.get();
          }
        } else if (!CollectionUtils.isEmpty(
            discoverRequest.getPatient().getUnverifiedIdentifiers())) {
          // Now search using patient reference number.
          String patientReferenceNumber =
              discoverRequest.getPatient().getUnverifiedIdentifiers().get(0).getValue();
          // If patient reference number was provided in request as unverified identifier.
          if (StringUtils.hasLength(patientReferenceNumber)) {
            Patient patientByReference = patientRepo.findByPatientReference(patientReferenceNumber);
            // If patient is not found in database using their reference number
            // or if found but not matched by demographics, then send error.
            if (Objects.isNull(patientByReference)
                || !(isGenderMatch(patientByReference, gender)
                    && (isYearOfBirthInRange(patientByReference, yearOfBirth)
                        && (isFuzzyNameMatch(patientByReference, name))))) {
              ErrorResponse errorResponse = new ErrorResponse();
              errorResponse.setCode(1000);
              errorResponse.setMessage("HIP -> Patient details mismatch with reference number");
              onDiscoverNoPatientRequest(discoverRequest, errorResponse);
            } else {
              patient = patientByReference;
            }
          }
        } else {
          // Patient not found in database. Request Patient details from HIP.
          HIPPatient hipPatient = hipClient.getPatient(abhaAddress);
          onDiscoverRequest(
              discoverRequest,
              hipPatient.getPatientReference(),
              hipPatient.getPatientDisplay(),
              hipPatient.getCareContexts());
          addPatienttoDatabase(hipPatient);
        }
      }
    }
    processCareContexts(patient, abhaAddress, discoverRequest);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  private void addPatienttoDatabase(HIPPatient hipPatient) {
    Patient patient = new Patient();
    patient.setName(hipPatient.getName());
    patient.setDisplay(hipPatient.getPatientDisplay());
    patient.setPatientMobile(hipPatient.getPatientMobile());
    patient.setDateOfBirth(hipPatient.getDateOfBirth());
    patient.setGender(hipPatient.getGender());
    patient.setAbhaAddress(hipPatient.getAbhaAddress());
    patient.setPatientReference(hipPatient.getPatientReference());

    patientService.upsertPatients(Arrays.asList(patient));
  }

  private Optional<Patient> findPatientUsingMobile(
      DiscoverRequest discoverRequest,
      String patientMobileNumber,
      String yearOfBirth,
      String gender,
      String name) {
    List<Patient> patientsByMobileNumber = patientRepo.findByPatientMobile(patientMobileNumber);
    if (!CollectionUtils.isEmpty(discoverRequest.getPatient().getUnverifiedIdentifiers())) {
      String patientReferenceNumber =
          discoverRequest.getPatient().getUnverifiedIdentifiers().get(0).getValue();
      // If patient reference number was provided in request as unverified identifier.
      if (StringUtils.hasLength(patientReferenceNumber)) {
        return patientsByMobileNumber.stream()
            .filter(p -> patientReferenceNumber.equals(p.getPatientReference()))
            .findFirst();
      } else {
        return matchPatientByDemographics(patientsByMobileNumber, yearOfBirth, gender, name);
      }
    } else {
      return matchPatientByDemographics(patientsByMobileNumber, yearOfBirth, gender, name);
    }
  }

  private Optional<Patient> matchPatientByDemographics(
      List<Patient> patientsByMobileNumber, String yearOfBirth, String gender, String name) {
    return patientsByMobileNumber.stream()
        .filter(patient -> isGenderMatch(patient, gender))
        .filter(patient -> isYearOfBirthInRange(patient, yearOfBirth))
        .filter(patient -> isFuzzyNameMatch(patient, name))
        .findFirst();
  }

  private void processCareContexts(
      Patient patient, String abhaAddress, DiscoverRequest discoverRequest) {
    // Get Linked Care Contexts which were fetched from database.
    List<CareContext> linkedCareContexts = patient.getCareContexts();
    // Get All Care Contexts of the given patient from HIP.
    HIPPatient hipPatient = hipClient.getPatientCareContexts(abhaAddress);
    if (Objects.isNull(hipPatient)) {
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(1000);
      errorResponse.setMessage("HIP -> Patient not found in HIP");
      onDiscoverNoPatientRequest(discoverRequest, errorResponse);
    } else if (CollectionUtils.isEmpty(hipPatient.getCareContexts())) {
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(1000);
      errorResponse.setMessage("HIP -> Care Contexts not found for patient: " + abhaAddress);
      onDiscoverNoPatientRequest(discoverRequest, errorResponse);
    } else {
      List<CareContext> careContexts = hipPatient.getCareContexts();
      List<CareContext> unlinkedCareContexts;
      if (CollectionUtils.isEmpty(linkedCareContexts)) {
        unlinkedCareContexts = careContexts;
      } else {
        Set<String> linkedCareContextsSet =
            linkedCareContexts.stream()
                .map(x -> x.getReferenceNumber())
                .collect(Collectors.toSet());
        unlinkedCareContexts =
            careContexts.stream()
                .filter(x -> !linkedCareContextsSet.contains(x.getReferenceNumber()))
                .collect(Collectors.toList());
      }
      onDiscoverRequest(
          discoverRequest,
          hipPatient.getPatientReference(),
          hipPatient.getPatientDisplay(),
          unlinkedCareContexts);
    }
  }

  /**
   * <B>Discovery</B>
   *
   * <p>Build the body with the respective careContexts into onDiscoverRequest.
   *
   * @param discoverRequest Response from ABDM gateway.
   * @param patientReference Patient reference number.
   * @param display Patient display name.
   * @param careContexts list of non-linked careContexts.
   */
  private void onDiscoverRequest(
      DiscoverRequest discoverRequest,
      String patientReference,
      String display,
      List<CareContext> careContexts) {

    OnDiscoverPatient onDiscoverPatient =
        OnDiscoverPatient.builder()
            .referenceNumber(patientReference)
            .display(display)
            .careContexts(careContexts)
            .matchedBy(Arrays.asList("MOBILE"))
            .build();
    OnDiscoverRequest onDiscoverRequest =
        OnDiscoverRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp().toString())
            .transactionId(discoverRequest.getTransactionId())
            .patient(onDiscoverPatient)
            .resp(RespRequest.builder().requestId(discoverRequest.getRequestId()).build())
            .build();
    log.info("onDiscover : " + onDiscoverRequest.toString());
    try {
      ResponseEntity<GenericResponse> responseEntity =
          requestManager.fetchResponseFromGateway(onDiscoverPath, onDiscoverRequest);
      log.info(onDiscoverPath + " : onDiscoverCall: " + responseEntity.getStatusCode());
      requestLogService.setDiscoverResponse(discoverRequest);
    } catch (Exception e) {
      log.info("Error: " + e);
    }
  }

  /**
   * <B>discovery</B>
   *
   * <p>build of onDiscoverRequest with error when patient not found.
   *
   * @param discoverRequest Response from ABDM gateway.
   * @param errorResponse The respective error message while matching patient data.
   */
  private void onDiscoverNoPatientRequest(
      DiscoverRequest discoverRequest, ErrorResponse errorResponse) {

    OnDiscoverErrorRequest onDiscoverErrorRequest =
        OnDiscoverErrorRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp().toString())
            .transactionId(discoverRequest.getTransactionId())
            .resp(RespRequest.builder().requestId(discoverRequest.getRequestId()).build())
            .error(errorResponse)
            .build();
    log.info("onDiscover : " + onDiscoverErrorRequest.toString());
    try {
      requestManager.fetchResponseFromGateway(onDiscoverPath, onDiscoverErrorRequest);
      log.info(
          onDiscoverPath
              + " Discover: requestId : "
              + discoverRequest.getRequestId()
              + ": Patient not found");
    } catch (Exception e) {
      log.error(e);
    }
  }

  /**
   * <B>discovery</B> Check of Year Of Birth in the range of +-5 years
   *
   * @param patient Particular patient record.
   * @param receivedYob Response : Year Of Birth.
   * @return true if in range else false
   */
  private boolean isYearOfBirthInRange(Patient patient, String receivedYob) {
    int existingDate = Integer.parseInt(patient.getDateOfBirth().substring(0, 4));
    return Math.abs(existingDate - Integer.parseInt(receivedYob)) <= 5;
  }

  /**
   * <B>discovery</B> Check of gender match with response gender.
   *
   * @param patient particular patient record.
   * @param receivedGender Response : gender.
   * @return true if gender matches or else false.
   */
  private boolean isGenderMatch(Patient patient, String receivedGender) {
    return receivedGender.equals(patient.getGender());
  }

  /**
   * <B>discovery</B> Matching of patient name with response name by jaroWinkler algorithm making
   * 0.5 a reasonable validation.
   *
   * @param patient particular patient record.
   * @param receivedName Response : name.
   * @return true if name matches or else false.
   */
  private boolean isFuzzyNameMatch(Patient patient, String receivedName) {
    return jaroWinkler.similarity(patient.getName(), receivedName) >= 0.5;
  }
}
