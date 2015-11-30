package com.fourcasters.forec.benchmark.launcher;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourcasters.forec.benchmark.throughput.MonitorClient;

public class MainClass {

	private static Thread t;
	private static final Logger LOG = LogManager.getLogger(MainClass.class);

	public static void main(String[] args) throws InterruptedException {
		//TODO
		/**
		 * - Read configuration
		 * - By reflection, pick the right benchmark
		 * - ...
		 */

		//check server is running

		//create and run benchmark
		final MonitorClient client = new MonitorClient();
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				client.receive("localhost", 5562, System.currentTimeMillis());
			}
		});
		t.setDaemon(true);
		t.start();
		for(int i = 0; i < 6; i++) {
			Thread.sleep(10000L);
			LOG.info("10 seconds are gone");
		}
		//wrap up and close
		client.stop(System.currentTimeMillis());
		t.join();
		int[] results = client.getResults();
		System.out.println("Results: " + Arrays.toString(results));
	}
}
