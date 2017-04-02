package comm.util;

/**
 * A utility class containing various useful mathematical methods.
 * 
 * @author Andrei Purcarus
 *
 */
public final class MathUtility {
	/**
	 * Returns dividend % divisor in the range [0, divisor).
	 */
	public static final double positiveModulo(double dividend, double divisor) {
		double modulo = dividend % divisor;
		if (modulo >= 0)
			return modulo;
		else
			return modulo + divisor;
	}

	private MathUtility() {

	}
}
