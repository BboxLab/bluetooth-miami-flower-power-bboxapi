package fr.bytel.bluetoothsample.util;

/**
 * Manual reset event interface
 * 
 * @author Bertrand Martel Bouygues Telecom
 * 
 */
public interface IManualResetEvent {

	/**
	 * Getter for open reset variable
	 */
	public boolean getOpen();

	/**
	 * Setter for open reset variable
	 * 
	 * @param value
	 *            used to control monitor state
	 */
	public void setOpen(boolean value);

	/**
	 * Getter for monitor object
	 * 
	 * @return monitor object used for wait/notify state
	 */
	public Object getMonitor();
}
