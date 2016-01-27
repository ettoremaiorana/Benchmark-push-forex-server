package com.fourcasters.forec.benchmark.throughput;

import java.util.concurrent.locks.LockSupport;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Sender {

	private final Socket publisher;
	public Sender(Context context, String address) {
		publisher = context.socket(ZMQ.PUSH);
		publisher.bind(address);

	}
	
    public static void main (String[] args) throws Exception {
        // Prepare our context and publisher
        final Context context = ZMQ.context(1);
        final Sender publisher = new Sender(context, "tcp://*:5562");
        final byte[] messageInByte = "Hello, World!".getBytes();
        while (!Thread.currentThread ().isInterrupted ()) {
            publisher.send("topics", messageInByte);
//        	LockSupport.parkNanos(1L);
        }
        publisher.close();
        context.term ();
    }

    public void close() {
    	publisher.close();
	}

	public final boolean send(String topic, byte[] message) {
    	return publisher.send(message, 0);
    }
}
