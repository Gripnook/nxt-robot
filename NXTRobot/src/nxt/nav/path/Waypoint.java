package nxt.nav.path;

import nxt.data.Tile;
import nxt.nav.Direction;
import nxt.nav.Navigation;
import nxt.util.AngleUtility;

/**
 * A class that represents a waypoint of a path at which the robot needs to
 * turn.
 * 
 * @author Andrei Purcarus
 *
 */
public final class Waypoint implements PathItem {
	private final Direction _firstDirection;
	private final Direction _secondDirection;
	private final Tile _tile;

	public Waypoint(Direction firstDirection, Direction secondDirection,
			Tile tile) {
		this._firstDirection = firstDirection;
		this._secondDirection = secondDirection;
		this._tile = tile;
		if (!validDirections()) {
			throw new IllegalArgumentException("Invalid waypoint");
		}
	}

	@Override
	public final void traverseForward(Navigation navigation) {
		if (this._firstDirection == Direction.east) {
			if (this._secondDirection == Direction.north) {
				eastToNorth(navigation, this._tile);
			} else if (this._secondDirection == Direction.south) {
				eastToSouth(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.north) {
			if (this._secondDirection == Direction.east) {
				northToEast(navigation, this._tile);
			} else if (this._secondDirection == Direction.west) {
				northToWest(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.west) {
			if (this._secondDirection == Direction.north) {
				westToNorth(navigation, this._tile);
			} else if (this._secondDirection == Direction.south) {
				westToSouth(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.south) {
			if (this._secondDirection == Direction.east) {
				southToEast(navigation, this._tile);
			} else if (this._secondDirection == Direction.west) {
				southToWest(navigation, this._tile);
			}
		}
	}

	@Override
	public final void traverseBackward(Navigation navigation) {
		if (this._firstDirection == Direction.east) {
			if (this._secondDirection == Direction.north) {
				southToWest(navigation, this._tile);
			} else if (this._secondDirection == Direction.south) {
				northToWest(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.north) {
			if (this._secondDirection == Direction.east) {
				westToSouth(navigation, this._tile);
			} else if (this._secondDirection == Direction.west) {
				eastToSouth(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.west) {
			if (this._secondDirection == Direction.north) {
				southToEast(navigation, this._tile);
			} else if (this._secondDirection == Direction.south) {
				northToEast(navigation, this._tile);
			}
		} else if (this._firstDirection == Direction.south) {
			if (this._secondDirection == Direction.east) {
				westToNorth(navigation, this._tile);
			} else if (this._secondDirection == Direction.west) {
				eastToNorth(navigation, this._tile);
			}
		}
	}

	private boolean validDirections() {
		return AngleUtility.absoluteDifference(this._firstDirection.asAngle(),
				this._secondDirection.asAngle()) == 90;
	}

	private static void eastToNorth(Navigation navigation, Tile tile) {
		navigation.turnTo(0);
		navigation.turnLeft();
	}

	private static void eastToSouth(Navigation navigation, Tile tile) {
		navigation.turnTo(0);
		navigation.turnRight();
	}

	private static void northToEast(Navigation navigation, Tile tile) {
		navigation.turnTo(90);
		navigation.turnRight();
	}

	private static void northToWest(Navigation navigation, Tile tile) {
		navigation.turnTo(90);
		navigation.turnLeft();
	}

	private static void westToNorth(Navigation navigation, Tile tile) {
		navigation.turnTo(180);
		navigation.turnRight();
	}

	private static void westToSouth(Navigation navigation, Tile tile) {
		navigation.turnTo(180);
		navigation.turnLeft();
	}

	private static void southToWest(Navigation navigation, Tile tile) {
		navigation.turnTo(-90);
		navigation.turnRight();
	}

	private static void southToEast(Navigation navigation, Tile tile) {
		navigation.turnTo(-90);
		navigation.turnLeft();
	}
}
