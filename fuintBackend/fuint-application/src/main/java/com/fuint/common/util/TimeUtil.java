package com.fuint.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * 时间工具类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class TimeUtil {

    /**
     * 一天、一分钟、一小时对应的秒数
     */
    private static final Long ONE_MINUTE_TO_SECOND = 60L;
    private static final Long ONE_HOUR_TO_SECOND = ONE_MINUTE_TO_SECOND * 60;

    /**
     * 使用LocalDateTime进行格式化 保证多线程安全
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER2 = DateTimeFormatter.ofPattern("MM-dd");

    /**
     * 计算用餐使用时长，并返回格式化字符串（如 "1天2小时30分钟"）
     *
     * @param startTime 用餐开始时间（Date）
     * @param endTime   用餐结束时间（Date）
     * @return 格式化后的时长字符串，如 "1小时30分"、"2天5小时"
     * @throws IllegalArgumentException 如果参数为 null 或结束时间早于开始时间
     */
    public static String getMealTime(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return "";
        }
        if (endTime.before(startTime)) {
            return "";
        }
        // 转换为 Instant 并计算 Duration
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();
        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds(); // 总秒数
        if (seconds == 0) {
            return "0秒";
        }
        long days = seconds / 86400;         // 1天 = 86400秒
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (secs > 0 && sb.length() == 0) {
            // 如果前面都是0（比如只用了30秒），则显示秒
            sb.append(secs).append("秒");
        }
        return sb.toString();
    }

    public static String showTime(Date now, Date targetDate) {
        String showTime = "";
        if (targetDate != null) {
            // 5. 年内判断
            if (targetDate.getYear() == now.getYear()) {
                // 获取秒数差
                long betweenSeconds = (now.getTime() - targetDate.getTime()) / 1000;
                if (betweenSeconds < ONE_MINUTE_TO_SECOND) {
                    // 1. 1分钟内：刚刚
                    showTime = "刚刚";
                } else if (betweenSeconds < ONE_HOUR_TO_SECOND) {
                    // 2. 60分钟内
                    showTime = betweenSeconds / ONE_MINUTE_TO_SECOND + "分钟前";
                } else if (betweenSeconds < ONE_HOUR_TO_SECOND * 24) {
                    // 3. 24小时内：x小时前
                    showTime = betweenSeconds / ONE_HOUR_TO_SECOND + "小时前";
                } else {
                    // 4. >24小时：x月x日  08-1
                    showTime = dateToLocalDateTime(targetDate).format(DATE_TIME_FORMATTER2);
                }
            } else {
                showTime = dateToLocalDateTime(targetDate).format(DATE_TIME_FORMATTER1);
            }
        }
        return showTime;
    }

    /**
     * date转localDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 日期转换为时间戳，时间戳为秒
     *
     * @param day
     * @param format
     * @return
     * @throws ParseException
     */
    public static int date2timeStamp(String day, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return Integer.parseInt("" + sdf.parse(day).getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int date2timeStamp(Date date){
        return Integer.parseInt("" + date.getTime()/1000);
    }
    /**
     * 时间戳(秒)转换为字符日期
     *
     * @param seconds
     * @param format
     * @return
     */
    public static String timeStamp2Date(int seconds, String format) {
        if (seconds == 0) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 获取当前系统时间戳(秒)
     *
     * @return
     */
    public static int timeStamp() {
        return Integer.parseInt(System.currentTimeMillis() / 1000 + "");
    }

    /**
     * 判断指定日期是否在起始日期区间内
     *
     * @param startDate
     * @param endDate
     * @param date
     * @return boolean
     */
    public static boolean isSection(Date startDate, Date endDate, Date date) {
        if (startDate.getTime() <= date.getTime() && endDate.getTime() >= date.getTime()) {
            return true;
        } else {
            return false;
        }
    }

    public static String formatDate(Date date, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 获取过去几天内的日期数组
     * @param intervals      intervals天内
     * @return              日期数组
     */
    public static ArrayList<String> getDays(int intervals) {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = intervals -1; i >= 0; i--) {
            pastDaysList.add(getPastDate((i + 2)));
        }
        return pastDaysList;
    }

    /**
     * 获取过去第几天的日期
     * @param past
     * @return
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
        String result = format.format(today);
        return result;
    }
}
