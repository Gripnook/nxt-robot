package nxt.data;

/**
 * A simple data structure to hold raw and filtered distances from the
 * ultrasonic sensor, and the angle at which they were measured.
 * 
 * @author Andrei Purcarus
 *
 */
public final class AngleToDistancePair {
	public double angle;
	public int rawDistance;
	public int filteredDistance;

	public AngleToDistancePair(double angle, int rawDistance,
			int filteredDistance) {
		this.angle = angle;
		this.rawDistance = rawDistance;
		this.filteredDistance = filteredDistance;
	}
}
