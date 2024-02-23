package org.acme;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

/**
 * @since 20.02.2024
 */
@Converter(autoApply = true)
public class CalendarConverter implements AttributeConverter<Calendar, String> {

    @Override
    public String convertToDatabaseColumn(Calendar aCalendar) {
        try (Writer out = new StringWriter()) {
            new CalendarOutputter(false, Integer.MAX_VALUE).output(aCalendar, out);
            return out.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Calendar convertToEntityAttribute(String aDbData) {
        try (StringReader reader = new StringReader(aDbData)) {
            return new CalendarBuilder().build(reader);
        } catch (IOException | ParserException ex) {
            throw new RuntimeException(ex);
        }
    }

}
