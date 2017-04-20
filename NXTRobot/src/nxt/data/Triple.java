package nxt.data;

/**
 * A simple data structure used to store ordered triples of objects.
 *
 * @author Andrei Purcarus
 *
 */
public final class Triple<First, Second, Third> {
	public First first;
	public Second second;
	public Third third;

	public Triple(First first, Second second, Third third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
