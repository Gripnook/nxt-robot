package nxt.comm;

/**
 * The protocol to use for communicating data via bluetooth between robot and
 * computer. Each constant represents a byte to send to notify the receiver of
 * the following message.
 * 
 * @author Andrei Purcarus
 *
 */
public final class BluetoothProtocol {
	public static final byte X_POSITION = 0x00;
	public static final byte Y_POSITION = 0x01;
	public static final byte ORIENTATION = 0x02;
	public static final byte LEFT_US_DISTANCE = 0x03;
	public static final byte FRONT_US_DISTANCE = 0x04;
	public static final byte RIGHT_US_DISTANCE = 0x05;
	public static final byte CS_VALUE = 0x06;
	public static final byte END_TRANSMISSION = 0x07;
	public static final byte NULL_MESSAGE = 0x08;

	private BluetoothProtocol() {

	}
}
