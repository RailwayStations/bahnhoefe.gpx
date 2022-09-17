package org.railwaystations.rsapi.adapter.in.web.controller;

import org.railwaystations.rsapi.adapter.in.web.model.PhotoDto;
import org.railwaystations.rsapi.adapter.in.web.model.PhotoLicenseDto;
import org.railwaystations.rsapi.adapter.in.web.model.PhotoStationDto;
import org.railwaystations.rsapi.adapter.in.web.model.PhotoStationsDto;
import org.railwaystations.rsapi.adapter.in.web.model.PhotographerDto;
import org.railwaystations.rsapi.core.model.License;
import org.railwaystations.rsapi.core.model.Photo;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.model.User;
import org.railwaystations.rsapi.core.ports.in.FindPhotoStationsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@RestController
public class PhotoStationsController {

    @Autowired
    private FindPhotoStationsUseCase findPhotoStationsUseCase;

    @Value("${photoBaseUrl}")
    private String photoBaseUrl;

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"}, value = "/photoStationById/{country}/{id}")
    public PhotoStationsDto get(@PathVariable(value = "country") String country,
                                @PathVariable(value = "id") String id) {
        var station = findPhotoStationsUseCase.findByCountryAndId(country, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new PhotoStationsDto()
                .photoBaseUrl(photoBaseUrl)
                .licenses(mapLicenses(station))
                .photographers(mapPhotographers(station))
                .stations(mapStations(station));
    }

    private List<PhotoStationDto> mapStations(Station station) {
        return List.of(new PhotoStationDto()
                .country(station.getKey().getCountry())
                .id(station.getKey().getId())
                .title(station.getTitle())
                .lat(station.getCoordinates().getLat())
                .lon(station.getCoordinates().getLon())
                .shortCode(station.getDs100())
                .inactive(station.isActive() ? null : Boolean.TRUE)
                .photos(mapPhotos(station))
        );
    }

    private List<PhotoDto> mapPhotos(Station station) {
        return station.getPhotos().stream()
                .map(photo -> new PhotoDto()
                        .id(photo.getId())
                        .path(photo.getUrlPath())
                        .photographer(photo.getPhotographer().getDisplayName())
                        .license(photo.getLicense().name())
                        .createdAt(photo.getCreatedAt().toEpochMilli())
                        .outdated(photo.isOutdated() ? Boolean.TRUE : null))
                .toList();
    }

    private List<PhotographerDto> mapPhotographers(Station station) {
        return station.getPhotos().stream()
                .map(Photo::getPhotographer)
                .collect(toMap(User::getDisplayName, Function.identity(), (user1, user2) -> user1))
                .values().stream()
                .map(user -> new PhotographerDto()
                        .name(user.getDisplayName())
                        .url(user.getDisplayUrl()))
                .toList();
    }

    private List<PhotoLicenseDto> mapLicenses(Station station) {
        return station.getPhotos().stream()
                .map(Photo::getLicense)
                .collect(toMap(License::name, Function.identity(), (license1, license2) -> license1))
                .values().stream()
                .map(license -> new PhotoLicenseDto()
                        .id(license.name())
                        .name(license.getDisplayName())
                        .url(license.getUrl()))
                .toList();
    }

}
