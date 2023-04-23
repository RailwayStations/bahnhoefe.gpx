package org.railwaystations.rsapi.adapter.in.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.Objects;

/**
 * License used by a photo
 */

@JsonTypeName("PhotoLicense")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-04-23T18:41:39.638497575+02:00[Europe/Berlin]")
public class PhotoLicenseDto {

    private String id;

    private String name;

    private URI url;

    /**
     * Default constructor
     *
     * @deprecated Use {@link PhotoLicenseDto#PhotoLicenseDto(String, String, URI)}
     */
    @Deprecated
    public PhotoLicenseDto() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public PhotoLicenseDto(String id, String name, URI url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

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
    @JsonProperty("id")
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
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhotoLicenseDto url(URI url) {
        this.url = url;
        return this;
    }

    /**
     * URL of the license to link to from the photo
     *
     * @return url
     */
    @NotNull
    @Valid
    @JsonProperty("url")
    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
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

