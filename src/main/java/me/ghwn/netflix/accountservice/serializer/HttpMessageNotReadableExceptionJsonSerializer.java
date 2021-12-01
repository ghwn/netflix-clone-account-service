package me.ghwn.netflix.accountservice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;

@JsonComponent
public class HttpMessageNotReadableExceptionJsonSerializer extends JsonSerializer<HttpMessageNotReadableException> {

    @Override
    public void serialize(HttpMessageNotReadableException e, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName("errors");
        gen.writeStartArray();
        gen.writeStartObject();
        gen.writeStringField("message", "Unprocessable value contained");
        gen.writeEndObject();
        gen.writeEndArray();
    }

}
