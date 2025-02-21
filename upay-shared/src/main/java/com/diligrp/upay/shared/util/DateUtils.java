package com.diligrp.upay.shared.util;

import com.diligrp.upay.shared.Constants;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期格式转化工具类 - JDK1.8 TIME API
 */
public class DateUtils {

    public static String formatDateTime(LocalDateTime when, String format) {
        if (ObjectUtils.isNull(when)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return when.format(formatter);
    }

    public static String formatDateTime(LocalDateTime when) {
        return formatDateTime(when, Constants.DATE_TIME_FORMAT);
    }

    public static String formatDate(LocalDate when, String format) {
        if (ObjectUtils.isNull(when)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return when.format(formatter);
    }

    public static String formatDate(LocalDate when) {
        return formatDate(when, Constants.DATE_FORMAT);
    }

    public static String formatNow(String format) {
        return formatDateTime(LocalDateTime.now(), format);
    }

    public static String formatNow() {
        return formatNow(Constants.DATE_TIME_FORMAT);
    }

    public static String format(Date date) {
        return format(date, Constants.DATE_TIME_FORMAT);
    }
    
    public static LocalDateTime addDays(long days) {
    	LocalDateTime localDateTime = LocalDateTime.now();
    	return localDateTime.plusDays(days);
    }

    public static String format(Date date, String format) {
        if (ObjectUtils.isNull(date)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);

    }

    public static LocalDateTime parseDateTime(String datetimeStr, String format) {
        if (ObjectUtils.isEmpty(datetimeStr)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(datetimeStr, formatter);
    }

    public static LocalDateTime parseDateTime(String datetimeStr) {
        return parseDateTime(datetimeStr, Constants.DATE_TIME_FORMAT);
    }

    public static LocalDate parseDate(String dateStr, String format) {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(dateStr, formatter);
    }

    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, Constants.DATE_FORMAT);
    }

    public static Date parse(String dateStr) {
        return parse(dateStr, Constants.DATE_TIME_FORMAT);
    }

    public static Date parse(String dateStr, String format) {
        if (ObjectUtils.isEmpty(dateStr)) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    /**
    * 获取时间戳
    */
    public static long parseMilliSecond(LocalDateTime localDateTime){
        return parseMilliSecond(localDateTime,null);
    }

    public static long parseMilliSecond(LocalDateTime localDateTime, String zoneNumStr){
        //默认东八区
        if (ObjectUtils.isEmpty(zoneNumStr)){
            zoneNumStr = "+8";
        }
        return localDateTime.toInstant(ZoneOffset.of(zoneNumStr)).toEpochMilli();
    }
}
