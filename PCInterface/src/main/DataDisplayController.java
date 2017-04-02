package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import comm.BluetoothReciever;
import comm.data.BluetoothDataCenter;
import comm.data.Position;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

/**
 * The controller class for the data display screen.
 * 
 * @author Andrei Purcarus
 *
 */
public final class DataDisplayController {
	private Main _main;
	private BluetoothReciever _bluetoothReciever;
	private BluetoothDataCenter _dataCenter;

	/**
	 * Start time for the charts in ms.
	 */
	private long _startTime;
	private boolean _clearData = false;
	private boolean _writeToFile = false;

	private ArrayList<Long> _timeValues = new ArrayList<>();
	private ArrayList<Double> _xValues = new ArrayList<>();
	private ArrayList<Double> _yValues = new ArrayList<>();
	private ArrayList<Double> _orientationValues = new ArrayList<>();
	private ArrayList<Integer> _leftUSValues = new ArrayList<>();
	private ArrayList<Integer> _frontUSValues = new ArrayList<>();
	private ArrayList<Integer> _rightUSValues = new ArrayList<>();
	private ArrayList<Integer> _csValues = new ArrayList<>();

	private XYChart.Series<Number, Number> _leftUSSeries = new XYChart.Series<>();
	private XYChart.Series<Number, Number> _frontUSSeries = new XYChart.Series<>();
	private XYChart.Series<Number, Number> _rightUSSeries = new XYChart.Series<>();
	private XYChart.Series<Number, Number> _csSeries = new XYChart.Series<>();

	public final void setMain(Main main) {
		this._main = main;
	}

	public final void setBluetoothReciever(BluetoothReciever bluetoothReciever) {
		this._bluetoothReciever = bluetoothReciever;
		this._dataCenter = this._bluetoothReciever.getDataCenter();
	}

	public final void start() {
		this._bluetoothReciever.start();
		initializeCharts();
		startChartAnimation();
	}

	private void initializeCharts() {
		topLeftChart.setTitle("Left US Distance");
		topLeftChart.getXAxis().setLabel("time (ms)");
		topLeftChart.getYAxis().setLabel("distance (cm)");
		topLeftChart.getData().add(_leftUSSeries);

		topRightChart.setTitle("Front US Distance");
		topRightChart.getXAxis().setLabel("time (ms)");
		topRightChart.getYAxis().setLabel("distance (cm)");
		topRightChart.getData().add(_frontUSSeries);

		bottomLeftChart.setTitle("Right US Distance");
		bottomLeftChart.getXAxis().setLabel("time (ms)");
		bottomLeftChart.getYAxis().setLabel("distance (cm)");
		bottomLeftChart.getData().add(_rightUSSeries);

		bottomRightChart.setTitle("CS Value");
		bottomRightChart.getXAxis().setLabel("time (ms)");
		bottomRightChart.getYAxis().setLabel("value");
		bottomRightChart.getData().add(_csSeries);
	}

	private void startChartAnimation() {
		this._startTime = System.currentTimeMillis();
		new AnimationTimer() {
			private static final long CHART_ANIMATION_PERIOD = 50_000_000;
			private long _lastAnimationTime = System.nanoTime();

			@Override
			public void handle(long now) {
				if (now - this._lastAnimationTime > CHART_ANIMATION_PERIOD) {
					addData();
					clearDataIfNeeded();
					writeToFileIfNeeded();
				}
			}
		}.start();
	}

	private void addData() {
		long currentTime = System.currentTimeMillis();
		long time = currentTime - _startTime;
		Position position = _dataCenter.getPosition();
		int leftUSDistance = _dataCenter.getUSDistanceAtAngle(90);
		int frontUSDistance = _dataCenter.getUSDistanceAtAngle(0);
		int rightUSDistance = _dataCenter.getUSDistanceAtAngle(-90);
		int csValue = _dataCenter.getCSValue();
		this._timeValues.add(time);
		this._xValues.add(position.x);
		this._yValues.add(position.y);
		this._orientationValues.add(position.orientation);
		this._leftUSValues.add(leftUSDistance);
		this._frontUSValues.add(frontUSDistance);
		this._rightUSValues.add(rightUSDistance);
		this._csValues.add(csValue);
		this._leftUSSeries.getData().add(
				new Data<Number, Number>(time, leftUSDistance));
		this._frontUSSeries.getData().add(
				new Data<Number, Number>(time, frontUSDistance));
		this._rightUSSeries.getData().add(
				new Data<Number, Number>(time, rightUSDistance));
		this._csSeries.getData().add(new Data<Number, Number>(time, csValue));
	}

	private void clearDataIfNeeded() {
		boolean clearData = false;
		synchronized (this) {
			clearData = this._clearData;
		}
		if (clearData) {
			clearData();
			synchronized (this) {
				this._clearData = false;
			}
		}
	}

	private void writeToFileIfNeeded() {
		boolean writeToFile = false;
		synchronized (this) {
			writeToFile = this._writeToFile;
		}
		if (writeToFile) {
			writeToFile();
			clearData();
			synchronized (this) {
				this._writeToFile = false;
			}
		}
	}

	private void clearData() {
		setChartsVisible(false);
		this._timeValues = new ArrayList<>();
		this._xValues = new ArrayList<>();
		this._yValues = new ArrayList<>();
		this._orientationValues = new ArrayList<>();
		this._leftUSValues = new ArrayList<>();
		this._frontUSValues = new ArrayList<>();
		this._rightUSValues = new ArrayList<>();
		this._csValues = new ArrayList<>();
		this._leftUSSeries.getData().clear();
		this._frontUSSeries.getData().clear();
		this._rightUSSeries.getData().clear();
		this._csSeries.getData().clear();
		setChartsVisible(true);
		this._startTime = System.currentTimeMillis();
	}

	private void setChartsVisible(boolean value) {
		topRightChart.setVisible(value);
		topLeftChart.setVisible(value);
		bottomRightChart.setVisible(value);
		bottomLeftChart.setVisible(value);
	}

	private void writeToFile() {
		File file = new File("nxt_data.xlsx");
		int versionNumber = 1;
		while (file.exists()) {
			file = new File("nxt_data(" + Integer.toString(versionNumber++)
					+ ").xlsx");
		}
		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter
					.write("Time\tX\tY\tOrientation\tLeft\tFront\tRight\tCS\n");
			for (int i = 0; i < this._timeValues.size(); ++i) {
				fileWriter.write(this._timeValues.get(i) + "\t"
						+ this._xValues.get(i) + "\t" + this._yValues.get(i)
						+ "\t" + this._orientationValues.get(i) + "\t"
						+ this._leftUSValues.get(i) + "\t"
						+ this._frontUSValues.get(i) + "\t"
						+ this._rightUSValues.get(i) + "\t"
						+ this._csValues.get(i) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private XYChart<Number, Number> topLeftChart;
	@FXML
	private XYChart<Number, Number> topRightChart;
	@FXML
	private XYChart<Number, Number> bottomLeftChart;
	@FXML
	private XYChart<Number, Number> bottomRightChart;

	@FXML
	private void onClearData() {
		synchronized (this) {
			this._clearData = true;
		}
	}

	@FXML
	private void onWriteToFile() {
		synchronized (this) {
			this._writeToFile = true;
		}
	}

	@FXML
	private void onEndTransmission() throws IOException {
		this._bluetoothReciever.endTransmission();
		clearData();
		this._main.setConnectionMenu();
	}
}
