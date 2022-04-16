package org.railwaystations.rsapi.adapter.in.web.writer;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PhotographersTxtWriter extends AbstractHttpMessageConverter<Map<String, Long>> {

    public PhotographersTxtWriter() {
        super(MediaType.TEXT_PLAIN);
    }

    private static void photographerToCsv(final PrintWriter pw, final Map.Entry<String, Long> photographer) {
        pw.println(String.join("\t", Long.toString(photographer.getValue()), photographer.getKey()));
    }

    @Override
    protected boolean supports(@NonNull final Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    protected @NonNull Map<String, Long> readInternal(@NonNull final Class<? extends Map<String, Long>> clazz, @NonNull final HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("read not supported", inputMessage);
    }

    @Override
    protected void writeInternal(final Map<String, Long> stringLongMap, final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try (final PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8))) {
            pw.println("count\tphotographer");
            stringLongMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(photographer -> photographerToCsv(pw, photographer));
            pw.flush();
        }
    }

}