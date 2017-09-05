package org.railwaystations.api.resources;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.railwaystations.api.TokenGenerator;
import org.railwaystations.api.mail.Mailer;
import org.railwaystations.api.model.Registration;
import org.railwaystations.api.monitoring.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

@Path("/registration")
public class RegistrationResource {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationResource.class);

    private final String apiKey;
    private final TokenGenerator tokenGenerator;
    private final Monitor monitor;
    private final Mailer mailer;

    public RegistrationResource(final String apiKey, final TokenGenerator tokenGenerator, final Monitor monitor, final Mailer mailer) {
        this.apiKey = apiKey;
        this.tokenGenerator = tokenGenerator;
        this.monitor = monitor;
        this.mailer = mailer;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@HeaderParam("API-Key") final String apiKey, @NotNull @Valid final Registration registration)
            throws UnsupportedEncodingException {
        LOG.info("New " + registration);
        if (!this.apiKey.equals(apiKey)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        final String token = tokenGenerator.buildFor(registration.getNickname(), registration.getEmail());
        LOG.info("Token " + token);
        final String url = "http://railway-stations.org/uploadToken/" + token;

        final String text = String.format("Hallo %1s,%n%n" +
                        "vielen Dank für Deine Registrierung.%n" +
                        "Dein Upload Token lautet: %2s%n" +
                        "Klicke bitte auf %3s um ihn in die App zu übernehmen.%n" +
                        "Alternativ kannst Du auch mit Deinem Smartphone den angehängten QR-Code scannen oder den Code manuell in der Bahnhofsfoto App unter 'Meine Daten' eintragen.%n%n" +
                        "Viele Grüße%n" +
                        "Dein Bahnhofsfoto-Team",
                registration.getNickname(), token, url);
        mailer.send(registration.getEmail(), "Bahnhofsfotos upload token", text, generateComZXing(url));

        monitor.sendMessage(String.format("New %s", registration));

        return Response.accepted().build();
    }

    private File generateComZXing(final String url) {
        try {
            final File file = File.createTempFile("rsapi", "qrcode.png");
            final Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            final QRCodeWriter qrCodeWriter = new QRCodeWriter();
            final BitMatrix byteMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200, hintMap);
            final int crunchifyWidth = byteMatrix.getWidth();
            final BufferedImage image = new BufferedImage(crunchifyWidth, crunchifyWidth, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            final Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, crunchifyWidth, crunchifyWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < crunchifyWidth; i++) {
                for (int j = 0; j < crunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            ImageIO.write(image, "png", file);
            LOG.info("qr-code generated at: " + file.getAbsolutePath());
            return file;
        } catch (final IOException | WriterException e) {
            throw new RuntimeException("Error creating QR-Code", e);
        }
    }

}
