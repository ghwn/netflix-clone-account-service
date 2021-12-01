package me.ghwn.netflix.accountservice.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.io.IOException;

@JsonComponent
public class BindExceptionJsonSerializer extends JsonSerializer<BindException> {

    @Override
    public void serialize(BindException e, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName("errors");
        gen.writeStartArray();
        for (FieldError fieldError : e.getFieldErrors()) {
            gen.writeStartObject();
            gen.writeStringField("message", fieldError.getDefaultMessage());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

}
