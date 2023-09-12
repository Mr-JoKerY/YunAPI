package com.hyw.project.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SmsManagerTest {

    @Resource
    private SmsManager smsManager;

    @Test
    void sendSms() {
        smsManager.sendSms("15766091802", "557");
    }
}