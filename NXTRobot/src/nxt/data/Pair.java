package nxt.data;

/**
 * A simple data structure used to store ordered pairs of objects.
 *
 * @author Andrei Purcarus
 *
 */
public final class Pair<First, Second> {
	public First first;
	public Second second;

	public Pair(First first, Second second) {
		this.first = first;
		this.second = second;
	}
}
