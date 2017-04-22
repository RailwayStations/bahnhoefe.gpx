package github.pstorch.bahnhoefe.service.loader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.pstorch.bahnhoefe.service.model.Bahnhof;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBahnhoefeLoader implements BahnhoefeLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonFactory FACTORY = MAPPER.getFactory();

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBahnhoefeLoader.class);

    protected URL bahnhoefeUrl;
    protected URL photosUrl;

    private final CloseableHttpClient httpclient;


    protected AbstractBahnhoefeLoader(final URL photosUrl, final URL bahnhoefeUrl) {
        super();
        this.photosUrl = photosUrl;
        this.bahnhoefeUrl = bahnhoefeUrl;
        this.httpclient = HttpClients.custom().setDefaultRequestConfig(
                RequestConfig.custom()
                        .setSocketTimeout(5000)
                        .setConnectTimeout(5000)
                        .setConnectionRequestTimeout(5000).build()
        ).build();
    }

    public final void setBahnhoefeUrl(final String bahnhoefeUrl) throws MalformedURLException {
        this.bahnhoefeUrl = new URL(bahnhoefeUrl);
    }

    public final void setPhotosUrl(final String photosUrl) throws MalformedURLException {
        this.photosUrl = new URL(photosUrl);
    }

    @Override
    public abstract String getCountryCode();

    @Override
    public Map<Integer, Bahnhof> loadBahnhoefe() {
        try {
            return loadBahnhoefe(loadPhotos(new HashMap<>(), 0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected JsonNode readJsonFromUrl(final URL url) throws Exception {
        // shortcut for testing, need to find a better way
        if ("file".equals(url.getProtocol())) {
            return MAPPER.readTree(url);
        }

        // use Apache HTTP Client to retrieve remote content
        final HttpGet httpGet = new HttpGet(url.toURI());
        return httpclient.execute(httpGet, response -> {
            final int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                LOG.info("Got json response from {}", url);
                return MAPPER.readTree(FACTORY.createParser(EntityUtils.toString(response.getEntity(), "UTF-8")));
            } else {
                LOG.error("Error reading json from {}", url);
                throw new ClientProtocolException(String.format("Unexpected response status: %d", status));
            }
        });
    }

    private Map<Integer, String> loadPhotos(final Map<Integer, String> photoFlags, final int page) throws Exception {
        final JsonNode tree = readJsonFromUrl(photosUrl.getProtocol().startsWith("http")?new URL(photosUrl.toString() + "?page=" + page):photosUrl);
        for (int i = 0; i < tree.size(); i++) {
            final JsonNode bahnhofPhoto = tree.get(i);
            JsonNode id = bahnhofPhoto.get("ibnr");
            if (id == null || StringUtils.isBlank(id.asText())) {
                id = bahnhofPhoto.get("bahnhofsnr");
            }
            photoFlags.put(id.asInt(), bahnhofPhoto.get("fotograf-title").asText());
        }
        if (photosUrl.getProtocol().startsWith("http") && tree.size() > 0) {
            loadPhotos(photoFlags, page + 1);
        }
        return photoFlags;
    }

    protected abstract Map<Integer, Bahnhof> loadBahnhoefe(final Map<Integer, String> photoFlags) throws Exception;

}
