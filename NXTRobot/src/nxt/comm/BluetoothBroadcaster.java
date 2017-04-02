package nxt.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nxt.data.DataCenter;
import nxt.data.Position;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * A class for communicating via Bluetooth to the computer in order to send data
 * for analysis. The data is sent in pairs, with the first a byte being sent to
 * indicate the type of the second.
 * 
 * @author Andrei Purcarus
 * @author Leotard Niyonkuru
 *
 */
public final class BluetoothBroadcaster extends Thread {
	private static final long BROADCAST_PERIOD = 25;

	private final DataInputStream _inputStream;
	private final DataOutputStream _outputStream;

	private final DataCenter _dataCenter;

	private boolean _isBroadcasting;
	private final Object _sendLock;

	public BluetoothBroadcaster(DataCenter dataCenter) {
		BTConnection connection = Bluetooth.waitForConnection();
		this._inputStream = connection.openDataInputStream();
		this._outputStream = connection.openDataOutputStream();
		this._dataCenter = dataCenter;
		this._isBroadcasting = false;
		this._sendLock = new Object();
		waitForEndTransmissionFromComputerInNewThread();
	}

	@Override
	public final void run() {
		startBroadcasting();
		long startTime, endTime;
		while (isBroadcasting()) {
			startTime = System.currentTimeMillis();
			try {
				broadcastData();
			} catch (IOException e) {
				e.printStackTrace();
			}
			endTime = System.currentTimeMillis();
			long timeElapsedSinceLastPeriod = endTime - startTime;
			if (timeElapsedSinceLastPeriod < BROADCAST_PERIOD) {
				waitUntilBroadcastPeriodIsComplete(timeElapsedSinceLastPeriod);
			}
		}
	}

	public final void endTransmission() throws IOException {
		synchronized (this._sendLock) {
			if (isBroadcasting()) {
				this._outputStream
						.writeByte(BluetoothProtocol.END_TRANSMISSION);
				this._outputStream.flush();
				stopBroadcasting();
			}
		}
	}

	private void broadcastData() throws IOException {
		synchronized (this._sendLock) {
			if (isBroadcasting()) {
				Position position = this._dataCenter.getPosition();
				this._outputStream.writeByte(BluetoothProtocol.X_POSITION);
				this._outputStream.writeDouble(position.x);
				this._outputStream.writeByte(BluetoothProtocol.Y_POSITION);
				this._outputStream.writeDouble(position.y);
				this._outputStream.writeByte(BluetoothProtocol.ORIENTATION);
				this._outputStream.writeDouble(position.orientation);
				this._outputStream
						.writeByte(BluetoothProtocol.LEFT_US_DISTANCE);
				this._outputStream.writeInt(this._dataCenter
						.getFilteredUSDistanceAtAngle(90));
				this._outputStream
						.writeByte(BluetoothProtocol.FRONT_US_DISTANCE);
				this._outputStream.writeInt(this._dataCenter
						.getFilteredUSDistanceAtAngle(0));
				this._outputStream
						.writeByte(BluetoothProtocol.RIGHT_US_DISTANCE);
				this._outputStream.writeInt(this._dataCenter
						.getFilteredUSDistanceAtAngle(-90));
				this._outputStream.writeByte(BluetoothProtocol.CS_VALUE);
				this._outputStream.writeInt(this._dataCenter.getCSValue());
				this._outputStream.flush();
			}
		}
	}

	private synchronized boolean isBroadcasting() {
		return this._isBroadcasting;
	}

	private synchronized void startBroadcasting() {
		this._isBroadcasting = true;
	}

	private synchronized void stopBroadcasting() {
		this._isBroadcasting = false;
	}

	private void waitForEndTransmissionFromComputerInNewThread() {
		(new Thread() {
			public void run() {
				while (true) {
					byte message = BluetoothProtocol.NULL_MESSAGE;
					try {
						message = _inputStream.readByte();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (message == BluetoothProtocol.END_TRANSMISSION) {
						stopBroadcasting();
						System.exit(0);
					}
				}
			}
		}).start();
	}

	private static void waitUntilBroadcastPeriodIsComplete(
			long timeElapsedSinceLastPeriod) {
		try {
			Thread.sleep(BROADCAST_PERIOD - timeElapsedSinceLastPeriod);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
