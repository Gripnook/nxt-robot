package comm.util;

import java.util.Collection;
import java.util.Random;

import comm.data.Vector;
import comm.test.Assert;

/**
 * A utility class containing various useful methods for dealing with angles.
 *
 * @author Andrei Purcarus
 *
 */
public final class AngleUtility {
	/**
	 * Converts the given angle to the range [lowerBoundInDegrees,
	 * lowerBoundInDegrees + 360.0) if includeLowerBound is true and to
	 * (lowerBoundInDegrees, lowerBoundInDegrees + 360.0] if includeLowerBound
	 * is false.
	 */
	public static final double toRange(double angleInDegrees,
			double lowerBoundInDegrees, boolean includeLowerBound) {
		if (includeLowerBound) {
			while (angleInDegrees < lowerBoundInDegrees)
				angleInDegrees += 360.0;
			while (angleInDegrees >= lowerBoundInDegrees + 360.0)
				angleInDegrees -= 360.0;
		} else {
			while (angleInDegrees <= lowerBoundInDegrees)
				angleInDegrees += 360.0;
			while (angleInDegrees > lowerBoundInDegrees + 360.0)
				angleInDegrees -= 360.0;
		}
		return angleInDegrees;
	}

	/**
	 * Converts the given angle to the range [lowerBoundInDegrees,
	 * lowerBoundInDegrees + 360) if includeLowerBound is true and to
	 * (lowerBoundInDegrees, lowerBoundInDegrees + 360] if includeLowerBound is
	 * false.
	 */
	public static final int toRange(int angleInDegrees,
			int lowerBoundInDegrees, boolean includeLowerBound) {
		if (includeLowerBound) {
			while (angleInDegrees < lowerBoundInDegrees)
				angleInDegrees += 360.0;
			while (angleInDegrees >= lowerBoundInDegrees + 360.0)
				angleInDegrees -= 360.0;
		} else {
			while (angleInDegrees <= lowerBoundInDegrees)
				angleInDegrees += 360.0;
			while (angleInDegrees > lowerBoundInDegrees + 360.0)
				angleInDegrees -= 360.0;
		}
		return angleInDegrees;
	}

	/**
	 * Converts the given angle to the range [0.0, 360.0).
	 */
	public static final double normalize(double angleInDegrees) {
		return toRange(angleInDegrees, 0.0, true);
	}

	/**
	 * Converts the given angle to the range [0, 360).
	 */
	public static final int normalize(int angleInDegrees) {
		return toRange(angleInDegrees, 0, true);
	}

	/**
	 * Computes the proper average of a collection of angles. For example, the
	 * average of 45 degrees and 315 degrees is computed as 0 degrees and not
	 * 180 degrees. In the case of an indeterminate average, the value returned
	 * is the same as that of Math.atan2(0, 0). This can occur, for example,
	 * when averaging 0 degrees and 180 degrees. The average is returned in the
	 * range [0.0, 360.0).
	 */
	public static final double average(Collection<Double> anglesInDegrees) {
		Vector vectorSum = new Vector();
		for (double angleInDegrees : anglesInDegrees) {
			double angleInRadians = Math.toRadians(angleInDegrees);
			vectorSum.x += Math.cos(angleInRadians);
			vectorSum.y += Math.sin(angleInRadians);
		}
		return vectorSum.direction();
	}

	/**
	 * Computes the proper average of an array of angles. For example, the
	 * average of 45 degrees and 315 degrees is computed as 0 degrees and not
	 * 180 degrees. In the case of an indeterminate average, the value returned
	 * is the same as that of Math.atan2(0.0, 0.0). This can occur, for example,
	 * when averaging 0 degrees and 180 degrees. The average is returned in the
	 * range [0.0, 360.0).
	 */
	public static final double average(double[] anglesInDegrees) {
		return average(ArrayUtility.toList(ArrayUtility
				.toObject(anglesInDegrees)));
	}

	/**
	 * Computes the proper average of an array of angles. For example, the
	 * average of 45 degrees and 315 degrees is computed as 0 degrees and not
	 * 180 degrees. In the case of an indeterminate average, the value returned
	 * is the same as that of Math.atan2(0.0, 0.0). This can occur, for example,
	 * when averaging 0 degrees and 180 degrees. The average is returned in the
	 * range [0.0, 360.0).
	 */
	public static final double average(Double[] anglesInDegrees) {
		return average(ArrayUtility.toList(anglesInDegrees));
	}

	/**
	 * Computes the minimal absolute difference between the first angle and the
	 * second angle.
	 */
	public static final double absoluteDifference(double firstAngleInDegrees,
			double secondAngleInDegrees) {
		double correctDifferenceInDegrees = difference(firstAngleInDegrees,
				secondAngleInDegrees);
		double absoluteDifferenceInDegrees = Math
				.abs(correctDifferenceInDegrees);
		return absoluteDifferenceInDegrees;
	}

	/**
	 * Returns the minimal difference between the first angle and the second
	 * angle.
	 */
	public static final double difference(double firstAngleInDegrees,
			double secondAngleInDegrees) {
		double differenceInDegrees = firstAngleInDegrees - secondAngleInDegrees;
		double correctDifferenceInDegrees = toRange(differenceInDegrees,
				-180.0, false);
		return correctDifferenceInDegrees;
	}

	private AngleUtility() {

	}

	public static void main(String[] args) {
		AngleUtilityTest.testToRange();
		AngleUtilityTest.testNormalize();
		AngleUtilityTest.testAverage();
		AngleUtilityTest.testDifference();
	}

	private static class AngleUtilityTest {
		private static double ACCEPTABLE_ERROR = 1e-8;

		private static void testToRange() {
			testDoubleToRangeWithIncludedLowerBound();
			testDoubleToRangeWithExcludedLowerBound();
			testIntegerToRangeWithIncludedLowerBound();
			testIntegerToRangeWithExcludedLowerBound();
		}

		private static void testDoubleToRangeWithIncludedLowerBound() {
			testDoubleToRange(true);
		}

		private static void testDoubleToRangeWithExcludedLowerBound() {
			testDoubleToRange(false);
		}

		private static void testDoubleToRange(boolean includeLowerBound) {
			double lowerBoundInDegrees = AngleGenerator.generateDouble();
			double upperBoundInDegrees = lowerBoundInDegrees + 360.0;
			double correctAngleInDegrees = includeLowerBound ? lowerBoundInDegrees
					: upperBoundInDegrees;

			double convertedLowerBoundInDegrees = AngleUtility
					.toRange(lowerBoundInDegrees, lowerBoundInDegrees,
							includeLowerBound);
			double convertedUpperBoundInDegrees = AngleUtility
					.toRange(upperBoundInDegrees, lowerBoundInDegrees,
							includeLowerBound);

			Assert.assertEqual(convertedLowerBoundInDegrees,
					correctAngleInDegrees);
			Assert.assertEqual(convertedUpperBoundInDegrees,
					correctAngleInDegrees);
		}

		private static void testIntegerToRangeWithIncludedLowerBound() {
			testIntegerToRange(true);
		}

		private static void testIntegerToRangeWithExcludedLowerBound() {
			testIntegerToRange(false);
		}

		private static void testIntegerToRange(boolean includeLowerBound) {
			int lowerBoundInDegrees = AngleGenerator.generateInteger();
			int upperBoundInDegrees = lowerBoundInDegrees + 360;
			int correctAngleInDegrees = includeLowerBound ? lowerBoundInDegrees
					: upperBoundInDegrees;

			int convertedLowerBoundInDegrees = AngleUtility
					.toRange(lowerBoundInDegrees, lowerBoundInDegrees,
							includeLowerBound);
			int convertedUpperBoundInDegrees = AngleUtility
					.toRange(upperBoundInDegrees, lowerBoundInDegrees,
							includeLowerBound);

			Assert.assertEqual(convertedLowerBoundInDegrees,
					correctAngleInDegrees);
			Assert.assertEqual(convertedUpperBoundInDegrees,
					correctAngleInDegrees);
		}

		private static void testNormalize() {
			testDoubleNormalize();
			testIntegerNormalize();
		}

		private static void testDoubleNormalize() {
			Assert.assertEqual(normalize(0.0), 0.0);
			Assert.assertEqual(normalize(360.0), 0.0);
		}

		private static void testIntegerNormalize() {
			Assert.assertEqual(normalize(0), 0);
			Assert.assertEqual(normalize(360), 0);
		}

		private static void testAverage() {
			testAverageStandard();
			testAverageWrapAround();
			testAverageEmptyArray();
			testAverageResultRange();
		}

		private static void testAverageStandard() {
			double[] anglesInDegrees = { 0.0, 30.0, 45.0, 60.0, 90.0 };
			double averageInDegrees = average(anglesInDegrees);
			double properAverageInDegrees = 45.0;
			double error = AngleUtility.absoluteDifference(averageInDegrees,
					properAverageInDegrees);
			Assert.assertTrue(error <= ACCEPTABLE_ERROR);
		}

		private static void testAverageWrapAround() {
			double[] anglesInDegrees = { 30.0, 330.0 };
			double averageInDegrees = average(anglesInDegrees);
			double properAverageInDegrees = 0.0;
			double error = AngleUtility.absoluteDifference(averageInDegrees,
					properAverageInDegrees);
			Assert.assertTrue(error <= ACCEPTABLE_ERROR);
		}

		private static void testAverageEmptyArray() {
			double[] anglesInDegrees = {};
			double averageInDegrees = average(anglesInDegrees);
			double properAverageInDegrees = 0.0;
			double error = AngleUtility.absoluteDifference(averageInDegrees,
					properAverageInDegrees);
			Assert.assertTrue(error <= ACCEPTABLE_ERROR);
		}

		private static void testAverageResultRange() {
			double[] anglesInDegrees = { -360.0, -300.0 };
			double averageInDegrees = average(anglesInDegrees);
			double properAverageInDegrees = 30.0;
			double error = AngleUtility.absoluteDifference(averageInDegrees,
					properAverageInDegrees);
			Assert.assertTrue(error <= ACCEPTABLE_ERROR);
		}

		private static void testDifference() {
			testDifferenceStandard();
			testDifferenceWrapAround();
			testDifferenceEqual();
			testAbsoluteDifferenceStandard();
			testAbsoluteDifferenceWrapAround();
			testAbsoluteDifferenceEqual();
		}

		private static void testDifferenceStandard() {
			double differenceInDegrees = difference(60.0, 30.0);
			double correctDifferenceInDegrees = 30.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}

		private static void testDifferenceWrapAround() {
			double differenceInDegrees = difference(345.0, 15.0);
			double correctDifferenceInDegrees = -30.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}

		private static void testDifferenceEqual() {
			double differenceInDegrees = difference(60.0, 60.0);
			double correctDifferenceInDegrees = 0.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}

		private static void testAbsoluteDifferenceStandard() {
			double differenceInDegrees = absoluteDifference(60.0, 90.0);
			double correctDifferenceInDegrees = 30.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}

		private static void testAbsoluteDifferenceWrapAround() {
			double differenceInDegrees = absoluteDifference(345.0, 15.0);
			double correctDifferenceInDegrees = 30.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}

		private static void testAbsoluteDifferenceEqual() {
			double differenceInDegrees = absoluteDifference(30.0, 30.0);
			double correctDifferenceInDegrees = 0.0;
			Assert.assertEqual(differenceInDegrees, correctDifferenceInDegrees);
		}
	}

	private static class AngleGenerator {
		private static final int LOWER_BOUND = -360;
		private static final int UPPER_BOUND = 360;
		private static final Random RNG = new Random();

		private static double generateDouble() {
			double angleInDegrees = (RNG.nextDouble() % (UPPER_BOUND - LOWER_BOUND))
					+ LOWER_BOUND;
			return angleInDegrees;
		}

		private static int generateInteger() {
			int angleInDegrees = RNG.nextInt(UPPER_BOUND - LOWER_BOUND)
					+ LOWER_BOUND;
			return angleInDegrees;
		}
	}
}