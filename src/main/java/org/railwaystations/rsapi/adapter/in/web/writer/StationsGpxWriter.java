package org.railwaystations.rsapi.adapter.in.web.writer;

import org.railwaystations.rsapi.adapter.in.web.model.StationDto;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.List;

public class StationsGpxWriter extends AbstractHttpMessageConverter<List<StationDto>> {

    private static final String UTF_8 = "UTF-8";

    private static final String NAME_ELEMENT = "name";

    private static final String WPT_ELEMENT = "wpt";

    private static final String LON_ELEMENT = "lon";

    private static final String LAT_ELEMENT = "lat";

    public static final MediaType GPX_MEDIA_TYPE = new MediaType("application", "gpx+xml");

    public static final String GPX_MEDIA_TYPE_VALUE = "application/gpx+xml";

    public StationsGpxWriter() {
        super(GPX_MEDIA_TYPE);
    }

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected @NonNull List<StationDto> readInternal(@NonNull Class<? extends List<StationDto>> clazz, @NonNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("read not supported", inputMessage);
    }

    @Override
    protected void writeInternal(List<StationDto> stations, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        XMLStreamWriter xmlw;
        try {
            xmlw = XMLOutputFactory.newInstance().createXMLStreamWriter(outputMessage.getBody(), StationsGpxWriter.UTF_8);
            xmlw.writeStartDocument(StationsGpxWriter.UTF_8, "1.0");
            xmlw.writeCharacters("\n");
            xmlw.writeStartElement("gpx");
            xmlw.writeDefaultNamespace("http://www.topografix.com/GPX/1/1");
            xmlw.writeAttribute("version", "1.1");
            xmlw.writeCharacters("\n");
            stations.forEach(station -> stationToXml(xmlw, station));
            xmlw.writeEndElement();
            xmlw.flush();
        } catch (XMLStreamException e) {
            throw new HttpMessageNotWritableException("Error converting a Station to gpx", e);
        }
    }

    private static void stationToXml(XMLStreamWriter xmlw, StationDto station) {
        try {
            xmlw.writeStartElement(StationsGpxWriter.WPT_ELEMENT);
            xmlw.writeAttribute(StationsGpxWriter.LAT_ELEMENT, Double.toString(station.getLat()));
            xmlw.writeAttribute(StationsGpxWriter.LON_ELEMENT, Double.toString(station.getLon()));
            xmlw.writeStartElement(StationsGpxWriter.NAME_ELEMENT);
            xmlw.writeCharacters(station.getTitle());
            xmlw.writeEndElement();
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
        } catch (XMLStreamException e) {
            throw new HttpMessageNotWritableException("Error converting a Station to gpx", e);
        }
    }

}
