package nxt.drivers;

import lejos.nxt.UltrasonicSensor;
import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.util.AngleUtility;

/**
 * A class that polls the ultrasonic sensor for distance values and sends them
 * to be processed.
 *
 * @author Andrei Purcarus
 *
 */
public final class USPoller extends Thread {
	private static final long PING_DELAY = 20;

	private final UltrasonicSensor US;
	/**
	 * The angle at which the ultrasonic sensor is pointing. Angle is taken
	 * relative to the orientation of the robot, with 0 degrees being the
	 * robot's forward orientation and angles increasing counterclockwise.
	 */
	private final int _angle;

	private static final int NUM_VALUES_TO_STORE = 5;
	private int[] _usValues;

	private final DataCenter _dataCenter;

	private boolean _isPolling;

	public static final long getTimeRequiredForPolling() {
		return PING_DELAY;
	}

	public static final void waitTimeRequiredForPolling() {
		try {
			Thread.sleep(PING_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static final long getTimeRequiredForInitialization() {
		return NUM_VALUES_TO_STORE * PING_DELAY;
	}

	public static final void waitTimeRequiredForInitialization() {
		try {
			Thread.sleep(NUM_VALUES_TO_STORE * PING_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param angle
	 *            The angle at which the ultrasonic sensor is pointing. Angle is
	 *            taken relative to the orientation of the robot, with 0 degrees
	 *            being the robot's forward orientation and angles increasing
	 *            counterclockwise.
	 * @param dataCenter
	 *            The data storage location to send data to.
	 */
	public USPoller(int angle, DataCenter dataCenter) {
		int normalizedAngle = AngleUtility.normalize(angle);
		switch (normalizedAngle) {
		case 0:
			this.US = NXTConstants.FRONT_US;
			break;
		case 90:
			this.US = NXTConstants.LEFT_US;
			break;
		case 270:
			this.US = NXTConstants.RIGHT_US;
			break;
		default:
			throw new IllegalArgumentException("Invalid angle");
		}
		this._angle = normalizedAngle;
		this._usValues = new int[NUM_VALUES_TO_STORE];
		this._dataCenter = dataCenter;
		this._isPolling = true;
	}

	@Override
	public final void run() {
		initializeValues();
		while (true) {
			updateValues();
			switch (this._angle) {
			case 0:
				sendCurrentValuesToDataCenterForFrontUS();
				break;
			case 90:
			case 270:
				sendCurrentValuesToDataCenterForSideUS();
				break;
			}
		}
	}

	private void initializeValues() {
		for (int i = 0; i < NUM_VALUES_TO_STORE; ++i) {
			this._usValues[i] = getUSReading();
		}
	}

	private void updateValues() {
		int distance = getUSReading();
		for (int i = 0; i < NUM_VALUES_TO_STORE - 1; ++i) {
			this._usValues[i] = this._usValues[i + 1];
		}
		this._usValues[NUM_VALUES_TO_STORE - 1] = distance;
	}

	private void sendCurrentValuesToDataCenterForFrontUS() {
		sendCurrentValuesToDataCenter();
	}

	private void sendCurrentValuesToDataCenterForSideUS() {
		if (this._dataCenter.isWallFollowing()) {
			if (!this._isPolling) {
				this._isPolling = true;
				initializeValues();
			}
			sendCurrentValuesToDataCenter();
		} else {
			this.US.off();
			this._isPolling = false;
		}
	}

	private void sendCurrentValuesToDataCenter() {
		this._dataCenter.setRawUSDistanceAtAngle(getCurrentRawValue(),
				this._angle);
		this._dataCenter.setFilteredUSDistanceAtAngle(
				getCurrentFilteredValue(), this._angle);
	}

	private int getCurrentRawValue() {
		return this._usValues[NUM_VALUES_TO_STORE - 1];
	}

	private int getCurrentFilteredValue() {
		int minimumDistance = 255;
		for (int i = 0; i < NUM_VALUES_TO_STORE; ++i) {
			if (this._usValues[i] < minimumDistance)
				minimumDistance = this._usValues[i];
		}
		return minimumDistance;
	}

	private int getUSReading() {
		this.US.ping();
		waitTimeRequiredForPolling();
		return this.US.getDistance();
	}
}
