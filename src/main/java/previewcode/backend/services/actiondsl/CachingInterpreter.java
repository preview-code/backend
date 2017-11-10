package previewcode.backend.services.actiondsl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.atlassian.fugue.Try;
import io.vavr.collection.List;
import static previewcode.backend.services.actiondsl.ActionDSL.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Caches results.
 * Extend this class to make an interpreter that caches results.
 * <br>
 * <br>
 * Use {@link ActionCache.Builder} to create and configure a cache.
 * Keep in mind that sharing one cache for multiple interpreter is preferred
 * for performance reasons. Therefore, you should first configure the cache for
 * each interpreter and then create each interpreter using the configured cache.
 * <br>
 * <br>
 * See {@link previewcode.backend.services.interpreters.GitHubAuthInterpreter}
 * an {@link previewcode.backend.MainModule} for an example.
 */
public class CachingInterpreter extends Interpreter {

    final ActionCache cache;

    public CachingInterpreter(ActionCache cache) {
        super();
        this.cache = cache;
    }

    Cache<Action<?>, Object> getCache() {
        return cache.getCache();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected <A> Try<A> runLeaf(Action<A> action) {
        if (!cache.getExpireTimes().containsKey(action.getClass())) {
            return super.runLeaf(action);
        }
        A cachedResult = (A) cache.getCache().getIfPresent(action);
        if (cachedResult == null) {
            Try<A> result = super.runLeaf(action);
            if (result.isSuccess()) {
                cache.getCache().put(action, result.toOption().get());
            }
            return result;
        }
        return Try.successful(cachedResult);
    }
}
