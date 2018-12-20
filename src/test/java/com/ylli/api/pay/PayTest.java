package com.ylli.api.pay;

import com.ylli.api.pay.service.MessageService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class PayTest {

    @Autowired
    MessageService messageService;

    @Test
    public void test() {
        messageService.addNotifyJobs(0L, "testUrl", "testParams");
    }
}
