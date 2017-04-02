package nxt.drivers;

import nxt.NXTConstants;

/**
 * A class to allow access to the motors controlling the robot's wheels. This
 * class is thread-safe.
 *
 * @author Andrei Purcarus
 *
 */
public final class MotorController {
	private static int MOTOR_ACCELERATION = 4000;

	static {
		NXTConstants.LEFT_MOTOR.setAcceleration(MOTOR_ACCELERATION);
		NXTConstants.RIGHT_MOTOR.setAcceleration(MOTOR_ACCELERATION);
	}

	/**
	 * Moves the robot forward at the given speed in cm/s.
	 */
	synchronized public static final void moveForward(double speed) {
		int leftMotorSpeed = Math.abs(convertLinearSpeedToMotorSpeed(speed,
				NXTConstants.LEFT_RADIUS));
		int rightMotorSpeed = Math.abs(convertLinearSpeedToMotorSpeed(speed,
				NXTConstants.RIGHT_RADIUS));
		setMotorSpeeds(leftMotorSpeed, rightMotorSpeed);
	}

	/**
	 * Moves the robot backward at the given speed in cm/s.
	 */
	synchronized public static final void moveBackward(double speed) {
		int leftMotorSpeed = -Math.abs(convertLinearSpeedToMotorSpeed(speed,
				NXTConstants.LEFT_RADIUS));
		int rightMotorSpeed = -Math.abs(convertLinearSpeedToMotorSpeed(speed,
				NXTConstants.RIGHT_RADIUS));
		setMotorSpeeds(leftMotorSpeed, rightMotorSpeed);
	}

	/**
	 * Turns the robot by the specified angle counterclockwise at the given
	 * speed in deg/s.
	 */
	synchronized public static final void turn(double angle, double speed) {
		double wheelDistance = getProperWheelDistanceForOnPointRotation(angle);
		turnOnPivot(angle, speed, wheelDistance / 2, wheelDistance / 2);
	}

	synchronized public static final void floatMotors() {
		NXTConstants.LEFT_MOTOR.flt();
		NXTConstants.RIGHT_MOTOR.flt();
	}

	/**
	 * Turns the robot by the specified angle counterclockwise at the given
	 * speed in deg/s. Does this by using the left wheel as a pivot.
	 */
	synchronized public static final void turnOnLeftWheel(double angle,
			double speed) {
		turnOnPivot(angle, speed, 0, NXTConstants.TURN_ON_LEFT_WHEEL_DISTANCE);
	}

	/**
	 * Turns the robot by the specified angle counterclockwise at the given
	 * speed in deg/s. Does this by using the right wheel as a pivot.
	 */
	synchronized public static final void turnOnRightWheel(double angle,
			double speed) {
		turnOnPivot(angle, speed, NXTConstants.TURN_ON_RIGHT_WHEEL_DISTANCE, 0);
	}

	/**
	 * Returns the number of degrees the left wheel has turned forward since the
	 * beginning. Not synchronized to allow read access to the tachometers while
	 * the robot is moving.
	 */
	public static final int getLeftTachoCount() {
		return NXTConstants.DIRECTION * NXTConstants.LEFT_MOTOR.getTachoCount();
	}

	/**
	 * Returns the number of degrees the right wheel has turned forward since
	 * the beginning. Not synchronized to allow read access to the tachometers
	 * while the robot is moving.
	 */
	public static final int getRightTachoCount() {
		return NXTConstants.DIRECTION
				* NXTConstants.RIGHT_MOTOR.getTachoCount();
	}

	private MotorController() {

	}

	/**
	 * Converts the given angle for the robot to the angle a wheel with the
	 * given radius and distance from a pivot would have to turn to make the
	 * robot turn by that angle.
	 */
	private static int convertAngleToMotorAngle(double angle,
			double radiusOfWheel, double distanceFromPivot) {
		return (int) (angle * distanceFromPivot / radiusOfWheel);
	}

	/**
	 * Converts the given speed in cm/s to the speed a wheel with the given
	 * radius would have to turn to make the robot move at that speed.
	 */
	private static int convertLinearSpeedToMotorSpeed(double speed,
			double radiusOfWheel) {
		return (int) Math.toDegrees(speed / radiusOfWheel);
	}

	/**
	 * Converts the given angular speed for the robot to the angular speed a
	 * wheel with the given radius and distance from a pivot would have to turn
	 * at to make the robot turn at that speed.
	 */
	private static int convertAngularSpeedToMotorSpeed(double speed,
			double radiusOfWheel, double distanceFromPivot) {
		return (int) (speed * distanceFromPivot / radiusOfWheel);
	}

	private static double getProperWheelDistanceForOnPointRotation(double angle) {
		if (angle >= 0)
			return NXTConstants.COUNTERCLOCKWISE_WHEEL_DISTANCE;
		else
			return NXTConstants.CLOCKWISE_WHEEL_DISTANCE;
	}

	/**
	 * Turns the robot by the specified angle counterclockwise at the given
	 * speed in deg/s, with the given distances from wheels to pivot.
	 */
	private static void turnOnPivot(double angle, double speed,
			double leftWheelToPivotDistance, double rightWheelToPivotDistance) {
		int leftMotorSpeed = convertAngularSpeedToMotorSpeed(speed,
				NXTConstants.LEFT_RADIUS, leftWheelToPivotDistance);
		int rightMotorSpeed = convertAngularSpeedToMotorSpeed(speed,
				NXTConstants.RIGHT_RADIUS, rightWheelToPivotDistance);

		NXTConstants.LEFT_MOTOR.setSpeed(leftMotorSpeed);
		NXTConstants.RIGHT_MOTOR.setSpeed(rightMotorSpeed);

		NXTConstants.LEFT_MOTOR.rotate(
				NXTConstants.DIRECTION
						* -convertAngleToMotorAngle(angle,
								NXTConstants.LEFT_RADIUS,
								leftWheelToPivotDistance), true);
		NXTConstants.RIGHT_MOTOR.rotate(
				NXTConstants.DIRECTION
						* convertAngleToMotorAngle(angle,
								NXTConstants.RIGHT_RADIUS,
								rightWheelToPivotDistance), true);

		while (NXTConstants.RIGHT_MOTOR.isMoving()
				|| NXTConstants.LEFT_MOTOR.isMoving()) {
			// Wait for both wheels to complete turning.
		}
	}

	/**
	 * Sets the given motor speeds and makes the motors start moving.
	 */
	private static void setMotorSpeeds(int leftMotorSpeed, int rightMotorSpeed) {
		setAbsoluteMotorSpeeds(leftMotorSpeed, rightMotorSpeed);
		setMotorDirections(leftMotorSpeed, rightMotorSpeed);
	}

	/**
	 * Sets the motor speeds but does not start the motors.
	 */
	private static void setAbsoluteMotorSpeeds(int leftMotorSpeed,
			int rightMotorSpeed) {
		NXTConstants.LEFT_MOTOR.setSpeed(Math.abs(leftMotorSpeed));
		NXTConstants.RIGHT_MOTOR.setSpeed(Math.abs(rightMotorSpeed));
	}

	/**
	 * Starts the motors in the directions given by the speeds, without changing
	 * the current speeds.
	 */
	private static void setMotorDirections(int leftMotorSpeed,
			int rightMotorSpeed) {
		if (leftMotorSpeed * NXTConstants.DIRECTION < 0)
			NXTConstants.LEFT_MOTOR.backward();
		else
			NXTConstants.LEFT_MOTOR.forward();

		if (rightMotorSpeed * NXTConstants.DIRECTION < 0)
			NXTConstants.RIGHT_MOTOR.backward();
		else
			NXTConstants.RIGHT_MOTOR.forward();
	}
}
