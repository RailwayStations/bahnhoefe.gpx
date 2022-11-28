package org.railwaystations.rsapi.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.railwaystations.rsapi.core.ports.out.Mailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.error.include-message=always"})
@ActiveProfiles("test")
class ProfileIntegrationTests extends AbstractMariaDBBaseTest {

    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private Mailer mailer;

    @Test
    void register() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.postForEntity(
                String.format("http://localhost:%d%s", port, "/registration"), new HttpEntity<>("""
                        {
                        	"nickname": "nickname ",
                        	"email": "nick.name@example.com",
                        	"license": "CC0",
                        	"photoOwner": true,
                        	"link": ""
                        }
                        """, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        verify(mailer, Mockito.times(1))
                .send(eq("nick.name@example.com"),
                        eq("Railway-Stations.org new password"),
                        matches("""
                                Hello,

                                your new password is: .*

                                Cheers
                                Your Railway-Stations-Team

                                ---
                                Hallo,

                                Dein neues Passwort lautet: .*

                                Viele Grüße
                                Dein Bahnhofsfoto-Team"""));
    }

    @Test
    void registerDifferentEmail() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = restTemplate.postForEntity(
                String.format("http://localhost:%d%s", port, "/registration"), new HttpEntity<>("""
                        {
                        	"nickname": "user14",
                        	"email": "other@example.com",
                        	"license": "CC0",
                        	"photoOwner": true,
                        	"link": "link"
                        }""", headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getProfileForbidden() {
        var headers = new HttpHeaders();
        headers.add("Nickname", "nickname");
        headers.add("Email", "nickname@example.com");
        headers.add("Upload-Token", "wrong");
        var response = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getMyProfileWithEmailAndUploadToken() throws IOException {
        var headers = new HttpHeaders();
        headers.add("Upload-Token", "uON60I7XWTIN");
        headers.add("Email", "user10@example.com");
        var response = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProfile(response, "@user10", "https://www.example.com/user10", false, "user10@example.com");
    }

    @Test
    void getMyProfileWithNameAndUploadToken() throws IOException {
        var headers = new HttpHeaders();
        headers.add("Upload-Token", "uON60I7XWTIN");
        headers.add("Email", "@user10");
        var response = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProfile(response, "@user10", "https://www.example.com/user10", false, "user10@example.com");
    }

    @Test
    void getMyProfileWithBasicAuthUploadToken() throws IOException {
        var response = restTemplate.withBasicAuth("@user10", "uON60I7XWTIN")
                .getForEntity(String.format("http://localhost:%d%s", port, "/myProfile"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProfile(response, "@user10", "https://www.example.com/user10", false, "user10@example.com");
    }

    @Test
    void getMyProfileWithBasicAuthPassword() throws IOException {
        var response = restTemplate.withBasicAuth("@user27", "y89zFqkL6hro")
                .getForEntity(String.format("http://localhost:%d%s", port, "/myProfile"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertProfile(response, "@user27", "https://www.example.com/user27", false, null);
    }

    @Test
    void getMyProfileWithBasicAuthPasswordFail() {
        var response = restTemplate.withBasicAuth("@user27", "blahblubb")
                .getForEntity(String.format("http://localhost:%d%s", port, "/myProfile"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void assertProfile(ResponseEntity<String> response, String name, String link, boolean anonymous, String email) throws IOException {
        var jsonNode = mapper.readTree(response.getBody());
        assertThat(jsonNode.get("nickname").asText()).isEqualTo(name);
        if (email != null) {
            assertThat(jsonNode.get("email").asText()).isEqualTo(email);
        } else {
            assertThat(jsonNode.get("email")).isNull();
        }
        if (link != null) {
            assertThat(jsonNode.get("link").asText()).isEqualTo(link);
        } else {
            assertThat(jsonNode.get("link")).isNull();
        }
        assertThat(jsonNode.get("license").asText()).isEqualTo("CC0 1.0 Universell (CC0 1.0)");
        assertThat(jsonNode.get("photoOwner").asBoolean()).isEqualTo(true);
        assertThat(jsonNode.get("anonymous").asBoolean()).isEqualTo(anonymous);
        assertThat(jsonNode.has("uploadToken")).isEqualTo(false);
    }

    @Test
    void updateMyProfileAndChangePassword() throws IOException {
        var firstPassword = "GDAkhaeU2vrK";
        var headers = new HttpHeaders();
        headers.add("Upload-Token", firstPassword);
        headers.add("Email", "user14@example.com");
        var responseGetBefore = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(responseGetBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseGetBefore.getBody()).isNotNull();
        assertProfile(responseGetBefore, "@user14", "https://www.example.com/user14", false, "user14@example.com");

        headers.setContentType(MediaType.APPLICATION_JSON);
        var responsePostUpdate = restTemplate.postForEntity(
                String.format("http://localhost:%d%s", port, "/myProfile"), new HttpEntity<>("""
                        {
                        	"nickname": "user14",
                        	"email": "user14@example.com",
                        	"license": "CC0",
                        	"photoOwner": true,
                        	"anonymous": true
                        }""", headers), String.class);
        assertThat(responsePostUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responsePostUpdate.getBody()).isNotNull();

        var responseGetAfter = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(responseGetAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseGetAfter.getBody()).isNotNull();
        assertProfile(responseGetAfter, "user14", null, true, "user14@example.com");

        var secondPassword = "!\"$%&/()=?-1234567890";
        changePassword(firstPassword, secondPassword, true);
        changePassword(secondPassword, "\\=oF`)X77__U}G", false);
    }

    private void changePassword(String oldPassword, String newPassword, boolean changePasswordViaHeader) {
        var headers = new HttpHeaders();
        headers.setBasicAuth("user14@example.com", oldPassword);

        HttpEntity<Object> changePasswordRequest;
        if (changePasswordViaHeader) {
            headers.add("New-Password", newPassword);
            changePasswordRequest = new HttpEntity<>(headers);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            var changePassword = mapper.createObjectNode();
            changePassword.set("newPassword", new TextNode(newPassword));
            changePasswordRequest = new HttpEntity<>(changePassword, headers);
        }

        var responseChangePassword = restTemplate.postForEntity(
                String.format("http://localhost:%d%s", port, "/changePassword"), changePasswordRequest, String.class);
        assertThat(responseChangePassword.getStatusCode()).isEqualTo(HttpStatus.OK);

        var responseAfterChangedPassword = restTemplate
                .withBasicAuth("user14@example.com", newPassword)
                .getForEntity(String.format("http://localhost:%d%s", port, "/myProfile"), String.class);
        assertThat(responseAfterChangedPassword.getStatusCode()).isEqualTo(HttpStatus.OK);

        var responseWithOldPassword = restTemplate.exchange(String.format("http://localhost:%d%s", port, "/myProfile"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(responseWithOldPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}
