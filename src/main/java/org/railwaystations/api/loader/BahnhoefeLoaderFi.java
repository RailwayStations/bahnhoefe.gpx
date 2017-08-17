package org.railwaystations.api.loader;

import org.railwaystations.api.model.Country;

import java.net.URL;

public class BahnhoefeLoaderFi extends AbstractBahnhoefeLoader {

    public BahnhoefeLoaderFi() {
        this(null, null);
    }

    public BahnhoefeLoaderFi(final URL bahnhoefeUrl, final URL photosUrl) {
        super(new Country("fi", "Finnland",
                "bahnhofsfotos@deutschlands-bahnhoefe.de",
                "@android_oma, #dbHackathon, #dbOpendata, #Bahnhofsfoto, @khgdrn",
                "https://www.junat.net/en/"),
                bahnhoefeUrl, photosUrl);
    }

}
