package nxt.data;

/**
 * A simple data structure used to store tile locations on the grid.
 *
 * @author Andrei Purcarus
 *
 */
public final class Tile {
	public int x;
	public int y;

	public Tile() {
		this.x = 0;
		this.y = 0;
	}

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 */
	public Tile(Tile tile) {
		this.x = tile.x;
		this.y = tile.y;
	}
}