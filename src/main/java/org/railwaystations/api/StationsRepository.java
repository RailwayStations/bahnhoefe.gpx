package org.railwaystations.api;

import org.apache.commons.lang3.StringUtils;
import org.railwaystations.api.db.CountryDao;
import org.railwaystations.api.db.StationDao;
import org.railwaystations.api.model.Country;
import org.railwaystations.api.model.Station;
import org.railwaystations.api.model.Statistic;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StationsRepository {

    private final CountryDao countryDao;
    private final StationDao stationDao;

    public StationsRepository(final CountryDao countryDao, final StationDao stationDao) {
        super();
        this.countryDao = countryDao;
        this.stationDao = stationDao;
    }

    public Map<Station.Key, Station> getStationsByCountry(final Set<String> countryCodes) {
        final Set<Station> stations;
        if (countryCodes == null || countryCodes.isEmpty()) {
            stations = stationDao.all();
        } else {
            stations = stationDao.findByCountryCodes(countryCodes);
        }
        return stations.stream().collect(Collectors.toMap(Station::getKey, Function.identity()));
    }

    public Set<Country> getCountries() {
        return Collections.unmodifiableSet(countryDao.list());
    }

    public String getCountryStatisticMessage() {
        final StringBuilder message = new StringBuilder("Countries statistic: \n");
        for (final Country aCountry : getCountries()) {
            final Statistic stat = getStatistic(aCountry.getCode());
            message.append("- ")
                    .append(aCountry.getCode())
                    .append(": ")
                    .append(stat.getWithPhoto())
                    .append(" of ")
                    .append(stat.getTotal())
                    .append("\n");
        }
        return message.toString();
    }

    public Map<Station.Key, String> findByName(final String name) {
        return stationDao.findByName(name);
    }

    public Statistic getStatistic(final String country) {
        return stationDao.getStatistic(country);
    }

    public Station findByCountryAndId(final String country, final String stationId) {
        if (StringUtils.isBlank(country) || StringUtils.isBlank(stationId)) {
            return null;
        }
        final Station.Key key = new Station.Key(country, stationId);
        return findByKey(key);
    }

    public Station findByKey(final Station.Key key) {
        return stationDao.findByKey(key.getCountry(), key.getId()).stream().findFirst().orElse(null);
    }

    public Map<String, Long> getPhotographerMap(final String country) {
        return stationDao.getPhotographerMap(country);
    }

}
