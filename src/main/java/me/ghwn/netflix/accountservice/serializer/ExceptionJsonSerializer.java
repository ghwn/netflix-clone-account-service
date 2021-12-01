package me.ghwn.netflix.accountservice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class ExceptionJsonSerializer extends JsonSerializer<Exception> {

    @Override
    public void serialize(Exception e, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName("errors");
        gen.writeStartArray();
        gen.writeStartObject();
        gen.writeStringField("message", e.getMessage());
        gen.writeEndObject();
        gen.writeEndArray();
    }

}
