/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.facade.consent;

import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.HIUConsentInterface;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.FetchPatientConsentRequest;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.InitConsentRequest;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.ConsentResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.ConsentStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HIUFacadeConsentController {

  private final HIUConsentInterface hiuConsentInterface;

  @Autowired
  public HIUFacadeConsentController(HIUConsentInterface hiuConsentInterface) {
    this.hiuConsentInterface = hiuConsentInterface;
  }

  @PostMapping({"/consent-init"})
  public ResponseEntity<FacadeResponse> initiateConsentRequest(
      @RequestBody InitConsentRequest initConsentRequest) {
    FacadeResponse facadeResponse = hiuConsentInterface.initiateConsentRequest(initConsentRequest);
    return new ResponseEntity<>(facadeResponse, facadeResponse.getHttpStatusCode());
  }

  @GetMapping({"/consent-status/{requestId}"})
  public ResponseEntity<ConsentStatusResponse> consentRequestStatus(
      @PathVariable("requestId") String clientRequestId) throws IllegalDataStateException {
    ConsentStatusResponse consentStatusResponse =
        hiuConsentInterface.consentRequestStatus(clientRequestId);
    return new ResponseEntity<>(consentStatusResponse, consentStatusResponse.getHttpStatusCode());
  }

  @PostMapping({"/fetch-consent"})
  public ResponseEntity<ConsentResponse> fetchConsent(
      @RequestBody FetchPatientConsentRequest fetchPatientConsentRequest)
      throws IllegalDataStateException {
    ConsentResponse consentResponse = hiuConsentInterface.fetchConsent(fetchPatientConsentRequest);
    return new ResponseEntity<>(consentResponse, consentResponse.getHttpStatusCode());
  }

  @ExceptionHandler(IllegalDataStateException.class)
  private ResponseEntity<FacadeResponse> handleIllegalDataStateException(
      IllegalDataStateException ex) {
    return new ResponseEntity<>(
        FacadeResponse.builder()
            .error(ErrorResponse.builder().message(ex.getMessage()).build())
            .build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
