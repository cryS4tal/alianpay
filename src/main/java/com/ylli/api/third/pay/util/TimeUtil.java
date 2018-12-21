package com.ylli.api.third.pay.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    /**
     * 格式转换类型: yyyyMMdd HH:mm:ss
     */
    public final static String FORMAT_STRING1 = "yyyyMMdd HH:mm:ss";
    /**
     * 格式转换类型: yyyyMMdd
     */
    public final static String FORMAT_STRING2 = "yyyyMMdd";
    /**
     * 格式转换类型: HH:mm:ss
     */
    public final static String FORMAT_STRING3 = "HHmmss";
    /**
     * 格式转换类型: yyyyMMddHHmmss
     */
    public final static String FORMAT_STRING4 = "yyyyMMddHHmmss";
    /**
     * 格式转换类型: yyyy-MM-dd HH:mm:ss
     */
    public final static String FORMAT_STRING5 = "yyyy-MM-dd HH:mm:ss";
    /**
     * 格式转换类型: yyyy-MM-dd
     */
    public final static String FORMAT_STRING6 = "yyyy-MM-dd";
    /**
     * 格式转换类型: HH:mm:ss
     */
    public final static String FORMAT_STRING7 = "HH:mm:ss";
    /**
     * 格式转换类型 yyyyMMddHHmm
     */
    public final static String FORMAT_STRING8 = "yyyyMMddHHmm";
    /**
     * 格式转换类型: yyyyMMdd
     */
    public final static String FORMAT_STRING9 = "yyMMddHH";
    /**
     * 格式转换类型: yyyy-MM
     */
    public final static String FORMAT_STRING10 = "yyyy-MM";
    /**
     * 格式转换类型: yyyy-MM-dd H
     */
    public final static String FORMAT_STRING11 = "yyyy-MM-dd H";
    /**
     * 格式转换类型: yyyy/MM/dd HH:mm:ss
     */
    public final static String FORMAT_STRING12 = "yyyy/MM/dd HH:mm:ss";

    /**
     * @Author yangdf
     * @Description Date转换成String
     * @Params [date, formatString -转换格式，如yyyyMMdd]
     * @ReturnType java.lang.String
     * @Date 2018/5/11 17:33
     * @ModifyDetail
     * @ModifyDate
     */
    public static String dateToString(Date date, String formatString) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatString);
        return sdf.format(date);
    }

    /**
     * 字符串解析成日期
     *
     * @param date
     * @param formatString
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String date, String formatString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(formatString);
        return sdf.parse(date);
    }

    /**
     * 指定时间截取到天
     *
     * @return返回短时间格式 yyyy-MM-dd
     */
    public static Date shortDate(Date paramDate) {
        return shortDate(paramDate, FORMAT_STRING6);
    }

    /**
     * 截取指定时间
     *
     * @param paramDate
     * @param format
     * @return
     */
    public static Date shortDate(Date paramDate, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        String dateString = dateToString(paramDate, format);
        ParsePosition pos = new ParsePosition(0);
        Date currentDate = df.parse(dateString, pos);
        return currentDate;
    }

    /**
     * 返回指定日期之后几年,几个月,几天后的日期
     *
     * @param paramDate
     * @param years
     * @param months
     * @param days
     * @return
     */
    public static Date nextDate(Date paramDate, Integer years, Integer months, Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paramDate);
        if (null != years) {
            calendar.add(Calendar.YEAR, years);
        }
        if (null != months) {
            calendar.add(Calendar.MONTH, months);
        }
        if (null != days) {
            calendar.add(Calendar.DAY_OF_YEAR, days);
        }
        paramDate = calendar.getTime();
        return paramDate;
    }

    /**
     * 返回指定日期之后几小时年,几分钟,几秒后的日期
     *
     * @param paramDate
     * @param hours
     * @param ms
     * @param ss
     * @return
     */
    public static Date nextTime(Date paramDate, Integer hours, Integer ms, Integer ss) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(paramDate);
        if (null != hours) {
            calendar.add(Calendar.HOUR_OF_DAY, hours);
        }
        if (null != ms) {
            calendar.add(Calendar.MINUTE, ms);
        }
        if (null != ss) {
            calendar.add(Calendar.SECOND, ss);
        }
        paramDate = calendar.getTime();
        return paramDate;
    }

    public static void main(String[] args) {
        System.out.println(dateToString(TimeUtil.nextDate(TimeUtil.shortDate(new Date()), null, null, -2), FORMAT_STRING5));
    }
}
