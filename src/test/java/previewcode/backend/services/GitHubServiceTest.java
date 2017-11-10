package previewcode.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.fugue.Unit;
import io.vavr.control.Option;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.InstallationID;
import previewcode.backend.api.exceptionmapper.NoTokenException;
import previewcode.backend.services.actiondsl.Interpreter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.GitHubActions.*;
import static previewcode.backend.services.actions.RequestContextActions.*;

public class GitHubServiceTest {

    IGithubService.V2 ghService = new IGithubService.V2();

    Action<Unit> authAction = ghService.authenticate();



    @Test
    void authenticate_checksUserAgent() {
        assertStopped(() -> interpret()
                .on(GetUserAgent.class).stop()
                .unsafeEvaluate(authAction));
    }

    @Test
    void authenticate_nonWebHook_checksQueryParam(){
        assertStopped(() -> interpret()
                .on(GetUserAgent.class).returnA("non-webhook-user-agent")
                .on(IsWebHookUserAgent.class).returnA(false)
                .on(GetQueryParam.class).stop(action -> assertThat(action.param).isEqualTo("access_token"))
                .unsafeEvaluate(authAction)
        );
    }

    @Test
    void authenticate_nonWebHook_throwsNoTokenException() {
        assertThatExceptionOfType(NoTokenException.class)
        .isThrownBy(() -> interpret()
                .on(GetUserAgent.class).returnA("non-webhook-user-agent")
                .on(IsWebHookUserAgent.class).returnA(false)
                .on(GetQueryParam.class).returnA(Option.none())
                .unsafeEvaluate(authAction)
        );
    }

    @Test
    void authenticate_nonWebHook_fetchesGitHubUser_withToken() {
        assertStopped(() -> interpret()
                .on(GetUserAgent.class).returnA("non-webhook-user-agent")
                .on(IsWebHookUserAgent.class).returnA(false)
                .on(GetQueryParam.class).returnA(Option.of("someToken123"))
                .on(GetUser.class).stop(fetchAction -> assertThat(fetchAction.token.token).isEqualTo("someToken123"))
                .unsafeEvaluate(authAction)
        );
    }

    @Test
    void authenticate_webhook_checksUserAgent(){
        Interpreter.Stepper<Unit> stepper = interpret()
                .on(GetUserAgent.class).returnA("some-user-agent")
                .on(IsWebHookUserAgent.class).returnA(true)
                .stepwiseEval(authAction);
        assertThat(stepper.next()).containsOnly(isWebHookUserAgent("some-user-agent"));
    }

    @Test
    void authenticate_webhook_readsRequestBody_andHeader(){
        Interpreter.Stepper<Unit> stepper = interpret()
                .on(GetUserAgent.class).returnA("GitHub-Hookshot/")
                .on(IsWebHookUserAgent.class).returnA(true)
                .stepwiseEval(authAction);
        stepper.next();
        assertThat(stepper.next()).containsOnly(getRequestBody, getHeader("X-Hub-Signature"));
    }

    @Test
    void authenticate_webhook_verifiesSignature(){
        Interpreter.Stepper<Unit> stepper = interpret()
                .on(GetUserAgent.class).returnA("GitHub-Hookshot/")
                .on(IsWebHookUserAgent.class).returnA(true)
                .on(GetRequestBody.class).returnA("somebody")
                .on(GetHeader.class).returnA("signature")
                .stepwiseEval(authAction);

        stepper.next();
        stepper.next();
        assertThat(stepper.next()).containsOnly(verifyWebHookSecret("somebody", "signature"));
    }

    @Test
    void authenticate_webhook_fetchesInstallationID(){
        Interpreter.Stepper<Unit> stepper = interpret()
                .on(GetUserAgent.class).returnA("GitHub-Hookshot/")
                .on(IsWebHookUserAgent.class).returnA(true)
                .on(GetRequestBody.class).returnA("somebody")
                .on(GetHeader.class).returnA("signature")
                .ignore(VerifyWebhookSharedSecret.class)
                .stepwiseEval(authAction);

        stepper.next();
        stepper.next();
        stepper.next();
        assertThat(stepper.next()).containsOnly(getJsonBody);
    }

    @Test
    void authenticate_webhook_authenticatesInstallation() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{ \"installation\": { \"id\": 1234 } }");

        Interpreter.Stepper<Unit> stepper = interpret()
                .on(GetUserAgent.class).returnA("GitHub-Hookshot/")
                .on(IsWebHookUserAgent.class).returnA(true)
                .on(GetRequestBody.class).returnA("somebody")
                .on(GetHeader.class).returnA("signature")
                .ignore(VerifyWebhookSharedSecret.class)
                .on(GetJsonRequestBody.class).returnA(json)
                .stepwiseEval(authAction);

        stepper.next();
        stepper.next();
        stepper.next();
        stepper.next();
        assertThat(stepper.next()).containsOnly(authenticateInstallation(new InstallationID("1234")));
    }



    void assertStopped(ThrowableAssert.ThrowingCallable t) {
        assertThatExceptionOfType(Interpreter.StoppedException.class).isThrownBy(t);
    }
}
