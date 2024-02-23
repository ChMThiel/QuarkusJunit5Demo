package org.acme;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import static net.fortuna.ical4j.data.FoldingWriter.MAX_FOLD_LENGTH;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

@Provider
@Consumes("text/calendar")
@Produces("text/calendar")
public class CalendarMarshaler implements MessageBodyReader<Calendar>, MessageBodyWriter<Calendar> {

    @Override
    public boolean isReadable(Class<?> aType, Type aGenericType, Annotation[] aNnotations, MediaType aMediaType) {
        return Calendar.class.isAssignableFrom(aType);
    }

    @Override
    public Calendar readFrom(Class<Calendar> aType, Type aGenericType, Annotation[] aNnotations, MediaType aMediaType, MultivaluedMap<String, String> aHttpHeaders, InputStream aEntityStream) throws IOException, WebApplicationException {
        try {
            return new CalendarBuilder().build(aEntityStream);
        } catch (IOException | ParserException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> aType, Type aGenericType, Annotation[] aNnotations, MediaType aMediaType) {
        return Calendar.class.isAssignableFrom(aType);
    }

    @Override
    public void writeTo(Calendar aCalendar, Class<?> aType, Type aGenericType, Annotation[] aNnotations, MediaType aMediaType, MultivaluedMap<String, Object> aHttpHeaders, OutputStream aEntityStream) throws IOException, WebApplicationException {
        new CalendarOutputter(false, MAX_FOLD_LENGTH).output(aCalendar, aEntityStream);
    }
}
