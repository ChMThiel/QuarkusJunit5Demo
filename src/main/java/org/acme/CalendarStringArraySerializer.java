package org.acme;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import net.fortuna.ical4j.data.CalendarOutputter;
import static net.fortuna.ical4j.data.FoldingWriter.MAX_FOLD_LENGTH;
import net.fortuna.ical4j.model.Calendar;

public class CalendarStringArraySerializer extends StdSerializer<Calendar> {

    private static final long serialVersionUID = 2275930663376896204L;

    public CalendarStringArraySerializer() {
        super(Calendar.class);
    }

    @Override
    public void serialize(Calendar aCalendar, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider)
            throws IOException {
        try (Writer out = new StringWriter()) {
            //never fold
            new CalendarOutputter(false, MAX_FOLD_LENGTH).output(aCalendar, out);
            String iCal = out.toString();
            String[] stringArray = iCal.lines()
                    .map(this::escape)
                    .toArray(String[]::new);
            aJsonGenerator.writeArray(stringArray, 0, stringArray.length);
        }
    }

    final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    String escape(String s) {
        return  s.replace("\"", "\\\"");
//        try {
//            return OBJECT_MAPPER.writeValueAsString(s);
//        } catch (JsonProcessingException jsonProcessingException) {
//            throw new RuntimeException(jsonProcessingException);
//        }
    }
}
