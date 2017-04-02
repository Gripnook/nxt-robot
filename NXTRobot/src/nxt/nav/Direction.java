package nxt.nav;

/**
 * An enum for the four cardinal directions.
 * 
 * @author Andrei Purcarus
 *
 */
public enum Direction {
	north(90), east(0), south(270), west(180);

	private final int _angle;

	private Direction(int angle) {
		this._angle = angle;
	}

	public int asAngle() {
		return this._angle;
	}
}
