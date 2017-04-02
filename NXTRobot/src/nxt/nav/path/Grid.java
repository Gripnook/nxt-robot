package nxt.nav.path;

import nxt.data.Tile;

/**
 * A class representing the grid the robot navigates in, with each tile having
 * an attribute such as being an obstacle or the number of tiles necessary to
 * reach it from the starting tile.
 * 
 * @author Andrei Purcarus
 *
 */
public final class Grid {
	private static final int NOT_CHECKED = -1;
	private static final int OBSTACLE = -2;
	private static final int NOT_ACCESSIBLE = -3;

	private static final int START_TILE_X = toGrid(0);
	private static final int START_TILE_Y = toGrid(0);

	private final int _rows;
	private final int _columns;
	private int[][] _grid;

	public Grid(int rows, int columns) {
		this._rows = rows;
		this._columns = columns;
		this._grid = new int[rows][columns];
		initializeGrid();
		addStartingArea();
	}

	public final void addObstacles(Tile[] obstacles) {
		for (Tile tile : obstacles) {
			this._grid[toGrid(tile.x)][toGrid(tile.y)] = OBSTACLE;
		}
	}

	public final void addNotAccessible(Tile[] notAccessible) {
		for (Tile tile : notAccessible) {
			this._grid[toGrid(tile.x)][toGrid(tile.y)] = NOT_ACCESSIBLE;
		}
	}

	public final int get(int row, int column) {
		return this._grid[toGrid(row)][toGrid(column)];
	}

	public final int get(Tile tile) {
		return get(tile.x, tile.y);
	}

	public final void set(int row, int column, int value) {
		this._grid[toGrid(row)][toGrid(column)] = value;
	}

	public final void set(Tile tile, int value) {
		set(tile.x, tile.y, value);
	}

	public final boolean isInitialized(int row, int column) {
		int value = this._grid[toGrid(row)][toGrid(column)];
		return value != NOT_CHECKED;
	}

	public final boolean isInitialized(Tile tile) {
		return isInitialized(tile.x, tile.y);
	}

	public final boolean isObstacle(int row, int column) {
		if (!isInBounds(row, column))
			return true;
		int value = this._grid[toGrid(row)][toGrid(column)];
		return value == OBSTACLE;
	}

	public final boolean isObstacle(Tile tile) {
		return isObstacle(tile.x, tile.y);
	}

	public final boolean isAccessible(int row, int column) {
		if (!isInBounds(row, column))
			return false;
		int value = this._grid[toGrid(row)][toGrid(column)];
		return (value != OBSTACLE && value != NOT_ACCESSIBLE);
	}

	public final boolean isAccessible(Tile tile) {
		return isAccessible(tile.x, tile.y);
	}

	public final boolean isInBounds(int row, int column) {
		row = toGrid(row);
		column = toGrid(column);
		return (0 <= row && row < this._rows)
				&& (0 <= column && column < this._columns);
	}

	public final boolean isInBounds(Tile tile) {
		return isInBounds(tile.x, tile.y);
	}

	private void initializeGrid() {
		for (int row = 0; row < this._rows; ++row) {
			for (int column = 0; column < this._columns; ++column) {
				this._grid[row][column] = NOT_CHECKED;
			}
		}
	}

	private void addStartingArea() {
		this._grid[START_TILE_X][START_TILE_Y] = 0;
	}

	/**
	 * Converts the given tile coordinate to its equivalent coordinate in the
	 * grid.
	 */
	private static int toGrid(int tileCoordinate) {
		return tileCoordinate + 1;
	}
}
