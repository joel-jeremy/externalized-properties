package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.DateTimeFormat;

import java.lang.reflect.Type;
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
import java.time.format.DateTimeFormatter;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion to Java's built in date/time types:
 * <ul>
 * <li>{@link LocalDateTime}</li>
 * <li>{@link LocalDate}</li>
 * <li>{@link LocalTime}</li>
 * <li>{@link OffsetDateTime}</li>
 * <li>{@link OffsetTime}</li>
 * <li>{@link ZonedDateTime}</li>
 * <li>{@link Instant}</li>
 * <li>{@link DayOfWeek}</li>
 * <li>{@link Month}</li>
 * <li>{@link MonthDay}</li>
 * <li>{@link Year}</li>
 * <li>{@link YearMonth}</li>
 * </ul>
 * 
 * @apiNote The {@link DateTimeFormat} annotation may be used to specify a date/time 
 * format/pattern to use when converting to date/time types.
 */
public class DateTimeConverter implements Converter<Object> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return LocalDateTime.class.equals(targetType) ||
            LocalDate.class.equals(targetType) ||
            LocalTime.class.equals(targetType) ||
            OffsetDateTime.class.equals(targetType) ||
            OffsetTime.class.equals(targetType) ||
            ZonedDateTime.class.equals(targetType) || 
            Instant.class.equals(targetType) ||
            DayOfWeek.class.equals(targetType) ||
            Month.class.equals(targetType) ||
            MonthDay.class.equals(targetType) ||
            Year.class.equals(targetType) ||
            YearMonth.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(ConversionContext context) {
        requireNonNull(context, "context");

        Type targetType = context.targetType();
        
        if (LocalDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalDateTime(context)
            );
        } 
        else if (LocalDate.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalDate(context)
            );
        } 
        else if (LocalTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalTime(context)
            );
        } 
        else if (OffsetDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseOffsetDateTime(context)
            );
        } 
        else if (OffsetTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseOffsetTime(context)
            );
        } 
        else if (ZonedDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseZonedDateTime(context)
            );
        } 
        else if (Instant.class.equals(targetType)) {
            return ConversionResult.of(
                Instant.parse(context.value())
            );
        } 
        else if (DayOfWeek.class.equals(targetType)) {
            return ConversionResult.of(
                DayOfWeek.valueOf(context.value())
            );
        } 
        else if (Month.class.equals(targetType)) {
            return ConversionResult.of(
                Month.valueOf(context.value())
            );
        } 
        else if (MonthDay.class.equals(targetType)) {
            return ConversionResult.of(
                parseMonthDay(context)
            );
        } 
        else if (Year.class.equals(targetType)) {
            return ConversionResult.of(
                parseYear(context)
            );
        } 
        else if (YearMonth.class.equals(targetType)) {
            return ConversionResult.of(
                parseYearMonth(context)
            );
        }
        
        return ConversionResult.skip();
    }

    private YearMonth parseYearMonth(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            YearMonth.parse(context.value(), dateTimeFormatter) :
            YearMonth.parse(context.value());
    }

    private Year parseYear(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            Year.parse(context.value(), dateTimeFormatter) :
            Year.parse(context.value());
    }

    private MonthDay parseMonthDay(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            MonthDay.parse(context.value(), dateTimeFormatter) :
            MonthDay.parse(context.value());
    }

    private ZonedDateTime parseZonedDateTime(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            ZonedDateTime.parse(context.value(), dateTimeFormatter) :
            ZonedDateTime.parse(context.value());
    }

    private OffsetTime parseOffsetTime(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            OffsetTime.parse(context.value(), dateTimeFormatter) :
            OffsetTime.parse(context.value());
    }

    private OffsetDateTime parseOffsetDateTime(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            OffsetDateTime.parse(context.value(), dateTimeFormatter) :
            OffsetDateTime.parse(context.value());
    }

    private LocalTime parseLocalTime(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            LocalTime.parse(context.value(), dateTimeFormatter) :
            LocalTime.parse(context.value());
    }

    private LocalDate parseLocalDate(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            LocalDate.parse(context.value(), dateTimeFormatter) :
            LocalDate.parse(context.value());
    }

    private LocalDateTime parseLocalDateTime(ConversionContext context) {
        DateTimeFormatter dateTimeFormatter = determineDateTimeFormatterOrNull(context);
        return dateTimeFormatter != null ?
            LocalDateTime.parse(context.value(), dateTimeFormatter) :
            LocalDateTime.parse(context.value());
    }

    private DateTimeFormatter determineDateTimeFormatterOrNull(ConversionContext context) {
        DateTimeFormat dateTimeFormat = getDateTimeFormatOrNull(context);
        if (dateTimeFormat == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(dateTimeFormat.value());
    }
    
    private DateTimeFormat getDateTimeFormatOrNull(ConversionContext context) {
        return context.proxyMethod()
            .findAnnotation(DateTimeFormat.class)
            .orElse(null);
    }
}
