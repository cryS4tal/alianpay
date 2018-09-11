package com.ylli.api.base.util;

import com.ylli.api.base.exception.AwesomeException;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ylli on 2017/4/8.
 */
public class AwesomeDateTime {
    private Date date;

    public AwesomeDateTime() {
    }

    public AwesomeDateTime(String date) {
        if (date != null) {
            DateFormat formatter = new ISO8601DateFormat();
            try {
                this.setDate(formatter.parse(date));
            } catch (ParseException ex) {
                throw new AwesomeException(HttpServletResponse.SC_BAD_REQUEST,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "时间格式错误，期望格式[yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm]]");
            }
        }
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    public void setDate(Date date) {
        this.date = new Date(date.getTime());
    }

    public Timestamp getTimestamp() {
        return Timestamp.from(date.toInstant());
    }

    public Timestamp getDateTimestamp() {
        return Timestamp.from(removeTime(date).toInstant());
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
