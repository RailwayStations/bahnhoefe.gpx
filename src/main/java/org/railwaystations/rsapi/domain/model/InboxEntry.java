package org.railwaystations.rsapi.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InboxEntry extends PublicInboxEntry {
    @JsonProperty
    private int id;

    @JsonIgnore
    private int photographerId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String photographerNickname;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String photographerEmail;

    @JsonIgnore
    private String extension;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String comment;

    @JsonProperty
    private String rejectReason;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean done;

    @JsonProperty
    private Command command;

    @JsonProperty(value = "hasPhoto", access = JsonProperty.Access.READ_ONLY)
    private boolean hasPhoto;

    @JsonProperty(value = "crc32", access = JsonProperty.Access.READ_ONLY)
    private Long crc32;

    @JsonProperty(value = "hasConflict", access = JsonProperty.Access.READ_ONLY)
    private boolean conflict;

    @JsonProperty(value = "problemReportType", access = JsonProperty.Access.READ_ONLY)
    private ProblemReportType problemReportType;

    @JsonProperty(value = "isProcessed", access = JsonProperty.Access.READ_ONLY)
    private boolean processed;

    @JsonProperty(value = "inboxUrl", access = JsonProperty.Access.READ_ONLY)
    private String inboxUrl;

    @JsonProperty(value = "DS100")
    private String ds100;

    @JsonProperty(value = "active")
    private Boolean active;

    @JsonProperty(value = "ignoreConflict")
    private Boolean ignoreConflict;

    @JsonProperty(value = "createStation")
    private Boolean createStation;

    @JsonIgnore
    private boolean notified;

    /**
     * Constructor with all values from database
     */
    public InboxEntry(final int id, final String countryCode, final String stationId, final String title,
                      final Coordinates coordinates, final int photographerId, final String photographerNickname, final String photographerEmail,
                      final String extension, final String comment, final String rejectReason,
                      final Long createdAt, final boolean done, final Command command, final boolean hasPhoto,
                      final boolean conflict, final ProblemReportType problemReportType, final Boolean active,
                      final Long crc32, final boolean notified) {
        super(countryCode, stationId, title, coordinates);
        this.id = id;
        this.photographerId = photographerId;
        this.photographerNickname = photographerNickname;
        this.photographerEmail = photographerEmail;
        this.extension = extension;
        this.comment = comment;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.done = done;
        this.command = command;
        this.hasPhoto = hasPhoto;
        this.conflict = conflict;
        this.problemReportType = problemReportType;
        this.active = active;
        this.crc32 = crc32;
        this.notified = notified;
    }

    /**
     * Constructor to insert new record from photoUpload
     */
    public InboxEntry(final String countryCode, final String stationId, final String title,
                      final Coordinates coordinates, final int photographerId,
                      final String extension, final String comment, final ProblemReportType problemReportType,
                      final Boolean active) {
        this(0, countryCode, stationId, title, coordinates, photographerId, null, null, extension,
                comment, null, System.currentTimeMillis(), false, null, false,
                false, problemReportType, active, null, false);
    }

    /**
     * Constructor to deserialize json for updating the records
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    public InboxEntry(@JsonProperty("id") final int id,
                      @JsonProperty("countryCode") final String countryCode,
                      @JsonProperty("stationId") final String stationId,
                      @JsonProperty("rejectReason") final String rejectReason,
                      @JsonProperty("command") final Command command,
                      @JsonProperty("DS100") final String ds100,
                      @JsonProperty("active") final Boolean active,
                      @JsonProperty("ignoreConflict") final Boolean ignoreConflict,
                      @JsonProperty(value = "createStation") final Boolean createStation) {
        this(id, countryCode, stationId, null, null, 0, null, null,
                null, null, rejectReason, null, false, command, false,
                false, null, active != null ? active : true, null, false);
        this.ds100 = ds100;
        this.ignoreConflict = ignoreConflict;
        this.createStation = createStation;
    }

    public InboxEntry() {
        super();
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setPhotographerId(final int photographerId) {
        this.photographerId = photographerId;
    }

    public void setPhotographerNickname(final String photographerNickname) {
        this.photographerNickname = photographerNickname;
    }

    public void setPhotographerEmail(final String photographerEmail) {
        this.photographerEmail = photographerEmail;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setRejectReason(final String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public void setCreatedAt(final Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setDone(final boolean done) {
        this.done = done;
    }

    public void setCommand(final Command command) {
        this.command = command;
    }

    public boolean isHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(final boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public void setCrc32(final Long crc32) {
        this.crc32 = crc32;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setProblemReportType(final ProblemReportType problemReportType) {
        this.problemReportType = problemReportType;
    }

    public void setProcessed(final boolean processed) {
        this.processed = processed;
    }

    @JsonProperty(value = "DS100")
    public String getDs100() {
        return ds100;
    }

    public void setDs100(final String ds100) {
        this.ds100 = ds100;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public Boolean getIgnoreConflict() {
        return ignoreConflict;
    }

    public void setIgnoreConflict(final Boolean ignoreConflict) {
        this.ignoreConflict = ignoreConflict;
    }

    public Boolean getCreateStation() {
        return createStation;
    }

    public void setCreateStation(final Boolean createStation) {
        this.createStation = createStation;
    }

    public void setNotified(final boolean notified) {
        this.notified = notified;
    }

    public int getId() {
        return id;
    }

    public int getPhotographerId() {
        return photographerId;
    }

    public String getPhotographerNickname() {
        return photographerNickname;
    }

    public String getPhotographerEmail() {
        return photographerEmail;
    }

    public String getComment() {
        return comment;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public boolean isDone() {
        return done;
    }

    public Command getCommand() {
        return command;
    }

    public String getExtension() {
        return extension;
    }

    public boolean hasPhoto() {
        return hasPhoto;
    }

    public boolean hasConflict() {
        return conflict;
    }

    public ProblemReportType getProblemReportType() {
        return problemReportType;
    }

    public String getFilename() {
        return getFilename(id, extension);
    }

    public static String getFilename(final Integer id, final String extension) {
        if (id == null || extension == null) {
            return null;
        }
        return String.format("%d.%s", id, extension);
    }

    public void isProcessed(final boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public String getInboxUrl() {
        return inboxUrl;
    }

    public void setInboxUrl(final String inboxUrl) {
        this.inboxUrl = inboxUrl;
    }

    public boolean isProblemReport() {
        return problemReportType != null;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean ignoreConflict() {
        return ignoreConflict;
    }

    public Boolean createStation() {
        return createStation;
    }

    public void setConflict(final boolean conflict) {
        this.conflict = conflict;
    }

    public boolean hasCoords() {
        return coordinates != null && !coordinates.hasZeroCoords();
    }

    public Long getCrc32() {
        return crc32;
    }

    public boolean isNotified() {
        return notified;
    }

    public enum Command {
        IMPORT,
        ACTIVATE_STATION,
        DEACTIVATE_STATION,
        DELETE_STATION,
        DELETE_PHOTO,
        MARK_SOLVED,
        REJECT,
        CHANGE_NAME,
        UPDATE_LOCATION
    }

}