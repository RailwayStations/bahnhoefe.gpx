package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import java.util.Objects;

/**
 * counts the pending inbox entries
 */

@Schema(name = "InboxCountResponse", description = "counts the pending inbox entries")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-04-25T21:59:47.380653632+02:00[Europe/Berlin]")
public class InboxCountResponseDto   {

  @JsonProperty("pendingInboxEntries")
  private Long pendingInboxEntries;

  public InboxCountResponseDto pendingInboxEntries(Long pendingInboxEntries) {
    this.pendingInboxEntries = pendingInboxEntries;
    return this;
  }

  /**
   * Get pendingInboxEntries
   * @return pendingInboxEntries
  */
  
  @Schema(name = "pendingInboxEntries", required = false)
  public Long getPendingInboxEntries() {
    return pendingInboxEntries;
  }

  public void setPendingInboxEntries(Long pendingInboxEntries) {
    this.pendingInboxEntries = pendingInboxEntries;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InboxCountResponseDto inboxCountResponse = (InboxCountResponseDto) o;
    return Objects.equals(this.pendingInboxEntries, inboxCountResponse.pendingInboxEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pendingInboxEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InboxCountResponseDto {\n");
    sb.append("    pendingInboxEntries: ").append(toIndentedString(pendingInboxEntries)).append("\n");
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

