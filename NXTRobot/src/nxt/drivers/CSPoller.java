package nxt.drivers;

import lejos.nxt.Sound;
import nxt.NXTConstants;
import nxt.data.DataCenter;

/**
 * A class that continuously polls the color sensor for data and uses that data
 * to detect grid lines on the floor.
 *
 * @author Andrei Purcarus
 *
 */
public final class CSPoller extends Thread {
	private static final int MAX_VALUE_FOR_GRID_LINE = 48;

	private static final int NUM_VALUES_TO_STORE = 5;
	private int[] _csValues;

	private final DataCenter _dataCenter;

	public static final boolean isOnGridLine(int csValue) {
		return csValue <= MAX_VALUE_FOR_GRID_LINE;
	}

	public CSPoller(DataCenter dataCenter) {
		this._dataCenter = dataCenter;
		this._csValues = new int[NUM_VALUES_TO_STORE];
	}

	@Override
	public final void run() {
		initialize();
		while (true) {
			updateValues();
			sendCurrentValueToDataCenter();
			notifyDataCenterIfGridLineDetected();
		}
	}

	private void initialize() {
		NXTConstants.CS.setFloodlight(true);
		initializeValues();
	}

	private void initializeValues() {
		for (int i = 0; i < NUM_VALUES_TO_STORE; ++i) {
			this._csValues[i] = NXTConstants.CS.getLightValue();
		}
	}

	private void updateValues() {
		int csValue = NXTConstants.CS.getLightValue();
		for (int i = 0; i < NUM_VALUES_TO_STORE - 1; ++i) {
			this._csValues[i] = this._csValues[i + 1];
		}
		this._csValues[NUM_VALUES_TO_STORE - 1] = csValue;
	}

	private void sendCurrentValueToDataCenter() {
		this._dataCenter.setCSValue(getCurrentFilteredValue());
	}

	private void notifyDataCenterIfGridLineDetected() {
		int currentFilteredValue = getCurrentFilteredValue();
		if (isOnGridLine(currentFilteredValue)) {
			Sound.beep();
			this._dataCenter.notifyAllCSListeners();
		}
	}

	@SuppressWarnings("unused")
	private int getCurrentRawValue() {
		return this._csValues[NUM_VALUES_TO_STORE - 1];
	}

	private int getCurrentFilteredValue() {
		int sumOfCSValues = 0;
		for (int i = 0; i < NUM_VALUES_TO_STORE; ++i) {
			sumOfCSValues += this._csValues[i];
		}
		int averageCSValue = sumOfCSValues / NUM_VALUES_TO_STORE;
		return averageCSValue;
	}
}
