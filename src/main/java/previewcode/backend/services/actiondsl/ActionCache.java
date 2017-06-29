package previewcode.backend.services.actiondsl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import previewcode.backend.services.actiondsl.ActionDSL.Action;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A cache for Action<?> results.
 * Use {@link ActionCache.Builder} to create and configure a cache
 */
public class ActionCache {
    private final Cache<Action<?>, Object> cache;
    private final Map<Class<?>, Long> expireTimes;

    protected ActionCache(Cache<Action<?>, Object> cache, Map<Class<?>, Long> expireTimes) {
        this.cache = cache;
        this.expireTimes = expireTimes;
    }

    @SuppressWarnings("unchecked")
    public <A, X extends Action<A>> A get(X action, Function<? super X, A> runAction) {
        return (A) cache.get(action, __ -> runAction.apply(action));
    }

    protected Cache<Action<?>, Object> getCache() {
        return cache;
    }

    public Map<Class<?>, Long> getExpireTimes() {
        return expireTimes;
    }


    public static class Builder {
        private final Map<Class<?>, Long> expireTimes;

        public static class ExpireStep<A extends Action<X>, X> {
            private final Class<A> actionClass;
            private final Builder currentBuilder;

            private ExpireStep(Class<A> actionClass, Builder currentBuilder) {
                this.actionClass = actionClass;
                this.currentBuilder = currentBuilder;
            }

            public Builder afterWrite(long time, TimeUnit unit) {
                Map<Class<?>, Long> expireTimes = new HashMap<>();
                expireTimes.putAll(currentBuilder.expireTimes);
                expireTimes.put(actionClass, unit.toNanos(time));
                return new Builder(expireTimes);
            }
        }

        public static class FinalStep {
            private final Builder currentBuilder;
            private final long maxEntries;
            private final Caffeine<Object, Object> baseConfig;


            public FinalStep(Builder currentBuilder, long maxEntries, Caffeine<Object, Object> baseConfig) {
                this.currentBuilder = currentBuilder;
                this.maxEntries = maxEntries;
                this.baseConfig = baseConfig;
            }

            public FinalStep fromCaffeineConfig(Caffeine<Object, Object> newConfig) {
                return new FinalStep(currentBuilder, maxEntries, newConfig);
            }

            public ActionCache build() {
                Caffeine<Object, Object> caffeine = baseConfig != null ? baseConfig : Caffeine.newBuilder();

                if (this.maxEntries < Long.MAX_VALUE) {
                    caffeine = caffeine.maximumSize(this.maxEntries);
                }
                if (currentBuilder.expireTimes.size() > 0) {
                    caffeine = caffeine.expireAfter(new Expiry<Object, Object>() {
                        @Override
                        public long expireAfterCreate(@Nonnull Object action, @Nonnull Object result, long currentTime) {
                            return expireDuration(action);
                        }

                        @Override
                        public long expireAfterUpdate(@Nonnull Object action, @Nonnull Object result, long currentTime, long currentDuration) {
                            long duration = expireDuration(action);
                            return duration > 0 ? duration : currentDuration;
                        }

                        private long expireDuration(@Nonnull Object action) {
                            Long aLong = currentBuilder.expireTimes.get(action.getClass());
                            if (aLong != null) {
                                return aLong;
                            } else {
                                return 0;
                            }
                        }

                        @Override
                        public long expireAfterRead(@Nonnull Object action, @Nonnull Object result, long currentTime, long currentDuration) {
                            return Long.MAX_VALUE;
                        }

                    });
                }
                return new ActionCache(caffeine.build(), currentBuilder.expireTimes);
            }
        }

        public Builder() {
            this(new HashMap<>());
        }

        public Builder(Builder fromBuilder) {
            this(fromBuilder.expireTimes);
        }

        private Builder(Map<Class<?>, Long> expireTimes) {
            this.expireTimes = expireTimes;
        }

        public FinalStep maximumEntries(long maxSize) {
            return new Builder.FinalStep(this, maxSize, null);
        }

        public <A extends Action<X>, X> ExpireStep expire(Class<A> actionClass) {
            return new ExpireStep<>(actionClass, this);
        }
    }
}
