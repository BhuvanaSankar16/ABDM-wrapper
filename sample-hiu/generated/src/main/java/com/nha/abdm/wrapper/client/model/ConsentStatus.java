/*
 * Swagger HIU Facade - OpenAPI 3.0
 * This is a set of interfaces based on the OpenAPI 3.0 specification for a wrapper client
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.nha.abdm.wrapper.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.nha.abdm.wrapper.client.model.IdRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nha.abdm.wrapper.client.invoker.JSON;

/**
 * ConsentStatus
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-02-09T05:40:12.260755493Z[Etc/UTC]")
public class ConsentStatus {
  public static final String SERIALIZED_NAME_ID = "id";
  @SerializedName(SERIALIZED_NAME_ID)
  private String id;

  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private String status;

  public static final String SERIALIZED_NAME_CONSENT_ARTEFACTS = "consentArtefacts";
  @SerializedName(SERIALIZED_NAME_CONSENT_ARTEFACTS)
  private List<IdRequest> consentArtefacts;

  public ConsentStatus() {
  }

  public ConsentStatus id(String id) {
    
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @javax.annotation.Nullable
  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public ConsentStatus status(String status) {
    
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  @javax.annotation.Nullable
  public String getStatus() {
    return status;
  }


  public void setStatus(String status) {
    this.status = status;
  }


  public ConsentStatus consentArtefacts(List<IdRequest> consentArtefacts) {
    
    this.consentArtefacts = consentArtefacts;
    return this;
  }

  public ConsentStatus addConsentArtefactsItem(IdRequest consentArtefactsItem) {
    if (this.consentArtefacts == null) {
      this.consentArtefacts = new ArrayList<>();
    }
    this.consentArtefacts.add(consentArtefactsItem);
    return this;
  }

   /**
   * Get consentArtefacts
   * @return consentArtefacts
  **/
  @javax.annotation.Nullable
  public List<IdRequest> getConsentArtefacts() {
    return consentArtefacts;
  }


  public void setConsentArtefacts(List<IdRequest> consentArtefacts) {
    this.consentArtefacts = consentArtefacts;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentStatus consentStatus = (ConsentStatus) o;
    return Objects.equals(this.id, consentStatus.id) &&
        Objects.equals(this.status, consentStatus.status) &&
        Objects.equals(this.consentArtefacts, consentStatus.consentArtefacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, consentArtefacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentStatus {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    consentArtefacts: ").append(toIndentedString(consentArtefacts)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("id");
    openapiFields.add("status");
    openapiFields.add("consentArtefacts");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to ConsentStatus
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!ConsentStatus.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ConsentStatus is not found in the empty JSON string", ConsentStatus.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!ConsentStatus.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `ConsentStatus` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }
      if ((jsonObj.get("id") != null && !jsonObj.get("id").isJsonNull()) && !jsonObj.get("id").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `id` to be a primitive type in the JSON string but got `%s`", jsonObj.get("id").toString()));
      }
      if ((jsonObj.get("status") != null && !jsonObj.get("status").isJsonNull()) && !jsonObj.get("status").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `status` to be a primitive type in the JSON string but got `%s`", jsonObj.get("status").toString()));
      }
      if (jsonObj.get("consentArtefacts") != null && !jsonObj.get("consentArtefacts").isJsonNull()) {
        JsonArray jsonArrayconsentArtefacts = jsonObj.getAsJsonArray("consentArtefacts");
        if (jsonArrayconsentArtefacts != null) {
          // ensure the json data is an array
          if (!jsonObj.get("consentArtefacts").isJsonArray()) {
            throw new IllegalArgumentException(String.format("Expected the field `consentArtefacts` to be an array in the JSON string but got `%s`", jsonObj.get("consentArtefacts").toString()));
          }

          // validate the optional field `consentArtefacts` (array)
          for (int i = 0; i < jsonArrayconsentArtefacts.size(); i++) {
            IdRequest.validateJsonObject(jsonArrayconsentArtefacts.get(i).getAsJsonObject());
          };
        }
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ConsentStatus.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ConsentStatus' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ConsentStatus> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ConsentStatus.class));

       return (TypeAdapter<T>) new TypeAdapter<ConsentStatus>() {
           @Override
           public void write(JsonWriter out, ConsentStatus value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public ConsentStatus read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of ConsentStatus given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ConsentStatus
  * @throws IOException if the JSON string is invalid with respect to ConsentStatus
  */
  public static ConsentStatus fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ConsentStatus.class);
  }

 /**
  * Convert an instance of ConsentStatus to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

