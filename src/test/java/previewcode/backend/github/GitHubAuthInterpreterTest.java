package previewcode.backend.github;

import io.atlassian.fugue.Unit;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.GitHubUser;
import previewcode.backend.DTO.GitHubUserToken;
import previewcode.backend.DTO.InstallationID;
import previewcode.backend.api.exceptionmapper.NotAuthorizedException;
import previewcode.backend.api.filter.IJWTTokenCreator;
import previewcode.backend.services.actiondsl.ActionCache;
import previewcode.backend.services.actiondsl.ActionDSL;
import previewcode.backend.services.actiondsl.Interpreter;
import previewcode.backend.services.http.IHttpRequestExecutor;
import previewcode.backend.services.interpreters.GitHubAuthInterpreter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static previewcode.backend.services.actiondsl.ActionDSL.Action;
import static previewcode.backend.services.actions.GitHubActions.*;

public class GitHubAuthInterpreterTest {

    static final SecretKeySpec sharedSecret = new SecretKeySpec("very-secret".getBytes(), "HmacSHA1");
    static final String exampleBody = "Hello World!";
    static final String headerPrefix = "sha1=";
    static final String integrationId = "123456789";
    static final InstallationID installationID = new InstallationID("987654321");
    static String expectedSHA1;

    IHttpRequestExecutor defaultHttpExec = r -> {
        throw new RuntimeException("Cannot fire requests in a test");
    };

    IJWTTokenCreator jwtCreator = integrationId -> integrationId + "token";

    ActionCache cache = new ActionCache.Builder().maximumEntries(0).build();

    class GitHubInterpreter extends GitHubAuthInterpreter {
        public GitHubInterpreter(String integrationId, SecretKeySpec sharedSecret, IJWTTokenCreator jwtTokenCreator, IHttpRequestExecutor http, ActionCache cache) {
            super(integrationId, sharedSecret, jwtTokenCreator, http, cache);
        }

        @Override
        protected void authViaOldApi(GetUser action) throws IOException {
            // Do nothing
        }
    }

    Interpreter ghInterpreter = new GitHubInterpreter(integrationId, sharedSecret, jwtCreator, defaultHttpExec, cache);

    @BeforeAll
    static void setup() throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(sharedSecret);
        expectedSHA1 = Hex.encodeHexString(mac.doFinal(exampleBody.getBytes()));
    }

    @Test
    void isWebHook_succeedsForCorrectString(){
        assertThat(eval(isWebHookUserAgent("GitHub-Hookshot/")))
                .isTrue();
    }

    @Test
    void isWebHook_failsForEmptyString(){
        assertThat(eval(isWebHookUserAgent("")))
                .isFalse();
    }

    @Test
    void isWebHook_failsForNull(){
        assertThat(eval(isWebHookUserAgent(null)))
                .isFalse();
    }

    @Test
    void isWebHook_failsForSimilarStrings(){
        assertThat(eval(isWebHookUserAgent("GitHub-Hookshot")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("GitHub-Hookshot\\")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("Github-Hookshot/")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("Hookshot/")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("GitHub")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("-")))
                .isFalse();

        assertThat(eval(isWebHookUserAgent("/")))
                .isFalse();
    }

    @Test
    void verifySecret_succeedsForCorrectParameters(){
        Action<?> action = verifyWebHookSecret(exampleBody, headerPrefix + expectedSHA1);
        eval(action);
    }

    @Test
    void verifySecret_failsWhenChanging_requestBody() {
        Action<?> action = verifyWebHookSecret(exampleBody+".", headerPrefix + expectedSHA1);

        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> eval(action));
    }

    @Test
    void verifySecret_failsWhenChanging_receivedSHA() {
        Action<?> action = verifyWebHookSecret(exampleBody, headerPrefix + expectedSHA1+"a");

        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> eval(action));
    }

    @Test
    void verifySecret_failWhenChanging_storedKey() throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec newKey = new SecretKeySpec("very-secret!".getBytes(), "HmacSHA1");
        ghInterpreter = new GitHubInterpreter(integrationId, newKey, jwtCreator, defaultHttpExec, cache);

        Action<?> action = verifyWebHookSecret(exampleBody, headerPrefix + expectedSHA1);

        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> eval(action));
    }

    @Test
    void verifySecret_shaMustBePrefixed() {
        Action<?> action = verifyWebHookSecret(exampleBody, expectedSHA1);

        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> eval(action));
    }


    @Test
    void getUser_callsGitHub_withAuthHeader() {
        String userToken = "328943724";
        Action<?> action = getUser(new GitHubUserToken(userToken));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> {
                    assertThat(request.header("Authorization")).isEqualTo("token " + userToken);
                    throw new DoneException();
                }, cache);

        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void getUser_callsGitHub_userEndpoint() {
        Action<?> action = getUser(new GitHubUserToken(""));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> {
                    assertThat(request.url().pathSegments()).containsExactly("user");
                    throw new DoneException();
                }, cache);

        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void getUser_callsGitHub_withHTTPS() {
        Action<?> action = getUser(new GitHubUserToken(""));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> {
                    assertThat(request.isHttps()).isTrue();
                    throw new DoneException();
                }, cache);

        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void getUser_callsGitHub_onCorrectHost() {
        Action<?> action = getUser(new GitHubUserToken(""));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> {
                    assertThat(request.url().host()).isEqualTo("api.github.com");
                    throw new DoneException();
                }, cache);

        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void getUser_callsGitHub_methodGET() {
        Action<?> action = getUser(new GitHubUserToken(""));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> {
                    assertThat(request.method()).isEqualTo("GET");
                    throw new DoneException();
                }, cache);

        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void getUser_parses_idAndLogin(){
        String exampleUserResponse = "{\n" +
                "  \"login\": \"octocat\",\n" +
                "  \"id\": 1\n" +
                "}";


        Action<GitHubUser> action = getUser(new GitHubUserToken(""));
        GitHubInterpreter interpreter = new GitHubInterpreter(null, null, null,
                request -> exampleUserResponse, cache);

        GitHubUser parsedResponse = interpreter.unsafeEvaluate(action);
        assertThat(parsedResponse.id).isEqualTo(1);
        assertThat(parsedResponse.login).isEqualTo("octocat");
    }

    @Test
    void authInstallation_usesJWT() {
        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, i -> {
            assertThat(i).isEqualTo(integrationId);
            throw new DoneException();
        }, null, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void authInstallation_callsGitHub_withPOST() {

        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, jwtCreator,
                request -> {
                    assertThat(request.method()).isEqualTo("POST");
                    throw new DoneException();
                }, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void authInstallation_callsGitHub_withHTTPS() {

        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, jwtCreator,
                request -> {
                    assertThat(request.isHttps()).isTrue();
                    throw new DoneException();
                }, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void authInstallation_callsGitHub_withCorrectPath() {

        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, jwtCreator,
                request -> {
                    assertThat(request.url().pathSegments())
                            .containsExactly("installations", installationID.id, "access_tokens");
                    throw new DoneException();
                }, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void authInstallation_callsGitHub_withAcceptHeader() {

        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, jwtCreator,
                request -> {
                    assertThat(request.header("Accept")).isEqualTo("application/vnd.github.machine-man-preview+json");
                    throw new DoneException();
                }, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    @Test
    void authInstallation_callsGitHub_withAuthorizationHeader() {

        GitHubInterpreter interpreter = new GitHubInterpreter(integrationId, null, jwtCreator,
                request -> {
                    assertThat(request.header("Authorization")).isEqualTo("Bearer " + jwtCreator.create(integrationId));
                    throw new DoneException();
                }, cache);

        Action<Unit> action = authenticateInstallation(installationID);
        assertThatExceptionOfType(DoneException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(action));
    }

    class DoneException extends RuntimeException {}

    protected <T> T eval(ActionDSL.Action<T> action){
        return ghInterpreter.unsafeEvaluate(action);
    }
}
