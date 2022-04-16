package org.railwaystations.rsapi.adapter.in.web.controller;

import org.railwaystations.rsapi.adapter.in.web.writer.StationsGpxWriter;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.ports.in.FindPhotoStationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
public class StationsController {

    private static final String COUNTRY = "country";
    private static final String PHOTOGRAPHER = "photographer";
    private static final String HAS_PHOTO = "hasPhoto";
    private static final String MAX_DISTANCE = "maxDistance";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String ID = "id";
    private static final String ACTIVE = "active";
    private static final String SINCE_HOURS = "sinceHours";

    private final FindPhotoStationsUseCase findPhotoStationsUseCase;

    public StationsController(final FindPhotoStationsUseCase findPhotoStationsUseCase) {
        this.findPhotoStationsUseCase = findPhotoStationsUseCase;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", StationsGpxWriter.GPX_MEDIA_TYPE_VALUE}, value = "/stations")
    public List<Station> get(@RequestParam(value = COUNTRY, required = false) final Set<String> countries,
                             @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                             @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                             @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                             @RequestParam(value = LAT, required = false) final Double lat,
                             @RequestParam(value = LON, required = false) final Double lon,
                             @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return findPhotoStationsUseCase.findStationsBy(countries, hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"}, value = "/stations.json")
    public List<Station> getAsJson(@RequestParam(value = COUNTRY, required = false) final Set<String> countries,
                                  @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                                  @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                                  @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                                  @RequestParam(value = LAT, required = false) final Double lat,
                                  @RequestParam(value = LON, required = false) final Double lon,
                                  @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return get(countries, hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = {StationsGpxWriter.GPX_MEDIA_TYPE_VALUE}, value = "/stations.gpx")
    public List<Station> getAsGpx(@RequestParam(value = COUNTRY, required = false) final Set<String> countries,
                             @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                             @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                             @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                             @RequestParam(value = LAT, required = false) final Double lat,
                             @RequestParam(value = LON, required = false) final Double lon,
                             @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return get(countries, hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", StationsGpxWriter.GPX_MEDIA_TYPE_VALUE}, value = "/{country}/stations")
    public List<Station> getWithCountry(@PathVariable(COUNTRY) final String country,
                                        @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                                        @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                                        @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                                        @RequestParam(value = LAT, required = false) final Double lat,
                                        @RequestParam(value = LON, required = false) final Double lon,
                                        @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return get(Collections.singleton(country), hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"}, value = "/{country}/stations.json")
    public List<Station> getWithCountryAsJson(@PathVariable(COUNTRY) final String country,
                                              @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                                              @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                                              @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                                              @RequestParam(value = LAT, required = false) final Double lat,
                                              @RequestParam(value = LON, required = false) final Double lon,
                                              @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return getWithCountry(country, hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = {StationsGpxWriter.GPX_MEDIA_TYPE_VALUE}, value = "/{country}/stations.gpx")
    public List<Station> getWithCountryAsGpx(@PathVariable(COUNTRY) final String country,
                                              @RequestParam(value = HAS_PHOTO, required = false) final Boolean hasPhoto,
                                              @RequestParam(value = PHOTOGRAPHER, required = false) final String photographer,
                                              @RequestParam(value = MAX_DISTANCE, required = false) final Integer maxDistance,
                                              @RequestParam(value = LAT, required = false) final Double lat,
                                              @RequestParam(value = LON, required = false) final Double lon,
                                              @RequestParam(value = ACTIVE, required = false) final Boolean active) {
        return getWithCountry(country, hasPhoto, photographer, maxDistance, lat, lon, active);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", value = "/{country}/stations/{id}")
    public Station getById(@PathVariable(COUNTRY) final String country,
                           @PathVariable(ID) final String id) {
        return findPhotoStationsUseCase.findByCountryAndId(country, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", value = "/recentPhotoImports")
    public List<Station> recentPhotoImports(@RequestParam(value = SINCE_HOURS, required = false, defaultValue = "10") final long sinceHours) {
        return findPhotoStationsUseCase.findRecentImports(Instant.now().minus(sinceHours, ChronoUnit.HOURS));
    }

}