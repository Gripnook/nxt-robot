package nxt.nav;

import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.data.Position;
import nxt.drivers.MotorController;

/**
 * A class which keeps track of and updates the position of the robot.
 *
 * @author Andrei Purcarus
 *
 */
public final class Odometer extends Thread {
	private static final long ODOMETER_PERIOD = 50;

	private final DataCenter _dataCenter;

	private int _previousLeftTacho = 0;
	private int _previousRightTacho = 0;

	public Odometer(DataCenter dataCenter) {
		this._dataCenter = dataCenter;
	}

	@Override
	public final void run() {
		long updateStartTime, updateEndTime;
		while (true) {
			updateStartTime = System.currentTimeMillis();
			update();
			updateEndTime = System.currentTimeMillis();
			long timeElapsedSinceLastUpdate = updateEndTime - updateStartTime;
			waitUntilPeriodIsComplete(timeElapsedSinceLastUpdate);
		}
	}

	private void update() {
		int currentLeftTacho = MotorController.getLeftTachoCount();
		int currentRightTacho = MotorController.getRightTachoCount();

		int leftTachoDifference = currentLeftTacho - this._previousLeftTacho;
		int rightTachoDifference = currentRightTacho - this._previousRightTacho;

		double leftDistanceTraveled = convertWheelAngleToDistance(
				leftTachoDifference, NXTConstants.LEFT_RADIUS);
		double rightDistanceTraveled = convertWheelAngleToDistance(
				rightTachoDifference, NXTConstants.RIGHT_RADIUS);

		this._previousLeftTacho = currentLeftTacho;
		this._previousRightTacho = currentRightTacho;

		double correctWheelDistance = getCorrectWheelDistance(
				leftDistanceTraveled, rightDistanceTraveled);
		double angleChange = convertWheelDistancesToAngle(leftDistanceTraveled,
				rightDistanceTraveled, correctWheelDistance);
		double averageDistanceTravelled = (leftDistanceTraveled + rightDistanceTraveled) / 2;

		updatePosition(averageDistanceTravelled, angleChange);
	}

	private void updatePosition(double averageDistanceTravelled,
			double angleChange) {
		Thread.currentThread().setPriority(MAX_PRIORITY);
		Position position = this._dataCenter.getPosition();
		double orientationOfDistanceChange = Math
				.toRadians(position.orientation + angleChange / 2);

		position.x += averageDistanceTravelled
				* Math.cos(orientationOfDistanceChange);
		position.y += averageDistanceTravelled
				* Math.sin(orientationOfDistanceChange);
		position.orientation += angleChange;

		this._dataCenter.setPosition(position);
		Thread.currentThread().setPriority(NORM_PRIORITY);
	}

	private static double getCorrectWheelDistance(double leftDistanceTraveled,
			double rightDistanceTraveled) {
		double differenceInDistancesTraveled = rightDistanceTraveled
				- leftDistanceTraveled;
		if (leftDistanceTraveled == 0)
			return NXTConstants.TURN_ON_LEFT_WHEEL_DISTANCE;
		else if (rightDistanceTraveled == 0)
			return NXTConstants.TURN_ON_RIGHT_WHEEL_DISTANCE;
		else if (differenceInDistancesTraveled >= 0)
			return NXTConstants.COUNTERCLOCKWISE_WHEEL_DISTANCE;
		else
			return NXTConstants.CLOCKWISE_WHEEL_DISTANCE;
	}

	/**
	 * Converts the angle traveled by a wheel to the distance traveled by the
	 * wheel.
	 */
	private static double convertWheelAngleToDistance(double angle,
			double radiusOfWheel) {
		return Math.toRadians(angle) * radiusOfWheel;
	}

	/**
	 * Converts the distances traveled by two wheels to the angle by which the
	 * robot has rotated.
	 */
	private static double convertWheelDistancesToAngle(
			double leftDistanceTraveled, double rightDistanceTraveled,
			double wheelDistance) {
		double differenceInDistancesTraveled = rightDistanceTraveled
				- leftDistanceTraveled;
		return Math.toDegrees(differenceInDistancesTraveled / wheelDistance);
	}

	private static void waitUntilPeriodIsComplete(
			long timeElapsedSinceLastPeriod) {
		if (timeElapsedSinceLastPeriod < ODOMETER_PERIOD) {
			try {
				Thread.sleep(ODOMETER_PERIOD - timeElapsedSinceLastPeriod);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
