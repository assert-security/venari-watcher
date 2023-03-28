package com.assertsecurity.venariwatcher.start;

import com.assertsecurity.venariwatcher.dnsserver.DnsServer;
import com.assertsecurity.venariwatcher.httpserver.HttpServer;
import com.assertsecurity.venariwatcher.ldapserver.LdapServer;
import com.assertsecurity.venariwatcher.rmiserver.RmiServer;
import com.assertsecurity.venariwatcher.smtpserver.SmtpServer;

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
	private static int ldapPort = 3002;
	private static int rmiPort = 3003;
	private static int smtpPort = 587;

	private HttpServer _httpServer;
	private HttpServer _deprecatedHttpServer;
	private LdapServer _ldapServer;
	private RmiServer _rmiServer;
	private DnsServer _dnsServer;
	private SmtpServer _smtpServer;

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

		if (cmd.hasOption("s")) {
			try {
				smtpPort = Integer.parseInt(cmd.getOptionValue('s'));
			} catch (Exception e) {
				System.err.println("Format to change default values is PORT");
				System.exit(1);
			}
		}

        Servers servers = new Servers();
		servers._httpServer = new HttpServer(ipAddress, httpPort);
		servers._ldapServer = new LdapServer(ipAddress, ldapPort);
		servers._rmiServer = new RmiServer(ipAddress, rmiPort);
		servers._dnsServer = new DnsServer(dnsPort);
		servers._smtpServer = new SmtpServer(ipAddress, smtpPort);

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

		Thread threadSmtp = new Thread(servers._smtpServer);
		threadSmtp.start();

    }

	public Servers() {
	}

	public static Options cmdlineOptions() {
		Options opts = new Options();
		Option address = new Option("a", true, "The address of the server (ip or domain). Format: IPADDRESS");
		opts.addOption(address);
		Option http_port = new Option("h", true, "The port of HTTP server. Format: PORT");
		opts.addOption(http_port);
		Option rmi_port = new Option("r", true, "The port of RMI server. Format: PORT");
		opts.addOption(rmi_port);
		Option ldap_port = new Option("l", true, "The port of LDAP server. Format: PORT");
		opts.addOption(ldap_port);
		Option dns_port = new Option("d", true, "The port of DNS server. Format: PORT");
		opts.addOption(dns_port);
		Option smtp_port = new Option("s", true, "The port of SMTP server. Format: PORT");
		opts.addOption(smtp_port);
		Option help = new Option("?", true, "Display the help menu.");
		opts.addOption(help);
		return opts;
	}


}