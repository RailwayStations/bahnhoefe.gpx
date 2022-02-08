package org.railwaystations.rsapi.resources;

import org.railwaystations.rsapi.StationsRepository;
import org.railwaystations.rsapi.model.Statistic;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticResource {

    private static final String COUNTRY = "country";

    private final StationsRepository repository;

    public StatisticResource(final StationsRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE}, value = "/stats")
    public Statistic get(@Nullable @RequestParam(StatisticResource.COUNTRY) final String country) {
        return getWithCountry(country);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/stats.json")
    public Statistic getAsJson(@Nullable @RequestParam(StatisticResource.COUNTRY) final String country) {
        return getWithCountry(country);
    }

    @GetMapping(produces = {MediaType.TEXT_PLAIN_VALUE}, value = "/stats.txt")
    public Statistic getAsText(@Nullable @RequestParam(StatisticResource.COUNTRY) final String country) {
        return getWithCountry(country);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE}, value = "/{country}/stats")
    public Statistic getWithCountry(@PathVariable(StatisticResource.COUNTRY) final String country) {
        return getStatisticMap(country);
    }

    @GetMapping(produces = {MediaType.TEXT_PLAIN_VALUE}, value = "/{country}/stats.txt")
    public Statistic getWithCountryAsText(@PathVariable(StatisticResource.COUNTRY) final String country) {
        return getStatisticMap(country);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, value = "/{country}/stats.json")
    public Statistic getWithCountryAsJson(@PathVariable(StatisticResource.COUNTRY) final String country) {
        return getStatisticMap(country);
    }

    private Statistic getStatisticMap(final String country) {
        return repository.getStatistic(country);
    }

}