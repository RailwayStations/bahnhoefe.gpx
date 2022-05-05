package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * NextZResponseDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-05-01T17:58:43.387298988+02:00[Europe/Berlin]")
public class NextZResponseDto   {

  @JsonProperty("nextZ")
  private String nextZ;

  public NextZResponseDto nextZ(String nextZ) {
    this.nextZ = nextZ;
    return this;
  }

  /**
   * Get nextZ
   * @return nextZ
  */
  @NotNull 
  @Schema(name = "nextZ", required = true)
  public String getNextZ() {
    return nextZ;
  }

  public void setNextZ(String nextZ) {
    this.nextZ = nextZ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NextZResponseDto nextZResponse = (NextZResponseDto) o;
    return Objects.equals(this.nextZ, nextZResponse.nextZ);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nextZ);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NextZResponseDto {\n");
    sb.append("    nextZ: ").append(toIndentedString(nextZ)).append("\n");
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
}
