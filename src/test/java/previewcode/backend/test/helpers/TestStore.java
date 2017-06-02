package previewcode.backend.test.helpers;

import com.google.inject.servlet.ServletModule;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

public class TestStore<T> extends AnnotatedClassInstantiator<ServletModule> {

    protected T getFromStore(ExtensionContext context) {
        T t = (T) getStore(context).get(getStoreKey(context), Object.class);
        if (t != null) {
            return t;
        } else {
            return context.getParent().map(this::getFromStore).orElse(null);
        }
    }

    protected void putObjectToStore(ExtensionContext context, T obj) {
        getStore(context).put(getStoreKey(context), obj);
    }

    private String getStoreKey(ExtensionContext context) {
        return context.getUniqueId();
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context));
    }
}
