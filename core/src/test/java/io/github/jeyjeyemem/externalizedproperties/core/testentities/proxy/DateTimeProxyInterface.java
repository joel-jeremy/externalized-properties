package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

public interface DateTimeProxyInterface {
    @ExternalizedProperty("localdatetime.property")
    LocalDateTime localDateTime();

    @ExternalizedProperty("localdatetime.customformat.property")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a")
    LocalDateTime localDateTimeCustomFormat();

    @ExternalizedProperty("localdate.property")
    LocalDate localDate();

    @ExternalizedProperty("localdate.customformat.property")
    @DateTimeFormat("MMM.dd.yyyy")
    LocalDate localDateCustomFormat();

    @ExternalizedProperty("localtime.property")
    LocalTime localTime();

    @ExternalizedProperty("localtime.customformat.property")
    @DateTimeFormat("h:mm:ss a")
    LocalTime localTimeCustomFormat();

    @ExternalizedProperty("offsetdatetime.property")
    OffsetDateTime offsetDateTime();

    @ExternalizedProperty("offsetdatetime.customformat.property")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a (ZZZZ)")
    OffsetDateTime offsetDateTimeCustomFormat();

    @ExternalizedProperty("offsettime.property")
    OffsetTime offsetTime();

    @ExternalizedProperty("offsettime.customformat.property")
    @DateTimeFormat("h:mm:ss a (ZZZZ)")
    OffsetTime offsetTimeCustomFormat();

    @ExternalizedProperty("zoneddatetime.property")
    ZonedDateTime zonedDateTime();

    @ExternalizedProperty("zoneddatetime.customformat.property")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a (VV) (ZZZZ)")
    ZonedDateTime zonedDateTimeCustomFormat();

    @ExternalizedProperty("instant.property")
    Instant instant();

    @ExternalizedProperty("dayofweek.property")
    DayOfWeek dayOfWeek();

    @ExternalizedProperty("month.property")
    Month month();

    @ExternalizedProperty("monthday.property")
    MonthDay monthDay();

    @ExternalizedProperty("monthday.customformat.property")
    @DateTimeFormat("MMMM.dd")
    MonthDay monthDayCustomFormat();

    @ExternalizedProperty("year.property")
    Year year();

    @ExternalizedProperty("year.property")
    @DateTimeFormat("yy")
    Year yearCustomFormat();

    @ExternalizedProperty("yearmonth.customformat.property")
    YearMonth yearMonth();

    @ExternalizedProperty("yearmonth.property")
    @DateTimeFormat("yyyy MMMM")
    YearMonth yearMonthCustomFormat();
}
