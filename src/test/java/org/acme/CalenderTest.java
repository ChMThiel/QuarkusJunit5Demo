package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;
import static org.aesh.readline.terminal.Key.s;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.mnode.ical4j.serializer.JCalDecoder;

/**
 * @since 20.02.2024
 */
@QuarkusTest
public class CalenderTest {

    @Inject
    EntityManager em;
    @Inject
    ObjectMapper mapper;

    @Test
    void shouldMarshal() throws Exception {
        //given
        Calendar dayShiftCalendar = new CalendarBuilder().build(ICalTest.class.getResourceAsStream("/dayShift.ics"));
        assertFalse(dayShiftCalendar.validate().hasErrors());

        String json = mapper.writeValueAsString(dayShiftCalendar);

        System.err.println(json);

        Calendar readValue = mapper.readValue(json, Calendar.class);
        System.out.println("read\n" + readValue);
    }

    //"1970-03-29T02:00:00"
//    @Test
//    void shouldParseI() throws Exception {
//        //given
//        String input = "1970-03-29T02:00:00";
//        TemporalAccessor parsed = DateTimeFormatter
//                .ofPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss[X]")
//                .parseBest(input, Instant::from, LocalDateTime::from);
//        ZoneOffset offset = ZoneOffset.from(parsed);
//        OffsetDateTime offsetDateTime = OffsetDateTime.from(parsed);
//        String formated = DateTimeFormatter
//                .ofPattern("yyyyMMdd'T'HHmmssX")
//                .withZone(ZoneOffset.UTC)
//                .format(offsetDateTime);
//        //when
//        String decode = JCalDecoder.DATE_TIME.decode(input);
//        System.out.println(decode);
//        //then
//    }

    @Test
    @Transactional
    void shouldWriteAndReadToDB() throws Exception {
        //given
        Calendar dayShiftCalendar = new CalendarBuilder().build(ICalTest.class.getResourceAsStream("/calendar.ics"));
        assertFalse(dayShiftCalendar.validate().hasErrors());
        print(dayShiftCalendar);
        Pojo pojo = new Pojo();
        pojo.name = "IT";
        pojo.calendar = dayShiftCalendar;
        //when
        em.persist(pojo);
        em.flush();
        em.clear();
        Pojo found = em.find(Pojo.class, "IT");
        //then
        assertFalse(found.calendar.validate().hasErrors());
        print(found.calendar);
    }

     public void print(Calendar aCalendar) throws ParseException {
        OffsetDateTime from = OffsetDateTime.of(2024, 2, 1, 7, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 3, 1, 7, 0, 0, 0, ZoneOffset.UTC);
        Period period = new Period(from, to);
        for (CalendarComponent component : aCalendar.getComponents("VEVENT")) {
            System.out.println();
            System.out.println(component.getProperties("RRULE"));
            System.out.println(component.getProperties("SUMMARY"));
            System.out.println(component.getProperties("DESCRIPTION"));
            System.out.println(component.getProperties("TRANSP"));
            System.out.println("Triggers at:");
            component.calculateRecurrenceSet(period)
                    .forEach(System.out::println);
        }
    }
}
