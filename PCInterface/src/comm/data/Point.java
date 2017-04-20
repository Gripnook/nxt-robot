package comm.data;

/**
 * A simple class used to store points.
 *
 * @author Andrei Purcarus
 *
 */
public class Point {
	public double x;
	public double y;

	public Point() {
		this.x = 0.0;
		this.y = 0.0;
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 */
	public Point(Point point) {
		this.x = point.x;
		this.y = point.y;
	}
}
