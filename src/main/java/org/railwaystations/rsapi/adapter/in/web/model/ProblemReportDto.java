package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a report of a problem with a station
 */

@JsonTypeName("ProblemReport")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-02-11T12:49:48.980080334+01:00[Europe/Berlin]")
public class ProblemReportDto {

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("stationId")
    private String stationId;

    @JsonProperty("photoId")
    private Long photoId;

    @JsonProperty("comment")
    private String comment;

    /**
     * Gets or Sets type
     */
    public enum TypeEnum {
        WRONG_LOCATION("WRONG_LOCATION"),

        STATION_ACTIVE("STATION_ACTIVE"),

        STATION_INACTIVE("STATION_INACTIVE"),

        STATION_NONEXISTENT("STATION_NONEXISTENT"),

        WRONG_PHOTO("WRONG_PHOTO"),

        PHOTO_OUTDATED("PHOTO_OUTDATED"),

        OTHER("OTHER"),

        WRONG_NAME("WRONG_NAME");

        private String value;

        TypeEnum(String value) {
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
        public static TypeEnum fromValue(String value) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    @JsonProperty("type")
    private TypeEnum type;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lon")
    private Double lon;

    public ProblemReportDto countryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    /**
     * Get countryCode
     *
     * @return countryCode
     */
    @NotNull
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public ProblemReportDto stationId(String stationId) {
        this.stationId = stationId;
        return this;
    }

    /**
     * Get stationId
     *
     * @return stationId
     */
    @NotNull
    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public ProblemReportDto photoId(Long photoId) {
        this.photoId = photoId;
        return this;
    }

    /**
     * Unique id of a photo, can be used for WRONG_PHOTO and PHOTO_OUTDATED type.
     *
     * @return photoId
     */

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public ProblemReportDto comment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Get comment
     *
     * @return comment
     */
    @NotNull
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ProblemReportDto type(TypeEnum type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     */
    @NotNull
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public ProblemReportDto lat(Double lat) {
        this.lat = lat;
        return this;
    }

    /**
     * Get lat
     *
     * @return lat
     */

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public ProblemReportDto lon(Double lon) {
        this.lon = lon;
        return this;
    }

    /**
     * Get lon
     *
     * @return lon
     */

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProblemReportDto problemReport = (ProblemReportDto) o;
        return Objects.equals(this.countryCode, problemReport.countryCode) &&
                Objects.equals(this.stationId, problemReport.stationId) &&
                Objects.equals(this.photoId, problemReport.photoId) &&
                Objects.equals(this.comment, problemReport.comment) &&
                Objects.equals(this.type, problemReport.type) &&
                Objects.equals(this.lat, problemReport.lat) &&
                Objects.equals(this.lon, problemReport.lon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, stationId, photoId, comment, type, lat, lon);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProblemReportDto {\n");
        sb.append("    countryCode: ").append(toIndentedString(countryCode)).append("\n");
        sb.append("    stationId: ").append(toIndentedString(stationId)).append("\n");
        sb.append("    photoId: ").append(toIndentedString(photoId)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    lat: ").append(toIndentedString(lat)).append("\n");
        sb.append("    lon: ").append(toIndentedString(lon)).append("\n");
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

