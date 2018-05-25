package org.railwaystations.api.loader;

import com.fasterxml.jackson.databind.JsonNode;
import org.railwaystations.api.model.Station;
import org.railwaystations.api.model.Country;
import org.railwaystations.api.model.Photo;
import org.railwaystations.api.monitoring.Monitor;

import java.net.URL;
import java.util.Map;

public class StationLoaderCh extends BaseStationLoader {

    public StationLoaderCh(final Country country, final URL photosUrl, final URL stationsUrl, final Monitor monitor) {
        super(country, photosUrl, stationsUrl, monitor);
    }

    @Override
    protected Station createStationFromElastic(final Map<Station.Key, Photo> photos, final JsonNode sourceJson) {
        final JsonNode fieldsJson = sourceJson.get("fields");
        final String id = fieldsJson.get("nummer").asText();
        final JsonNode abkuerzung = fieldsJson.get("abkuerzung");
        final Station.Key key = new Station.Key(getCountry().getCode(), id);
        return new Station(key,
                fieldsJson.get("name").asText(),
                readCoordinates(sourceJson),
                abkuerzung != null ? abkuerzung.asText() : null,
                photos.get(key));
    }

}
