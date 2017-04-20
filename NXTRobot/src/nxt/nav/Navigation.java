package nxt.nav;

import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.data.Point;
import nxt.data.Position;
import nxt.data.Vector;
import nxt.drivers.MotorController;
import nxt.nav.path.Path;
import nxt.nav.path.PathItem;
import nxt.util.AngleUtility;

/**
 * A class used to navigate the robot on the field.
 *
 * @author Andrei Purcarus
 *
 */
public final class Navigation {
	private static final double PRECISION_SPEED = 1;
	private static final double LOW_SPEED = 5;
	private static final double HIGH_SPEED = 15;

	private static final double TURN_SPEED = 60;

	private static final double MAX_PRECISION_POSITION_ERROR = 0.1;
	private static final double MAX_POSITION_ERROR = 1.0;
	private static final double MAX_ORIENTATION_ERROR = 3.0;

	private final DataCenter _dataCenter;

	public Navigation(DataCenter dataCenter) {
		this._dataCenter = dataCenter;
	}

	public final void travel(Point[] path) {
		for (Point point : path) {
			travelTo(point);
		}
	}

	public final void travelForward(Path path) {
		for (PathItem pathItem : path.getItems()) {
			pathItem.traverseForward(this);
		}
	}

	public final void travelBackward(Path path) {
		for (int i = path.getItems().size() - 1; i >= 0; --i) {
			path.getItems().get(i).traverseBackward(this);
		}
	}

	public final void moveForward(double distance) {
		Position position = this._dataCenter.getPosition();
		Point destination = new Point(position.x + distance
				* Math.cos(Math.toRadians(position.orientation)), position.y
				+ distance * Math.sin(Math.toRadians(position.orientation)));
		travelTo(destination);
	}

	public final void moveBackward(double distance) {
		Position position = this._dataCenter.getPosition();
		Point destination = new Point(position.x - distance
				* Math.cos(Math.toRadians(position.orientation)), position.y
				- distance * Math.sin(Math.toRadians(position.orientation)));
		travelToBackward(destination);
	}

	public final void travelTo(Point destination) {
		turnTo(computeAngleToDestination(destination));
		while (isTooFarFromDestination(destination)) {
			setSpeedProportionallyToDistanceFromDestination(destination);
			if (!isHeadingTheRightWay(destination))
				turnTo(computeAngleToDestination(destination));
		}
		MotorController.floatMotors();
	}

	public final void travelToBackward(Point destination) {
		turnTo(computeAngleToDestination(destination) + 180);
		while (isTooFarFromDestination(destination)) {
			setSpeedProportionallyToDistanceFromDestinationBackwards(destination);
			if (!isHeadingTheRightWayBackwards(destination))
				turnTo(computeAngleToDestination(destination) + 180);
		}
		MotorController.floatMotors();
	}

	public final void travelToPrecisely(Point destination) {
		turnTo(computeAngleToDestination(destination));
		while (isTooFarFromPreciseDestination(destination)) {
			MotorController.moveForward(PRECISION_SPEED);
			if (!isHeadingTheRightWay(destination))
				turnTo(computeAngleToDestination(destination));
		}
		MotorController.floatMotors();
	}

	public final void travelInTunnel(Point destination, Direction direction,
			double centerOfPath, boolean leftWall, boolean rightWall) {
		USCorrection tunnelCorrection = new USCorrection(direction,
				centerOfPath, this._dataCenter, leftWall, rightWall);
		tunnelCorrection.start();
		tunnelCorrection.correctHeading(this);
		tunnelCorrection.update();
		turnTo(computeAngleToDestination(destination));
		while (isTooFarFromDestination(destination)) {
			setSpeedProportionallyToDistanceFromDestination(destination);
			if (!isHeadingTheRightWay(destination))
				turnTo(computeAngleToDestination(destination));
			tunnelCorrection.update();
		}
		tunnelCorrection.end();

		MotorController.floatMotors();
	}

	public final void turn(double angle) {
		turn(angle, TURN_SPEED);
	}

	public final void turn(double angle, double speed) {
		MotorController.turn(angle, speed);
	}

	public final void turnTo(double angle) {
		turnTo(angle, TURN_SPEED);
	}

	public final void turnTo(double angle, double speed) {
		MotorController.turn(
				AngleUtility.difference(angle,
						this._dataCenter.getOrientation()), speed);
	}

	public final void turnLeft() {
		turn(45);
		moveForward(NXTConstants.COUNTERCLOCKWISE_WHEEL_DISTANCE / 2 * 1.414);
		turn(45);
	}

	public final void turnRight() {
		turn(-45);
		moveForward(NXTConstants.CLOCKWISE_WHEEL_DISTANCE / 2 * 1.414);
		turn(-45);
	}

	public final void floatMotors() {
		MotorController.floatMotors();
	}

	private double computeAngleToDestination(Point destination) {
		Point current = this._dataCenter.getPosition();
		Vector vector = new Vector(current, destination);
		return vector.direction();
	}

	private boolean isTooFarFromDestination(Point destination) {
		Point current = this._dataCenter.getPosition();
		Vector vector = new Vector(current, destination);
		return Math.abs(vector.x) > MAX_POSITION_ERROR
				|| Math.abs(vector.y) > MAX_POSITION_ERROR;
	}

	private boolean isTooFarFromPreciseDestination(Point destination) {
		Point current = this._dataCenter.getPosition();
		Vector vector = new Vector(current, destination);
		return Math.abs(vector.x) > MAX_PRECISION_POSITION_ERROR
				|| Math.abs(vector.y) > MAX_PRECISION_POSITION_ERROR;
	}

	private void setSpeedProportionallyToDistanceFromDestination(
			Point destination) {
		Point current = this._dataCenter.getPosition();
		Vector vector = new Vector(current, destination);
		double distanceFromDestination = vector.norm();
		double cutoffDistance = NXTConstants.TILE_DISTANCE;
		if (distanceFromDestination > cutoffDistance)
			distanceFromDestination = cutoffDistance;
		double speed = LOW_SPEED + (distanceFromDestination / cutoffDistance)
				* (HIGH_SPEED - LOW_SPEED);
		MotorController.moveForward(speed);
	}

	private void setSpeedProportionallyToDistanceFromDestinationBackwards(
			Point destination) {
		Point current = this._dataCenter.getPosition();
		Vector vector = new Vector(current, destination);
		double distanceFromDestination = vector.norm();
		double cutoffDistance = NXTConstants.TILE_DISTANCE;
		if (distanceFromDestination > cutoffDistance)
			distanceFromDestination = cutoffDistance;
		double speed = LOW_SPEED + (distanceFromDestination / cutoffDistance)
				* (HIGH_SPEED - LOW_SPEED);
		MotorController.moveBackward(speed);
	}

	private boolean isHeadingTheRightWay(Point destination) {
		double angleToDestination = computeAngleToDestination(destination);
		double orientationError = AngleUtility.absoluteDifference(
				angleToDestination, this._dataCenter.getOrientation());
		return orientationError <= MAX_ORIENTATION_ERROR;
	}

	private boolean isHeadingTheRightWayBackwards(Point destination) {
		double angleToDestination = computeAngleToDestination(destination);
		double orientationError = AngleUtility.absoluteDifference(
				angleToDestination, this._dataCenter.getOrientation() + 180);
		return orientationError <= MAX_ORIENTATION_ERROR;
	}
}
