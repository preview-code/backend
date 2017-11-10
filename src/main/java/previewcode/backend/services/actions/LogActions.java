package previewcode.backend.services.actions;
import io.atlassian.fugue.Unit;
import previewcode.backend.services.actiondsl.ActionDSL.*;

public abstract class LogActions extends Action<Unit> {
    public final String message;

    protected LogActions(String message) {
        this.message = message;
    }

    public static class LogInfo extends LogActions {
        public LogInfo(String message) {
            super(message);
        }
    }
}
