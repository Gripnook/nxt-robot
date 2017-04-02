package nxt.drivers;

import lejos.nxt.Sound;
import nxt.NXTConstants;

/**
 * A class used to control a ping-pong ball launcher. This class is thread-safe.
 *
 * @author Andrei Purcarus
 * @author Leotard Niyonkuru
 *
 */
public final class Launcher {
	private final static int LOW_MOTOR_SPEED = 100;
	private final static int MED_MOTOR_SPEED = 300;
	private final static int HIGH_MOTOR_SPEED = 500;

	private final static long LOAD_BALL_WAIT_TIME = 500;

	synchronized public static final void fire() {
		launchFromStored();
		storeFromLaunched();
	}

	synchronized public static final void fire(int numberOfBalls) {
		if (numberOfBalls > 0) {
			launchFromStored();
		}
		for (int i = 1; i < numberOfBalls; ++i) {
			launchFromLaunched();
		}
		storeFromLaunched();
	}

	private Launcher() {

	}

	private static void launchFromStored() {
		activateFromStored();
		armFromActive();
		launchFromArmed();
	}

	private static void launchFromLaunched() {
		activateFromLaunched();
		armFromActive();
		launchFromArmed();
	}

	private static void activateFromStored() {
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(-360);
	}

	private static void activateFromLaunched() {
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(-270);
	}

	private static void armFromActive() {
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(90);
		NXTConstants.LAUNCHER_MOTOR.setSpeed(LOW_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(75);

		// Shakes the launcher to try to ensure that the ball is loaded
		// properly.
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(-45);
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(50);
	}

	private static void launchFromArmed() {
		waitForLoad();
		Sound.twoBeeps();
		NXTConstants.LAUNCHER_MOTOR.setSpeed(MED_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(100);
	}

	private static void storeFromLaunched() {
		NXTConstants.LAUNCHER_MOTOR.setSpeed(HIGH_MOTOR_SPEED);
		NXTConstants.LAUNCHER_MOTOR.rotate(90);
	}

	private static void waitForLoad() {
		try {
			Thread.sleep(LOAD_BALL_WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
