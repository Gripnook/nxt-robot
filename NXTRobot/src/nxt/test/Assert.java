package nxt.test;

/**
 * A class providing static methods useful for testing purposes.
 *
 * @author Andrei Purcarus
 *
 */
public final class Assert {
	public static final void assertTrue(boolean expression,
			String messageOnFailure) {
		if (!expression)
			throw new AssertionError(messageOnFailure);
	}

	public static final void assertTrue(boolean expression) {
		assertTrue(expression, "");
	}

	public static final void assertFalse(boolean expression,
			String messageOnFailure) {
		assertTrue(!expression, messageOnFailure);
	}

	public static final void assertFalse(boolean expression) {
		assertFalse(expression, "");
	}

	public static final void assertEqual(Object first, Object second,
			String messageOnFailure) {
		assertTrue(first.equals(second), messageOnFailure);
	}

	public static final void assertEqual(Object first, Object second) {
		assertEqual(first, second, "");
	}

	public static final void assertNotEqual(Object first, Object second,
			String messageOnFailure) {
		assertFalse(first.equals(second), messageOnFailure);
	}

	public static final void assertNotEqual(Object first, Object second) {
		assertNotEqual(first, second, "");
	}

	private Assert() {

	}
}
