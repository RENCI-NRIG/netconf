package net.juniper.netconf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;

public class SendCommand {

	private static final String createVmap = "access-control vmap 1 meter committed-rate 1000000\n" + 
			"access-control vmap 1 meter maximum-burst-size 1024\n" + 
			"access-control vmap 1 meter action drop\n" +  
			"access-control vmap 1 meter enable\nexit\n" + 
			"vlan 1234\nvmap 1 serverports\nexit\n";

	private static final String createVlan = "vlan 1234\nname \"Test Vlan-1234\"\nexit\n";

	private static final String deleteVlan = "no vlan 1234\nexit\n";

	private static final String deleteVmap = "no access-control vmap 1\nexit\n";

	private static final String showVmap = "show access-control vmap\nexit\n";

	public static void main(String args[]) throws NetconfException, 
	ParserConfigurationException, SAXException, IOException {

		//Create the device object and establish a NETCONF session
		System.out.println("Creating device");
		Device device = new Device("host-ip", "login", "pass", null);
		System.out.println("Connecting");
		device.connect();
		System.out.println("Executing RPC");
		//Send RPC and receive RPC reply as XML
		//XML rpc_reply = device.executeRPC("get-chassis-inventory");
		try {
			//device.loadRunningTextConfiguration("vlan 1234\nname \"Test Vlan-1234\"\nexit\n", "merge");
			//device.loadRunningTextConfiguration("no vlan 1234\nexit\n", "merge");
			//device.loadRunningTextConfiguration(createVmap, "merge");

			//String ret = device.runCliCommand(showVmap);
			XML ret = device.getRunningConfig();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			// look for nodes and links
			XPathExpression expr = xpath.compile("/rpc-reply/data/configuration-text");
			String nl = (String)expr.evaluate(ret.getOwnerDocument(), XPathConstants.STRING);
			
			BufferedReader br = new BufferedReader(new StringReader(nl));
			String line = null;
			Pattern vmapPat = Pattern.compile("^access-control[\\s]+vmap[\\s]+([\\d]+).*$");
			while((line = br.readLine()) != null) {
				Matcher m = vmapPat.matcher(line);
				if (m.matches()) {
					System.out.println("VMAP ID " + m.group(1));
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Print the RPC reply and close the device
		//System.out.println(rpc_reply.toString());
		device.close();
	}
}