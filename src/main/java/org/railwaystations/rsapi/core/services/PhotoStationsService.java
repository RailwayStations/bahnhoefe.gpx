package org.railwaystations.rsapi.core.services;

import org.apache.commons.lang3.StringUtils;
import org.railwaystations.rsapi.adapter.out.db.StationDao;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.ports.in.FindPhotoStationsUseCase;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhotoStationsService implements FindPhotoStationsUseCase {

    private final StationDao stationDao;

    public PhotoStationsService(StationDao stationDao) {
        super();
        this.stationDao = stationDao;
    }

    @Override
    public Optional<Station> findByCountryAndId(String country, String stationId) {
        if (StringUtils.isBlank(stationId) || StringUtils.isBlank(country)) {
            return Optional.empty();
        }

        var key = new Station.Key(country, stationId);
        return stationDao.findByKey(key.getCountry(), key.getId()).stream().findFirst();
    }

    @Override
    public Set<Station> findRecentImports(Instant since) {
        return stationDao.findRecentImports(since);
    }

    @Override
    public Set<Station> findStationsBy(Set<String> countries, Boolean hasPhoto, Boolean active) {
        if (countries == null || countries.isEmpty()) {
            return stationDao.findByAllCountries(hasPhoto, active);
        } else {
            return stationDao.findByCountryCodes(countries, hasPhoto, active);
        }
    }

    @Override
    public Set<Station> findStationsBy(Set<String> countries, Boolean hasPhoto, String photographer, Boolean active) {
        var stations = findStationsBy(countries, hasPhoto, active);

        if (photographer == null) {
            return stations;
        }

        // TODO: can we search this on the DB?
        return stations.stream().filter(station -> station.appliesTo(photographer)).collect(Collectors.toSet());
    }

}
