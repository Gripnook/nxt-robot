package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import comm.BluetoothReciever;
import comm.data.BluetoothDataCenter;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lejos.pc.comm.NXTCommException;

/**
 * The controller class for the NXT connection menu screen.
 * 
 * @author Andrei Purcarus
 *
 */
public final class ConnectionMenuController {
	private static final String PATH_TO_DEFAULT_FIELD_FILE = "res"
			+ File.separator + "defaultConnection.txt";

	private Main _main;

	public void setMain(Main main) {
		this._main = main;
	}

	/**
	 * Connect with the last recorded parameters.
	 */
	public final void connect() throws IOException {
		onConnect();
	}

	@FXML
	private void initialize() {
		loadDefaultFields();
	}

	private void loadDefaultFields() {
		File defaultFieldsFile = new File(PATH_TO_DEFAULT_FIELD_FILE);
		try (FileReader fileReader = new FileReader(defaultFieldsFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			String name = bufferedReader.readLine();
			String id = bufferedReader.readLine();
			// Removes initial qualifier.
			name = name.replaceAll(".*=", "");
			id = id.replaceAll(".*=", "");
			this.nxtNameField.setText(name);
			this.nxtIDField.setText(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectToNXT(String name, String id) throws IOException {
		this._main.setLoadingMenu(name);
		try {
			BluetoothReciever bluetoothReciever = new BluetoothReciever(name,
					id, new BluetoothDataCenter());
			this._main.setDataDisplay(bluetoothReciever);
		} catch (NXTCommException e) {
			this._main.setErrorMessage(name);
		}
	}

	@FXML
	private TextField nxtNameField;

	@FXML
	private TextField nxtIDField;

	@FXML
	private void onConnect() throws IOException {
		String name = this.nxtNameField.getText().trim();
		String id = this.nxtIDField.getText().trim();
		connectToNXT(name, id);
	}

	@FXML
	private void onExit() {
		System.exit(0);
	}
}
