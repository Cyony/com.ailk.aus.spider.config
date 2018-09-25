package com.ailk.aus.spider.task;

import java.util.Properties;

import org.junit.Test;

public class TaskTest {

	@Test
	public void test() throws Exception {
		Properties prop = new Properties();
		prop.put("driver", "com.mysql.jdbc.Driver");
		prop.put("url", "jdbc:mysql://10.15.42.21:3306/spider_config");
		prop.put("password", "root");
		prop.put("userName", "root");
		SpiderTask task = new SpiderTask(prop);
		task.getAllTask("1,2").forEach(System.out::println);
	}

}
