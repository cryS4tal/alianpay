package com.ylli.api.user;

import com.ylli.api.user.service.UserBaseService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ylli on 2017/4/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class UserBaseTests {

    @Autowired
    UserBaseService userBaseService;

    @Test
    public void testRedis() {
        for (int i = 0; i < 10; i++) {
            System.out.println(userBaseService.getCode(1));
        }
    }

}
