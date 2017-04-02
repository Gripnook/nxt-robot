package nxt;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import nxt.data.DataCenter;
import nxt.data.Point;
import nxt.data.Position;
import nxt.data.Tile;
import nxt.data.Vector;
import nxt.drivers.CSPoller;
import nxt.drivers.Launcher;
import nxt.drivers.USPoller;
import nxt.nav.CSLocalization;
import nxt.nav.Direction;
import nxt.nav.Navigation;
import nxt.nav.Odometer;
import nxt.nav.CSCorrection;
import nxt.nav.USLocalization;
import nxt.nav.path.Path;
import nxt.nav.path.PathGenerator;
import nxt.util.AngleUtility;
import nxt.comm.BluetoothBroadcaster;

/**
 * A class to provide an interface for the operation of an NXT robot with three
 * ultrasonic sensors positioned in front and to the sides of the robot, one
 * color sensor aimed at the ground, and a launcher whose operation is specified
 * by the Launcher class.
 *
 * @author Andrei Purcarus
 *
 */
public class NXT extends Thread {
	private static final double LAUNCH_AREA_X_MIN = convertGridToCentimeters(8);
	private static final double LAUNCH_AREA_X_MAX = convertGridToCentimeters(11);
	private static final double LAUNCH_AREA_Y_MIN = convertGridToCentimeters(8);
	private static final double LAUNCH_AREA_Y_MAX = convertGridToCentimeters(11);

	private DataCenter _dataCenter;
	private Odometer _odometer;
	private CSCorrection _odometerCorrection;
	private Navigation _navigation;
	private USPoller _leftUS;
	private USPoller _frontUS;
	private USPoller _rightUS;
	private CSPoller _cs;
	private USLocalization _usLocalization;
	private CSLocalization _csLocalization;
	private BluetoothBroadcaster _bluetooth;

	private int _ballToFire = 1;

	public NXT() {
		this._dataCenter = new DataCenter();
		this._odometer = new Odometer(this._dataCenter);
		this._odometerCorrection = new CSCorrection(this._dataCenter);
		this._navigation = new Navigation(this._dataCenter);
		this._leftUS = new USPoller(90, this._dataCenter);
		this._frontUS = new USPoller(0, this._dataCenter);
		this._rightUS = new USPoller(-90, this._dataCenter);
		this._cs = new CSPoller(this._dataCenter);
		this._usLocalization = new USLocalization(this._dataCenter,
				this._navigation);
		this._csLocalization = new CSLocalization(this._dataCenter,
				this._navigation);
	}

	@Override
	public void run() {
		throw new IllegalStateException("Method run not overridden");
	}

	/**
	 * Moves forward 5 tiles.
	 */
	protected final void testRadii() {
		this._odometer.start();
		this._navigation.moveForward(convertGridToCentimeters(5));
		displayOdometer();
		System.exit(0);
	}

	/**
	 * Turns 3 * 360 degrees counterclockwise on its center of rotation.
	 */
	protected final void testCounterclockwiseCentralRotation() {
		this._odometer.start();
		this._navigation.turn(3 * 360);
		displayOdometer();
		System.exit(0);
	}

	/**
	 * Turns 3 * 360 degrees clockwise on its center of rotation.
	 */
	protected final void testClockwiseCentralRotation() {
		this._odometer.start();
		this._navigation.turn(3 * -360);
		displayOdometer();
		System.exit(0);
	}

	/**
	 * Localizes with US and goes to (0, 0) facing 90 degrees.
	 */
	protected final void testUSLocalization() {
		this._odometer.start();
		this._frontUS.start();
		this._bluetooth = new BluetoothBroadcaster(this._dataCenter);
		this._bluetooth.start();
		this._usLocalization.doLocalization();
		this._navigation.travelTo(new Point());
		this._navigation.turnTo(90);
		System.exit(0);
	}

	/**
	 * Continuously displays the color sensor readings on the computer.
	 */
	protected final void testColorSensor() {
		this._cs.start();
		this._leftUS.start();
		this._rightUS.start();
		this._frontUS.start();
		this._dataCenter.setWallFollowing(true);
		this._navigation.floatMotors();
		this._bluetooth = new BluetoothBroadcaster(this._dataCenter);
		this._bluetooth.start();
		while (true) {
			// Keep getting data.
		}
	}

	/**
	 * Localizes with CS and goes to (0, 0) facing 90 degrees. Robot must start
	 * off roughly at the origin facing 0 degrees.
	 */
	protected final void testCSLocalization() {
		this._odometer.start();
		this._cs.start();
		this._bluetooth = new BluetoothBroadcaster(this._dataCenter);
		this._bluetooth.start();
		this._csLocalization.doLocalization();
		this._navigation.travelTo(new Point());
		this._navigation.turnTo(90);
		System.exit(0);
	}

	/**
	 * Fires 6 balls on any button press except for escape, which ends it.
	 */
	protected final void testLauncher() {
		while (true) {
			int buttonChoice = Button.waitForAnyPress();
			switch (buttonChoice) {
			case Button.ID_ENTER:
			case Button.ID_LEFT:
			case Button.ID_RIGHT:
				break;
			case Button.ID_ESCAPE:
				System.exit(0);
			}
			Launcher.fire(6);
		}
	}

	/**
	 * Travels along the path with odometer correction enabled.
	 */
	protected final void testNavigation(Point[] path) {
		this._odometer.start();
		this._frontUS.start();
		this._cs.start();
		this._usLocalization.doLocalization();
		this._csLocalization.doLocalization();
		this._odometerCorrection.start();
		this._navigation.travel(convertGridToCentimeters(path));
		this._navigation.turnTo(90);
		this._odometerCorrection.stop();
		System.exit(0);
	}

	/**
	 * Localizes with US and CS, then positions itself to fire at the target and
	 * fires 6 shots.
	 */
	protected final void testLaunchPositioning(Point target1, Point target2) {
		this._odometer.start();
		this._frontUS.start();
		this._cs.start();
		this._usLocalization.doLocalization();
		this._csLocalization.doLocalization();
		int ballsToFirePerTarget = 3;
		if (turnToLaunch(convertGridToCentimeters(target1))) {
			Launcher.fire(ballsToFirePerTarget);
			this._ballToFire += 3;
			returnToDestinationInLaunchArea(new Point());
		}
		if (turnToLaunch(convertGridToCentimeters(target2))) {
			Launcher.fire(ballsToFirePerTarget);
			this._ballToFire += 3;
			returnToDestinationInLaunchArea(new Point());
		}
		System.exit(0);
	}

	protected final void testBetaDemo() {
		this._odometer.start();
		this._leftUS.start();
		this._frontUS.start();
		this._rightUS.start();
		this._cs.start();

		Point p1 = new Point(convertGridToCentimeters(-0.5),
				convertGridToCentimeters(2.5));
		Point p2_alpha = new Point(convertGridToCentimeters(-0.5),
				convertGridToCentimeters(3.2));
		Point p3 = new Point(convertGridToCentimeters(-0.5),
				convertGridToCentimeters(4.8));
		Point p4_tr = new Point(convertGridToCentimeters(-0.5),
				convertGridToCentimeters(5.2));
		Point p5_alpha = new Point(convertGridToCentimeters(0.2),
				convertGridToCentimeters(5.5));
		Point p6 = new Point(convertGridToCentimeters(0.8),
				convertGridToCentimeters(5.5));
		Point p7_tl = new Point(convertGridToCentimeters(1.2),
				convertGridToCentimeters(5.5));
		Point p8_tr = new Point(convertGridToCentimeters(1.5),
				convertGridToCentimeters(6.2));
		Point p9_alpha = new Point(convertGridToCentimeters(2.2),
				convertGridToCentimeters(6.5));
		Point p10 = new Point(convertGridToCentimeters(2.8),
				convertGridToCentimeters(6.5));
		Point p11 = new Point(convertGridToCentimeters(4.5),
				convertGridToCentimeters(6.25));
		Point p12_destination = new Point(convertGridToCentimeters(6),
				convertGridToCentimeters(6));
		Point target = new Point(convertGridToCentimeters(9),
				convertGridToCentimeters(9));

		this._usLocalization.doLocalization();
		this._csLocalization.doLocalization();
		// Indicates that the localization is finished.
		Sound.twoBeeps();

		this._odometerCorrection.start();
		this._navigation.travelTo(p1);
		this._navigation.travelTo(p2_alpha);
		this._navigation.travelInTunnel(p3, Direction.north, p3.x, true, true);
		this._navigation.travelTo(p4_tr);
		this._navigation.turnTo(90);
		this._navigation.turnRight();
		this._navigation.travelTo(p5_alpha);
		this._navigation.travelInTunnel(p6, Direction.east, p6.y, true, true);
		this._navigation.travelTo(p7_tl);
		this._navigation.turnTo(0);
		this._navigation.turnLeft();
		this._navigation.travelTo(p8_tr);
		this._navigation.turnTo(90);
		this._navigation.turnRight();
		this._navigation.travelTo(p9_alpha);
		this._navigation.travelInTunnel(p10, Direction.east, p10.y, true, true);
		this._navigation.travelTo(p11);
		this._navigation.travelTo(p12_destination);
		this._odometerCorrection.stop();

		this._csLocalization.doLocalization(p12_destination);

		this._odometerCorrection.start();
		turnToLaunch(p12_destination, target);
		this._odometerCorrection.stop();
		Launcher.fire();

		System.exit(0);
	}

	protected final void finalCompetition(Tile[] obstacles, Point firstTarget,
			Point secondTarget) {
		Tile[] randomObstacleZone = { new Tile(3, 3), new Tile(3, 4),
				new Tile(3, 5), new Tile(3, 6), new Tile(3, 7), new Tile(3, 8),
				new Tile(3, 9), new Tile(4, 9), new Tile(5, 9), new Tile(6, 9),
				new Tile(6, 8), new Tile(6, 7), new Tile(6, 6), new Tile(7, 6),
				new Tile(8, 6), new Tile(9, 6), new Tile(9, 5), new Tile(9, 4),
				new Tile(9, 3), new Tile(8, 3), new Tile(7, 3), new Tile(6, 3),
				new Tile(5, 3), new Tile(4, 3) };
		Tile destinationTile = new Tile(9, 9);
		Path path = PathGenerator.generatePath(12, 12, destinationTile,
				obstacles, randomObstacleZone);

		Point pathStart = new Point(convertGridToCentimeters(0.5),
				convertGridToCentimeters(0.5));
		Point destination = new Point(convertGridToCentimeters(10),
				convertGridToCentimeters(10));
		Point reversePathStart = new Point(convertGridToCentimeters(9.5),
				convertGridToCentimeters(9.5));
		Point origin = new Point(convertGridToCentimeters(0),
				convertGridToCentimeters(0));

		int ballsToFirePerTarget = 3;

		this._odometer.start();
		this._leftUS.start();
		this._frontUS.start();
		this._rightUS.start();
		this._cs.start();

		this._usLocalization.doLocalization();
		this._csLocalization.doLocalization();
		// Indicates that the localization is finished.
		Sound.twoBeeps();

		this._navigation.travelTo(pathStart);
		this._odometerCorrection.start();
		this._navigation.travelForward(path);
		this._odometerCorrection.stop();

		this._csLocalization.doLocalization(destination);

		if (turnToLaunch(convertGridToCentimeters(firstTarget))) {
			// Fires at +- 5 degrees to increase chances of hitting.
			this._navigation.turn(-5);
			for (int i = 0; i < ballsToFirePerTarget; ++i) {
				Launcher.fire();
				++this._ballToFire;
				this._navigation.turn(5);
			}
			returnToDestinationInLaunchArea(destination);
		}

		if (turnToLaunch(convertGridToCentimeters(secondTarget))) {
			// Fires at +- 5 degrees to increase chances of hitting.
			this._navigation.turn(-5);
			for (int i = 0; i < ballsToFirePerTarget; ++i) {
				Launcher.fire();
				++this._ballToFire;
				this._navigation.turn(5);
			}
			returnToDestinationInLaunchArea(destination);
		}

		this._csLocalization.doLocalization(destination);

		this._navigation.travelTo(reversePathStart);
		this._odometerCorrection.start();
		this._navigation.travelBackward(path);
		this._odometerCorrection.stop();

		this._csLocalization.doLocalization();

		this._navigation.travelToPrecisely(origin);
		this._navigation.turnTo(90);

		System.exit(0);
	}

	/**
	 * Positions itself to fire at the target using the navigation. Will try all
	 * possible reference points in the launch area. Returns true on success.
	 */
	private boolean turnToLaunch(Point target) {
		Position launchPosition = null;
		outerLoop: for (double x = LAUNCH_AREA_X_MIN; x < LAUNCH_AREA_X_MAX; x += convertGridToCentimeters(1)) {
			for (double y = LAUNCH_AREA_Y_MIN; y < LAUNCH_AREA_Y_MAX; y += convertGridToCentimeters(1)) {
				Position tempPosition = computePositionForLaunch(
						new Point(x, y), target);
				if (isPointInLaunchArea(tempPosition)) {
					launchPosition = tempPosition;
					break outerLoop;
				}
			}
		}
		if (launchPosition == null) {
			// Cannot fire.
			return false;
		}
		this._navigation.travelTo(launchPosition);
		this._navigation.travelToPrecisely(launchPosition);
		this._navigation.turnTo(launchPosition.orientation);
		return true;
	}

	/**
	 * Positions itself to fire at the target using the navigation. The
	 * reference point serves to narrow down the possibilities of where to go,
	 * and the robot will position itself on the line joining target and
	 * reference.
	 */
	private void turnToLaunch(Point reference, Point target) {
		Position launchPosition = computePositionForLaunch(reference, target);
		this._navigation.travelTo(launchPosition);
		this._navigation.travelToPrecisely(launchPosition);
		this._navigation.turnTo(launchPosition.orientation);
	}

	private Position computePositionForLaunch(Point reference, Point target) {
		Vector referenceToTarget = new Vector(reference, target);
		double angleInDegrees = referenceToTarget.direction();
		double angleInRadians = Math.toRadians(angleInDegrees);
		Vector launchPointToTarget = new Vector(
				NXTConstants.distanceOfLaunch(this._ballToFire)
						* Math.cos(angleInRadians),
				NXTConstants.distanceOfLaunch(this._ballToFire)
						* Math.sin(angleInRadians));
		Position launchPosition = new Position(
				target.x - launchPointToTarget.x, target.y
						- launchPointToTarget.y, angleInDegrees
						- NXTConstants.LAUNCH_ANGLE);
		return launchPosition;
	}

	private boolean isPointInLaunchArea(Point point) {
		boolean isInXLaunchArea = (LAUNCH_AREA_X_MIN <= point.x && point.x <= LAUNCH_AREA_X_MAX
				- NXTConstants.ROBOT_FRONT_RADIUS_OF_ROTATION);
		boolean isInYLaunchArea = (LAUNCH_AREA_Y_MIN <= point.y && point.y <= LAUNCH_AREA_Y_MAX
				- NXTConstants.ROBOT_FRONT_RADIUS_OF_ROTATION);
		return isInXLaunchArea && isInYLaunchArea;
	}

	/**
	 * Returns to the destination optimally such that it avoids hitting the
	 * wall.
	 */
	private void returnToDestinationInLaunchArea(Point destination) {
		Position position = this._dataCenter.getPosition();
		Vector positionToDestination = new Vector(position, destination);
		if (AngleUtility.absoluteDifference(positionToDestination.direction(),
				position.orientation) <= 90) {
			this._navigation.travelTo(destination);
		} else {
			this._navigation.travelToBackward(destination);
		}
	}

	/**
	 * Displays the odometer position on the LCD for 5 s.
	 */
	private void displayOdometer() {
		Position position = this._dataCenter.getPosition();
		LCD.clear();
		LCD.drawInt((int) position.x, 0, 0);
		LCD.drawInt((int) position.y, 0, 1);
		LCD.drawInt((int) position.orientation, 0, 2);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static double convertGridToCentimeters(double position) {
		return position * NXTConstants.TILE_DISTANCE;
	}

	private static Point convertGridToCentimeters(Point point) {
		return new Point(convertGridToCentimeters(point.x),
				convertGridToCentimeters(point.y));
	}

	private static Point[] convertGridToCentimeters(Point[] points) {
		Point[] result = new Point[points.length];
		for (int i = 0; i < points.length; ++i) {
			result[i] = convertGridToCentimeters(points[i]);
		}
		return result;
	}
}
