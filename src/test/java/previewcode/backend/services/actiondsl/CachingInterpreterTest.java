package previewcode.backend.services.actiondsl;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.testing.FakeTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.services.actiondsl.ActionDSL.*;


class ActionCacheTest {

    class O extends Action<Integer> {
        @Override
        public boolean equals(Object obj) {
            return obj.getClass().equals(this.getClass());
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }
    }
    class X extends O {}
    class Y extends O {}
    class Z extends O {}


    class CacheInterpreter extends CachingInterpreter {

        public CacheInterpreter(ActionCache cache) {
            super(cache);
            on(X.class).returnA(1);
            on(Y.class).returnA(2);
            on(Z.class).returnA(3);
        }
    }

    FakeTicker ticker;

    @BeforeEach
    void setup() {
        ticker = new FakeTicker();
    }

    private CacheInterpreter createDefaultInterpreter() {
        return new CacheInterpreter(new ActionCache.Builder()
                .expire(X.class).afterWrite(1, TimeUnit.SECONDS)
                .maximumEntries(10)
                .fromCaffeineConfig(Caffeine.newBuilder().ticker(ticker::read))
                .build()
        );
    }

    private CacheInterpreter createAdvancedInterpreter(){

        CacheInterpreter interpreter = new CacheInterpreter(new ActionCache.Builder()
                        .expire(X.class).afterWrite(1, TimeUnit.MINUTES)
                        .expire(Y.class).afterWrite(1, TimeUnit.HOURS)
                        .maximumEntries(10)
                        .fromCaffeineConfig(Caffeine.newBuilder().ticker(ticker::read))
                        .build()
        );

        // X will be cached for 1 minute
        interpreter.unsafeEvaluate(new X());
        // Y will be cached for 1 hour
        interpreter.unsafeEvaluate(new Y());
        // Z will NOT be cached
        interpreter.unsafeEvaluate(new Z());

        // When expired, evaluating any action will throw
        interpreter.on(X.class).stop();
        interpreter.on(Y.class).stop();
        interpreter.on(Z.class).stop();
        return interpreter;
    }


    @Test
    void cachesResults(){
        CacheInterpreter interpreter = createDefaultInterpreter();
        interpreter.unsafeEvaluate(new X());
        assertThat(interpreter.getCache().getIfPresent(new X())).isEqualTo(1);
    }

    @Test
    void readsFromCache(){
        CacheInterpreter interpreter = createDefaultInterpreter();

        // Caches the first result: 1
        interpreter.unsafeEvaluate(new X());
        interpreter.on(X.class).stop();
        // The cache should now return 1 without calling the interpreter
        assertThat(interpreter.unsafeEvaluate(new X())).isEqualTo(1);
        assertThat(interpreter.getCache().getIfPresent(new X())).isEqualTo(1);
    }

    @Test
    void doesNotCache_unconfiguredItems(){
        CacheInterpreter interpreter = createDefaultInterpreter();

        interpreter.unsafeEvaluate(new Y());

        // Throw on next call to the interpreter
        interpreter.on(Y.class).stop();

        // Cache miss causes interpreter to be run
        assertThat(interpreter.getCache().getIfPresent(new Y())).isNull();
        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(new Y()));
    }

    @Test
    void doesNotEvict_beforeSpecificTime_beforeDefaultTime(){
        CacheInterpreter interpreter = createAdvancedInterpreter();
        ticker.advance(59, TimeUnit.SECONDS);
        assertThat(interpreter.getCache().getIfPresent(new X())).isEqualTo(1);
        assertThat(interpreter.unsafeEvaluate(new X())).isEqualTo(1);
    }

    @Test
    void evicts_afterSpecificTime_beforeDefaultTime(){
        CacheInterpreter interpreter = createAdvancedInterpreter();
        ticker.advance(61, TimeUnit.SECONDS);
        assertThat(interpreter.getCache().getIfPresent(new X())).isNull();
        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(new X()));
    }

    @Test
    void doesNotEvict_beforeSpecificTime_afterDefaultTime(){
        CacheInterpreter interpreter = createAdvancedInterpreter();
        ticker.advance(59, TimeUnit.MINUTES);
        assertThat(interpreter.getCache().getIfPresent(new Y())).isEqualTo(2);
        assertThat(interpreter.unsafeEvaluate(new Y())).isEqualTo(2);
    }

    @Test
    void evicts_afterSpecificTime_afterDefaultTime(){
        CacheInterpreter interpreter = createAdvancedInterpreter();
        ticker.advance(61, TimeUnit.MINUTES);
        assertThat(interpreter.getCache().getIfPresent(new Y())).isNull();
        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(new Y()));
    }

}