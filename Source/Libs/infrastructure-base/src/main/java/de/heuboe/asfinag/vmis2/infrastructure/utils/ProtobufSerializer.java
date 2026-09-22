package de.heuboe.asfinag.vmis2.infrastructure.utils;

import com.google.protobuf.util.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;

import java.io.IOException;

/**
 * serializer of proto to json.
 */
public class ProtobufSerializer extends JsonSerializer<Message> {


    private final JsonFormat.Printer protobufJsonPrinter = JsonFormat.printer();

    @Override
    public void serialize(Message anyProtobufMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeRawValue(protobufJsonPrinter.print(anyProtobufMessage));
    }

}
