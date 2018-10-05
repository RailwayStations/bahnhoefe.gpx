package org.railwaystations.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("PMD.UnusedPrivateField")
public class Photographer {

    private final String name;
    private final String url;
    private final String license;

    @JsonIgnore
    private int id;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String normalizedName;
    @JsonIgnore
    private boolean ownPhotos;
    @JsonIgnore
    private boolean anonymous;
    @JsonIgnore
    private Long uploadTokenSalt;


    public Photographer(final String name, final String url, final String license) {
        this.name = name;
        this.url = url;
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getLicense() {
        return license;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Photographer that = (Photographer) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

}
