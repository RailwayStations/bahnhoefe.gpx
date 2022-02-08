package org.railwaystations.rsapi.resources;

import org.railwaystations.rsapi.WorkDir;
import org.railwaystations.rsapi.utils.FileUtils;
import org.railwaystations.rsapi.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;

@RestController
public class PhotoDownloadResource {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoDownloadResource.class);

    private final WorkDir workDir;

    public PhotoDownloadResource(final WorkDir workDir) {
        this.workDir = workDir;
    }

    @GetMapping(value = "/fotos/{countryCode}/{filename}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> fotos(@PathVariable("countryCode") final String countryCode,
                                 @PathVariable("filename") final String filename,
                                 @RequestParam(value = "width", required = false) final Integer width) throws IOException {
        return photos(countryCode, filename, width);
    }

    @GetMapping(value = "/photos/{countryCode}/{filename}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> photos(@PathVariable("countryCode") final String countryCode,
                                  @PathVariable("filename") final String filename,
                                  @RequestParam(value = "width", required = false) final Integer width) throws IOException {
        LOG.info("Download photo country={}, file={}", countryCode, filename);
        return downloadPhoto(new File(new File(workDir.getPhotosDir(), FileUtils.sanitizeFilename(countryCode)), FileUtils.sanitizeFilename(filename)), width);
    }

    private static ResponseEntity<byte[]> downloadPhoto(final File photo, final Integer width) throws IOException {
        if (!photo.exists() || !photo.canRead()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok().contentType(MediaType.valueOf(ImageUtil.extensionToMimeType(ImageUtil.getExtension(photo.getName())))).body(ImageUtil.scalePhoto(photo, width));
    }

    @GetMapping(value = "/inbox/{filename}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> inbox(@PathVariable("filename") final String filename,
                                 @RequestParam(value = "width", required = false) final Integer width) throws IOException {
        LOG.info("Download inbox file={}", filename);
        return downloadPhoto(new File(workDir.getInboxDir(), FileUtils.sanitizeFilename(filename)), width);
    }

    @GetMapping(value = "/inbox/processed/{filename}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> inboxProcessed(@PathVariable("filename") final String filename,
                                          @RequestParam(value = "width", required = false) final Integer width) throws IOException {
        LOG.info("Download inbox file={}", filename);
        return downloadPhoto(new File(workDir.getInboxProcessedDir(), FileUtils.sanitizeFilename(filename)), width);
    }

}