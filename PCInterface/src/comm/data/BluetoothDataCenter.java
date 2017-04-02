package comm.data;

import comm.util.AngleUtility;

/**
 * A class to provide a centralized, synchronized location to store the data
 * collected by the bluetooth communication with the robot.
 * 
 * @author Andrei Purcarus
 *
 */
public final class BluetoothDataCenter {
	private final Object _odometerLock;
	private Position _position;

	private final Object _usDataLock;
	private static final int NUM_DISTANCES_TO_STORE = 3;
	private int[] _angleToUSDistance;

	private final Object _csValueLock;
	private int _csValue;

	public BluetoothDataCenter() {
		this._odometerLock = new Object();
		this._position = new Position();

		this._usDataLock = new Object();
		this._angleToUSDistance = new int[NUM_DISTANCES_TO_STORE];
		for (int i = 0; i < NUM_DISTANCES_TO_STORE; ++i) {
			this._angleToUSDistance[i] = 255;
		}
		this._csValueLock = new Object();
		this._csValue = 0;
	}

	public final void setPosition(double x, double y, double orientation) {
		synchronized (this._odometerLock) {
			this._position.x = x;
			this._position.y = y;
			this._position.orientation = AngleUtility.normalize(orientation);
		}
	}

	public final void setPosition(Position position) {
		synchronized (this._odometerLock) {
			this._position.x = position.x;
			this._position.y = position.y;
			this._position.orientation = AngleUtility
					.normalize(position.orientation);
		}
	}

	public final Position getPosition() {
		synchronized (this._odometerLock) {
			return new Position(this._position);
		}
	}

	public final void setXPosition(double x) {
		synchronized (this._odometerLock) {
			this._position.x = x;
		}
	}

	public final double getXPosition() {
		synchronized (this._odometerLock) {
			return this._position.x;
		}
	}

	public final void setYPosition(double y) {
		synchronized (this._odometerLock) {
			this._position.y = y;
		}
	}

	public final double getYPosition() {
		synchronized (this._odometerLock) {
			return this._position.y;
		}
	}

	public final void setOrientation(double orientation) {
		synchronized (this._odometerLock) {
			this._position.orientation = AngleUtility.normalize(orientation);
		}
	}

	public final double getOrientation() {
		synchronized (this._odometerLock) {
			return this._position.orientation;
		}
	}

	/**
	 * Angle is taken relative to the orientation of the robot, with 0 degrees
	 * being the robot's forward orientation and angles increasing
	 * counterclockwise.
	 */
	public final void setUSDistanceAtAngle(int distance, int angle) {
		synchronized (this._usDataLock) {
			this._angleToUSDistance[indexOf(angle)] = distance;
		}
	}

	/**
	 * Angle is taken relative to the orientation of the robot, with 0 degrees
	 * being the robot's forward orientation and angles increasing
	 * counterclockwise.
	 */
	public final int getUSDistanceAtAngle(int angle) {
		synchronized (this._usDataLock) {
			return this._angleToUSDistance[indexOf(angle)];
		}
	}

	/**
	 * Gets the index of the specified angle in the _angleToUSDistance array.
	 */
	private int indexOf(int angle) {
		switch (AngleUtility.normalize(angle)) {
		case 90:
			return 0;
		case 0:
			return 1;
		case 270:
			return 2;
		default:
			throw new IllegalArgumentException("Invalid angle");
		}
	}

	public final void setCSValue(int csValue) {
		synchronized (this._csValueLock) {
			this._csValue = csValue;
		}
	}

	public final int getCSValue() {
		synchronized (this._csValueLock) {
			return this._csValue;
		}
	}
}
