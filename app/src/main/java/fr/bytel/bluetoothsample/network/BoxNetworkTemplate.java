package fr.bytel.bluetoothsample.network;

/**
 * Template that identify one box (uniquely)
 *
 * Created by abathur on 03/03/15.
 */
public class BoxNetworkTemplate {

    /**
     * one ip address
     */
    private String ipAddress="";

    /**
     * mac address
     */
    private String macAddress="";

    /**
     * Build box network template object
     *
     * @param ipAddress
     *      ip address
     * @param macAddress
     *      mac address
     */
    public BoxNetworkTemplate(String ipAddress, String macAddress)
    {
        this.ipAddress=ipAddress;
        this.macAddress=macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
