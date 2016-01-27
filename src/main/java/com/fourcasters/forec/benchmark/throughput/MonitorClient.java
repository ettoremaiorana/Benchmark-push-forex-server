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
	private long finalTime;
	private long initialTime;
	private final int[] msgsPerSecond = new int[SECONDS_IN_ONE_HOUR];
	private final byte[] dest = new byte[16];
	private final byte[] dest1 = new byte[16];
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
	private Thread currentThread;
	private int counter = 0;

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
        registerSignalHandler();
        while (work) {
        	int count = subscriber._recvDirectBuffer(buffer, 16, ZMQ.DONTWAIT);
//        	int count = subscriber.recvByteBuffer(buffer, ZMQ.DONTWAIT);
        	if (count <= 0) {
        		continue;
        	}
        	final long arrivalTime = System.currentTimeMillis();
        	final int slot = (int) ((arrivalTime - initialTime) / 1000);
        	msgsPerSecond[slot] = msgsPerSecond[slot] + 1;
        	//We're not interested in the content of the message
        	buffer.clear();
        	Arrays.fill(dest, (byte)0);
        	Arrays.fill(dest1, (byte)0);
        	counter  ++;
        }
        subscriber.close();
        context.close();
	}

	public void stop(long finalTime) {
		this.finalTime = finalTime;
		work = false;
		currentThread.interrupt();
	}

	private void registerSignalHandler() {
		ZMQ.register_signalhandler();
	}
	
	public int[] getResults() {
		if (work) {
			return EMTPY_RESULT;
		}
		System.out.println("Counter: " + counter);
    	final int slot = (int) ((finalTime - initialTime) / 1000);
    	return Arrays.copyOf(msgsPerSecond, slot);
	}
}
