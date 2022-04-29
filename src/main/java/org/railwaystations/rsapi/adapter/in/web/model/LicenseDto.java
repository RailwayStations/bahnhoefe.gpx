package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * Gets or Sets License
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-04-25T21:59:47.380653632+02:00[Europe/Berlin]")
public enum LicenseDto {
  
  CC0("CC0"),
  
  CC0_1_0_UNIVERSELL_CC0_1_0_("CC0 1.0 Universell (CC0 1.0)"),
  
  CC4("CC4"),
  
  CC_BY_SA_4_0("CC BY-SA 4.0");

  private String value;

  LicenseDto(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static LicenseDto fromValue(String value) {
    for (LicenseDto b : LicenseDto.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
