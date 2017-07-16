package org.railwaystations.api.loader;

import com.fasterxml.jackson.databind.JsonNode;
import org.railwaystations.api.model.Bahnhof;
import org.railwaystations.api.model.Country;
import org.railwaystations.api.model.Photo;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BahnhoefeLoaderCh extends AbstractBahnhoefeLoader {

    private static final String HITS_ELEMENT = "hits";

    public BahnhoefeLoaderCh() {
        this(null, null);
    }

    public BahnhoefeLoaderCh(final URL bahnhoefeUrl, final URL photosUrl) {
        super(new Country("ch", "Schweiz",
                "fotos@schweizer-bahnhoefe.ch",
                "@BahnhoefeCH, @android_oma, #BahnhofsfotoCH",
                "http://fahrplan.sbb.ch/bin/stboard.exe/dn?input={title}&REQTrain_name=&boardType=dep&time=now&maxJourneys=20&selectDate=today&productsFilter=1111111111&start=yes"),
                bahnhoefeUrl, photosUrl);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected Map<Integer, Bahnhof> loadBahnhoefe(final Map<Integer, Photo> photos) throws Exception {
        final Map<Integer, Bahnhof> bahnhoefe = new HashMap<>();

        final JsonNode hits = readJsonFromUrl(getBahnhoefeUrl())
                                .get(BahnhoefeLoaderCh.HITS_ELEMENT)
                                .get(BahnhoefeLoaderCh.HITS_ELEMENT);
        for (int i = 0; i < hits.size(); i++) {
            final JsonNode bahnhofJson = hits.get(i).get("_source").get("fields");
            final JsonNode geopos = bahnhofJson.get("geopos");
            final Integer id = bahnhofJson.get("nummer").asInt();
            final Bahnhof bahnhof = new Bahnhof(id,
                    getCountry().getCode(),
                    bahnhofJson.get("name").asText(),
                    geopos.get(0).asDouble(),
                    geopos.get(1).asDouble(),
                    photos.get(id));
            bahnhoefe.put(bahnhof.getId(), bahnhof);
        }
        return bahnhoefe;
    }

}
