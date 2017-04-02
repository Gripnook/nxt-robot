package nxt.nav.path;

import nxt.NXTConstants;
import nxt.data.Point;
import nxt.data.Tile;
import nxt.nav.Direction;
import nxt.nav.Navigation;

/**
 * A class that represents the segment of path between two points.
 * 
 * @author Andrei Purcarus
 *
 */
public final class PathSegment implements PathItem {
	private Direction _forwardDirection;
	private Direction _backwardDirection;
	private double _centerOfPath;
	private Point _tunnelDestinationForward;
	private Point _finalDestinationForward;
	private Point _tunnelDestinationBackward;
	private Point _finalDestinationBackward;
	private final Tile _firstTile;
	private final Tile _lastTile;
	private final boolean _leftWall;
	private final boolean _rightWall;

	public PathSegment(Tile firstTile, Tile lastTile, boolean leftWall,
			boolean rightWall) {
		this._firstTile = firstTile;
		this._lastTile = lastTile;
		this._leftWall = leftWall;
		this._rightWall = rightWall;
		if (validPathSegment()) {
			initialize();
		} else {
			throw new IllegalArgumentException("Invalid path segment");
		}
	}

	@Override
	public final void traverseForward(Navigation navigation) {
		if (this._leftWall || this._rightWall) {
			navigation.travelInTunnel(this._tunnelDestinationForward,
					this._forwardDirection, this._centerOfPath, this._leftWall,
					this._rightWall);
			navigation.travelTo(this._finalDestinationForward);
		} else {
			navigation.travelTo(this._finalDestinationForward);
		}
	}

	@Override
	public final void traverseBackward(Navigation navigation) {
		if (this._leftWall || this._rightWall) {
			// Reversing the path means right becomes left.
			navigation.travelInTunnel(this._tunnelDestinationBackward,
					this._backwardDirection, this._centerOfPath,
					this._rightWall, this._leftWall);
			navigation.travelTo(this._finalDestinationBackward);
		} else {
			navigation.travelTo(this._finalDestinationBackward);
		}
	}

	private boolean validPathSegment() {
		if (this._firstTile.x == this._lastTile.x
				&& this._firstTile.y == this._lastTile.y) {
			return false;
		} else if (this._firstTile.x == this._lastTile.x) {
			return true;
		} else if (this._firstTile.y == this._lastTile.y) {
			return true;
		} else {
			return false;
		}
	}

	private void initialize() {
		if (this._firstTile.x == this._lastTile.x) {
			initializeForVerticalTravel();
		} else {
			initializeForHorizontalTravel();
		}
	}

	private void initializeForHorizontalTravel() {
		this._centerOfPath = (this._firstTile.y + 0.5)
				* NXTConstants.TILE_DISTANCE;
		int xDifference = this._lastTile.x - this._firstTile.x;
		if (xDifference > 0) {
			initializeForEastTravel();
		} else {
			initializeForWestTravel();
		}
	}

	private void initializeForVerticalTravel() {
		this._centerOfPath = (this._firstTile.x + 0.5)
				* NXTConstants.TILE_DISTANCE;
		int yDifference = this._lastTile.y - this._firstTile.y;
		if (yDifference > 0) {
			initializeForNorthTravel();
		} else {
			initializeForSouthTravel();
		}
	}

	private void initializeForEastTravel() {
		this._forwardDirection = Direction.east;
		this._backwardDirection = Direction.west;
		this._tunnelDestinationForward = new Point(this._lastTile.x
				* NXTConstants.TILE_DISTANCE - NXTConstants.ROBOT_X_MAX,
				this._centerOfPath);
		this._finalDestinationForward = new Point(this._lastTile.x
				* NXTConstants.TILE_DISTANCE
				+ NXTConstants.WHEEL_TO_WALL_DISTANCE, this._centerOfPath);
		this._tunnelDestinationBackward = new Point((this._firstTile.x + 1)
				* NXTConstants.TILE_DISTANCE + NXTConstants.ROBOT_X_MAX,
				this._centerOfPath);
		this._finalDestinationBackward = new Point((this._firstTile.x + 1)
				* NXTConstants.TILE_DISTANCE
				- NXTConstants.WHEEL_TO_WALL_DISTANCE, this._centerOfPath);
	}

	private void initializeForWestTravel() {
		this._forwardDirection = Direction.west;
		this._backwardDirection = Direction.east;
		this._tunnelDestinationForward = new Point((this._lastTile.x + 1)
				* NXTConstants.TILE_DISTANCE + NXTConstants.ROBOT_X_MAX,
				this._centerOfPath);
		this._finalDestinationForward = new Point((this._lastTile.x + 1)
				* NXTConstants.TILE_DISTANCE
				- NXTConstants.WHEEL_TO_WALL_DISTANCE, this._centerOfPath);
		this._tunnelDestinationBackward = new Point(this._firstTile.x
				* NXTConstants.TILE_DISTANCE - NXTConstants.ROBOT_X_MAX,
				this._centerOfPath);
		this._finalDestinationBackward = new Point(this._firstTile.x
				* NXTConstants.TILE_DISTANCE
				+ NXTConstants.WHEEL_TO_WALL_DISTANCE, this._centerOfPath);
	}

	private void initializeForNorthTravel() {
		this._forwardDirection = Direction.north;
		this._backwardDirection = Direction.south;
		this._tunnelDestinationForward = new Point(this._centerOfPath,
				this._lastTile.y * NXTConstants.TILE_DISTANCE
						- NXTConstants.ROBOT_X_MAX);
		this._finalDestinationForward = new Point(this._centerOfPath,
				this._lastTile.y * NXTConstants.TILE_DISTANCE
						+ NXTConstants.WHEEL_TO_WALL_DISTANCE);
		this._tunnelDestinationBackward = new Point(this._centerOfPath,
				(this._firstTile.y + 1) * NXTConstants.TILE_DISTANCE
						+ NXTConstants.ROBOT_X_MAX);
		this._finalDestinationBackward = new Point(this._centerOfPath,
				(this._firstTile.y + 1) * NXTConstants.TILE_DISTANCE
						- NXTConstants.WHEEL_TO_WALL_DISTANCE);
	}

	private void initializeForSouthTravel() {
		this._forwardDirection = Direction.south;
		this._backwardDirection = Direction.north;
		this._tunnelDestinationForward = new Point(this._centerOfPath,
				(this._lastTile.y + 1) * NXTConstants.TILE_DISTANCE
						+ NXTConstants.ROBOT_X_MAX);
		this._finalDestinationForward = new Point(this._centerOfPath,
				(this._lastTile.y + 1) * NXTConstants.TILE_DISTANCE
						- NXTConstants.WHEEL_TO_WALL_DISTANCE);
		this._tunnelDestinationBackward = new Point(this._centerOfPath,
				this._firstTile.y * NXTConstants.TILE_DISTANCE
						- NXTConstants.ROBOT_X_MAX);
		this._finalDestinationBackward = new Point(this._centerOfPath,
				this._firstTile.y * NXTConstants.TILE_DISTANCE
						+ NXTConstants.WHEEL_TO_WALL_DISTANCE);
	}
}
