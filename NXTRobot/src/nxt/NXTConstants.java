package nxt;

import lejos.nxt.ColorSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * A class for storing all the sensors, motors and constants defining the NXT
 * hardware.
 *
 * @author Andrei Purcarus
 *
 */
public final class NXTConstants {
	public static final double LEFT_RADIUS;
	public static final double RIGHT_RADIUS;
	public static final double COUNTERCLOCKWISE_WHEEL_DISTANCE;
	public static final double CLOCKWISE_WHEEL_DISTANCE;
	public static final double TURN_ON_LEFT_WHEEL_DISTANCE;
	public static final double TURN_ON_RIGHT_WHEEL_DISTANCE;
	public static final double AVERAGE_WHEEL_DISTANCE;

	public static final NXTRegulatedMotor LEFT_MOTOR;
	public static final NXTRegulatedMotor RIGHT_MOTOR;
	public static final int DIRECTION;

	public static final NXTRegulatedMotor LAUNCHER_MOTOR;
	public static final double LAUNCH_X_MIN;
	public static final double LAUNCH_X_MAX;
	public static final double LAUNCH_Y;
	public static final double LAUNCH_DISTANCE_MIN;
	public static final double LAUNCH_DISTANCE_MAX;
	public static final double LAUNCH_ANGLE;

	public static final SensorPort LEFT_US_PORT;
	public static final UltrasonicSensor LEFT_US;
	public static final double LEFT_US_DISTANCE;
	public static final SensorPort FRONT_US_PORT;
	public static final UltrasonicSensor FRONT_US;
	public static final double FRONT_US_DISTANCE;
	public static final SensorPort RIGHT_US_PORT;
	public static final UltrasonicSensor RIGHT_US;
	public static final double RIGHT_US_DISTANCE;

	public static final SensorPort CS_PORT;
	public static final ColorSensor CS;
	public static final double CS_X;
	public static final double CS_Y;
	public static final double CS_DISTANCE;
	public static final double CS_ANGLE;

	public static final double ROBOT_X_MIN;
	public static final double ROBOT_X_MAX;
	public static final double ROBOT_Y_MIN;
	public static final double ROBOT_Y_MAX;
	public static final double ROBOT_RADIUS_OF_ROTATION;
	public static final double ROBOT_FRONT_RADIUS_OF_ROTATION;

	public static final double TILE_DISTANCE;

	/**
	 * The distance between a wheel and the nearest wall when traveling in the
	 * center of a tile with walls on each side.
	 */
	public static final double WHEEL_TO_WALL_DISTANCE;

	static {
		LEFT_RADIUS = 2.072;
		RIGHT_RADIUS = 2.055;
		COUNTERCLOCKWISE_WHEEL_DISTANCE = 16.55;
		CLOCKWISE_WHEEL_DISTANCE = 16.565;
		TURN_ON_LEFT_WHEEL_DISTANCE = 16.885;
		TURN_ON_RIGHT_WHEEL_DISTANCE = 16.885;
		AVERAGE_WHEEL_DISTANCE = (COUNTERCLOCKWISE_WHEEL_DISTANCE + CLOCKWISE_WHEEL_DISTANCE) / 2;

		LEFT_MOTOR = Motor.A;
		RIGHT_MOTOR = Motor.C;
		DIRECTION = 1;

		LAUNCHER_MOTOR = Motor.B;
		LAUNCH_X_MIN = 155;
		LAUNCH_X_MAX = 160;
		LAUNCH_Y = 0.0;
		LAUNCH_DISTANCE_MIN = Math.sqrt(LAUNCH_X_MIN * LAUNCH_X_MIN + LAUNCH_Y
				* LAUNCH_Y);
		LAUNCH_DISTANCE_MAX = Math.sqrt(LAUNCH_X_MAX * LAUNCH_X_MAX + LAUNCH_Y
				* LAUNCH_Y);
		LAUNCH_ANGLE = Math.toDegrees(Math.atan2(LAUNCH_Y, LAUNCH_X_MIN));

		LEFT_US_PORT = SensorPort.S3;
		LEFT_US = new UltrasonicSensor(LEFT_US_PORT);
		LEFT_US_DISTANCE = 10;
		FRONT_US_PORT = SensorPort.S1;
		FRONT_US = new UltrasonicSensor(FRONT_US_PORT);
		FRONT_US_DISTANCE = 4;
		RIGHT_US_PORT = SensorPort.S4;
		RIGHT_US = new UltrasonicSensor(RIGHT_US_PORT);
		RIGHT_US_DISTANCE = 6;

		CS_PORT = SensorPort.S2;
		CS = new ColorSensor(CS_PORT);
		CS_Y = 0.3;
		CS_X = -11.5;
		CS_DISTANCE = Math.sqrt(CS_X * CS_X + CS_Y * CS_Y);
		CS_ANGLE = Math.toDegrees(Math.atan2(CS_Y, CS_X));

		ROBOT_X_MIN = -16;
		ROBOT_X_MAX = 6.5;
		ROBOT_Y_MIN = -11;
		ROBOT_Y_MAX = 11;
		double maximumXDistance = Math.max(Math.abs(ROBOT_X_MIN),
				Math.abs(ROBOT_X_MAX));
		double maximumYDistance = Math.max(Math.abs(ROBOT_Y_MIN),
				Math.abs(ROBOT_Y_MAX));
		ROBOT_RADIUS_OF_ROTATION = Math.sqrt(maximumXDistance
				* maximumXDistance + maximumYDistance * maximumYDistance);
		ROBOT_FRONT_RADIUS_OF_ROTATION = Math.sqrt(ROBOT_X_MAX * ROBOT_X_MAX
				+ maximumYDistance * maximumYDistance);

		TILE_DISTANCE = 30.48;

		WHEEL_TO_WALL_DISTANCE = 0.5 * (TILE_DISTANCE - AVERAGE_WHEEL_DISTANCE);
	}

	public static final double distanceOfLaunch(int ballNumber) {
		return LAUNCH_DISTANCE_MIN + ballNumber
				* (LAUNCH_DISTANCE_MAX - LAUNCH_DISTANCE_MIN) / 6;
	}

	private NXTConstants() {

	}
}
