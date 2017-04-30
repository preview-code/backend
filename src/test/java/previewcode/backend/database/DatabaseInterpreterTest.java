package previewcode.backend.database;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.services.actiondsl.ActionDSL;
import previewcode.backend.test.helpers.DatabaseTests;

@DatabaseTests
public class DatabaseInterpreterTest {

    protected DatabaseInterpreter dbInterpreter;

    // Mock data to insert in the database
    protected static final String owner = "preview-code";
    protected static final String name = "backend";
    protected static final Integer number = 42;

    protected PullRequestIdentifier pullIdentifier = new PullRequestIdentifier(owner, name, number);

    @BeforeEach
    public void setup(DSLContext db) {
        this.dbInterpreter = new DatabaseInterpreter(db);
    }

    protected <T> T eval(ActionDSL.Action<T> action) throws Exception {
        return dbInterpreter.unsafeEvaluate(action);
    }
}
