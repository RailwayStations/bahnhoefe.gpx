package org.railwaystations.rsapi.adapter.out.mastodon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.railwaystations.rsapi.core.model.InboxEntry;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.ports.out.MastodonBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MastodonBotHttpClient implements MastodonBot {

    private static final Logger LOG = LoggerFactory.getLogger(MastodonBotHttpClient.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MastodonBotConfig config;

    private final CloseableHttpClient httpclient;

    public MastodonBotHttpClient(final MastodonBotConfig config) {
        super();
        this.config = config;
        this.httpclient = HttpClients.custom().setDefaultRequestConfig(
                RequestConfig.custom()
                        .setSocketTimeout(5000)
                        .setConnectTimeout(5000)
                        .setConnectionRequestTimeout(5000).build()
        ).build();
    }

    @Override
    @Async
    public void tootNewPhoto(final Station station, final InboxEntry inboxEntry) {
        if (StringUtils.isBlank(config.getInstanceUrl()) || StringUtils.isBlank(config.getToken()) || StringUtils.isBlank(config.getStationUrl())) {
            LOG.info("New photo for Station {} not tooted, {}", station.getKey(), this);
            return;
        }
        LOG.info("Sending toot for new photo of : {}", station.getKey());
        try {
            var status = String.format("%s%nby %s%n%s?countryCode=%s&stationId=%s",
                    station.getTitle(), station.getPhotographer(), config.getStationUrl(),
                    station.getKey().getCountry(), station.getKey().getId());
            if (StringUtils.isNotBlank(inboxEntry.getComment())) {
                status += String.format("%n%s", inboxEntry.getComment());
            }
            final var json = MAPPER.writeValueAsString(new Toot(status));
            final var httpPost = new HttpPost(config.getInstanceUrl() + "/api/v1/statuses");
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON.withCharset("UTF-8")));
            httpPost.setHeader("Authorization", "Bearer " + config.getToken());
            final var response = httpclient.execute(httpPost);
            final int statusCode = response.getStatusLine().getStatusCode();
            final var content = EntityUtils.toString(response.getEntity());
            if (statusCode >= 200 && statusCode < 300) {
                LOG.info("Got json response from {}: {}", httpPost.getURI(), content);
            } else {
                LOG.error("Error reading json from {}, status {}: {}", httpPost.getURI(), status, content);
            }
        } catch (final RuntimeException | IOException e) {
            LOG.error("Error sending Toot", e);
        }
    }

    @Override
    public String toString() {
        return "MastodonBot{" +
                "stationUrl='" + config.getStationUrl() + '\'' +
                ", instanceUrl='" + config.getInstanceUrl() + '\'' +
                '}';
    }

    static class Toot {
        private final String status;

        public Toot(final String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

}