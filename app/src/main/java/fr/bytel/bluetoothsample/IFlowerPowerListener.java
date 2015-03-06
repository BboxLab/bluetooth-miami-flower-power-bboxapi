package fr.bytel.bluetoothsample;

/**
 * @author Bertrand Martel Bouygues Telecom on 04/03/15.
 */
public interface IFlowerPowerListener {

    public void onSunLightChange(double value);

    public void onSoilEcChange(double soilEC);

    public void onSoilTempChange(double temp);

    public void onAirTempChange(double temp);

    public void onSoilWcChange(double wc);

}
