package comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import comm.BluetoothProtocol;
import comm.data.BluetoothDataCenter;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/**
 * A class to receive data from the robot and store it. The thread will run as
 * long as the robot does not end the transmission.
 * 
 * @author Andrei Purcarus
 *
 */
public final class BluetoothReciever extends Thread {
	private DataInputStream _inputStream;
	private DataOutputStream _outputStream;

	private BluetoothDataCenter _dataCenter;

	private boolean _isRecieving;

	public BluetoothReciever(String nxtName, String nxtID,
			BluetoothDataCenter dataCenter) throws NXTCommException {
		NXTComm nxtCommunication = NXTCommFactory
				.createNXTComm(NXTCommFactory.BLUETOOTH);
		NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, nxtName, nxtID);
		nxtCommunication.open(nxtInfo);
		this._inputStream = new DataInputStream(
				nxtCommunication.getInputStream());
		this._outputStream = new DataOutputStream(
				nxtCommunication.getOutputStream());
		this._dataCenter = dataCenter;
		this._isRecieving = false;
	}

	@Override
	public final void run() {
		startRecieving();
		while (isRecieving()) {
			try {
				recieveData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public final BluetoothDataCenter getDataCenter() {
		return this._dataCenter;
	}

	public final void endTransmission() throws IOException {
		if (isRecieving()) {
			this._outputStream.writeByte(BluetoothProtocol.END_TRANSMISSION);
			this._outputStream.flush();
			stopRecieving();
		}
	}

	private void recieveData() throws IOException {
		if (isRecieving()) {
			byte message = this._inputStream.readByte();
			switch (message) {
			case BluetoothProtocol.X_POSITION:
				double x = this._inputStream.readDouble();
				this._dataCenter.setXPosition(x);
				break;
			case BluetoothProtocol.Y_POSITION:
				double y = this._inputStream.readDouble();
				this._dataCenter.setYPosition(y);
				break;
			case BluetoothProtocol.ORIENTATION:
				double orientation = this._inputStream.readDouble();
				this._dataCenter.setOrientation(orientation);
				break;
			case BluetoothProtocol.LEFT_US_DISTANCE:
				int leftDistance = this._inputStream.readInt();
				this._dataCenter.setUSDistanceAtAngle(leftDistance, 90);
				break;
			case BluetoothProtocol.FRONT_US_DISTANCE:
				int frontDistance = this._inputStream.readInt();
				this._dataCenter.setUSDistanceAtAngle(frontDistance, 0);
				break;
			case BluetoothProtocol.RIGHT_US_DISTANCE:
				int rightDistance = this._inputStream.readInt();
				this._dataCenter.setUSDistanceAtAngle(rightDistance, -90);
				break;
			case BluetoothProtocol.CS_VALUE:
				int csValue = this._inputStream.readInt();
				this._dataCenter.setCSValue(csValue);
				break;
			case BluetoothProtocol.END_TRANSMISSION:
				stopRecieving();
				break;
			default:
				// Do nothing.
			}
		}
	}

	private synchronized boolean isRecieving() {
		return this._isRecieving;
	}

	private synchronized void startRecieving() {
		this._isRecieving = true;
	}

	private synchronized void stopRecieving() {
		this._isRecieving = false;
	}
}
