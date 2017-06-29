package previewcode.backend.DTO.serializers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import io.vavr.collection.List;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class WrappedTypeToJson<This, Wrapped> implements Converter<This, Wrapped> {

    @SuppressWarnings("unchecked")
    @Override
    public Wrapped convert(This value) {
        try {
            List<Field> fields = List.of(value.getClass().getDeclaredFields());
            List<Field> annotatedFields = fields.filter(f -> f.isAnnotationPresent(Wraps.class));
            Field field;
            if (annotatedFields.size() > 1) {
                throw new RuntimeException("Cannot convert wrapped type to json with multiple @Wrapped annotations");
            } else if (annotatedFields.size() == 0) {
                if (fields.size() != 1) {
                    throw new RuntimeException("Only wrapped types with one field or one @Wraps annotated field can be converted");
                } else {
                    field = fields.get();
                }
            } else {
                field = annotatedFields.get();
            }
            return (Wrapped) field.get(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field for " + evilGetGenericType(0) + ". Make sure the field is public");
        }
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(new TypeReference<This>(){});
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(new TypeReference<Wrapped>(){});
    }

    private String evilGetGenericType(int i) {
        return ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[i].getTypeName();
    }
}
