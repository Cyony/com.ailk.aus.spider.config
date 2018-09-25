package com.ailk.aus.spider.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExcutorTest {

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			executor.submit(() -> {
				try {
					Thread.sleep(5000);
					System.out.println("-------");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			System.out.println("sumbit a task:" + i);
		}
		executor.shutdown();
		while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
			System.out.println("+++");
			Thread.sleep(3000);
		}
		executor.shutdownNow();
		executor = null;
	}

}
