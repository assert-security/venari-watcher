package com.assertsecurity.venariwatcher.smtpserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ServerSocketFactory;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;

public class SmtpServer implements Runnable {
    private int _port;
    private String _ipAddress;
    private ServerSocket _ss;
    private boolean _exit;
    private Object _waitLock = new Object();

	private static final Pattern CRLF = Pattern.compile("\r\n");
	private static final Pattern WATCHERID = Pattern.compile("[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?");

    public SmtpServer (String ipAddress, int port ) throws IOException {
        _port = port;
        _ipAddress = ipAddress;
        _ss = ServerSocketFactory.getDefault().createServerSocket(this._port);
    }
    
    @Override
    public void run () {
        try {
			System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server] >> Listening on " + _ipAddress + ":" + _port);
            Socket s = null;
            try {
                while ( !this._exit && ( s = this._ss.accept() ) != null ) {
                    try {
                        s.setSoTimeout(5000);
                        Scanner input = new Scanner(new InputStreamReader(s.getInputStream(), StandardCharsets.ISO_8859_1)).useDelimiter(CRLF);
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.ISO_8859_1));
   
						List<SmtpMessage> receivedMail = new ArrayList<>();
						synchronized (_waitLock) {
                           /*
                            * We synchronize over the handle method and the list update because the client call completes inside
                            * the handle method and we have to prevent the client from reading the list until we've updated it.
                            */
                           receivedMail.addAll(handleTransaction(out, input));
                        }
						if (receivedMail.size() > 0) 
						{
							for (int i=0; i<receivedMail.size(); i++)
							{
								SmtpMessage message = receivedMail.get(i);
								Set<String> headerNames = message.getHeaderNames();
								if (headerNames.size() > 0) 
								{
									headerNames.forEach(x ->
									{
										Matcher matcher = WATCHERID.matcher(x);
										while (matcher.find()) {
											handlePayload(matcher.group());
										}
										String value = message.getHeaderValue(x);
										matcher = WATCHERID.matcher(value);
										while (matcher.find()) {    
											handlePayload(matcher.group());
										}
									});
									String body = message.getBody();
									Matcher matcher = WATCHERID.matcher(body);
									while (matcher.find()) {    
										handlePayload(matcher.group());
									}
							}
							}
						}
                    }
                    catch ( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                    finally {
                        System.out.println(DateTimeUtils.getDateTimeString() + " [RMI Server]  >> Closing connection");
                        s.close();
                    }
                }
            }
            finally {
                if ( s != null ) {
                    s.close();
                }
                if ( this._ss != null ) {
                    this._ss.close();
                }
            }
        }
        catch ( SocketException e ) {
            return;
        }
        catch ( Exception e ) {
            e.printStackTrace(System.err);
        }
    }

	private void handlePayload(String payload)
	{
        try
        {
            UUID id = UUID.fromString(payload);
            PayloadMapper.Set(id);
            System.out.println(DateTimeUtils.getDateTimeString() + " [SMTP Server]  >> Payload received " + id.toString());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }

	}

	/**
	 * Handle an SMTP transaction, i.e. all activity between initial connect and QUIT command.
	 *
	 * @param out   output stream
	 * @param input input stream
	 * @return List of SmtpMessage
	 * @throws IOException
	 */
	private static List<SmtpMessage> handleTransaction(PrintWriter out, Iterator<String> input) throws IOException {
		// Initialize the state machine
		SmtpState smtpState = SmtpState.CONNECT;
		SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "", smtpState);

		// Execute the connection request
		SmtpResponse smtpResponse = smtpRequest.execute();

		// Send initial response
		sendResponse(out, smtpResponse);
		smtpState = smtpResponse.getNextState();

		List<SmtpMessage> msgList = new ArrayList<>();
		SmtpMessage msg = new SmtpMessage();

		while (smtpState != SmtpState.CONNECT) {
			String line = input.next();

			if (line == null) {
				break;
			}

			// Create request from client input and current state
			SmtpRequest request = SmtpRequest.createRequest(line, smtpState);
			// Execute request and create response object
			SmtpResponse response = request.execute();
			// Move to next internal state
			smtpState = response.getNextState();
			// Send response to client
			sendResponse(out, response);

			// Store input in message
			String params = request.params;
			msg.store(response, params);

			// If message reception is complete save it
			if (smtpState == SmtpState.QUIT) {
				msgList.add(msg);
				msg = new SmtpMessage();
			}
		}

		return msgList;
	}

	/**
	 * Send response to client.
	 *
	 * @param out          socket output stream
	 * @param smtpResponse response object
	 */
	private static void sendResponse(PrintWriter out, SmtpResponse smtpResponse) {
		if (smtpResponse.getCode() > 0) {
			int code = smtpResponse.getCode();
			String message = smtpResponse.getMessage();
			out.print(code + " " + message + "\r\n");
			out.flush();
		}
	}

}
