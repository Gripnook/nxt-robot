package nxt.nav.path;

import java.util.Stack;

import nxt.data.Tile;
import nxt.nav.Direction;

/**
 * A class to generate a path to travel given the locations of obstacle tiles
 * and inaccessible tiles.
 * 
 * @author Andrei Purcarus
 *
 */
public class PathGenerator {
	private Grid _grid;
	private Tile _destination;
	private int _row;
	private int _column;
	private Stack<Tile> _pathStack;
	private Tile[] _tilePath;
	private Path _path;

	/**
	 * Generates a path to travel given the locations of obstacles and
	 * inaccessible tiles.
	 */
	public static Path generatePath(int rows, int columns, Tile destination,
			Tile[] obstacles, Tile[] notAccessible) {
		PathGenerator pathGenerator = new PathGenerator(rows, columns,
				destination, obstacles, notAccessible);
		return pathGenerator.generate();
	}

	private PathGenerator(int rows, int columns, Tile destination,
			Tile[] obstacles, Tile[] notAccessible) {
		this._grid = GridGenerator.generateGrid(rows, columns, obstacles,
				notAccessible);
		this._destination = destination;
	}

	private Path generate() {
		getTilePath();
		removeUnnecessaryTiles();
		convertTilesToPath();
		return this._path;
	}

	private void getTilePath() {
		this._row = this._destination.x;
		this._column = this._destination.y;
		this._pathStack = new Stack<>();
		Tile previousTile = null;
		this._pathStack.push(new Tile(this._row, this._column));
		while (this._grid.get(this._row, this._column) != 0) {
			Tile currentTile = new Tile(this._row, this._column);
			int currentValue = this._grid.get(currentTile);
			Tile top = new Tile(this._row + 1, this._column);
			Tile left = new Tile(this._row, this._column - 1);
			Tile right = new Tile(this._row, this._column + 1);
			Tile bottom = new Tile(this._row - 1, this._column);
			Tile sameDirectionTile = null;
			if (previousTile != null) {
				int xDifference = currentTile.x - previousTile.x;
				int yDifference = currentTile.y - previousTile.y;
				sameDirectionTile = new Tile(currentTile.x + xDifference,
						currentTile.y + yDifference);
			}
			previousTile = currentTile;
			if (sameDirectionTile != null
					&& pushToStack(sameDirectionTile, currentValue)) {
				continue;
			} else if (pushToStack(top, currentValue)) {
				continue;
			} else if (pushToStack(left, currentValue)) {
				continue;
			} else if (pushToStack(right, currentValue)) {
				continue;
			} else if (pushToStack(bottom, currentValue)) {
				continue;
			} else {
				throw new IllegalStateException("No path to destination");
			}
		}
		this._tilePath = new Tile[this._pathStack.size()];
		for (int i = 0; i < this._tilePath.length; ++i) {
			this._tilePath[i] = this._pathStack.pop();
		}
	}

	private boolean pushToStack(Tile child, int parentValue) {
		if (this._grid.isInBounds(child)
				&& this._grid.get(child) == parentValue - 1) {
			this._pathStack.push(child);
			this._row = child.x;
			this._column = child.y;
			return true;
		} else {
			return false;
		}
	}

	private void removeUnnecessaryTiles() {
		Tile[] newPath = new Tile[this._tilePath.length];
		newPath[0] = new Tile(this._tilePath[0]);
		int count = 1;
		for (int i = 1; i < this._tilePath.length - 1; ++i) {
			if (isRedundant(i)) {
				continue;
			} else {
				newPath[count++] = this._tilePath[i];
			}
		}
		newPath[count++] = this._tilePath[this._tilePath.length - 1];
		Tile[] result = new Tile[count];
		for (int i = 0; i < count; ++i) {
			result[i] = newPath[i];
		}
		this._tilePath = result;
	}

	private boolean isRedundant(int index) {
		boolean isOnSameX = this._tilePath[index].x == this._tilePath[index - 1].x
				&& this._tilePath[index].x == this._tilePath[index + 1].x;
		boolean isOnSameY = this._tilePath[index].y == this._tilePath[index - 1].y
				&& this._tilePath[index].y == this._tilePath[index + 1].y;
		if (isOnSameX) {
			boolean currentEastWall = this._grid.isObstacle(
					this._tilePath[index].x + 1, this._tilePath[index].y);
			boolean currentWestWall = this._grid.isObstacle(
					this._tilePath[index].x - 1, this._tilePath[index].y);
			boolean previousEastWall = this._grid.isObstacle(
					this._tilePath[index - 1].x + 1,
					this._tilePath[index - 1].y);
			boolean previousWestWall = this._grid.isObstacle(
					this._tilePath[index - 1].x - 1,
					this._tilePath[index - 1].y);
			return (currentEastWall == previousEastWall && currentWestWall == previousWestWall);
		}
		if (isOnSameY) {
			boolean currentNorthWall = this._grid.isObstacle(
					this._tilePath[index].x, this._tilePath[index].y + 1);
			boolean currentSouthWall = this._grid.isObstacle(
					this._tilePath[index].x, this._tilePath[index].y - 1);
			boolean previousNorthWall = this._grid.isObstacle(
					this._tilePath[index - 1].x,
					this._tilePath[index - 1].y + 1);
			boolean previousSouthWall = this._grid.isObstacle(
					this._tilePath[index - 1].x,
					this._tilePath[index - 1].y - 1);
			return (currentNorthWall == previousNorthWall && currentSouthWall == previousSouthWall);
		}
		return false;
	}

	private void convertTilesToPath() {
		this._path = new Path();
		this._path.add(new PathSegment(this._tilePath[0], this._tilePath[1],
				false, false));
		for (int i = 1; i < this._tilePath.length - 1; ++i) {
			Tile previous = this._tilePath[i - 1];
			Tile current = this._tilePath[i];
			Tile next = this._tilePath[i + 1];
			Direction before = getDirection(previous, current);
			Direction after = getDirection(current, next);
			if (before == after) {
				if (before == Direction.east) {
					boolean leftWall = this._grid.isObstacle(current.x,
							current.y + 1);
					boolean rightWall = this._grid.isObstacle(current.x,
							current.y - 1);
					this._path.add(new PathSegment(current, next, leftWall,
							rightWall));
				} else if (before == Direction.north) {
					boolean leftWall = this._grid.isObstacle(current.x - 1,
							current.y);
					boolean rightWall = this._grid.isObstacle(current.x + 1,
							current.y);
					this._path.add(new PathSegment(current, next, leftWall,
							rightWall));
				} else if (before == Direction.west) {
					boolean leftWall = this._grid.isObstacle(current.x,
							current.y - 1);
					boolean rightWall = this._grid.isObstacle(current.x,
							current.y + 1);
					this._path.add(new PathSegment(current, next, leftWall,
							rightWall));
				} else if (before == Direction.south) {
					boolean leftWall = this._grid.isObstacle(current.x + 1,
							current.y);
					boolean rightWall = this._grid.isObstacle(current.x - 1,
							current.y);
					this._path.add(new PathSegment(current, next, leftWall,
							rightWall));
				}
			} else {
				this._path.add(new Waypoint(before, after, current));
				this._path.add(new PathSegment(current, next, false, false));
			}
		}
	}

	private Direction getDirection(Tile first, Tile last) {
		if (first.x == last.x) {
			int yDifference = last.y - first.y;
			if (yDifference > 0) {
				return Direction.north;
			} else {
				return Direction.south;
			}
		} else {
			int xDifference = last.x - first.x;
			if (xDifference > 0) {
				return Direction.east;
			} else {
				return Direction.west;
			}
		}
	}
}
