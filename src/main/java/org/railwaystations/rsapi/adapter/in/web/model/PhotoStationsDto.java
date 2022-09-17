package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stations with photos
 */

@Schema(name = "PhotoStations", description = "Stations with photos")
@JsonTypeName("PhotoStations")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-09-17T10:27:28.459965650+02:00[Europe/Berlin]")
public class PhotoStationsDto {

  @JsonProperty("photoBaseUrl")
  private String photoBaseUrl;

  @JsonProperty("licenses")
  @Valid
  private List<PhotoLicenseDto> licenses = new ArrayList<>();

  @JsonProperty("photographers")
  @Valid
  private List<PhotographerDto> photographers = new ArrayList<>();

  @JsonProperty("stations")
  @Valid
  private List<PhotoStationDto> stations = new ArrayList<>();

  public PhotoStationsDto photoBaseUrl(String photoBaseUrl) {
    this.photoBaseUrl = photoBaseUrl;
    return this;
  }

  /**
   * Base URL of all photos
   * @return photoBaseUrl
  */
  @NotNull 
  @Schema(name = "photoBaseUrl", example = "https://api.railway-stations.org/photos/", description = "Base URL of all photos", required = true)
  public String getPhotoBaseUrl() {
    return photoBaseUrl;
  }

  public void setPhotoBaseUrl(String photoBaseUrl) {
    this.photoBaseUrl = photoBaseUrl;
  }

  public PhotoStationsDto licenses(List<PhotoLicenseDto> licenses) {
    this.licenses = licenses;
    return this;
  }

  public PhotoStationsDto addLicensesItem(PhotoLicenseDto licensesItem) {
    if (this.licenses == null) {
      this.licenses = new ArrayList<>();
    }
    this.licenses.add(licensesItem);
    return this;
  }

  /**
   * List of used licenses, might be empty if no photos available
   * @return licenses
  */
  @NotNull @Valid 
  @Schema(name = "licenses", description = "List of used licenses, might be empty if no photos available", required = true)
  public List<PhotoLicenseDto> getLicenses() {
    return licenses;
  }

  public void setLicenses(List<PhotoLicenseDto> licenses) {
    this.licenses = licenses;
  }

  public PhotoStationsDto photographers(List<PhotographerDto> photographers) {
    this.photographers = photographers;
    return this;
  }

  public PhotoStationsDto addPhotographersItem(PhotographerDto photographersItem) {
    if (this.photographers == null) {
      this.photographers = new ArrayList<>();
    }
    this.photographers.add(photographersItem);
    return this;
  }

  /**
   * List of all photographers, might be empty if no photos available
   * @return photographers
  */
  @NotNull @Valid 
  @Schema(name = "photographers", description = "List of all photographers, might be empty if no photos available", required = true)
  public List<PhotographerDto> getPhotographers() {
    return photographers;
  }

  public void setPhotographers(List<PhotographerDto> photographers) {
    this.photographers = photographers;
  }

  public PhotoStationsDto stations(List<PhotoStationDto> stations) {
    this.stations = stations;
    return this;
  }

  public PhotoStationsDto addStationsItem(PhotoStationDto stationsItem) {
    if (this.stations == null) {
      this.stations = new ArrayList<>();
    }
    this.stations.add(stationsItem);
    return this;
  }

  /**
   * List of the stations
   * @return stations
  */
  @NotNull @Valid 
  @Schema(name = "stations", description = "List of the stations", required = true)
  public List<PhotoStationDto> getStations() {
    return stations;
  }

  public void setStations(List<PhotoStationDto> stations) {
    this.stations = stations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PhotoStationsDto photoStations = (PhotoStationsDto) o;
    return Objects.equals(this.photoBaseUrl, photoStations.photoBaseUrl) &&
        Objects.equals(this.licenses, photoStations.licenses) &&
        Objects.equals(this.photographers, photoStations.photographers) &&
        Objects.equals(this.stations, photoStations.stations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(photoBaseUrl, licenses, photographers, stations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PhotoStationsDto {\n");
    sb.append("    photoBaseUrl: ").append(toIndentedString(photoBaseUrl)).append("\n");
    sb.append("    licenses: ").append(toIndentedString(licenses)).append("\n");
    sb.append("    photographers: ").append(toIndentedString(photographers)).append("\n");
    sb.append("    stations: ").append(toIndentedString(stations)).append("\n");
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

