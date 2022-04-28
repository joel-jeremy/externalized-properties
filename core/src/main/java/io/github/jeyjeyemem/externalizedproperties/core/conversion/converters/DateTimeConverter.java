package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.DateTimeFormat;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    /**
     * The {@link ConverterProvider} for {@link DateTimeConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link DateTimeConverter}.
     */
    public static ConverterProvider<DateTimeConverter> provider() {
        return (externalizedProperties, rootConverter) -> new DateTimeConverter();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return LocalDateTime.class.equals(targetType) ||
            OffsetDateTime.class.equals(targetType) ||
            ZonedDateTime.class.equals(targetType) || 
            LocalDate.class.equals(targetType) ||
            LocalTime.class.equals(targetType) ||
            OffsetTime.class.equals(targetType) ||
            Instant.class.equals(targetType) ||
            DayOfWeek.class.equals(targetType) ||
            Month.class.equals(targetType) ||
            MonthDay.class.equals(targetType) ||
            Year.class.equals(targetType) ||
            YearMonth.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        if (LocalDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalDateTime(proxyMethod, valueToConvert)
            );
        } 
        else if (OffsetDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseOffsetDateTime(proxyMethod, valueToConvert)
            );
        }
        else if (ZonedDateTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseZonedDateTime(proxyMethod, valueToConvert)
            );
        } 
        else if (LocalDate.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalDate(proxyMethod, valueToConvert)
            );
        } 
        else if (LocalTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseLocalTime(proxyMethod, valueToConvert)
            );
        }  
        else if (OffsetTime.class.equals(targetType)) {
            return ConversionResult.of(
                parseOffsetTime(proxyMethod, valueToConvert)
            );
        } 
        else if (Instant.class.equals(targetType)) {
            return ConversionResult.of(
                Instant.parse(valueToConvert)
            );
        } 
        else if (DayOfWeek.class.equals(targetType)) {
            return ConversionResult.of(
                DayOfWeek.valueOf(valueToConvert)
            );
        } 
        else if (Month.class.equals(targetType)) {
            return ConversionResult.of(
                Month.valueOf(valueToConvert)
            );
        } 
        else if (MonthDay.class.equals(targetType)) {
            return ConversionResult.of(
                parseMonthDay(proxyMethod, valueToConvert)
            );
        } 
        else if (Year.class.equals(targetType)) {
            return ConversionResult.of(
                parseYear(proxyMethod, valueToConvert)
            );
        } 
        else if (YearMonth.class.equals(targetType)) {
            return ConversionResult.of(
                parseYearMonth(proxyMethod, valueToConvert)
            );
        }
        
        return ConversionResult.skip();
    }

    private static YearMonth parseYearMonth(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            YearMonth.parse(valueToConvert, dateTimeFormatter) :
            YearMonth.parse(valueToConvert);
    }

    private static Year parseYear(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            Year.parse(valueToConvert, dateTimeFormatter) :
            Year.parse(valueToConvert);
    }

    private static MonthDay parseMonthDay(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            MonthDay.parse(valueToConvert, dateTimeFormatter) :
            MonthDay.parse(valueToConvert);
    }

    private static ZonedDateTime parseZonedDateTime(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            ZonedDateTime.parse(valueToConvert, dateTimeFormatter) :
            ZonedDateTime.parse(valueToConvert);
    }

    private static OffsetTime parseOffsetTime(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            OffsetTime.parse(valueToConvert, dateTimeFormatter) :
            OffsetTime.parse(valueToConvert);
    }

    private static OffsetDateTime parseOffsetDateTime(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            OffsetDateTime.parse(valueToConvert, dateTimeFormatter) :
            OffsetDateTime.parse(valueToConvert);
    }

    private static LocalTime parseLocalTime(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            LocalTime.parse(valueToConvert, dateTimeFormatter) :
            LocalTime.parse(valueToConvert);
    }

    private static LocalDate parseLocalDate(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            LocalDate.parse(valueToConvert, dateTimeFormatter) :
            LocalDate.parse(valueToConvert);
    }

    private static LocalDateTime parseLocalDateTime(
            ProxyMethod proxyMethod, 
            String valueToConvert
    ) {
        DateTimeFormatter dateTimeFormatter = 
            determineDateTimeFormatterOrNull(proxyMethod);
        return dateTimeFormatter != null ?
            LocalDateTime.parse(valueToConvert, dateTimeFormatter) :
            LocalDateTime.parse(valueToConvert);
    }

    private static @Nullable DateTimeFormatter determineDateTimeFormatterOrNull(
            ProxyMethod proxyMethod
    ) {
        DateTimeFormat dateTimeFormat = getDateTimeFormatOrNull(proxyMethod);
        if (dateTimeFormat == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(dateTimeFormat.value());
    }
    
    private static @Nullable DateTimeFormat getDateTimeFormatOrNull(
            ProxyMethod proxyMethod
    ) {
        return proxyMethod
            .findAnnotation(DateTimeFormat.class)
            .orElse(null);
    }
}
