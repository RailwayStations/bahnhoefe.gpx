package org.railwaystations.rsapi.adapter.in.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.railwaystations.rsapi.adapter.in.web.InboxResponseMapper;
import org.railwaystations.rsapi.adapter.in.web.api.InboxApi;
import org.railwaystations.rsapi.adapter.in.web.model.AdminInboxCommandResponseDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxCommandDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxCountResponseDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxEntryDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxResponseDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxStateQueryRequestDto;
import org.railwaystations.rsapi.adapter.in.web.model.InboxStateQueryResponseDto;
import org.railwaystations.rsapi.adapter.in.web.model.NextZResponseDto;
import org.railwaystations.rsapi.adapter.in.web.model.ProblemReportDto;
import org.railwaystations.rsapi.adapter.in.web.model.ProblemReportTypeDto;
import org.railwaystations.rsapi.adapter.in.web.model.PublicInboxEntryDto;
import org.railwaystations.rsapi.core.model.Coordinates;
import org.railwaystations.rsapi.core.model.InboxCommand;
import org.railwaystations.rsapi.core.model.InboxEntry;
import org.railwaystations.rsapi.core.model.InboxStateQuery;
import org.railwaystations.rsapi.core.model.ProblemReport;
import org.railwaystations.rsapi.core.model.ProblemReportType;
import org.railwaystations.rsapi.core.model.PublicInboxEntry;
import org.railwaystations.rsapi.core.ports.in.ManageInboxUseCase;
import org.railwaystations.rsapi.core.ports.in.ManageProfileUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;

import static org.railwaystations.rsapi.adapter.in.web.InboxResponseMapper.toHttpStatus;
import static org.railwaystations.rsapi.adapter.in.web.RequestUtil.getAuthUser;
import static org.railwaystations.rsapi.adapter.in.web.RequestUtil.getRequest;
import static org.railwaystations.rsapi.adapter.in.web.RequestUtil.getUserAgent;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
public class InboxController implements InboxApi {

    private final ManageInboxUseCase manageInboxUseCase;

    private final ManageProfileUseCase manageProfileUseCase;

    private final LocaleResolver localeResolver;

    private ProblemReport toDomain(ProblemReportDto problemReport) {
        return ProblemReport.builder()
                .countryCode(problemReport.getCountryCode())
                .stationId(problemReport.getStationId())
                .title(problemReport.getTitle())
                .photoId(problemReport.getPhotoId())
                .type(toDomain(problemReport.getType()))
                .comment(problemReport.getComment())
                .coordinates(mapCoordinates(problemReport.getLat(), problemReport.getLon()))
                .build();
    }

    private ProblemReportType toDomain(ProblemReportTypeDto dtoType) {
        return switch (dtoType) {
            case OTHER -> ProblemReportType.OTHER;
            case WRONG_NAME -> ProblemReportType.WRONG_NAME;
            case WRONG_PHOTO -> ProblemReportType.WRONG_PHOTO;
            case PHOTO_OUTDATED -> ProblemReportType.PHOTO_OUTDATED;
            case STATION_ACTIVE -> ProblemReportType.STATION_ACTIVE;
            case STATION_INACTIVE -> ProblemReportType.STATION_INACTIVE;
            case WRONG_LOCATION -> ProblemReportType.WRONG_LOCATION;
            case STATION_NONEXISTENT -> ProblemReportType.STATION_NONEXISTENT;
            case DUPLICATE -> ProblemReportType.DUPLICATE;
        };
    }

    private Coordinates mapCoordinates(Double lat, Double lon) {
        if (lat == null || lon == null) {
            return null;
        }
        return new Coordinates(lat, lon);
    }

    private List<PublicInboxEntryDto> toPublicInboxEntryDto(List<PublicInboxEntry> publicInboxEntries) {
        return publicInboxEntries.stream()
                .map(this::toDto)
                .toList();
    }

    private PublicInboxEntryDto toDto(PublicInboxEntry publicInboxEntry) {
        return new PublicInboxEntryDto(publicInboxEntry.getCountryCode(),
                publicInboxEntry.getStationId(),
                publicInboxEntry.getTitle(),
                publicInboxEntry.getLat(),
                publicInboxEntry.getLon());
    }

    private List<Long> toIdList(List<InboxStateQueryRequestDto> inboxStateQueryRequestDtos) {
        return inboxStateQueryRequestDtos.stream()
                .map(InboxStateQueryRequestDto::getId)
                .toList();
    }

    private List<InboxStateQueryResponseDto> toInboxStateQueryDto(List<InboxStateQuery> inboxStateQueries) {
        return inboxStateQueries.stream()
                .map(this::toDto)
                .toList();
    }

    private InboxStateQueryResponseDto toDto(InboxStateQuery inboxStateQuery) {
        return new InboxStateQueryResponseDto(inboxStateQuery.getId(), toDto(inboxStateQuery.getState()))
                .countryCode(inboxStateQuery.getCountryCode())
                .stationId(inboxStateQuery.getStationId())
                .title(inboxStateQuery.getTitle())
                .newTitle(inboxStateQuery.getNewTitle())
                .inboxUrl(inboxStateQuery.getInboxUrl())
                .lat(inboxStateQuery.getCoordinates() != null ? inboxStateQuery.getCoordinates().getLat() : null)
                .lon(inboxStateQuery.getCoordinates() != null ? inboxStateQuery.getCoordinates().getLon() : null)
                .newLat(inboxStateQuery.getNewCoordinates() != null ? inboxStateQuery.getNewCoordinates().getLat() : null)
                .newLon(inboxStateQuery.getNewCoordinates() != null ? inboxStateQuery.getNewCoordinates().getLon() : null)
                .filename(inboxStateQuery.getFilename())
                .crc32(inboxStateQuery.getCrc32())
                .createdAt(inboxStateQuery.getCreatedAt().toEpochMilli())
                .comment(inboxStateQuery.getComment())
                .problemReportType(toDto(inboxStateQuery.getProblemReportType()))
                .rejectedReason(inboxStateQuery.getRejectedReason());
    }

    public InboxStateQueryResponseDto.StateEnum toDto(InboxStateQuery.InboxState inboxState) {
        return switch (inboxState) {
            case REVIEW, CONFLICT -> InboxStateQueryResponseDto.StateEnum.REVIEW;
            case ACCEPTED -> InboxStateQueryResponseDto.StateEnum.ACCEPTED;
            case REJECTED -> InboxStateQueryResponseDto.StateEnum.REJECTED;
            case UNKNOWN -> InboxStateQueryResponseDto.StateEnum.UNKNOWN;
        };
    }

    private List<InboxEntryDto> toInboxEntryDto(List<InboxEntry> inboxEntries) {
        return inboxEntries.stream()
                .map(this::toDto)
                .toList();
    }

    private InboxEntryDto toDto(InboxEntry inboxEntry) {
        return new InboxEntryDto(inboxEntry.getId(),
                inboxEntry.getPhotographerNickname(),
                inboxEntry.getComment(),
                inboxEntry.getCreatedAt().toEpochMilli(),
                inboxEntry.isDone(),
                inboxEntry.hasPhoto())
                .countryCode(inboxEntry.getCountryCode())
                .stationId(inboxEntry.getStationId())
                .title(inboxEntry.getTitle())
                .newTitle(inboxEntry.getNewTitle())
                .lat(inboxEntry.getLat())
                .lon(inboxEntry.getLon())
                .newLat(inboxEntry.getNewLat())
                .newLon(inboxEntry.getNewLon())
                .active(inboxEntry.getActive())
                .inboxUrl(inboxEntry.getInboxUrl())
                .filename(inboxEntry.getFilename())
                .photoId(inboxEntry.getPhotoId())
                .hasConflict(inboxEntry.isConflict())
                .isProcessed(inboxEntry.isProcessed())
                .photographerEmail(inboxEntry.getPhotographerEmail())
                .problemReportType(toDto(inboxEntry.getProblemReportType()));
    }

    private ProblemReportTypeDto toDto(ProblemReportType problemReportType) {
        if (problemReportType == null) {
            return null;
        }
        return switch (problemReportType) {
            case STATION_NONEXISTENT -> ProblemReportTypeDto.STATION_NONEXISTENT;
            case WRONG_LOCATION -> ProblemReportTypeDto.WRONG_LOCATION;
            case STATION_ACTIVE -> ProblemReportTypeDto.STATION_ACTIVE;
            case STATION_INACTIVE -> ProblemReportTypeDto.STATION_INACTIVE;
            case PHOTO_OUTDATED -> ProblemReportTypeDto.PHOTO_OUTDATED;
            case WRONG_PHOTO -> ProblemReportTypeDto.WRONG_PHOTO;
            case WRONG_NAME -> ProblemReportTypeDto.WRONG_NAME;
            case OTHER -> ProblemReportTypeDto.OTHER;
            case DUPLICATE -> ProblemReportTypeDto.DUPLICATE;
        };
    }

    private InboxCommand toDomain(InboxCommandDto command) {
        return InboxCommand.builder()
                .id(command.getId())
                .countryCode(command.getCountryCode())
                .stationId(command.getStationId())
                .title(command.getTitle())
                .coordinates(mapCoordinates(command.getLat(), command.getLon()))
                .rejectReason(command.getRejectReason())
                .ds100(command.getDS100())
                .active(command.getActive() != null ? command.getActive() : null)
                .conflictResolution(toDomain(command.getConflictResolution()))
                .build();
    }

    private InboxCommand.ConflictResolution toDomain(InboxCommandDto.ConflictResolutionEnum conflictResolution) {
        if (conflictResolution == null) {
            return InboxCommand.ConflictResolution.DO_NOTHING;
        }
        return switch (conflictResolution) {
            case DO_NOTHING -> InboxCommand.ConflictResolution.DO_NOTHING;
            case OVERWRITE_EXISTING_PHOTO -> InboxCommand.ConflictResolution.OVERWRITE_EXISTING_PHOTO;
            case IMPORT_AS_NEW_PRIMARY_PHOTO -> InboxCommand.ConflictResolution.IMPORT_AS_NEW_PRIMARY_PHOTO;
            case IMPORT_AS_NEW_SECONDARY_PHOTO -> InboxCommand.ConflictResolution.IMPORT_AS_NEW_SECONDARY_PHOTO;
            case IGNORE_NEARBY_STATION -> InboxCommand.ConflictResolution.IGNORE_NEARBY_STATION;
        };
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<InboxCountResponseDto> adminInboxCountGet() {
        return ResponseEntity.ok(new InboxCountResponseDto(manageInboxUseCase.countPendingInboxEntries()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<List<InboxEntryDto>> adminInboxGet() {
        return ResponseEntity.ok(toInboxEntryDto(manageInboxUseCase.listAdminInbox(getAuthUser().getUser())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<AdminInboxCommandResponseDto> adminInboxPost(InboxCommandDto uploadCommand) {
        log.info("Executing adminInbox commandDto {} for Nickname: {}", uploadCommand.getCommand(), getAuthUser().getUsername());
        try {
            var command = toDomain(uploadCommand);
            switch (uploadCommand.getCommand()) {
                case REJECT -> manageInboxUseCase.rejectInboxEntry(command);
                case IMPORT_PHOTO -> manageInboxUseCase.importPhoto(command);
                case IMPORT_MISSING_STATION -> manageInboxUseCase.importMissingStation(command);
                case ACTIVATE_STATION -> manageInboxUseCase.updateStationActiveState(command, true);
                case DEACTIVATE_STATION -> manageInboxUseCase.updateStationActiveState(command, false);
                case DELETE_STATION -> manageInboxUseCase.deleteStation(command);
                case DELETE_PHOTO -> manageInboxUseCase.deletePhoto(command);
                case MARK_SOLVED -> manageInboxUseCase.markProblemReportSolved(command);
                case CHANGE_NAME -> manageInboxUseCase.changeStationTitle(command);
                case UPDATE_LOCATION -> manageInboxUseCase.updateLocation(command);
                case PHOTO_OUTDATED -> manageInboxUseCase.markPhotoOutdated(command);
                default ->
                        throw new IllegalArgumentException("Unexpected commandDto value: " + uploadCommand.getCommand());
            }
        } catch (IllegalArgumentException e) {
            log.warn("adminInbox commandDto {} failed", uploadCommand, e);
            return new ResponseEntity<>(new AdminInboxCommandResponseDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new AdminInboxCommandResponseDto(HttpStatus.OK.value(), "ok"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NextZResponseDto> nextZGet() {
        return ResponseEntity.ok(new NextZResponseDto(manageInboxUseCase.getNextZ()));
    }

    @Override
    public ResponseEntity<List<PublicInboxEntryDto>> publicInboxGet() {
        return ResponseEntity.ok(toPublicInboxEntryDto(manageInboxUseCase.publicInbox()));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<InboxResponseDto> reportProblemPost(ProblemReportDto problemReport) {
        var locale = localeResolver.resolveLocale(getRequest());
        var user = getAuthUser().getUser();
        if (!user.getLocale().equals(locale)) {
            manageProfileUseCase.updateLocale(user, locale);
        }

        var inboxResponse = InboxResponseMapper.toDto(manageInboxUseCase.reportProblem(toDomain(problemReport), user, getUserAgent()));
        return new ResponseEntity<>(inboxResponse, toHttpStatus(inboxResponse.getState()));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<List<InboxStateQueryResponseDto>> userInboxGet() {
        return ResponseEntity.ok(toInboxStateQueryDto(manageInboxUseCase.userInbox(getAuthUser().getUser())));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<List<InboxStateQueryResponseDto>> userInboxPost(List<InboxStateQueryRequestDto> uploadStateQueries) {
        return ResponseEntity.ok(toInboxStateQueryDto(manageInboxUseCase.userInbox(getAuthUser().getUser(), toIdList(uploadStateQueries))));
    }

}
