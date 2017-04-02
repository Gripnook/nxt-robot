package nxt.nav;

import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.data.Point;
import nxt.data.Position;
import nxt.data.Triple;
import nxt.util.AngleUtility;

/**
 * A class used to perform localization using the color sensor.
 *
 * @author Andrei Purcarus
 *
 */
public final class CSLocalization implements CSListener {
	private static final long WAIT_TIME_AFTER_LOCALIZING = 100;

	private static final double MAX_INITIAL_ANGLE_ERROR = 5;
	private static final double DESIRED_ANGLE_PRECISION = 0.5;

	private static final int NUM_LINES_TO_DETECT = 4;
	private static final int MAX_TRIES = 3;
	private static final long MIN_DELAY_BETWEEN_GRID_DETECTION = 100;
	private Position[] _gridLinePositions;
	private int _linesDetected;
	private long _lastPingTime;

	private final DataCenter _dataCenter;
	private final Navigation _navigation;

	public CSLocalization(DataCenter dataCenter, Navigation navigation) {
		this._dataCenter = dataCenter;
		this._navigation = navigation;
		this._gridLinePositions = new Position[NUM_LINES_TO_DETECT];
		this._linesDetected = 0;
		this._lastPingTime = System.currentTimeMillis();
	}

	public final void doLocalization() {
		doCSLocalization(new Point());
	}

	public final void doLocalization(Point gridLineIntersection) {
		doCSLocalization(gridLineIntersection);
	}

	@Override
	public final void ping() {
		long currentPingTime = System.currentTimeMillis();
		if (currentPingTime - this._lastPingTime >= MIN_DELAY_BETWEEN_GRID_DETECTION) {
			if (this._linesDetected < NUM_LINES_TO_DETECT) {
				addGridLine();
			} else {
				// Sets the value too high so no more data is collected.
				this._linesDetected = NUM_LINES_TO_DETECT + 1;
			}
			this._lastPingTime = currentPingTime;
		}
	}

	private void addGridLine() {
		this._gridLinePositions[this._linesDetected] = this._dataCenter
				.getPosition();
		this._gridLinePositions[this._linesDetected].orientation = AngleUtility
				.normalize(this._gridLinePositions[this._linesDetected].orientation
						+ NXTConstants.CS_ANGLE);
		++this._linesDetected;
	}

	private void doCSLocalization(Point gridLineIntersection) {
		setupLocalization(gridLineIntersection);
		if (!collectData())
			return;
		Triple<Double, Double, Double> angleDifferencesAndAngleError = getCorrectAngleDifferencesAndAngleError();
		double angleDifferenceBetweenXGridLines = angleDifferencesAndAngleError.first;
		double angleDifferenceBetweenYGridLines = angleDifferencesAndAngleError.second;
		double averageAngleError = angleDifferencesAndAngleError.third;
		correctPosition(gridLineIntersection, angleDifferenceBetweenXGridLines,
				angleDifferenceBetweenYGridLines, averageAngleError);
		refineLocalization(gridLineIntersection);
		waitForOtherThreadsToUpdate();
	}

	private void setupLocalization(Point gridLineIntersection) {
		this._navigation.travelTo(gridLineIntersection);
		this._navigation.turnTo(225 - NXTConstants.CS_ANGLE);
	}

	private boolean collectData() {
		int numberOfTries = 0;
		this._dataCenter.addCSListener(this);
		while (this._linesDetected != NUM_LINES_TO_DETECT) {
			this._navigation.turn(-360);
			if (this._linesDetected != NUM_LINES_TO_DETECT) {
				this._gridLinePositions = new Position[NUM_LINES_TO_DETECT];
				this._linesDetected = 0;
				++numberOfTries;
			}
			if (numberOfTries > MAX_TRIES) {
				// Gives up if it tries to localize too many times.
				this._dataCenter.removeCSListener(this);
				return false;
			}
		}
		this._dataCenter.removeCSListener(this);
		this._linesDetected = 0;
		return true;
	}

	/**
	 * Returns the correct angle differences between the detected x grid lines
	 * and the detected y grid lines, as well as the average error in the
	 * current orientation of the robot, in that order.
	 */
	private Triple<Double, Double, Double> getCorrectAngleDifferencesAndAngleError() {
		// Tries to perform correction using all 4 possible settings to account
		// for errors in measurement making the robot think its in one quadrant
		// when it is actually in another.
		// Only accepts corrections that change the orientation by less than
		// 45 degrees.
		int[] xModifiers = { 1, 1, -1, -1 };
		int[] yModifiers = { 1, -1, 1, -1 };
		for (int i = 0; i < 4; ++i) {
			double angleDifferenceBetweenXGridLines = xModifiers[i]
					* AngleUtility.difference(
							this._gridLinePositions[0].orientation,
							this._gridLinePositions[2].orientation);
			double angleDifferenceBetweenYGridLines = yModifiers[i]
					* AngleUtility.difference(
							this._gridLinePositions[1].orientation,
							this._gridLinePositions[3].orientation);
			double[] actualAngles = getActualAngles(
					angleDifferenceBetweenXGridLines,
					angleDifferenceBetweenYGridLines);
			double[] angleErrors = new double[NUM_LINES_TO_DETECT];
			for (int j = 0; j < NUM_LINES_TO_DETECT; ++j) {
				angleErrors[j] = actualAngles[j]
						- this._gridLinePositions[j].orientation;
			}
			double averageError = AngleUtility.average(angleErrors);
			if (AngleUtility.absoluteDifference(averageError, 0) <= 45) {
				Triple<Double, Double, Double> result = new Triple<>(
						angleDifferenceBetweenXGridLines,
						angleDifferenceBetweenYGridLines, averageError);
				return result;
			}
		}
		throw new RuntimeException("Not supposed to get here");
	}

	private double[] getActualAngles(double angleDifferenceBetweenXGridLines,
			double angleDifferenceBetweenYGridLines) {
		double[] actualAngles = new double[NUM_LINES_TO_DETECT];
		if (angleDifferenceBetweenXGridLines >= 0) {
			actualAngles[0] = 90.0 + angleDifferenceBetweenXGridLines / 2;
			actualAngles[2] = 90.0 - angleDifferenceBetweenXGridLines / 2;
		} else {
			actualAngles[0] = 270.0 + angleDifferenceBetweenXGridLines / 2;
			actualAngles[2] = 270.0 - angleDifferenceBetweenXGridLines / 2;
		}
		if (angleDifferenceBetweenYGridLines >= 0) {
			actualAngles[1] = angleDifferenceBetweenYGridLines / 2;
			actualAngles[3] = -angleDifferenceBetweenYGridLines / 2;
		} else {
			actualAngles[1] = 180.0 + angleDifferenceBetweenYGridLines / 2;
			actualAngles[3] = 180.0 - angleDifferenceBetweenYGridLines / 2;
		}
		return actualAngles;
	}

	private void correctPosition(Point gridLineIntersection,
			double angleDifferenceBetweenXGridLines,
			double angleDifferenceBetweenYGridLines, double averageAngleError) {
		Position actualPosition = new Position();
		if (angleDifferenceBetweenYGridLines >= 0.0)
			actualPosition.x = gridLineIntersection.x
					- NXTConstants.CS_DISTANCE
					* Math.cos(Math.toRadians(angleDifferenceBetweenYGridLines) / 2);
		else
			actualPosition.x = gridLineIntersection.x
					+ NXTConstants.CS_DISTANCE
					* Math.cos(Math.toRadians(angleDifferenceBetweenYGridLines) / 2);
		if (angleDifferenceBetweenXGridLines >= 0.0)
			actualPosition.y = gridLineIntersection.y
					- NXTConstants.CS_DISTANCE
					* Math.cos(Math.toRadians(angleDifferenceBetweenXGridLines) / 2);
		else
			actualPosition.y = gridLineIntersection.y
					+ NXTConstants.CS_DISTANCE
					* Math.cos(Math.toRadians(angleDifferenceBetweenXGridLines) / 2);

		double currentAngle = this._dataCenter.getOrientation();
		actualPosition.orientation = currentAngle + averageAngleError;

		this._dataCenter.setPosition(actualPosition);
	}

	private void refineLocalization(Point gridLineIntersection) {
		this._navigation.travelTo(gridLineIntersection);
		this._navigation.travelToPrecisely(gridLineIntersection);
		this._navigation.turnTo(270 - NXTConstants.CS_ANGLE
				- MAX_INITIAL_ANGLE_ERROR);
		while (!this._dataCenter.isCSOnGridLine()) {
			this._navigation.turn(DESIRED_ANGLE_PRECISION);
		}
		double minimumAngle = this._dataCenter.getOrientation();
		while (this._dataCenter.isCSOnGridLine()) {
			this._navigation.turn(DESIRED_ANGLE_PRECISION);
		}
		double maximumAngle = this._dataCenter.getOrientation();
		double averageAngle = AngleUtility.average(new double[] { minimumAngle,
				maximumAngle });
		this._dataCenter.setOrientation(maximumAngle + 270 - averageAngle
				- NXTConstants.CS_ANGLE);
	}

	private static void waitForOtherThreadsToUpdate() {
		try {
			Thread.sleep(WAIT_TIME_AFTER_LOCALIZING);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
