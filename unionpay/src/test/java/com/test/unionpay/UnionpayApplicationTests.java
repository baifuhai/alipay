package com.test.unionpay;

import com.unionpay.acp.sdk.SDKConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UnionpayApplicationTests {

	@Autowired
	SDKConfig sdkConfig;

	@Test
	public void contextLoads() {
		System.out.println();
	}

}

