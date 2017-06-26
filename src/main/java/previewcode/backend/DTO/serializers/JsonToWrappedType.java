package previewcode.backend.DTO.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

public class JsonToWrappedType<Wrapped, This> implements Converter<Wrapped, This> {
    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(new TypeReference<Wrapped>(){});
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(new TypeReference<This>(){});
    }

    @SuppressWarnings("unchecked")
    @Override
    public This convert(Wrapped value) {
        String wrappedName = evilGetGenericType(0);
        String destinationName = evilGetGenericType(1);
        try {
            Class<Wrapped> wrappedClass = (Class<Wrapped>) Class.forName(wrappedName);
            Class<This> aClass = (Class<This>) Class.forName(destinationName);
            return aClass.getConstructor(wrappedClass).newInstance(value);

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Cannot convert '" + value.toString() + "' from: " + wrappedName + " to: " + destinationName, e);
        }
    }

    private String evilGetGenericType(int i) {
        return ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[i].getTypeName();
    }
}
