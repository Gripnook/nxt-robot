package main;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * The controller class for the error message screen.
 * 
 * @author Andrei Purcarus
 *
 */
public final class ErrorMessageController {
	private Main _main;
	private String _nxtName;

	public final void setMain(Main main) {
		this._main = main;
	}

	public final void setNXTName(String nxtName) {
		this._nxtName = nxtName;
		this.errorMessageLabel.setText("Failed to connect to " + this._nxtName
				+ ".");
	}

	@FXML
	private Label errorMessageLabel;

	@FXML
	private void onTryAgain() throws IOException {
		this._main.resetLoadingMenuForAnotherTrial(this._nxtName);
	}

	@FXML
	private void onCancel() throws IOException {
		this._main.setConnectionMenu();
	}
}