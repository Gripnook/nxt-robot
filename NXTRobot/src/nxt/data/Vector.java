package nxt.data;

import nxt.util.AngleUtility;

/**
 * A simple data structure used to store two dimensional vectors.
 *
 * @author Andrei Purcarus
 *
 */
public final class Vector {
	public double x;
	public double y;

	public Vector() {
		this.x = 0.0;
		this.y = 0.0;
	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 */
	public Vector(Vector vector) {
		this.x = vector.x;
		this.y = vector.y;
	}

	public Vector(Point start, Point end) {
		this.x = end.x - start.x;
		this.y = end.y - start.y;
	}

	public final double norm() {
		double normSquared = this.x * this.x + this.y * this.y;
		return Math.sqrt(normSquared);
	}

	/**
	 * Returns the direction of the vector in the range [0, 360). If both x and
	 * y are 0, then returns the same value as Math.atan2(0, 0).
	 */
	public final double direction() {
		return AngleUtility
				.normalize(Math.toDegrees(Math.atan2(this.y, this.x)));
	}
}
