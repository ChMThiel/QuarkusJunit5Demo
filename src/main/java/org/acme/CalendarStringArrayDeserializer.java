package org.acme;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

public class CalendarStringArrayDeserializer extends StdDeserializer<Calendar> {

    private static final long serialVersionUID = -3939963035149126231L;

    protected CalendarStringArrayDeserializer() {
        super(Calendar.class);
    }

    @Override
    public Calendar deserialize(JsonParser aJsonParser, DeserializationContext aDeserializationContext) throws IOException {
        List<String> readValue = new ObjectMapper().readValue(aJsonParser, new TypeReference<List<String>>(){});
        String iCal = readValue.stream().collect(Collectors.joining(System.lineSeparator())); //use same line-feed as FoldingWriter("\r\n "
        try (StringReader reader = new StringReader(iCal)) {
            return new CalendarBuilder().build(reader);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
