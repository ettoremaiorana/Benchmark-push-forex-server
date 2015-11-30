package com.fourcasters.forec.benchmark.throughput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.fourcasters.forec.benchmark.launcher.Client;

public class MonitorClient implements Client {

	//1024 * 4bytes = 4096 bytes, to be tuned in according with cache size
	private static final int SECONDS_IN_ONE_HOUR = 60*24;
	private static final Logger LOG = LogManager.getLogger(MonitorClient.class);
	private static final int[] EMTPY_RESULT = new int[0];

	private volatile boolean work = false;
	private int counter = 0;
	private long finalTime;
	private long initialTime;
	private final int[] msgsPerSecond = new int[SECONDS_IN_ONE_HOUR];
	private final byte[] dest = new byte[1024];
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
	private Thread currentThread;

	/**
	 * - Receives messages and records their arrivals in slot of time units.
	 */
	public void receive(String address, int port, long initialTime) {
		this.currentThread = Thread.currentThread();
		this.initialTime = initialTime;
        // Prepare our context and subscriber
		final Context context = ZMQ.context(1);
		final Socket subscriber = context.socket(ZMQ.SUB);
		final String connectionString = "tcp://" + address + ":" + port;
		LOG.info("Connecting to " + connectionString);
        subscriber.connect(connectionString);
        subscriber.subscribe("topics".getBytes());
        LOG.info("Connected and subscribed");
        work = true;
        while (work) {
        	int count = subscriber.recvZeroCopy(buffer, 1024, 0);
        	if (count <= 0) {
        		continue;
        	}
        	count = 0;
        	buffer.clear();
        	while (count == 0) {
        		count = subscriber.recvZeroCopy(buffer, 1024, 0);
        	}
        	buffer.flip();
        	buffer.get(dest, 0, count);
        	final long arrivalTime = System.currentTimeMillis();
        	final int slot = (int) ((arrivalTime - initialTime) / 1000);
        	msgsPerSecond[slot] = msgsPerSecond[slot] + 1;
        	//We're not interested in the content of the message
        	buffer.clear();
        	Arrays.fill(dest, (byte)0);
        	if (++counter % 100 == 0) {
        		LOG.info("100 messages");
        	}
        }
        subscriber.close();
        context.close();
	}

	public void stop(long finalTime) {
		this.finalTime = finalTime;
		work = false;
		currentThread.interrupt();
	}

	public int[] getResults() {
		if (work) {
			return EMTPY_RESULT;
		}
    	final int slot = (int) ((finalTime - initialTime) / 1000);
    	return Arrays.copyOf(msgsPerSecond, slot);
	}
}
