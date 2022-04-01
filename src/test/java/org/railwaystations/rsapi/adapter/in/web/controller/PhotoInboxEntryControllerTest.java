package org.railwaystations.rsapi.adapter.in.web.controller;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.railwaystations.rsapi.adapter.out.db.*;
import org.railwaystations.rsapi.adapter.out.mastodon.MastodonBotConfig;
import org.railwaystations.rsapi.adapter.out.mastodon.MastodonBotHttpClient;
import org.railwaystations.rsapi.adapter.out.monitoring.MockMonitor;
import org.railwaystations.rsapi.adapter.out.photostorage.PhotoFileStorage;
import org.railwaystations.rsapi.adapter.out.photostorage.WorkDir;
import org.railwaystations.rsapi.app.auth.AuthUser;
import org.railwaystations.rsapi.app.auth.RSAuthenticationProvider;
import org.railwaystations.rsapi.app.auth.RSUserDetailsService;
import org.railwaystations.rsapi.core.model.*;
import org.railwaystations.rsapi.core.services.InboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("PMD.TooManyStaticImports")
public class PhotoInboxEntryControllerTest {

    public static final String IMAGE_CONTENT = "image-content";
    private WorkDir workDir;
    private InboxController inboxController;
    private InboxDao inboxDao = null;
    private final MockMonitor monitor = new MockMonitor();
    private RSAuthenticationProvider authenticator;

    @BeforeEach
    public void setUp() throws IOException {
        final Station.Key key0815 = new Station.Key("ch", "0815");
        final Station station0815 = new Station(key0815, "Station 0815", new Coordinates(40.1, 7.0), "LAL", new Photo(key0815, "URL", createUser("Jim Knopf", 18), null, "CC0"), true);
        final Station.Key key4711 = new Station.Key("de", "4711");
        final Station station4711 = new Station(key4711, "Lummerland", new Coordinates(50.0, 9.0), "XYZ", null, true);
        final Station.Key key1234 = new Station.Key("de", "1234");
        final Station station1234 = new Station(key1234, "Neverland", new Coordinates(51.0, 10.0), "ABC", new Photo(key1234, "URL", createUser("Jim Knopf"), null, "CC0"), true);
        final Station.Key key5678 = new Station.Key("de", "5678");
        final Station station5678 = new Station(key5678, "Phantasia", new Coordinates(51.0, 10.0), "DEF", new Photo(key5678, "URL", createUser("nickname"), null, "CC0"), true);
        final Station.Key key9876 = new Station.Key("de", "9876");
        final Station station9876 = new Station(key9876, "Station 9876", new Coordinates(52.0, 8.0), "EFF", new Photo(key9876, "URL", createUser("nickname", 42), null, "CC0"), true);

        final UserDao userDao = mock(UserDao.class);
        final User userNickname = new User("nickname", null, "CC0", 42, "nickname@example.com", true, true, null, false, User.EMAIL_VERIFIED, false);
        when(userDao.findByEmail("nickname@example.com")).thenReturn(Optional.of(userNickname));
        final User userSomeuser = new User("someuser", "someuser@example.com", "CC0", true, null, true, null, true);
        when(userDao.findByEmail("someuser@example.com")).thenReturn(Optional.of(userSomeuser));
        inboxDao = mock(InboxDao.class);
        final CountryDao countryDao = mock(CountryDao.class);
        final PhotoDao photoDao = mock(PhotoDao.class);

        workDir = new WorkDir(Files.createTempDirectory("rsapi").toString(), null);
        final StationDao stationDao = mock(StationDao.class);
        when(stationDao.findByKey(key4711.getCountry(), key4711.getId())).thenReturn(Set.of(station4711));
        when(stationDao.findByKey(key1234.getCountry(), key1234.getId())).thenReturn(Set.of(station1234));
        when(stationDao.findByKey(key5678.getCountry(), key5678.getId())).thenReturn(Set.of(station5678));
        when(stationDao.findByKey(key0815.getCountry(), key0815.getId())).thenReturn(Set.of(station0815));
        when(stationDao.findByKey(key9876.getCountry(), key9876.getId())).thenReturn(Set.of(station9876));

        authenticator = mock(RSAuthenticationProvider.class);

        inboxController = new InboxController(new InboxService(stationDao, new PhotoFileStorage(workDir), monitor,
                inboxDao, userDao, countryDao, photoDao, "http://inbox.railway-stations.org", new MastodonBotHttpClient(new MastodonBotConfig())),
                authenticator, new RSUserDetailsService(userDao));
    }

    private InboxResponse whenPostImage(final String nickname, final int userId, final String email, final String stationId, final String country,
                                        final String stationTitle, final Double latitude, final Double longitude, final String comment) throws IOException {
        return whenPostImage(nickname, userId, email, stationId, country, stationTitle, latitude, longitude, comment, User.EMAIL_VERIFIED);
    }

    private InboxResponse whenPostImage(final String nickname, final int userId, final String email, final String stationId, final String country,
                                        final String stationTitle, final Double latitude, final Double longitude, final String comment, final String emailVerification) throws IOException {
        final byte[] inputBytes = "image-content".getBytes(Charset.defaultCharset());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(inputBytes);
        final ResponseEntity<InboxResponse> response = inboxController.photoUpload(request, "UserAgent", stationId, country, "image/jpeg",
                stationTitle, latitude, longitude, comment, null,
                new AuthUser(new User(nickname, null, "CC0", userId, email, true, false, null, false, emailVerification, true), Collections.emptyList()));
        return response.getBody();
    }

    @Test
    public void testPostIframeUnauthorized() throws IOException {
        when(authenticator.authenticate(new UsernamePasswordAuthenticationToken("unknown@example.com", "secretUploadToken"))).thenReturn(null);
        when(inboxDao.insert(any())).thenReturn(1);
        final String response = whenPostImageIframe("unknown@example.com", "http://localhost/uploadPage.php");

        assertThat(response, containsString("UNAUTHORIZED"));
        verify(inboxDao, never()).insert(any());
        assertThat(monitor.getMessages().size(), is(0));
    }

    @Test
    public void testPostIframeEmailNotVerified() throws IOException {
        when(authenticator.authenticate(new UsernamePasswordAuthenticationToken("someuser@example.com", "secretUploadToken"))).thenReturn(new UsernamePasswordAuthenticationToken("","", Collections.emptyList()));
        when(inboxDao.insert(any())).thenReturn(1);
        final String response = whenPostImageIframe("someuser@example.com", "http://localhost/uploadPage.php");

        assertThat(response, containsString("UNAUTHORIZED"));
        assertThat(response, containsString("Email not verified"));
        verify(inboxDao, never()).insert(any());
        assertThat(monitor.getMessages().size(), is(0));
    }

    @Test
    public void testPostIframeMaliciousReferer() throws IOException {
        when(authenticator.authenticate(new UsernamePasswordAuthenticationToken("nickname@example.com", "secretUploadToken"))).thenReturn(new UsernamePasswordAuthenticationToken("","", Collections.emptyList()));
        when(inboxDao.insert(any())).thenReturn(1);
        try {
            final String response = whenPostImageIframe("nickname@example.com", "http://localhost/uploadPage.php<script>alert('FooBar!');</script>");
            fail("IllegalArgumentException expected, but got: " + response);
        } catch (final IllegalArgumentException e) {
            assertThat(e.getCause(), instanceOf(URISyntaxException.class));
        }
        verify(inboxDao, never()).insert(any());
        assertThat(monitor.getMessages().size(), is(0));
    }

    @Test
    public void testPostIframe() throws IOException {
        final ArgumentCaptor<InboxEntry> uploadCaptor = ArgumentCaptor.forClass(InboxEntry.class);
        when(authenticator.authenticate(new UsernamePasswordAuthenticationToken("nickname@example.com", "secretUploadToken"))).thenReturn(new UsernamePasswordAuthenticationToken("","", Collections.emptyList()));
        when(inboxDao.insert(any())).thenReturn(1);
        final String response = whenPostImageIframe("nickname@example.com", "http://localhost/uploadPage.php");

        assertThat(response, containsString("REVIEW"));
        assertFileWithContentExistsInInbox("image-content", "1.jpg");
        verify(inboxDao).insert(uploadCaptor.capture());
        assertUpload(uploadCaptor.getValue(), "de","4711", null, null);

        assertThat(monitor.getMessages().get(0), equalTo("New photo upload for Lummerland - de:4711\nSome Comment\nhttp://inbox.railway-stations.org/1.jpg\nby nickname\nvia UserAgent"));
    }

    private String whenPostImageIframe(final String email,
                                       final String referer) throws IOException {
        return (String) inboxController.photoUploadIframe("UserAgent", email, "secretUploadToken", "4711", "de", null, null, null, "Some Comment", null,
                new MockMultipartFile("1.jpg", "1.jpg", "image/jpeg", "image-content".getBytes(Charset.defaultCharset())), referer).getModel().get("response");
    }

    @Test
    public void testPost() throws IOException {
        final ArgumentCaptor<InboxEntry> uploadCaptor = ArgumentCaptor.forClass(InboxEntry.class);
        when(inboxDao.insert(any())).thenReturn(1);
        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com","4711", "de", null, null, null, "Some Comment");

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.REVIEW));
        assertThat(response.getId(), equalTo(1));
        assertThat(response.getFilename(), equalTo("1.jpg"));
        assertFileWithContentExistsInInbox("image-content", "1.jpg");
        verify(inboxDao).insert(uploadCaptor.capture());
        assertUpload(uploadCaptor.getValue(), "de","4711", null, null);

        assertThat(monitor.getMessages().get(0), equalTo("New photo upload for Lummerland - de:4711\nSome Comment\nhttp://inbox.railway-stations.org/1.jpg\nby @nick name\nvia UserAgent"));
    }

    private void assertUpload(final InboxEntry inboxEntry, final String countryCode, final String stationId, final String title, final Coordinates coordinates) {
        assertThat(inboxEntry.getCountryCode(), equalTo(countryCode));
        assertThat(inboxEntry.getStationId(), equalTo(stationId));
        assertThat(inboxEntry.getTitle(), equalTo(title));
        assertThat(inboxEntry.getPhotographerId(), equalTo(42));
        assertThat(inboxEntry.getComment(), equalTo("Some Comment"));
        assertThat(Duration.between(inboxEntry.getCreatedAt(), Instant.now()).getSeconds() < 5, equalTo(true));
        if (coordinates != null) {
            assertThat(inboxEntry.getCoordinates().getLat(), equalTo(coordinates.getLat()));
            assertThat(inboxEntry.getCoordinates().getLon(), equalTo(coordinates.getLon()));
        } else {
            assertThat(inboxEntry.getCoordinates(), nullValue());
        }
        assertThat(inboxEntry.isDone(), equalTo(false));
    }

    @Test
    public void testPostMissingStation() throws IOException {
        when(inboxDao.insert(any())).thenReturn(4);
        final ArgumentCaptor<InboxEntry> uploadCaptor = ArgumentCaptor.forClass(InboxEntry.class);
        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com",null, null, "Missing Station", 50.9876d, 9.1234d, "Some Comment");

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.REVIEW));
        assertThat(response.getId(), equalTo(4));
        assertThat(response.getFilename(), equalTo("4.jpg"));
        assertFileWithContentExistsInInbox(IMAGE_CONTENT, "4.jpg");
        verify(inboxDao).insert(uploadCaptor.capture());
        assertUpload(uploadCaptor.getValue(), null,null, "Missing Station", new Coordinates(50.9876, 9.1234));

        assertThat(monitor.getMessages().get(0), equalTo("Photo upload for missing station Missing Station at https://map.railway-stations.org/index.php?mlat=50.9876&mlon=9.1234&zoom=18&layers=M\nSome Comment\nhttp://inbox.railway-stations.org/4.jpg\nby @nick name\nvia UserAgent"));
    }

    @ParameterizedTest
    @CsvSource({"-91d, 9.1234d",
                "91d, 9.1234d",
                "50.9876d, -181d",
                "50.9876d, 181d",
    })
    public void testPostMissingStationLatLonOutOfRange(final Double latitude, final Double longitude) throws IOException {
        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com",null, null, "Missing Station", latitude, longitude, null);

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.LAT_LON_OUT_OF_RANGE));
        assertThat(response.getId(), nullValue());
        assertThat(response.getFilename(), nullValue());
    }

    @Test
    public void testPostSomeUserWithTokenSalt() throws IOException {
        when(inboxDao.insert(any())).thenReturn(3);
        final InboxResponse response = whenPostImage("@someuser", 11, "someuser@example.com","4711", "de", null, null, null, null);

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.REVIEW));
        assertThat(response.getId(), equalTo(3));
        assertThat(response.getFilename(), equalTo("3.jpg"));
        assertFileWithContentExistsInInbox(IMAGE_CONTENT, "3.jpg");
        assertThat(monitor.getMessages().get(0), equalTo("New photo upload for Lummerland - de:4711\n\nhttp://inbox.railway-stations.org/3.jpg\nby @someuser\nvia UserAgent"));
    }

    @Test
    public void testPostDuplicateInbox() throws IOException {
        when(inboxDao.insert(any())).thenReturn(2);
        when(inboxDao.countPendingInboxEntriesForStation(null, "de", "4711")).thenReturn(1);

        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com","4711", "de", null, null, null, null);

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.CONFLICT));
        assertThat(response.getId(), equalTo(2));
        assertThat(response.getFilename(), equalTo("2.jpg"));
        assertFileWithContentExistsInInbox(IMAGE_CONTENT, "2.jpg");
        assertThat(monitor.getMessages().get(0), equalTo("New photo upload for Lummerland - de:4711\n\nhttp://inbox.railway-stations.org/2.jpg (possible duplicate!)\nby @nick name\nvia UserAgent"));
    }

    @Test
    public void testUserInbox() {
        final User user = new User("nickname", null, "CC0", 42, "nickname@example.com", true, false, null, false, null, true);

        when(inboxDao.findById(1)).thenReturn(new InboxEntry(1, "de", "4711", "Station 4711", new Coordinates(50.1,9.2), user.getId(), user.getName(), null, "jpg", null, null, Instant.now(), false, null, false, false, null, null, null, false));
        when(inboxDao.findById(2)).thenReturn(new InboxEntry(2, "de", "1234", "Station 1234", new Coordinates(50.1,9.2), user.getId(), user.getName(), null, "jpg", null, null, Instant.now(), true, null, false, false, null, null, null, false));
        when(inboxDao.findById(3)).thenReturn(new InboxEntry(3, "de", "5678", "Station 5678", new Coordinates(50.1,9.2), user.getId(), user.getName(), null, "jpg", null, "rejected", Instant.now(), true, null, false, false, null, null, null, false));
        when(inboxDao.findById(4)).thenReturn(new InboxEntry(4, "ch", "0815", "Station 0815", new Coordinates(50.1,9.2), user.getId(), user.getName(), null, "jpg", null, null, Instant.now(), false, null, false, false, null, null, null, false));

        final List<InboxStateQuery> inboxStateQueries = new ArrayList<>();
        inboxStateQueries.add(new InboxStateQuery(1, "de", "4711", null, null, null));
        inboxStateQueries.add(new InboxStateQuery(2, "de", "1234", null, null, null));
        inboxStateQueries.add(new InboxStateQuery(3, "de", "5678", null, null, null));
        inboxStateQueries.add(new InboxStateQuery(4,"ch", "0815", null, null, null));

        final List<InboxStateQuery> uploadStateQueriesResult = inboxController.userInbox(new AuthUser(user, Collections.emptyList()), inboxStateQueries);

        assertThat(uploadStateQueriesResult.get(0).getState(), is(InboxStateQuery.InboxState.REVIEW));
        assertThat(uploadStateQueriesResult.get(0).getFilename(), is("1.jpg"));
        assertThat(uploadStateQueriesResult.get(1).getState(), is(InboxStateQuery.InboxState.ACCEPTED));
        assertThat(uploadStateQueriesResult.get(2).getState(), is(InboxStateQuery.InboxState.REJECTED));
        assertThat(uploadStateQueriesResult.get(3).getState(), is(InboxStateQuery.InboxState.CONFLICT));
    }

    private void assertFileWithContentExistsInInbox(final String content, final String filename) throws IOException {
        final Path image = workDir.getInboxDir().resolve(filename);
        assertThat(Files.exists(image), equalTo(true));

        final byte[] inputBytes = content.getBytes(Charset.defaultCharset());
        final byte[] outputBytes = new byte[inputBytes.length];
        IOUtils.readFully(Files.newInputStream(image), outputBytes);
        assertThat(outputBytes, equalTo(inputBytes));
    }

    @Test
    public void testPostDuplicate() throws IOException {
        when(inboxDao.insert(any())).thenReturn(5);
        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com","1234", "de", null, null, null, null);

        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.CONFLICT));
        assertThat(response.getId(), equalTo(5));
        assertThat(response.getFilename(), equalTo("5.jpg"));
        assertFileWithContentExistsInInbox(IMAGE_CONTENT, "5.jpg");
        assertThat(monitor.getMessages().get(0), equalTo("New photo upload for Neverland - de:1234\n\nhttp://inbox.railway-stations.org/5.jpg (possible duplicate!)\nby @nick name\nvia UserAgent"));
    }

    @Test
    public void testPostEmailNotVerified() throws IOException {
        final InboxResponse response = whenPostImage("@nick name", 42, "nickname@example.com","1234", "de", null, null, null, null, User.EMAIL_VERIFICATION_TOKEN + "blahblah");
        assertThat(response.getState(), equalTo(InboxResponse.InboxResponseState.UNAUTHORIZED));
    }

    @Test
    public void testPostInvalidCountry() throws IOException {
        final ResponseEntity<InboxResponse> response = inboxController.photoUpload(new MockHttpServletRequest(), "UserAgent", "4711", "xy", "image/jpeg",
                null, null, null, null, null,
                new AuthUser(new User("nickname", null, "CC0", 0,  "nickname@example.com", true, false, null, false, User.EMAIL_VERIFIED, true), Collections.emptyList()));
        final InboxResponse inboxResponse = response.getBody();
        assert inboxResponse != null;
        assertThat(inboxResponse.getState(), equalTo(InboxResponse.InboxResponseState.NOT_ENOUGH_DATA));
        assertThat(inboxResponse.getId(), nullValue());
        assertThat(inboxResponse.getFilename(), nullValue());
    }

    @Test
    public void testPostProblemReport() {
        when(inboxDao.insert(any())).thenReturn(6);
        final ResponseEntity<InboxResponse> response = inboxController.reportProblem("UserAgent", new ProblemReport("de", "1234", ProblemReportType.OTHER, "something is wrong", null),
                new AuthUser(new User("@nick name", null, "CC0", 42, "nickname@example.com", true, false, null, false, User.EMAIL_VERIFIED, true), Collections.emptyList()));

        final InboxResponse responseBody = response.getBody();
        assertThat(responseBody, notNullValue());
        assertThat(responseBody.getState(), equalTo(InboxResponse.InboxResponseState.REVIEW));
        assertThat(responseBody.getId(), equalTo(6));
        assertThat(responseBody.getFilename(), nullValue());
        assertThat(monitor.getMessages().get(0), equalTo("New problem report for Neverland - de:1234\nOTHER: something is wrong\nby @nick name\nvia UserAgent"));
    }

    @Test
    public void testPostProblemReportEmailNotVerified() {
        final ResponseEntity<InboxResponse> response = inboxController.reportProblem("UserAgent", new ProblemReport("de", "1234", ProblemReportType.OTHER, "something is wrong", null),
                new AuthUser(new User("@nick name", null, "CC0", 42, "nickname@example.com", true, false, null, false, User.EMAIL_VERIFICATION_TOKEN + "blah", true), Collections.emptyList()));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody().getState(), equalTo(InboxResponse.InboxResponseState.UNAUTHORIZED));
    }

    private User createUser(final String name) {
        return createUser(name, 0);
    }

    private User createUser(final String name, final int id) {
        return new User(name, "photographerUrl", "CC0", id, null, true, false, null, false, User.EMAIL_VERIFIED, true);
    }


}
