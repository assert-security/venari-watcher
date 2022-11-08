package com.assertsecurity.venariwatcher.start;

import com.assertsecurity.venariwatcher.dnsserver.DnsServer;
import com.assertsecurity.venariwatcher.httpserver.HttpServer;
import com.assertsecurity.venariwatcher.ldapserver.LdapServer;
import com.assertsecurity.venariwatcher.rmiserver.RmiServer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Servers {

	private static String ipAddress = "0.0.0.0";
	private static int httpPort = 80;
	private static int dnsPort = 53;
	private static int deprecatedHttpPort = 3001;
	private static int ldapPort = 3002;
	private static int rmiPort = 3003;

	private HttpServer _httpServer;
	private HttpServer _deprecatedHttpServer;
	private LdapServer _ldapServer;
	private RmiServer _rmiServer;
	private DnsServer _dnsServer;

    public static void main(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(cmdlineOptions(), args);
		} catch (Exception e) {
			System.err.println("Cmdlines parse failed.");
			new HelpFormatter().printHelp("Venari-Watcher", cmdlineOptions());
			System.exit(1);
		}
		if (cmd.hasOption("a")) {
			try {
				ipAddress = cmd.getOptionValue('a');
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}
		if (cmd.hasOption("h")) {
			try {
				httpPort = Integer.parseInt(cmd.getOptionValue('h'));
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}
		if (cmd.hasOption("r")) {
			try {
				rmiPort = Integer.parseInt(cmd.getOptionValue('r'));
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}
		if (cmd.hasOption("l")) {
			try {
				ldapPort = Integer.parseInt(cmd.getOptionValue('l'));
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}

		if (cmd.hasOption("d")) {
			try {
				dnsPort = Integer.parseInt(cmd.getOptionValue('d'));
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}

        Servers servers = new Servers();
		servers._httpServer = new HttpServer(ipAddress, httpPort);
		servers._deprecatedHttpServer = new HttpServer(ipAddress, deprecatedHttpPort);
		servers._ldapServer = new LdapServer(ipAddress, ldapPort);
		servers._rmiServer = new RmiServer(ipAddress, rmiPort);
		servers._dnsServer = new DnsServer(dnsPort);

		System.out.println("----------------------------Server Log----------------------------");

		Thread threadHttp = new Thread(servers._httpServer);
		threadHttp.start();
		
		Thread threadLdap = new Thread(servers._ldapServer);
		threadLdap.start();

		Thread threadRmi = new Thread(servers._rmiServer);
		threadRmi.start();

		Thread threadDeprecatedHttp = new Thread(servers._deprecatedHttpServer);
		threadDeprecatedHttp.start();

		Thread threadDns = new Thread(servers._dnsServer);
		threadDns.start();


    }

	public Servers() {
	}

	public static Options cmdlineOptions() {
		Options opts = new Options();
		Option http_addr = new Option("h", true, "The address of HTTP server (ip or domain). Format: IP:PORT");
		opts.addOption(http_addr);
		Option rmi_addr = new Option("r", true, "The address of RMI server (ip or domain). Format: IP:PORT");
		opts.addOption(rmi_addr);
		Option ldap_addr = new Option("l", true, "The address of LDAP server (ip or domain). Format: IP:PORT");
		opts.addOption(ldap_addr);
		Option help = new Option("?", true, "Display the help menu.");
		opts.addOption(help);
		return opts;
	}


}