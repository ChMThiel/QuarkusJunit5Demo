package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

@QuarkusTest
public class GreetingResourceTest {
    
    @Inject
    ObjectMapper objectMapper;
    
    @BeforeEach
    void initRestAssuredConfig() {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (cls, charset) -> {
                    new JacksonConfiguration().customize(objectMapper); //use same config as in production
                    return objectMapper;
                }));
    }

    @Test
    @Tag("otherTag")
    @DisplayName("testHelloEndpoint")
    public void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello RESTEasy"));
    }
    
    @Test
    void shouldCreatePojoWithCalendarAsStringArrayAndReadItAgain() throws Exception {
        String json = """
                      {
                          "name": "MY_TEST",
                          "calendar": [
                              "BEGIN:VCALENDAR",
                              "VERSION:2.0",
                              "PRODID:-// https://add-to-calendar-pro.com // button v2.5.10 //EN",
                              "CALSCALE:GREGORIAN",
                              "METHOD:PUBLISH",
                              "BEGIN:VTIMEZONE",
                              "TZID:Europe/Berlin",
                              "X-LIC-LOCATION:Europe/Berlin",
                              "LAST-MODIFIED:20240205T192834Z",
                              "BEGIN:DAYLIGHT",
                              "TZNAME:CEST",
                              "TZOFFSETFROM:+0100",
                              "TZOFFSETTO:+0200",
                              "DTSTART:19700329T020000",
                              "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU",
                              "END:DAYLIGHT",
                              "BEGIN:STANDARD",
                              "TZNAME:CET",
                              "TZOFFSETFROM:+0200",
                              "TZOFFSETTO:+0100",
                              "DTSTART:19701025T030000",
                              "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU",
                              "END:STANDARD",
                              "END:VTIMEZONE",
                              "BEGIN:VEVENT",
                              "UID:3c9de19f-1dec-4507-a41b-4213d270f359",
                              "DTSTAMP:20240220T065249Z",
                              "DTSTART;TZID=Europe/Berlin:20240209T140000",
                              "DTEND;TZID=Europe/Berlin:20240209T220000",
                              "SUMMARY:Day Shift",
                              "DESCRIPTION:Late shift",
                              "X-ALT-DESC;FMTTYPE=text/html:<!DOCTYPE HTML PUBLIC \\\"\\\"-//W3C//DTD HTML 3.2//",
                              " EN\\\"\\\"><HTML><BODY>Late shift</BODY></HTML>",
                              "RRULE:FREQ=WEEKLY;WKST=MO;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR",
                              "SEQUENCE:0",
                              "STATUS:CONFIRMED",
                              "CREATED:20240220T065213Z",
                              "LAST-MODIFIED:20240220T065213Z",
                              "END:VEVENT",
                              "END:VCALENDAR"
                          ]
                      }
                      """;
        //given
        given()
                //when
                .when()
                .contentType(ContentType.JSON)
                .body(json)
                .post("/hello/pojo")
                //then
                .then().log().all()
                .statusCode(204);
        //given
        Pojo output = given()
                //when
                .when()
                .contentType(ContentType.JSON)
                .get("/hello/pojo/MY_TEST")
                //then
                .then().log().all()
                .statusCode(200)
                .extract().body().as(Pojo.class);
        Pojo input = objectMapper.readValue(json, Pojo.class);
        assertThat(input.calendar, is(output.calendar));
    }

}
