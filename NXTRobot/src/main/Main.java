package main;

import lejos.nxt.Button;
import nxt.NXT;
import nxt.data.Point;
import nxt.data.Tile;

public class Main {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		int buttonChoice = Button.waitForAnyPress();
		switch (buttonChoice) {
		case Button.ID_ENTER:
		case Button.ID_LEFT:
		case Button.ID_RIGHT:
			break;
		case Button.ID_ESCAPE:
			return;
		}

		final Tile[] map1 = { new Tile(-1, 3), new Tile(-1, 7), new Tile(0, 3),
				new Tile(0, 5), new Tile(0, 9), new Tile(1, 6), new Tile(1, 8),
				new Tile(2, 4), new Tile(2, 5), new Tile(3, -1),
				new Tile(3, 0), new Tile(5, 1), new Tile(6, 0), new Tile(6, 2),
				new Tile(7, 1), new Tile(8, -1), new Tile(10, -1),
				new Tile(10, 1) };
		final Tile[] map2 = { new Tile(-1, 3), new Tile(-1, 7), new Tile(0, 3),
				new Tile(0, 7), new Tile(0, 9), new Tile(1, 4), new Tile(1, 9),
				new Tile(2, 7), new Tile(2, 9), new Tile(3, -1),
				new Tile(4, 0), new Tile(4, 1), new Tile(6, 0),
				new Tile(7, -1), new Tile(7, 2), new Tile(8, 1),
				new Tile(9, 0), new Tile(9, 2) };
		final Tile[] map3 = { new Tile(-1, 5), new Tile(-1, 10),
				new Tile(0, 3), new Tile(0, 5), new Tile(0, 8), new Tile(1, 6),
				new Tile(1, 9), new Tile(2, 3), new Tile(2, 9), new Tile(3, 0),
				new Tile(4, 1), new Tile(5, -1), new Tile(5, 2),
				new Tile(6, 0), new Tile(8, 0), new Tile(9, 1), new Tile(9, 2),
				new Tile(10, -1) };
		final Point firstTarget = new Point(9, 14);
		final Point secondTarget = new Point(14, 13);

		(new NXT() {
			@Override
			public void run() {
				this.finalCompetition(map3, firstTarget, secondTarget);
			}
		}).start();

		while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
			// Wait for escape button or for NXT to exit.
		}
		System.exit(0);
	}
}
