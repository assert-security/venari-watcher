package com.assertsecurity.venariwatcher.dnsserver;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

public class DnsServer implements Runnable {
    private volatile boolean running = false;
    private static final int UDP_SIZE = 512;
    private final int port;
    private int requestCount = 0;

    public DnsServer(int port) {
        this.port = port;
    }

    public void run () {
        try {
			System.out.println(DateTimeUtils.getDateTimeString() + " [DNS Server] >> Listening on port " + this.port);
            this.running = true;
            serve();
        } catch (IOException ex) {
            stop();
            throw new RuntimeException(ex);
        }
    }

    public void stop() {
        running = false;
    }

    public int getRequestCount() {
        return requestCount;
    }

    private void serve() throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        while (running) {
            process(socket);
        }
    }

    private void process(DatagramSocket socket) throws IOException {
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);
        ++requestCount;
        System.out.println(DateTimeUtils.getDateTimeString() + " [DNS Server]  >> " + String.format("processing... %d", requestCount));

        // Build the response
        Message request = new Message(in);
        String question = request.getQuestion().getName().toString();
        if (question.endsWith(".")) 
        {
            question = question.substring(0, question.length() - 1);
        }
        try
        {
            UUID id = UUID.fromString(question);
            PayloadMapper.Set(id);
            System.out.println(DateTimeUtils.getDateTimeString() + " [DNS Server]  >> Payload received " + id.toString());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

        String ip = "1.2.3.4";
        try(final DatagramSocket s = new DatagramSocket())
        {
            s.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = s.getLocalAddress().getHostAddress();
        }        
        Message response = new Message(request.getHeader().getID());
        response.addRecord(request.getQuestion(), Section.QUESTION);
        // Add answers as needed
        response.addRecord(Record.fromString(Name.root, Type.A, DClass.IN, 86400, ip, Name.root), Section.ANSWER);

        byte[] resp = response.toWire();
        DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
        socket.send(outdp);
    }
}
