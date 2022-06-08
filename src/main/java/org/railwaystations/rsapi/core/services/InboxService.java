package org.railwaystations.rsapi.core.services;

import org.apache.commons.lang3.StringUtils;
import org.railwaystations.rsapi.adapter.out.db.CountryDao;
import org.railwaystations.rsapi.adapter.out.db.InboxDao;
import org.railwaystations.rsapi.adapter.out.db.PhotoDao;
import org.railwaystations.rsapi.adapter.out.db.StationDao;
import org.railwaystations.rsapi.adapter.out.db.UserDao;
import org.railwaystations.rsapi.core.model.Coordinates;
import org.railwaystations.rsapi.core.model.Country;
import org.railwaystations.rsapi.core.model.InboxEntry;
import org.railwaystations.rsapi.core.model.InboxResponse;
import org.railwaystations.rsapi.core.model.InboxStateQuery;
import org.railwaystations.rsapi.core.model.Photo;
import org.railwaystations.rsapi.core.model.ProblemReport;
import org.railwaystations.rsapi.core.model.PublicInboxEntry;
import org.railwaystations.rsapi.core.model.Station;
import org.railwaystations.rsapi.core.model.User;
import org.railwaystations.rsapi.core.ports.in.ManageInboxUseCase;
import org.railwaystations.rsapi.core.ports.out.MastodonBot;
import org.railwaystations.rsapi.core.ports.out.Monitor;
import org.railwaystations.rsapi.core.ports.out.PhotoStorage;
import org.railwaystations.rsapi.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class InboxService implements ManageInboxUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(InboxService.class);

    private final PhotoStorage photoStorage;
    private final InboxDao inboxDao;
    private final StationDao stationDao;
    private final UserDao userDao;
    private final CountryDao countryDao;
    private final PhotoDao photoDao;
    private final String inboxBaseUrl;
    private final MastodonBot mastodonBot;
    private final Monitor monitor;

    public InboxService(final StationDao stationDao, final PhotoStorage photoStorage, final Monitor monitor,
                        final InboxDao inboxDao, final UserDao userDao, final CountryDao countryDao,
                        final PhotoDao photoDao, @Value("${inboxBaseUrl}") final String inboxBaseUrl, final MastodonBot mastodonBot) {
        this.stationDao = stationDao;
        this.photoStorage = photoStorage;
        this.monitor = monitor;
        this.inboxDao = inboxDao;
        this.userDao = userDao;
        this.countryDao = countryDao;
        this.photoDao = photoDao;
        this.inboxBaseUrl = inboxBaseUrl;
        this.mastodonBot = mastodonBot;
    }

    @Override
    public InboxResponse reportProblem(final ProblemReport problemReport, final User user, final String clientInfo) {
        if (!user.isEmailVerified()) {
            LOG.info("New problem report failed for user {}, email {} not verified", user.getName(), user.getEmail());
            return InboxResponse.of(InboxResponse.InboxResponseState.UNAUTHORIZED, "Email not verified");
        }

        LOG.info("New problem report: Nickname: {}; Country: {}; Station-Id: {}",
                user.getName(), problemReport.getCountryCode(), problemReport.getStationId());
        final var station = findStationByCountryAndId(problemReport.getCountryCode(), problemReport.getStationId());
        if (station.isEmpty()) {
            return InboxResponse.of(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA, "Station not found");
        }
        if (StringUtils.isBlank(problemReport.getComment())) {
            return InboxResponse.of(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA, "Comment is mandatory");
        }
        if (problemReport.getType() == null) {
            return InboxResponse.of(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA, "Problem type is mandatory");
        }
        if (problemReport.getType().needsPhoto() && !station.get().hasPhoto()) {
            return InboxResponse.of(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA, "Problem type is only applicable to station with photo");
        }
        final var inboxEntry = new InboxEntry(problemReport.getCountryCode(), problemReport.getStationId(),
                null, problemReport.getCoordinates(), user.getId(), null, problemReport.getComment(),
                problemReport.getType(), null);
        monitor.sendMessage(String.format("New problem report for %s - %s:%s%n%s: %s%nby %s%nvia %s",
                station.get().getTitle(), station.get().getKey().getCountry(), station.get().getKey().getId(), problemReport.getType(),
                StringUtils.trimToEmpty(problemReport.getComment()), user.getName(), clientInfo));
        return InboxResponse.of(InboxResponse.InboxResponseState.REVIEW, inboxDao.insert(inboxEntry));
    }

    @Override
    public List<PublicInboxEntry> publicInbox() {
        return inboxDao.findPublicInboxEntries();
    }

    @Override
    public List<InboxStateQuery> userInbox(@NotNull final User user, final List<Long> ids) {
        LOG.info("Query uploadStatus for Nickname: {}", user.getName());

        return ids.stream()
                .map(inboxDao::findById)
                .filter(inboxEntry -> inboxEntry != null && inboxEntry.getPhotographerId() == user.getId())
                .map(this::mapToInboxStateQuery)
                .toList();
    }

    private InboxStateQuery mapToInboxStateQuery(final InboxEntry inboxEntry) {
        return InboxStateQuery.builder()
                .id(inboxEntry.getId())
                .state(calculateInboxState(inboxEntry))
                .rejectedReason(inboxEntry.getRejectReason())
                .countryCode(inboxEntry.getCountryCode())
                .stationId(inboxEntry.getStationId())
                .coordinates(inboxEntry.getCoordinates())
                .filename(inboxEntry.getFilename())
                .inboxUrl(getInboxUrl(inboxEntry.getFilename(), photoStorage.isProcessed(inboxEntry.getFilename())))
                .crc32(inboxEntry.getCrc32())
                .build();
    }

    private InboxStateQuery.InboxState calculateInboxState(final InboxEntry inboxEntry) {
        if (inboxEntry.isDone()) {
            if (inboxEntry.getRejectReason() == null) {
                return InboxStateQuery.InboxState.ACCEPTED;
            } else {
                return InboxStateQuery.InboxState.REJECTED;
            }
        } else {
            if (hasConflict(inboxEntry.getId(),
                    findStationByCountryAndId(inboxEntry.getCountryCode(), inboxEntry.getStationId()).orElse(null))
                    || (inboxEntry.getStationId() == null && hasConflict(inboxEntry.getId(), inboxEntry.getCoordinates()))) {
                return InboxStateQuery.InboxState.CONFLICT;
            } else {
                return InboxStateQuery.InboxState.REVIEW;
            }
        }
    }

    @Override
    public List<InboxEntry> listAdminInbox(@NotNull final User user) {
        LOG.info("Load adminInbox for Nickname: {}", user.getName());
        final var pendingInboxEntries = inboxDao.findPendingInboxEntries();
        pendingInboxEntries.forEach(this::updateInboxEntry);
        return pendingInboxEntries;
    }

    private void updateInboxEntry(final InboxEntry inboxEntry) {
        final var filename = inboxEntry.getFilename();
        inboxEntry.isProcessed(photoStorage.isProcessed(filename));
        if (!inboxEntry.isProblemReport()) {
            inboxEntry.setInboxUrl(getInboxUrl(filename, inboxEntry.isProcessed()));
        }
        if (inboxEntry.getStationId() == null && !inboxEntry.getCoordinates().hasZeroCoords()) {
            inboxEntry.setConflict(hasConflict(inboxEntry.getId(), inboxEntry.getCoordinates()));
        }
    }

    private String getInboxUrl(final String filename, final boolean processed) {
        return inboxBaseUrl + (processed ? "/processed/" : "/") + filename;
    }

    @Override
    public void processAdminInboxCommand(@NotNull final User user, @NotNull final InboxEntry command) {
        LOG.info("Executing adminInbox command {} for Nickname: {}", command.getCommand(), user.getName());
        final var inboxEntry = inboxDao.findById(command.getId());
        if (inboxEntry == null || inboxEntry.isDone()) {
            throw new IllegalArgumentException("No pending inbox entry found");
        }
        switch (command.getCommand()) {
            case REJECT -> rejectInboxEntry(inboxEntry, command.getRejectReason());
            case IMPORT -> importUpload(inboxEntry, command);
            case ACTIVATE_STATION -> updateStationActiveState(inboxEntry, true);
            case DEACTIVATE_STATION -> updateStationActiveState(inboxEntry, false);
            case DELETE_STATION -> deleteStation(inboxEntry);
            case DELETE_PHOTO -> deletePhoto(inboxEntry);
            case MARK_SOLVED -> markProblemReportSolved(inboxEntry);
            case CHANGE_NAME -> {
                if (StringUtils.isBlank(command.getTitle())) {
                    throw new IllegalArgumentException("Empty new title: " + command.getTitle());
                }
                changeStationTitle(inboxEntry, command.getTitle());
            }
            case UPDATE_LOCATION -> updateLocation(inboxEntry, command);
            case PHOTO_OUTDATED -> markPhotoOutdated(inboxEntry);
            default -> throw new IllegalArgumentException("Unexpected command value: " + command.getCommand());
        }
    }

    private void markPhotoOutdated(final InboxEntry inboxEntry) {
        assertStationExistsAndHasPhoto(inboxEntry);
        photoDao.updatePhotoOutdated(inboxEntry.getCountryCode(), inboxEntry.getStationId());
        inboxDao.done(inboxEntry.getId());
    }

    private void updateLocation(final InboxEntry inboxEntry, final InboxEntry command) {
        var coordinates = inboxEntry.getCoordinates();
        if (command.hasCoords()) {
            coordinates = command.getCoordinates();
        }
        if (coordinates == null || !coordinates.isValid()) {
            throw new IllegalArgumentException("Can't update location, coordinates: " + command.getCommand());
        }

        final var station = assertStationExists(inboxEntry);
        stationDao.updateLocation(station.getKey(), coordinates);
        inboxDao.done(inboxEntry.getId());
    }

    @Override
    public long countPendingInboxEntries() {
        return inboxDao.countPendingInboxEntries();
    }

    @Override
    public String getNextZ() {
        return "Z" + (stationDao.getMaxZ() + 1);
    }

    private void updateStationActiveState(final InboxEntry inboxEntry, final boolean active) {
        final var station = assertStationExists(inboxEntry);
        station.setActive(active);
        stationDao.updateActive(station.getKey(), station.isActive());
        inboxDao.done(inboxEntry.getId());
        LOG.info("Problem report {} station {} set active to {}", inboxEntry.getId(), station.getKey(), active);
    }

    private void changeStationTitle(final InboxEntry inboxEntry, final String newTitle) {
        final var station = assertStationExists(inboxEntry);
        stationDao.changeStationTitle(station, newTitle);
        inboxDao.done(inboxEntry.getId());
        LOG.info("Problem report {} station {} change name to {}", inboxEntry.getId(), station.getKey(), newTitle);
    }

    private void deleteStation(final InboxEntry inboxEntry) {
        final var station = assertStationExists(inboxEntry);
        photoDao.delete(station.getKey());
        stationDao.delete(station.getKey());
        inboxDao.done(inboxEntry.getId());
        LOG.info("Problem report {} station {} deleted", inboxEntry.getId(), station.getKey());
    }

    private void deletePhoto(final InboxEntry inboxEntry) {
        final var station = assertStationExistsAndHasPhoto(inboxEntry);
        photoDao.delete(station.getKey());
        inboxDao.done(inboxEntry.getId());
        LOG.info("Problem report {} photo of station {} deleted", inboxEntry.getId(), station.getKey());
    }

    private void markProblemReportSolved(final InboxEntry inboxEntry) {
        assertStationExists(inboxEntry);
        inboxDao.done(inboxEntry.getId());
        LOG.info("Problem report {} accepted", inboxEntry.getId());
    }

    private Station assertStationExists(final InboxEntry inboxEntry) {
        return findStationByCountryAndId(inboxEntry.getCountryCode(), inboxEntry.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
    }

    private Station assertStationExistsAndHasPhoto(final InboxEntry inboxEntry) {
        final var station = assertStationExists(inboxEntry);
        if (!station.hasPhoto()) {
            throw new IllegalArgumentException("Station has no photo");
        }
        return station;
    }

    private void importUpload(final InboxEntry inboxEntry, final InboxEntry command) {
        LOG.info("Importing upload {}, {}", inboxEntry.getId(), inboxEntry.getFilename());

        if (inboxEntry.isProblemReport()) {
            throw new IllegalArgumentException("Can't import a problem report");
        }

        final var station = findOrCreateStation(inboxEntry, command);

        if (station.hasPhoto() && !command.ignoreConflict()) {
            throw new IllegalArgumentException("Station already has a photo");
        }
        if (hasConflict(inboxEntry.getId(), station) && !command.ignoreConflict()) {
            throw new IllegalArgumentException("There is a conflict with another upload");
        }

        final var photographer = userDao.findById(inboxEntry.getPhotographerId()).orElseThrow();
        final var country = countryDao.findById(StringUtils.lowerCase(station.getKey().getCountry()))
                .orElseThrow(() -> new IllegalArgumentException("Country not found"));

        try {
            final var photo = new Photo(station.getKey(), getPhotoUrlPath(inboxEntry, station, country),
                    photographer, Instant.now(), getLicenseForPhoto(photographer, country));
            if (station.hasPhoto()) {
                photoDao.update(photo);
            } else {
                photoDao.insert(photo);
            }
            station.setPhoto(photo);

            photoStorage.importPhoto(inboxEntry, country, station);
            inboxDao.done(inboxEntry.getId());
            LOG.info("Upload {} accepted: {}", inboxEntry.getId(), inboxEntry.getFilename());
            mastodonBot.tootNewPhoto(station, inboxEntry);
        } catch (final Exception e) {
            LOG.error("Error importing upload {} photo {}", inboxEntry.getId(), inboxEntry.getFilename());
            throw new RuntimeException("Error moving file", e);
        }
    }

    private String getPhotoUrlPath(final InboxEntry inboxEntry, final Station station, final Country country) {
        return "/" + country.getCode() + "/" + station.getKey().getId() + "." + inboxEntry.getExtension();
    }

    /**
     * Gets the applicable license for the given country.
     * We need to override the license for some countries, because of limitations of the "Freedom of panorama".
     */
    protected static String getLicenseForPhoto(final User photographer, final Country country) {
        if (country != null && StringUtils.isNotBlank(country.getOverrideLicense())) {
            return country.getOverrideLicense();
        }
        return photographer.getLicense();
    }    

    private Station findOrCreateStation(final InboxEntry inboxEntry, final InboxEntry command) {
        var station = findStationByCountryAndId(inboxEntry.getCountryCode(), inboxEntry.getStationId());
        if (station.isEmpty() && command.createStation()) {
            station = findStationByCountryAndId(command.getCountryCode(), command.getStationId());
            station.ifPresent(s -> LOG.info("Importing missing station upload {} to existing station {}", inboxEntry.getId(), s.getKey()));
        }

        return station.orElseGet(()-> {
            if (!command.createStation() || StringUtils.isNotBlank(inboxEntry.getStationId())) {
                throw new IllegalArgumentException("Station not found");
            }

            // create station
            final var country = countryDao.findById(StringUtils.lowerCase(command.getCountryCode()));
            if (country.isEmpty()) {
                throw new IllegalArgumentException("Country not found");
            }
            if (StringUtils.isBlank(command.getStationId())) {
                throw new IllegalArgumentException("Station ID can't be empty");
            }
            if (hasConflict(inboxEntry.getId(), inboxEntry.getCoordinates()) && !command.ignoreConflict()) {
                throw new IllegalArgumentException("There is a conflict with a nearby station");
            }
            if (command.hasCoords() && !command.getCoordinates().isValid()) {
                throw new IllegalArgumentException("Lat/Lon out of range");
            }

            var coordinates = inboxEntry.getCoordinates();
            if (command.hasCoords()) {
                coordinates = command.getCoordinates();
            }

            final var title = command.getTitle() != null ? command.getTitle() : inboxEntry.getTitle();

            final var newStation = new Station(new Station.Key(command.getCountryCode(), command.getStationId()), title, coordinates, command.getDs100(), null, command.getActive());
            stationDao.insert(newStation);
            return newStation;
        });
    }

    private void rejectInboxEntry(final InboxEntry inboxEntry, final String rejectReason) {
        inboxDao.reject(inboxEntry.getId(), rejectReason);
        if (inboxEntry.isProblemReport()) {
            LOG.info("Rejecting problem report {}, {}", inboxEntry.getId(), rejectReason);
            return;
        }

        LOG.info("Rejecting upload {}, {}, {}", inboxEntry.getId(), rejectReason, inboxEntry.getFilename());

        try {
            photoStorage.reject(inboxEntry);
        } catch (final IOException e) {
            LOG.warn("Unable to move rejected file {}", inboxEntry.getFilename(), e);
        }
    }

    @Override
    public InboxResponse uploadPhoto(final String clientInfo, final InputStream body, final String stationId,
                                     final String country, final String contentType, final String stationTitle,
                                     final Double latitude, final Double longitude, final String comment,
                                     final Boolean active, final User user) {
        if (!user.isEmailVerified()) {
            LOG.info("Photo upload failed for user {}, email not verified", user.getName());
            return InboxResponse.of(InboxResponse.InboxResponseState.UNAUTHORIZED,"Email not verified");
        }

        final var station = findStationByCountryAndId(country, stationId);
        final Coordinates coordinates;
        if (station.isEmpty()) {
            LOG.warn("Station not found");
            if (StringUtils.isBlank(stationTitle) || latitude == null || longitude == null) {
                LOG.warn("Not enough data for missing station: title={}, latitude={}, longitude={}", stationTitle, latitude, longitude);
                return InboxResponse.of(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA, "Not enough data: either 'country' and 'stationId' or 'title', 'latitude' and 'longitude' have to be provided");
            }
            coordinates = new Coordinates(latitude, longitude);
            if (!coordinates.isValid()) {
                LOG.warn("Lat/Lon out of range: latitude={}, longitude={}", latitude, longitude);
                return InboxResponse.of(InboxResponse.InboxResponseState.LAT_LON_OUT_OF_RANGE, "'latitude' and/or 'longitude' out of range");
            }
        } else {
            coordinates = null;
        }

        final var extension = ImageUtil.mimeToExtension(contentType);
        if (extension == null) {
            LOG.warn("Unknown contentType '{}'", contentType);
            return InboxResponse.of(InboxResponse.InboxResponseState.UNSUPPORTED_CONTENT_TYPE, "unsupported content type (only jpg and png are supported)");
        }

        final boolean conflict = hasConflict(null, station.orElse(null)) || hasConflict(null, coordinates);

        final String filename;
        final String inboxUrl;
        final Long id;
        final Long crc32;
        try {
            id = station.map(s -> inboxDao.insert(new InboxEntry(s.getKey().getCountry(), s.getKey().getId(), stationTitle,
                        null, user.getId(), extension, comment, null, active)))
                    .orElseGet(() -> inboxDao.insert(new InboxEntry(country, null, stationTitle,
                        coordinates, user.getId(), extension, comment, null, active)));
            filename = InboxEntry.getFilename(id, extension);
            crc32 = photoStorage.storeUpload(body, filename);
            inboxDao.updateCrc32(id, crc32);

            final var duplicateInfo = conflict ? " (possible duplicate!)" : "";
            inboxUrl = inboxBaseUrl + "/" + UriUtils.encodePath(filename, StandardCharsets.UTF_8);
            if (station.isPresent()) {
                monitor.sendMessage(String.format("New photo upload for %s - %s:%s%n%s%n%s%s%nby %s%nvia %s",
                        station.get().getTitle(), station.get().getKey().getCountry(), station.get().getKey().getId(),
                        StringUtils.trimToEmpty(comment), inboxUrl, duplicateInfo, user.getName(), clientInfo), photoStorage.getUploadFile(filename));
            } else {
                monitor.sendMessage(String.format("Photo upload for missing station %s at https://map.railway-stations.org/index.php?mlat=%s&mlon=%s&zoom=18&layers=M%n%s%n%s%s%nby %s%nvia %s",
                        stationTitle, latitude, longitude,
                        StringUtils.trimToEmpty(comment), inboxUrl, duplicateInfo, user.getName(), clientInfo), photoStorage.getUploadFile(filename));
            }
        } catch (final PhotoStorage.PhotoTooLargeException e) {
            return InboxResponse.of(InboxResponse.InboxResponseState.PHOTO_TOO_LARGE, "Photo too large, max " + e.getMaxSize() + " bytes allowed");
        } catch (final IOException e) {
            LOG.error("Error uploading photo", e);
            return InboxResponse.of(InboxResponse.InboxResponseState.ERROR, "Internal Error");
        }

        return InboxResponse.builder()
                .state(conflict ? InboxResponse.InboxResponseState.CONFLICT : InboxResponse.InboxResponseState.REVIEW)
                .id(id)
                .filename(filename)
                .inboxUrl(inboxUrl)
                .crc32(crc32)
                .build();
    }

    private boolean hasConflict(final Long id, final Station station) {
        if (station == null) {
            return false;
        }
        if (station.hasPhoto()) {
            return true;
        }
        return inboxDao.countPendingInboxEntriesForStation(id, station.getKey().getCountry(), station.getKey().getId()) > 0;
    }

    private boolean hasConflict(final Long id, final Coordinates coordinates) {
        if (coordinates == null || coordinates.hasZeroCoords()) {
            return false;
        }
        return inboxDao.countPendingInboxEntriesForNearbyCoordinates(id, coordinates) > 0 || stationDao.countNearbyCoordinates(coordinates) > 0;
    }

    private Optional<Station> findStationByCountryAndId(final String countryCode, final String stationId) {
        return stationDao.findByKey(countryCode, stationId).stream().findFirst();
    }
    
}
