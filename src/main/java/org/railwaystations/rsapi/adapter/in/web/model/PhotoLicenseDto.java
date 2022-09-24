package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * License used by a photo
 */

@JsonTypeName("PhotoLicense")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-09-21T21:45:29.210227244+02:00[Europe/Berlin]")
public class PhotoLicenseDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    public PhotoLicenseDto id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Unique id of the license
     *
     * @return id
     */
    @NotNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PhotoLicenseDto name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Name of the license to display at the photo
     *
     * @return name
     */
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhotoLicenseDto url(String url) {
        this.url = url;
        return this;
    }

    /**
     * URL of the license to link to from the photo
     *
     * @return url
     */
    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhotoLicenseDto photoLicense = (PhotoLicenseDto) o;
        return Objects.equals(this.id, photoLicense.id) &&
                Objects.equals(this.name, photoLicense.name) &&
                Objects.equals(this.url, photoLicense.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PhotoLicenseDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

