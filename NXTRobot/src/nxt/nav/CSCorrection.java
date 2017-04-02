package nxt.nav;

import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.data.Point;
import nxt.data.Vector;
import nxt.util.MathUtility;

/**
 * A class that implements correction on the position when grid lines are
 * detected on the floor by a color sensor.
 *
 * @author Andrei Purcarus
 *
 */
public final class CSCorrection implements CSListener {
	/**
	 * The maximum error allowed between the reported position of the robot and
	 * the position of a grid line to correct at least one of the coordinates of
	 * the robot.
	 */
	private static final int CORRECTION_BANDWIDTH = 5;

	/**
	 * The maximum error allowed between the reported position of the robot and
	 * the position of a grid line to always correct both coordinates of the
	 * robot.
	 */
	private static final int ABSOLUTE_CORRECTION_BANDWIDTH = 2;

	private final DataCenter _dataCenter;

	public CSCorrection(DataCenter dataCenter) {
		this._dataCenter = dataCenter;
	}

	public final void start() {
		this._dataCenter.addCSListener(this);
	}

	public final void stop() {
		this._dataCenter.removeCSListener(this);
	}

	@Override
	public final void ping() {
		// Performs correction in a new thread to not stall the current one.
		(new Thread() {
			@Override
			public void run() {
				performCorrection();
			}
		}).start();
	}

	private void performCorrection() {
		Vector csVector = getCSVector();
		Point position = this._dataCenter.getPosition();
		Point csLocation = new Point(position.x + csVector.x, position.y
				+ csVector.y);

		Point gridLineIntersection = getNearestGridLineIntersection(csLocation);

		correctOdometer(csLocation, gridLineIntersection, csVector);
	}

	private Vector getCSVector() {
		double orientation = this._dataCenter.getOrientation();
		double orientationInRadians = Math.toRadians(orientation);
		double x = NXTConstants.CS_DISTANCE
				* Math.cos(orientationInRadians
						+ Math.toRadians(NXTConstants.CS_ANGLE));
		double y = NXTConstants.CS_DISTANCE
				* Math.sin(orientationInRadians
						+ Math.toRadians(NXTConstants.CS_ANGLE));
		return new Vector(x, y);
	}

	private Point getNearestGridLineIntersection(Point csLocation) {
		double xGridLine, yGridLine;

		double distanceToLowerXGridLine = MathUtility.positiveModulo(
				csLocation.x, NXTConstants.TILE_DISTANCE);
		if (distanceToLowerXGridLine <= NXTConstants.TILE_DISTANCE / 2)
			xGridLine = csLocation.x - distanceToLowerXGridLine;
		else
			xGridLine = csLocation.x
					+ (NXTConstants.TILE_DISTANCE - distanceToLowerXGridLine);

		double distanceToLowerYGridLine = MathUtility.positiveModulo(
				csLocation.y, NXTConstants.TILE_DISTANCE);
		if (distanceToLowerYGridLine <= NXTConstants.TILE_DISTANCE / 2)
			yGridLine = csLocation.y - distanceToLowerYGridLine;
		else
			yGridLine = csLocation.y
					+ (NXTConstants.TILE_DISTANCE - distanceToLowerYGridLine);

		return new Point(xGridLine, yGridLine);
	}

	private void correctOdometer(Point csLocation, Point gridLineIntersection,
			Vector csVector) {
		double nearestXGridLine = gridLineIntersection.x;
		double nearestYGridLine = gridLineIntersection.y;

		Vector distanceFromGridLineIntersection = new Vector(
				Math.abs(csLocation.x - nearestXGridLine),
				Math.abs(csLocation.y - nearestYGridLine));

		if (isCloseToGridLineIntersection(distanceFromGridLineIntersection)) {
			this._dataCenter.setXPosition(gridLineIntersection.x - csVector.x);
			this._dataCenter.setYPosition(gridLineIntersection.y - csVector.y);
		} else if (isCloseToGridInXButNotY(distanceFromGridLineIntersection)) {
			this._dataCenter.setXPosition(nearestXGridLine - csVector.x);
		} else if (isCloseToGridInYButNotX(distanceFromGridLineIntersection)) {
			this._dataCenter.setYPosition(nearestYGridLine - csVector.y);
		}
	}

	private boolean isCloseToGridLineIntersection(Vector distance) {
		return distance.norm() < ABSOLUTE_CORRECTION_BANDWIDTH;
	}

	private boolean isCloseToGridInXButNotY(Vector distance) {
		return distance.x < CORRECTION_BANDWIDTH && distance.y > distance.x;
	}

	private boolean isCloseToGridInYButNotX(Vector distance) {
		return distance.y < CORRECTION_BANDWIDTH && distance.x > distance.y;
	}

}