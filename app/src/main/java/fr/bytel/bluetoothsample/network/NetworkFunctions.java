package fr.bytel.bluetoothsample.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Functions used to retrieve mac address and IP (for all network interfaces available)
 *
 * @author Bertrand Martel Bouygues Telecom on 02/03/15.
 */
public class NetworkFunctions {


    public static String getIpV4FromString(String ipString)
    {
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ipString);
        if (matcher.find()) {
            return matcher.group();
        }
        else{
            return "0.0.0.0";
        }
    }

    public static ArrayList<BoxNetworkTemplate> getIpList()
    {
        String ip;
        ArrayList<BoxNetworkTemplate> ipList = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if (!NetworkFunctions.getIpV4FromString(ip).equals("0.0.0.0"))
                    {
                        byte[] mac = iface.getHardwareAddress();

                        String macAddr="";

                        if (mac!=null) {
                            System.out.print("Current MAC address : ");

                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < mac.length; i++) {
                                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                            }
                            macAddr = sb.toString();
                        }
                        ipList.add(new BoxNetworkTemplate(ip,macAddr));
                    }

                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return ipList;
    }
}
