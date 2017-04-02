package main;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * The controller class for the loading menu screen.
 * 
 * @author Andrei Purcarus
 *
 */
public final class LoadingMenuController {
	private Main _main;
	private String _nxtName;

	public final void setMain(Main main) {
		this._main = main;
	}

	public final void setNXTName(String nxtName) {
		this._nxtName = nxtName;
		this.connectingLabel.setText("Connecting to " + this._nxtName + "...");
	}

	@FXML
	private Label connectingLabel;

	@FXML
	private void onCancel() throws IOException {
		this._main.setConnectionMenu();
	}
}
