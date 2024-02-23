package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;

@ApplicationScoped
@Path("/hello")
@Transactional
public class GreetingResource {

    @Inject
    EntityManager em;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @GET
    @Path("/ical")
    @Produces("text/calendar") //see https://www.ietf.org/rfc/rfc2445.txt
    public Calendar getICal() throws IOException, ParserException {
        return getCalendar();
    }

    @GET
    @Path("/pojo/{name}")
    @Transactional
    public Pojo getPojo(@PathParam("name") String name) throws IOException, ParserException {
        Pojo found = em.find(Pojo.class, name);
        return found;
    }

    @POST
    @Path("/pojo")
    @Transactional
    public void createPojo(Pojo aPojo) throws IOException, ParserException {
        em.persist(aPojo);
        System.out.println("org.acme.GreetingResource.createPojo()" + aPojo);
    }

    @POST
    @Path("/ical")
    @Consumes({"text/calendar", MediaType.APPLICATION_JSON})
    public void createICal(Calendar aCalendar) throws IOException, ParserException, ParseException {
        print(aCalendar);
    }

    public void print(Calendar aCalendar) throws ParseException {
        OffsetDateTime from = OffsetDateTime.of(2024, 2, 1, 7, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 3, 1, 7, 0, 0, 0, ZoneOffset.UTC);
        Period period = new Period(from, to);
        for (CalendarComponent component : aCalendar.getComponents("VEVENT")) {
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

    public Calendar getCalendar() throws ParserException, IOException {
        return new CalendarBuilder().build(GreetingResource.class.getResourceAsStream("/dayShift.ics"));
    }
}
