package pro.trevor.cobblemonuncraftables.util;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.function.Function;

public class JsonCodec<T> {

    private final Class<T> type;
    private final Function<T, JsonObject> serializer;
    private final Function<JsonObject, T> deserializer;

    public JsonCodec(Class<T> type, Function<T, JsonObject> serializer, Function<JsonObject, T> deserializer) {
        this.type = type;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public Class<T> getType() {
        return type;
    }

    public JsonObject encode(T object) {
        return serializer.apply(object);
    }

    public T decode(JsonObject object) {
        return deserializer.apply(object);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JsonCodec<?> jsonCodec)) return false;
        return Objects.equals(type, jsonCodec.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
