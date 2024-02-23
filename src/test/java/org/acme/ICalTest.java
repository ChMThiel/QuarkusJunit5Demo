package org.acme;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import static java.util.function.Predicate.not;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.CalendarComponent;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

/**
 *
 * @since 19.02.2024
 */
class ICalTest {

    @Test
    void shouldCalculateProductiveTime() throws Exception {
        CalendarBuilder calendarBuilder = new CalendarBuilder();
        Calendar dayShiftCalendar = calendarBuilder.build(ICalTest.class.getResourceAsStream("/dayShift.ics"));
        //one week
        OffsetDateTime from = OffsetDateTime.of(2024, 2, 19, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 2, 25, 0, 0, 0, 0, ZoneOffset.UTC);
        //when
        Duration productiveTime = getProductiveTime(dayShiftCalendar, from, to, calendarBuilder.getRegistry());
        //then
        assertThat(productiveTime, is(Duration.of(40, ChronoUnit.HOURS)));
    }

    @Test
    void shouldCalculateProductiveTimeIntheMiddleOFAShift() throws Exception {
        CalendarBuilder calendarBuilder = new CalendarBuilder();
        Calendar dayShiftCalendar = calendarBuilder.build(ICalTest.class.getResourceAsStream("/dayShift.ics"));
        OffsetDateTime from = OffsetDateTime.of(2024, 2, 19, 15, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 2, 21, 17, 0, 0, 0, ZoneOffset.UTC);
        //when
        Duration productiveTime = getProductiveTime(dayShiftCalendar, from, to, calendarBuilder.getRegistry());
        //then
        assertThat(productiveTime, is(Duration.of(18, ChronoUnit.HOURS)));
    }

    @Test
    void shouldCalculateProductiveTimeWithSummerTimeSwitch() throws Exception {
        CalendarBuilder calendarBuilder = new CalendarBuilder();
        Calendar dayShiftCalendar = calendarBuilder.build(ICalTest.class.getResourceAsStream("/nightShift.ics"));
        //Summer-Time switch at 31.3. 02:00
        OffsetDateTime from = OffsetDateTime.of(2024, 3, 30, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 4, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        //when
        Duration productiveTime = getProductiveTime(dayShiftCalendar, from, to, calendarBuilder.getRegistry());
        //then
        assertThat(productiveTime, is(Duration.of(15, ChronoUnit.HOURS)));
    }

    //https://datatracker.ietf.org/doc/html/rfc5545#section-3.8.2.7
    //TRANSP: OPAQUE      = Zeit wird verbraucht/geblockt       -> Productive Zeit
    //TRANSP: TRANSPARENT = Zeit wird nicht verbraucht/geblockt -> Unproductive Zeit
    //Zusammen mit
    //https://datatracker.ietf.org/doc/html/rfc5545#section-3.2.9
    //FREEBUSY;FBTYPE=BUSY:19980415T133000Z/19980415T170000Z = Zeit is geblockt für scheduling
    Duration getProductiveTime(Calendar aCalendar, OffsetDateTime aFromDate, OffsetDateTime aToDate, TimeZoneRegistry aTimeZoneRegistry) {
        //TODO check for SummerTime-Change
        ZoneId zoneId = aCalendar
                .getComponent(Component.VTIMEZONE)
                .map(c -> c.getRequiredProperty(Property.TZID).getValue())
                .map(aTimeZoneRegistry::getTimeZone)
                //                .map(TimeZone::getTimeZone)
                .map(t -> t.toZoneId())
                .orElse(ZoneId.systemDefault());
        System.out.println("Zone " + zoneId);
        Period<OffsetDateTime> period = new Period(aFromDate, aToDate);
        Interval interval = period.toInterval(zoneId);
        System.out.println("Interval " + interval);
        return aCalendar
                .getComponents(Component.VEVENT).stream()
                .filter(c -> c.getProperty("TRANSP").map(Property::getValue).map("OPAQUE"::equals).orElse(false))
                .map(c -> c.calculateRecurrenceSet(period))
                .flatMap(Set<Period<OffsetDateTime>>::stream)
                .map(p -> p.toInterval())
                .map(i -> i.intersection(interval))
                .map(i -> {
                    ZonedDateTime start = LocalDateTime.ofInstant(i.getStart(), ZoneOffset.UTC).atZone(zoneId);
                    ZonedDateTime end = LocalDateTime.ofInstant(i.getEnd(), ZoneOffset.UTC).atZone(zoneId);
                    Duration duration = Duration.between(start, end);
                    System.out.println("Duration between " + start + " and " + end + " is " + duration);
                    return duration;
                })
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
    }

    @Test
    void shouldGetShiftAtCertainPointInTime() throws Exception {
        Calendar calendar = new CalendarBuilder().build(ICalTest.class.getResourceAsStream("/EarlyAndLateShifts.ics"));
        assertThat(getShiftId(calendar, OffsetDateTime.of(2024, 2, 22, 9, 0, 0, 0, ZoneOffset.UTC)), is("123"));
        assertThat(getShiftId(calendar, OffsetDateTime.of(2024, 2, 22, 16, 0, 0, 0, ZoneOffset.UTC)), is("456"));
        assertThat(getShiftId(calendar, OffsetDateTime.of(2024, 2, 22, 3, 0, 0, 0, ZoneOffset.UTC)), is(nullValue()));
    }
    
    Object getShiftId(Calendar aCalendar, OffsetDateTime aDate) {
        Period period = new Period(aDate, aDate);
        return aCalendar.getComponents(Component.VEVENT).stream()
                .filter(e -> e.getProperty("X-SHIFT_ID").isPresent())
                .map(c -> c.calculateRecurrenceSet(period))
                .filter(not(Set::isEmpty))
                .flatMap(Set<Period>::stream)
                .map(Period::getComponent)
                .map(e -> e.getProperty("X-SHIFT_ID").get())
                .findFirst()
                .map(Property::getValue)
                .orElse(null); //TODO UUID conversion
                
    }

    @Test
    void shouldMerge() throws Exception {
        Calendar dayShiftCalendar = new CalendarBuilder().build(ICalTest.class.getResourceAsStream("/dayShift.ics"));
        Calendar user1Calendar = new CalendarBuilder().build(ICalTest.class.getResourceAsStream("/user1.ics"));
//        calendar.withComponent(component)

        // Create the date range which is desired.
        OffsetDateTime from = OffsetDateTime.of(2024, 2, 22, 15, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 2, 22, 15, 0, 0, 0, ZoneOffset.UTC);
        Period period = new Period(from, to);
//
//        // For each VEVENT in the ICS
//        System.out.println("\nDay shift alone Trigger in Period: " + period + "\n");
//        for (CalendarComponent component : dayShiftCalendar.getComponents("VEVENT")) {
//
//            System.out.println();
//            System.out.print(component.getProperties("RRULE"));
//            System.out.print(component.getProperties("SUMMARY"));
//            System.out.print(component.getProperties("DESCRIPTION"));
//            System.out.print(component.getProperties("TRANSP"));
//            System.out.println("Triggers at:");
//            component.calculateRecurrenceSet(period)
//                    .forEach(System.out::println);
//        }
//
//        System.out.println("\nUser 1 alone Trigger in Period: " + period + "\n");
//        for (CalendarComponent component : user1Calendar.getComponents("VEVENT")) {
//
//            System.out.println();
//            System.out.print(component.getProperties("RRULE"));
//            System.out.print(component.getProperties("SUMMARY"));
//            System.out.print(component.getProperties("DESCRIPTION"));
//            System.out.print(component.getProperties("TRANSP"));
//            System.out.println("Triggers at:");
//            component.calculateRecurrenceSet(period)
//                    .forEach(System.out::println);
//        }

        //merge user-cal into shift-cal
        Calendar mergedCalender = dayShiftCalendar.merge(user1Calendar);
        System.out.println("\nShift Calendar with merged User 1 Trigger in Period: " + period + "\n");
        for (CalendarComponent component : mergedCalender.getComponents("VEVENT")) {

            System.out.println();
            System.out.print(component.getProperties("RRULE"));
            System.out.print(component.getProperties("SUMMARY"));
            System.out.print(component.getProperties("DESCRIPTION"));
            System.out.print(component.getProperties("TRANSP"));
            System.out.println("Triggers at:");
            component.calculateRecurrenceSet(period)
                    .forEach(System.out::println);
        }

    }

}
