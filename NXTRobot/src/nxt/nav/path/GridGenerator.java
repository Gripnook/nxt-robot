package nxt.nav.path;

import java.util.Stack;

import nxt.data.Tile;

/**
 * A class that creates a Grid object and computes the minimum distance from the
 * starting point to all of the other points.
 * 
 * @author Andrei Purcarus
 *
 */
public final class GridGenerator {
	private Grid _grid;
	private Stack<Tile> _tilesToVisit;

	/**
	 * Generates a grid in which each tile possesses the value of the shortest
	 * distance between the origin and that tile.
	 */
	public static final Grid generateGrid(int rows, int columns,
			Tile[] obstacles, Tile[] notAccessible) {
		GridGenerator gridGenerator = new GridGenerator(rows, columns,
				obstacles, notAccessible);
		return gridGenerator.generate();
	}

	private GridGenerator(int rows, int columns, Tile[] obstacles,
			Tile[] notAccessible) {
		this._grid = new Grid(rows, columns);
		this._grid.addObstacles(obstacles);
		this._grid.addNotAccessible(notAccessible);
	}

	private Grid generate() {
		this._tilesToVisit = new Stack<>();
		this._tilesToVisit.push(new Tile(0, 0));
		while (!this._tilesToVisit.isEmpty()) {
			Tile current = this._tilesToVisit.pop();
			Tile top = new Tile(current.x + 1, current.y);
			Tile left = new Tile(current.x, current.y - 1);
			Tile right = new Tile(current.x, current.y + 1);
			Tile bottom = new Tile(current.x - 1, current.y);
			pushToStack(top, current);
			pushToStack(left, current);
			pushToStack(right, current);
			pushToStack(bottom, current);
		}
		return this._grid;
	}

	private void pushToStack(Tile child, Tile parent) {
		if (shouldPush(child, parent)) {
			this._grid.set(child, this._grid.get(parent) + 1);
			this._tilesToVisit.push(child);
		}
	}

	private boolean shouldPush(Tile child, Tile parent) {
		return this._grid.isAccessible(child)
				&& (!this._grid.isInitialized(child) || this._grid.get(child) > this._grid
						.get(parent) + 1);
	}
}
