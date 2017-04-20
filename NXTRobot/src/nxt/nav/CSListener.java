package nxt.nav;

/**
 * An interface to be implemented by all classes that wish to listen for the
 * color sensor's detection of a grid line.
 *
 * @author Andrei Purcarus
 *
 */
public interface CSListener {
	/**
	 * The method to be called to notify the listener of a grid line detection
	 * by the color sensor.
	 */
	public void ping();
}
