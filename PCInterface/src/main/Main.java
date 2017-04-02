package main;

import java.io.IOException;

import comm.BluetoothReciever;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main class for the application. Manages the different controllers and
 * views.
 * 
 * @author Andrei Purcarus
 *
 */
public final class Main extends Application {
	private Stage _stage;
	private ConnectionMenuController _connectionMenu;
	private LoadingMenuController _loadingMenu;
	private ErrorMessageController _errorMessage;
	private DataDisplayController _dataDisplay;

	private Parent _connectionMenuRoot;
	private Parent _loadingMenuRoot;
	private Parent _errorMessageRoot;
	private Parent _dataDisplayRoot;

	@Override
	public final void start(Stage stage) throws IOException {
		this._stage = stage;
		loadConnectionMenu();
		loadLoadingMenu();
		loadErrorMessage();
		loadDataDisplay();
		setStage(this._connectionMenuRoot, "Connection Menu");
	}

	public final static void main(String[] args) {
		launch(args);
	}

	public final void setConnectionMenu() throws IOException {
		closeStage();
		setStage(this._connectionMenuRoot, "Connection Menu");
	}

	public final void setLoadingMenu(String nxtName) throws IOException {
		closeStage();
		setStage(this._loadingMenuRoot, "Connecting...");
		this._loadingMenu.setNXTName(nxtName);
	}

	public final void resetLoadingMenuForAnotherTrial(String nxtName)
			throws IOException {
		this._connectionMenu.connect();
	}

	public final void setErrorMessage(String nxtName) throws IOException {
		closeStage();
		setStage(this._errorMessageRoot, "Error");
		this._errorMessage.setNXTName(nxtName);
	}

	public final void setDataDisplay(BluetoothReciever bluetoothReciever)
			throws IOException {
		closeStage();
		setStage(this._dataDisplayRoot, "Data Display");
		this._dataDisplay.setBluetoothReciever(bluetoothReciever);
		this._dataDisplay.start();
	}

	private void loadConnectionMenu() throws IOException {
		FXMLLoader loader = getLoader("ConnectionMenu.fxml");
		this._connectionMenuRoot = loader.load();
		this._connectionMenu = loader.getController();
		this._connectionMenu.setMain(this);
	}

	private void loadLoadingMenu() throws IOException {
		FXMLLoader loader = getLoader("LoadingMenu.fxml");
		this._loadingMenuRoot = loader.load();
		this._loadingMenu = loader.getController();
		this._loadingMenu.setMain(this);
	}

	private void loadErrorMessage() throws IOException {
		FXMLLoader loader = getLoader("ErrorMessage.fxml");
		this._errorMessageRoot = loader.load();
		this._errorMessage = loader.getController();
		this._errorMessage.setMain(this);
	}

	private void loadDataDisplay() throws IOException {
		FXMLLoader loader = getLoader("DataDisplay.fxml");
		this._dataDisplayRoot = loader.load();
		this._dataDisplay = loader.getController();
		this._dataDisplay.setMain(this);
	}

	private FXMLLoader getLoader(String fxml) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(fxml));
		return loader;
	}

	private void setStage(Parent root, String title) throws IOException {
		this._stage = new Stage();
		this._stage.setTitle(title);
		if (root.getScene() == null) {
			Scene scene = new Scene(root);
			this._stage.setScene(scene);
		} else {
			this._stage.setScene(root.getScene());
		}
		this._stage.centerOnScreen();
		this._stage.show();
	}

	private void closeStage() {
		((Stage) this._stage.getScene().getWindow()).close();
	}
}
