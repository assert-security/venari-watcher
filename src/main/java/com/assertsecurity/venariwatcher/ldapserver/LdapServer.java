package com.assertsecurity.venariwatcher.ldapserver;

import java.net.InetAddress;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;

public class LdapServer implements Runnable {

	private static final String LDAP_BASE = "dc=venari-watcher,dc=com";
    private int _port;
    private String _ipAddress;

    public LdapServer(String ipAddress, int port)
    {
        _ipAddress = ipAddress;
        _port = port;
    }

	@Override
	public void run() {
		try {
			InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
			config.setListenerConfigs(new InMemoryListenerConfig("listen",
					InetAddress.getByName(_ipAddress),
					_port, ServerSocketFactory.getDefault(), SocketFactory.getDefault(),
					(SSLSocketFactory) SSLSocketFactory.getDefault()));

			config.addInMemoryOperationInterceptor(new OperationInterceptor());
			InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
			System.out.println(DateTimeUtils.getDateTimeString() + " [LDAP Server] >> Listening on " + _ipAddress + ":" + _port);
			ds.startListening();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
}