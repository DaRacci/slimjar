package io.github.slimjar.resolver.reader.facade;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings({"java:S2326", "unused"}) // Used in reflection
public class TypeToken<T> {
    private final @NotNull Type rawType;

    public TypeToken() {
        this.rawType = getSuperclassTypeParameter(getClass());
    }

    @Contract(pure = true)
    public @NotNull Type rawType() {
        return rawType;
    }

    @Contract(pure = true)
    private static @NotNull Type getSuperclassTypeParameter(@NotNull final Class<?> subclass) {
        final var superclass = subclass.getGenericSuperclass();

        if (superclass instanceof Class) {
            throw new RuntimeException("Type parameter not found");
        }

        final var parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }
}
