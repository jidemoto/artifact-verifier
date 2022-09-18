package to.idemo.james.artifactverifier.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.util.Base64;

/**
 * Little base-64 decoder class courtesy of  <a href="https://stackoverflow.com/a/44373438">this StackOverflow post</a> with a couple adjustments
 */
public class Base64Deserializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    //Thread-safe after configuration
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Class<?> resultClass;

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String value = p.getValueAsString();
        Base64.Decoder decoder = Base64.getDecoder();

        try {
            byte[] decodedValue = decoder.decode(value);
            return objectMapper.readValue(decodedValue, this.resultClass);
        } catch (IllegalArgumentException | JsonParseException e) {
            String fieldName = p.getParsingContext().getCurrentName();
            Class<?> wrapperClass = p.getParsingContext().getCurrentValue().getClass();

            throw new InvalidFormatException(
                    p,
                    String.format("Value for '%s' is not a base64 encoded JSON", fieldName),
                    value,
                    wrapperClass
            );
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        this.resultClass = property.getType().getRawClass();
        return this;
    }
}
