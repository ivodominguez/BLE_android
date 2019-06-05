package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    public static String getFormattedDate (Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(date);
    }

    public static String getFormattedPatternDate (Date date) {
        return new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(date);
    }

    public static String getFormattedDate (Date date, Locale locale) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", locale).format(date);
    }

    public static String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
    }

    public static Date parseStringDate(String date) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    public static Date parseStringPatternDate(String date) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    public static Date parseStringPatternDateTime(String date) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    public static Date parseStringDateTime(String date) {
        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

}
