package com.fourcasters.forec.benchmark.throughput;

import java.util.concurrent.locks.LockSupport;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Publisher {

	private final Socket publisher;
	public Publisher(Context context, String address) {
		publisher = context.socket(ZMQ.PUB);
		publisher.bind(address);

	}
	
    public static void main (String[] args) throws Exception {
        // Prepare our context and publisher
        final Context context = ZMQ.context(1);
        final Publisher publisher = new Publisher(context, "tcp://*:5562");
        while (!Thread.currentThread ().isInterrupted ()) {
            publisher.send("topics", "my-message");
        }
        publisher.close();
        context.term ();
    }

    public void close() {
    	publisher.close();
	}

	public boolean send(String topic, String message) {
    	publisher.send(topic.getBytes(), ZMQ.SNDMORE);
    	return publisher.send(message.getBytes(), 0);
    }
}
