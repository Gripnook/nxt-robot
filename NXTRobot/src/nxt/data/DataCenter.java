package nxt.data;

import java.util.ArrayList;
import java.util.List;

import nxt.drivers.CSPoller;
import nxt.nav.CSListener;
import nxt.util.AngleUtility;

/**
 * A class to provide a centralized, synchronized location to store the data
 * collected by the various parts of the robot and to enable efficient
 * communication between threads.
 *
 * @author Andrei Purcarus
 *
 */
public final class DataCenter {
	private final Object _odometerLock;
	private Position _position;

	private final Object _usDataLock;
	private static final int NUM_DISTANCES_TO_STORE = 3;
	private int[] _angleToRawUSDistance;
	private int[] _angleToFilteredUSDistance;
	private boolean _isWallFollowing;

	private final Object _csListenerLock;
	private List<CSListener> _csListeners;

	private final Object _csValueLock;
	private int _csValue;

	public DataCenter() {
		this._odometerLock = new Object();
		this._position = new Position();

		this._usDataLock = new Object();
		this._angleToRawUSDistance = new int[NUM_DISTANCES_TO_STORE];
		this._angleToFilteredUSDistance = new int[NUM_DISTANCES_TO_STORE];
		for (int i = 0; i < NUM_DISTANCES_TO_STORE; ++i) {
			this._angleToRawUSDistance[i] = 255;
			this._angleToFilteredUSDistance[i] = 255;
		}
		this._isWallFollowing = false;

		this._csListenerLock = new Object();
		this._csListeners = new ArrayList<>();

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
	public final void setRawUSDistanceAtAngle(int distance, int angle) {
		synchronized (this._usDataLock) {
			this._angleToRawUSDistance[getIndexOf(angle)] = distance;
		}
	}

	/**
	 * Angle is taken relative to the orientation of the robot, with 0 degrees
	 * being the robot's forward orientation and angles increasing
	 * counterclockwise.
	 */
	public final int getRawUSDistanceAtAngle(int angle) {
		synchronized (this._usDataLock) {
			return this._angleToRawUSDistance[getIndexOf(angle)];
		}
	}

	/**
	 * Angle is taken relative to the orientation of the robot, with 0 degrees
	 * being the robot's forward orientation and angles increasing
	 * counterclockwise.
	 */
	public final void setFilteredUSDistanceAtAngle(int distance, int angle) {
		synchronized (this._usDataLock) {
			this._angleToFilteredUSDistance[getIndexOf(angle)] = distance;
		}
	}

	/**
	 * Angle is taken relative to the orientation of the robot, with 0 degrees
	 * being the robot's forward orientation and angles increasing
	 * counterclockwise.
	 */
	public final int getFilteredUSDistanceAtAngle(int angle) {
		synchronized (this._usDataLock) {
			return this._angleToFilteredUSDistance[getIndexOf(angle)];
		}
	}

	/**
	 * Gets the index of the specified angle in the _angleToUSDistance array.
	 */
	private int getIndexOf(int angle) {
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

	public final void setWallFollowing(boolean wallFollow) {
		synchronized (this._usDataLock) {
			this._isWallFollowing = wallFollow;
		}
	}

	public final boolean isWallFollowing() {
		synchronized (this._usDataLock) {
			return this._isWallFollowing;
		}
	}

	public final void addCSListener(CSListener csListener) {
		synchronized (this._csListenerLock) {
			if (!this._csListeners.contains(csListener))
				;
			this._csListeners.add(csListener);
		}
	}

	public final void removeCSListener(CSListener csListener) {
		synchronized (this._csListenerLock) {
			this._csListeners.remove(csListener);
		}
	}

	public final void notifyAllCSListeners() {
		synchronized (this._csListenerLock) {
			/*
			 * Notifies the listeners in a separate thread to not interrupt the
			 * execution of the calling thread.
			 */
			(new Thread() {
				@Override
				public void run() {
					synchronized (DataCenter.this._csListenerLock) {
						for (CSListener csListener : DataCenter.this._csListeners) {
							csListener.ping();
						}
					}
				}
			}).start();
		}
	}

	public final boolean isCSOnGridLine() {
		return CSPoller.isOnGridLine(this._csValue);
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
