package nxt.nav;

import nxt.NXTConstants;
import nxt.data.DataCenter;
import nxt.drivers.USPoller;

/**
 * A class used to correct the odometer while traveling through a one tile gap
 * with known obstacles on the sides.
 *
 * @author Andrei Purcarus
 *
 */
public final class USCorrection {
	/**
	 * The error assumed in block placement on a tile.
	 */
	private static final double BLOCK_PLACEMENT_ERROR = 2;

	/**
	 * The maximum distance to a wall allowed in order to accept a measurement.
	 */
	private static final double MAX_DISTANCE_TO_WALL = NXTConstants.TILE_DISTANCE;

	/**
	 * Minimum delay between consecutive corrections.
	 */
	private static final long MIN_DELAY = 1000;

	/**
	 * Maximum error allowed before correction.
	 */
	private static final double MAX_ORIENTATION_ERROR = 3;

	private final Direction _direction;
	private final double _centerOfPath;

	private long _lastTimeUpdated;
	private double _distanceToLeftWall;
	private double _distanceToRightWall;
	private final boolean _leftWall;
	private final boolean _rightWall;

	private final DataCenter _dataCenter;

	public USCorrection(Direction direction, double centerOfPath,
			DataCenter dataCenter, boolean leftWall, boolean rightWall) {
		this._direction = direction;
		this._centerOfPath = centerOfPath;
		this._dataCenter = dataCenter;
		this._leftWall = leftWall;
		this._rightWall = rightWall;
	}

	public final void start() {
		this._dataCenter.setWallFollowing(true);
		USPoller.waitTimeRequiredForInitialization();
		this._lastTimeUpdated = System.currentTimeMillis() - MIN_DELAY;
	}

	public final void end() {
		this._dataCenter.setWallFollowing(false);
	}

	public final void update() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - this._lastTimeUpdated >= MIN_DELAY) {
			this._lastTimeUpdated = currentTime;
			updateDistancesToWalls();
			if (!validDistancesToWall()) {
				return;
			}
			updateForCorrectDirection();
		}
	}

	/**
	 * Corrects the heading of the robot using knows walls on the side. The
	 * robot must have space in front of it, so it is recommended to call this
	 * method at the start of navigation through a walled area.
	 */
	public final void correctHeading(Navigation navigation) {
		navigation.turnTo(this._direction.asAngle());
		USPoller.waitTimeRequiredForInitialization();
		double leftDistance1 = this._dataCenter
				.getFilteredUSDistanceAtAngle(90)
				+ NXTConstants.LEFT_US_DISTANCE;
		double rightDistance1 = this._dataCenter
				.getFilteredUSDistanceAtAngle(-90)
				+ NXTConstants.RIGHT_US_DISTANCE;
		double distanceToMove = 10;
		navigation.moveForward(distanceToMove);
		USPoller.waitTimeRequiredForInitialization();
		double leftDistance2 = this._dataCenter
				.getFilteredUSDistanceAtAngle(90)
				+ NXTConstants.LEFT_US_DISTANCE;
		double rightDistance2 = this._dataCenter
				.getFilteredUSDistanceAtAngle(-90)
				+ NXTConstants.RIGHT_US_DISTANCE;
		navigation.moveBackward(distanceToMove);
		if ((this._leftWall && leftDistance1 > MAX_DISTANCE_TO_WALL)
				|| (this._rightWall && rightDistance1 > MAX_DISTANCE_TO_WALL)
				|| (this._leftWall && leftDistance2 > MAX_DISTANCE_TO_WALL)
				|| (this._rightWall && rightDistance2 > MAX_DISTANCE_TO_WALL)) {
			return; // Don't correct if one of the distances is not what is
					// should be.
		}
		double leftDifference = leftDistance2 - leftDistance1;
		double rightDifference = rightDistance2 - rightDistance1;
		double deviation = getDeviation(leftDifference, rightDifference,
				distanceToMove);
		if (deviation > MAX_ORIENTATION_ERROR) {
			this._dataCenter.setOrientation(this._direction.asAngle()
					+ deviation);
		}
	}

	private void updateDistancesToWalls() {
		this._distanceToLeftWall = this._dataCenter
				.getFilteredUSDistanceAtAngle(90)
				+ NXTConstants.LEFT_US_DISTANCE;
		this._distanceToRightWall = this._dataCenter
				.getFilteredUSDistanceAtAngle(-90)
				+ NXTConstants.RIGHT_US_DISTANCE;
	}

	private void updateForCorrectDirection() {
		switch (this._direction) {
		case east:
			updateForPositiveXDirection();
			break;
		case north:
			updateForPositiveYDirection();
			break;
		case west:
			updateForNegativeXDirection();
			break;
		case south:
			updateForNegativeYDirection();
			break;
		}
	}

	private void updateForPositiveXDirection() {
		if (this._leftWall && this._rightWall) {
			double averageYPosition = this._centerOfPath
					+ (this._distanceToRightWall - this._distanceToLeftWall)
					/ 2;
			this._dataCenter.setYPosition(averageYPosition);
		} else if (this._leftWall) {
			double yPosition = this._centerOfPath + NXTConstants.TILE_DISTANCE
					/ 2 - this._distanceToLeftWall + BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setYPosition(yPosition);
		} else if (this._rightWall) {
			double yPosition = this._centerOfPath - NXTConstants.TILE_DISTANCE
					/ 2 + this._distanceToRightWall - BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setYPosition(yPosition);
		}
	}

	private void updateForPositiveYDirection() {
		if (this._leftWall && this._rightWall) {
			double averageXPosition = this._centerOfPath
					+ (this._distanceToLeftWall - this._distanceToRightWall)
					/ 2;
			this._dataCenter.setXPosition(averageXPosition);
		} else if (this._leftWall) {
			double xPosition = this._centerOfPath - NXTConstants.TILE_DISTANCE
					/ 2 + this._distanceToLeftWall - BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setXPosition(xPosition);
		} else if (this._rightWall) {
			double xPosition = this._centerOfPath + NXTConstants.TILE_DISTANCE
					/ 2 - this._distanceToRightWall + BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setXPosition(xPosition);
		}
	}

	private void updateForNegativeXDirection() {
		if (this._leftWall && this._rightWall) {
			double averageYPosition = this._centerOfPath
					+ (this._distanceToLeftWall - this._distanceToRightWall)
					/ 2;
			this._dataCenter.setYPosition(averageYPosition);
		} else if (this._leftWall) {
			double yPosition = this._centerOfPath - NXTConstants.TILE_DISTANCE
					/ 2 + this._distanceToLeftWall - BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setYPosition(yPosition);
		} else if (this._rightWall) {
			double yPosition = this._centerOfPath + NXTConstants.TILE_DISTANCE
					/ 2 - this._distanceToRightWall + BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setYPosition(yPosition);
		}
	}

	private void updateForNegativeYDirection() {
		if (this._leftWall && this._rightWall) {
			double averageXPosition = this._centerOfPath
					+ (this._distanceToRightWall - this._distanceToLeftWall)
					/ 2;
			this._dataCenter.setXPosition(averageXPosition);
		} else if (this._leftWall) {
			double xPosition = this._centerOfPath + NXTConstants.TILE_DISTANCE
					/ 2 - this._distanceToLeftWall + BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setXPosition(xPosition);
		} else if (this._rightWall) {
			double xPosition = this._centerOfPath - NXTConstants.TILE_DISTANCE
					/ 2 + this._distanceToRightWall - BLOCK_PLACEMENT_ERROR;
			this._dataCenter.setXPosition(xPosition);
		}
	}

	private boolean validDistancesToWall() {
		return (!this._leftWall || this._distanceToLeftWall <= MAX_DISTANCE_TO_WALL)
				&& (!this._rightWall || this._distanceToRightWall <= MAX_DISTANCE_TO_WALL);
	}

	private double getDeviation(double leftDifference, double rightDifference,
			double distanceToMove) {
		if (this._rightWall && this._leftWall) {
			return Math.toDegrees(Math.asin((rightDifference - leftDifference)
					/ (2 * distanceToMove)));
		} else if (this._leftWall) {
			return Math
					.toDegrees(Math.asin(-leftDifference / (distanceToMove)));
		} else if (this._rightWall) {
			return Math
					.toDegrees(Math.asin(rightDifference / (distanceToMove)));
		} else {
			return 0;
		}
	}
}
