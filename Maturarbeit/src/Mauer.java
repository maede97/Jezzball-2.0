// Mauer.java
import java.awt.Color;
import java.awt.Point;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

public class Mauer extends Actor {
	private int ausrichtung; // Oben-Unten / Links-Rechts
	private boolean zweiter;
	// False wenn diese Mauer durch Klicken gebaut wurde
	private Actor kollege;
	private Mauer kollege_Mauer;
	// Partner, hat gegenübergesetzte Ausrichtung
	private boolean erst = true; // Nur für Erstausführung

	public boolean angehalten = false;

	private Jezzball mainClass;

	public Mauer(int ausrichtung, boolean zweiter, Jezzball jezzball) {
		// Alle übergebeben Variablen zu Variablen dieser Klasse machen
		this.ausrichtung = ausrichtung * 90;
		// Die Ausrichtung wird in Grad umgerechnet
		this.zweiter = zweiter;
		this.mainClass = jezzball;
	}

	public boolean isInside(int lower, int upper, int zahl) {
		// <true> falls <zahl> zwischen <lower> und <upper> liegt,
		// sonst <false>
		if (lower > upper) {
			int temp = lower;
			lower = upper;
			upper = temp;
		}
		if (zahl >= lower && zahl <= upper) {
			return true;
		} else {
			return false;
		}

	}

	public Location MoveLocation(int count) {
		// Gibt Location in <count> Scritten zurück
		Location loc_old = getLocation();
		move(count);
		Location new_loc = getLocation();
		setLocation(loc_old);
		return new_loc;
	}

	public boolean hasStopped() {
		return angehalten;
	}

	public Point Loc2Point(Location loc) {
		return new Point(loc.getX(), loc.getY());
	}

	public void act() {
		/*
		 * 1. Solange Aufbau bis schwarz oder Ausser Feld
		 * 
		 * 2. Mauer in Feste Mauer verwandeln
		 * 
		 * 3. Kugel-Box verkleinern
		 * 
		 * 4. Füllen
		 * 
		 * 5. Mauern entfernen und Punkte ausrechnen
		 */

		if (erst) {
			// Nur bei Erstausführung
			setDirection(ausrichtung);
			if (ausrichtung == 0 && !zweiter) {
				// Links-Rechts
				kollege = new Mauer(2, true, this.mainClass);
				kollege_Mauer = (Mauer) kollege;
				gameGrid.addActor(kollege, getLocation());
				erst = false;
			} else if (ausrichtung == 90 && !zweiter) {
				// Oben-Unten
				kollege = new Mauer(3, true, this.mainClass);
				kollege_Mauer = (Mauer) kollege;
				gameGrid.addActor(kollege, getLocation());
				erst = false;
			}
		}

		// 1.
		if (isInGrid() && gameGrid.getBg().getColor(MoveLocation(1)).equals(Color.WHITE)) {
			gameGrid.getBg().fillCell(getLocation(), Color.BLACK);
			move(1);
		} else {
			angehalten = true;
			// Anhalten

			// 2.
			if (!zweiter && kollege_Mauer.hasStopped()) {
				gameGrid.getBg().setPaintColor(Color.BLACK);
				gameGrid.getBg().drawLine(Loc2Point(getLocation()), Loc2Point(kollege_Mauer.getLocation()));
				// Mauer in Schwarz verwandeln

				// 3.

				for (Actor kugel_Actor : mainClass.Kugeln) {
					Kugel kugel = (Kugel) kugel_Actor;

					if (ausrichtung == 0) {
						// Mauer unter Kugel?
						if (getLocation().getY() > kugel.getLocation().getY()) {
							if (isInside(kugel.getUnten(), kugel.getOben(), getLocation().getY())) {
								if (isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
									kugel.setUnten(getLocation().getY());
								}
							}
						}
						// Mauer über Kugel?
						if (getLocation().getY() < kugel.getLocation().getY()) {
							if (isInside(kugel.getUnten(), kugel.getOben(), getLocation().getY())) {
								if (isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
									kugel.setOben(getLocation().getY());
								}
							}
						}
					} else {
						// Mauer rechts der Kugel?
						if (getLocation().getX() > kugel.getLocation().getX()) {
							if (isInside(kugel.getLinks(), kugel.getRechts(), getLocation().getX())) {
								if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel
										.getLocation().getY())) {
									kugel.setRechts(getLocation().getX());
								}
							}
						}
						// Mauer links der Kugel?
						if (getLocation().getX() < kugel.getLocation().getX()) {
							if (isInside(kugel.getLinks(), kugel.getRechts(), getLocation().getX())) {
								if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel
										.getLocation().getY())) {
									kugel.setLinks(getLocation().getX());
								}
							}
						}
					}
				}

				// 4.

				// alle_ORT => sind alle Kugeln auf der anderen Seite?
				Location kol_loc = kollege_Mauer.getLocation();
				// Ursprungs-Location für Kollege, da dieser immer wieder bewegt
				// wird

				if (ausrichtung == 0) {

					// Oben füllen
					kollege_Mauer.setDirection(270);
					while (kollege_Mauer.isInGrid()
							&& gameGrid.getBg().getColor(kollege_Mauer.MoveLocation(2)).equals(Color.WHITE)) {
						kollege_Mauer.move(1);
					}
					kollege_Mauer.move(2);
					boolean alle_unten = true;
					for (Actor kugel_Actor : mainClass.Kugeln) {
						Kugel kugel = (Kugel) kugel_Actor;
						if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel.getLocation()
								.getY())
								&& isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
							alle_unten = false;
						}
					}
					if (alle_unten) {
						// Füllen
						gameGrid.getBg().setPaintColor(Color.BLACK);
						gameGrid.getBg()
								.fillRectangle(Loc2Point(getLocation()), Loc2Point(kollege_Mauer.getLocation()));
					}
					kollege_Mauer.setLocation(kol_loc);
					// Unten füllen
					kollege_Mauer.setDirection(90);
					while (kollege_Mauer.isInGrid()
							&& gameGrid.getBg().getColor(kollege_Mauer.MoveLocation(2)).equals(Color.WHITE)) {
						kollege_Mauer.move(1);
					}
					kollege_Mauer.move(2);
					boolean alle_oben = true;
					for (Actor kugel_Actor : mainClass.Kugeln) {
						Kugel kugel = (Kugel) kugel_Actor;
						if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel.getLocation()
								.getY())
								&& isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
							alle_oben = false;
						}
					}
					if (alle_oben) {
						// Füllen
						gameGrid.getBg().setPaintColor(Color.BLACK);
						gameGrid.getBg()
								.fillRectangle(Loc2Point(getLocation()), Loc2Point(kollege_Mauer.getLocation()));
					}
				} else {
					kollege_Mauer.setLocation(kol_loc);
					// Rechts füllen
					kollege_Mauer.setDirection(0);
					while (kollege_Mauer.isInGrid()
							&& gameGrid.getBg().getColor(kollege_Mauer.MoveLocation(2)).equals(Color.WHITE)) {
						kollege_Mauer.move(1);
					}
					kollege_Mauer.move(2);
					boolean alle_links = true;
					for (Actor kugel_Actor : mainClass.Kugeln) {
						Kugel kugel = (Kugel) kugel_Actor;
						if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel.getLocation()
								.getY())
								&& isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
							alle_links = false;
						}
					}
					if (alle_links) {
						// Füllen
						gameGrid.getBg().setPaintColor(Color.BLACK);
						gameGrid.getBg()
								.fillRectangle(Loc2Point(getLocation()), Loc2Point(kollege_Mauer.getLocation()));
					}
					kollege_Mauer.setLocation(kol_loc);
					// Links füllen
					kollege_Mauer.setDirection(180);
					while (kollege_Mauer.isInGrid()
							&& gameGrid.getBg().getColor(kollege_Mauer.MoveLocation(2)).equals(Color.WHITE)) {
						kollege_Mauer.move(1);
					}
					kollege_Mauer.move(2);
					boolean alle_rechts = true;
					for (Actor kugel_Actor : mainClass.Kugeln) {
						Kugel kugel = (Kugel) kugel_Actor;
						if (isInside(getLocation().getY(), kollege_Mauer.getLocation().getY(), kugel.getLocation()
								.getY())
								&& isInside(getLocation().getX(), kollege_Mauer.getLocation().getX(), kugel
										.getLocation().getX())) {
							alle_rechts = false;
						}
					}
					if (alle_rechts) {
						// Füllen
						gameGrid.getBg().setPaintColor(Color.BLACK);
						gameGrid.getBg()
								.fillRectangle(Loc2Point(getLocation()), Loc2Point(kollege_Mauer.getLocation()));
					}
				}
				// 5.
				kollege_Mauer.removeSelf();
				removeSelf();
				mainClass.checkForWin();
			}
		}
	}
}