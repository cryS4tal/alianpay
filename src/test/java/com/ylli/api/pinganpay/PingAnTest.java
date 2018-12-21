package com.ylli.api.pinganpay;

import com.ylli.api.third.pay.model.PingAnGR;
import com.ylli.api.third.pay.model.PingAnQY;
import com.ylli.api.third.pay.service.PingAnService;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Ignore
public class PingAnTest {

    @Autowired
    PingAnService service;

    @Test
    public void createOrder() {
        List<PingAnQY> qies = new ArrayList<PingAnQY>() {
            {
                //add(new PingAnQY("15000090244196", "Q000201184", "01001034300004537000"));
                //add(new PingAnQY("15000090253485", "Q000201199", "01001034300004538000"));
                add(new PingAnQY("15000090253679", "Q000201200", "01001034300004539000"));
                //add(new PingAnQY("15000090253776", "Q000201201", "01001034300004540000"));
                //add(new PingAnQY("15000090253873", "Q000201202", "01001034300004541000"));
                //release 账户
                add(new PingAnQY("15000096544539", "Q000040814", "00901025000000179000"));
            }
        };

        List<PingAnGR> grs = new ArrayList<PingAnGR>() {
            {
                add(new PingAnGR("6226090000000048", "张三", "招商银行", "18100000000"));
                //add(new PingAnGR("6226388000000095", "张三", "华夏银行", "18100000000"));
                /*add(new PingAnGR("5200831111111113", "全渠道", "农业银行", "13552535506"));
                add(new PingAnGR("6226330151030000", "张小花", "华夏", "18100000005"));
                add(new PingAnGR("6226388000000087", "张三", "华夏", "17500000000"));
                add(new PingAnGR("6230580000032125424", "平安测试四七一零七", "平安银行", ""));
                add(new PingAnGR("6225750000000006", "王援朝", "招行", "18100000001"));
                add(new PingAnGR("9559981700000000004", "马小燕", "农行", "18100000002"));
                add(new PingAnGR("6226602900000009", "杰士塔威", "光大", "18100000003"));
                add(new PingAnGR("3568390000000003", "杰士塔威", "光大", "18100000003"));
                add(new PingAnGR("620707000000000001", "伊丽莎白", "工行", "18100000004"));
                add(new PingAnGR("6222357005000008", "伊丽莎白", "工行", "18100000004"));
                add(new PingAnGR("6225380092315250", "平安测试三二五零八", "平安银行", ""));
                add(new PingAnGR("6230580000054508325", "平安测试七八八零六", "平安银行", ""));*/
            }
        };

        int order = 5161;

        for (int i = 0; i < qies.size(); i++) {
            for (int j = 0; j < grs.size(); j++) {
                try {
                    service.createPingAnOrder(String.valueOf(order++), qies.get(i), grs.get(j));
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }


    @Test
    public void orderQuery() {
        service.payQuery();
    }

}
