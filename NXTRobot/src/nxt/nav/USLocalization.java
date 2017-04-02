package nxt.nav;

import java.util.ArrayList;
import java.util.List;

import nxt.NXTConstants;
import nxt.data.AngleToDistancePair;
import nxt.data.DataCenter;
import nxt.data.Pair;
import nxt.drivers.USPoller;
import nxt.util.AngleUtility;

/**
 * A class used to localize the robot using the wall and ultrasonic sensor data.
 *
 * @author Andrei Purcarus
 *
 */
public final class USLocalization {
	private static final long WAIT_TIME_AFTER_LOCALIZING = 100;

	private final DataCenter _dataCenter;
	private final Navigation _navigation;

	public USLocalization(DataCenter dataCenter, Navigation navigation) {
		this._dataCenter = dataCenter;
		this._navigation = navigation;
	}

	public final void doLocalization() {
		doMinimaLocalization();
	}

	/**
	 * Performs localization. Makes the robot turn 360 degrees while logging
	 * angles and distance pairs from the odometer and ultrasonic sensor
	 * respectively. Then computes the minimum distance to the walls and takes
	 * the corresponding angle T to be either 180 degrees or 270 degrees. Then
	 * checks which of T+90 or T-90 has a smaller distance associated with it,
	 * and uses that angle as the other of 180 degrees or 270 degrees. Then
	 * determines which is which and updates the position of the robot
	 * accordingly.
	 */
	private void doMinimaLocalization() {
		List<AngleToDistancePair> anglesToUSDistances = getData();

		int minimumUSDistance = getMinimumUSDistance(anglesToUSDistances);

		List<AngleToDistancePair> anglesToMinimumUSDistances = findAllEntriesWithRawUSDistance(
				anglesToUSDistances, minimumUSDistance);

		double minimumAngle = AngleUtility
				.average(getAnglesArray(anglesToMinimumUSDistances));

		Pair<AngleToDistancePair, AngleToDistancePair> xAndYEntries = getXAndYEntries(
				anglesToUSDistances, minimumAngle, minimumUSDistance);
		setPosition(xAndYEntries);

		waitForOtherThreadsToUpdate();
	}

	private List<AngleToDistancePair> getData() {
		List<AngleToDistancePair> anglesToUSDistances = new ArrayList<>();

		Thread robotTurning = new Thread() {
			@Override
			public void run() {
				// Turn slowly for data collection.
				_navigation.turn(360, 30);
			}
		};
		robotTurning.start();

		while (robotTurning.isAlive()) {
			addData(anglesToUSDistances);
			USPoller.waitTimeRequiredForPolling();
		}

		return anglesToUSDistances;
	}

	private void addData(List<AngleToDistancePair> anglesToUSDistances) {
		double angle = this._dataCenter.getOrientation();
		int rawUSDistance = this._dataCenter.getRawUSDistanceAtAngle(0);
		int filteredUSDistance = this._dataCenter
				.getFilteredUSDistanceAtAngle(0);
		anglesToUSDistances.add(new AngleToDistancePair(angle, rawUSDistance,
				filteredUSDistance));
	}

	private int getMinimumUSDistance(
			List<AngleToDistancePair> anglesToUSDistances) {
		int minimum = 255;
		for (AngleToDistancePair angleToUSDistances : anglesToUSDistances) {
			int rawUSDistance = angleToUSDistances.rawDistance;
			if (rawUSDistance < minimum)
				minimum = rawUSDistance;
		}
		return minimum;
	}

	private List<AngleToDistancePair> findAllEntriesWithRawUSDistance(
			List<AngleToDistancePair> anglesToUSDistances, int rawUSDistance) {
		List<AngleToDistancePair> result = new ArrayList<>();
		for (AngleToDistancePair angleToUSDistance : anglesToUSDistances) {
			if (angleToUSDistance.rawDistance == rawUSDistance) {
				result.add(angleToUSDistance);
			}
		}
		return result;
	}

	private Pair<AngleToDistancePair, AngleToDistancePair> getXAndYEntries(
			List<AngleToDistancePair> anglesToUSDistances, double minimumAngle,
			int minimumDistance) {
		AngleToDistancePair firstMinimumEntry = new AngleToDistancePair(
				minimumAngle, minimumDistance, minimumDistance);

		AngleToDistancePair secondMinimumEntry = getSecondMinimumEntry(
				anglesToUSDistances, minimumAngle);

		boolean firstEntryIsXEntry = AngleUtility.difference(
				secondMinimumEntry.angle, minimumAngle) > 0;

		AngleToDistancePair xEntry = firstEntryIsXEntry ? firstMinimumEntry
				: secondMinimumEntry;
		AngleToDistancePair yEntry = firstEntryIsXEntry ? secondMinimumEntry
				: firstMinimumEntry;

		return new Pair<>(xEntry, yEntry);
	}

	private AngleToDistancePair getSecondMinimumEntry(
			List<AngleToDistancePair> anglesToUSDistances, double minimumAngle) {
		double counterclockwiseMinimumAngle = AngleUtility
				.normalize(minimumAngle + 90);
		double clockwiseMinimumAngle = AngleUtility
				.normalize(minimumAngle - 90);

		AngleToDistancePair counterclockwiseEntry = find(anglesToUSDistances,
				counterclockwiseMinimumAngle);
		AngleToDistancePair clockwiseEntry = find(anglesToUSDistances,
				clockwiseMinimumAngle);

		return counterclockwiseEntry.filteredDistance <= clockwiseEntry.filteredDistance ? counterclockwiseEntry
				: clockwiseEntry;
	}

	private void setPosition(
			Pair<AngleToDistancePair, AngleToDistancePair> xAndYEntries) {
		double x = NXTConstants.FRONT_US_DISTANCE
				+ xAndYEntries.first.filteredDistance
				- NXTConstants.TILE_DISTANCE;
		double y = NXTConstants.FRONT_US_DISTANCE
				+ xAndYEntries.second.filteredDistance
				- NXTConstants.TILE_DISTANCE;
		double xAngleCorrectionFactor = AngleUtility
				.normalize(180.0 - xAndYEntries.first.angle);
		double yAngleCorrectionFactor = AngleUtility
				.normalize(270.0 - xAndYEntries.second.angle);
		double angleCorrectionFactor = AngleUtility.average(new double[] {
				xAngleCorrectionFactor, yAngleCorrectionFactor });
		double angle = this._dataCenter.getOrientation()
				+ angleCorrectionFactor;
		this._dataCenter.setPosition(x, y, angle);
	}

	private double[] getAnglesArray(
			List<AngleToDistancePair> anglesToUSDistances) {
		double[] anglesArray = new double[anglesToUSDistances.size()];
		for (int i = 0; i < anglesArray.length; ++i) {
			anglesArray[i] = anglesToUSDistances.get(i).angle;
		}
		return anglesArray;
	}

	/**
	 * Finds the entry whose angle is closest to the given angle.
	 */
	private AngleToDistancePair find(
			List<AngleToDistancePair> anglesToUSDistances, double angle) {
		AngleToDistancePair result = null;
		double minimumDistance = 180;
		for (AngleToDistancePair angleToUSDistances : anglesToUSDistances) {
			double currentDistance = AngleUtility.absoluteDifference(
					angleToUSDistances.angle, angle);
			if (currentDistance < minimumDistance) {
				minimumDistance = currentDistance;
				result = angleToUSDistances;
			}
		}
		return result;
	}

	private static void waitForOtherThreadsToUpdate() {
		try {
			Thread.sleep(WAIT_TIME_AFTER_LOCALIZING);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
