package com.ylli.api.sys;

import com.google.gson.Gson;
import com.ylli.api.sys.service.StatsService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Ignore
public class StatsTest {
    @Autowired
    StatsService statsService;

    @Test
    public void successRate(){
//        int i = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//        System.out.println(i);
        Object o = statsService.successRate(null, null, null);
        System.out.println(new Gson().toJson(o));
    }
}
