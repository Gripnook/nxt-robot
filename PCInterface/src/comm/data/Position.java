package comm.data;

/**
 * A simple class used to store positions, which involve both a point and an
 * orientation.
 *
 * @author Andrei Purcarus
 *
 */
public final class Position extends Point {
	public double orientation;

	public Position() {
		super();
		this.orientation = 0.0;
	}

	public Position(double x, double y, double orientation) {
		super(x, y);
		this.orientation = orientation;
	}

	/**
	 * Copy constructor.
	 */
	public Position(Position position) {
		super(position);
		this.orientation = position.orientation;
	}
}
