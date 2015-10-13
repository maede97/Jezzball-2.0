// Kugel.java
import java.awt.Color;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.Location;

public class Kugel extends Actor {
	private int var = 0; // Variable für Erstausführung

	private Jezzball mainClass; // Haupt-Klasse --> Jezzball

	private int Rand_Grenze = 6;
	// Pixel, welche am Rand der Bewegungs-Box als
	// Rest übrig bleiben --> verhindert weiss
	// werden der Mauer

	public int unten; // Positiv Y
	public int oben; // Y = 0
	public int links;// X = 0
	public int rechts;// Positiv X

	public Kugel(Jezzball mainClass, int slowDown) {
		// Geschwindigkeit der Kugel
		this.mainClass = mainClass;
		setSlowDown(slowDown);
	}

	public void setBox(int unten, int oben, int links, int rechts) {
		// Dies ist die Bewegungs-Box
		// Die Kugel kann sich nur in dieser Box bewegen
		this.unten = unten;
		this.oben = oben;
		this.links = links;
		this.rechts = rechts;
	}

	// Hier folgen alle Funktionen für die Bewegungs-Box
	public void setUnten(int unten) {
		this.unten = unten;
	}

	public void setOben(int oben) {
		this.oben = oben;
	}

	public void setLinks(int links) {
		this.links = links;
	}

	public void setRechts(int rechts) {
		this.rechts = rechts;
	}

	public int getUnten() {
		return unten;
	}

	public int getOben() {
		return oben;
	}

	public int getLinks() {
		return links;
	}

	public int getRechts() {
		return rechts;
	}

	public Location moveLocation(int anzahl) {
		// Gibt die Position der Bewegung in 2 Zügen zurück
		// Gebraucht für Kollisions-Erkennung
		Location loc = getLocation();
		move(anzahl);
		Location new_loc = getLocation();
		setLocation(loc);
		return new_loc;
	}

	public void act() {
		// Überschreibt die default-act-Funktion von JGameGrid
		if (var == 0) {
			// Nur bei Erstausführung --> Zufällige Richtung
			double Ausr = Math.random() * 360;
			while ((Ausr % 90 < 30) || (Ausr % 90 > 60)) {
				Ausr = Math.random() * 360;
			}
			setDirection(Ausr);
		}

		mainClass.StatusUpdate();

		// Ab hier folgt das Bewegen in der Box
		// Falls in der Nähe der Mauer, setze Ausrichtung neu
		Location Position = getLocation();

		double Ausrichtung = getDirection();

		// Die Kugel ist am linken Rand
		if (Position.getX() <= links + Rand_Grenze) {
			Ausrichtung = 180 - Ausrichtung;
		}
		// Die Kugel ist am rechten Rand
		if (Position.getX() >= rechts - Rand_Grenze) {
			Ausrichtung = 180 - Ausrichtung;
		}
		// Die Kugel ist am oberen Rand
		if (Position.getY() <= oben + Rand_Grenze) {
			Ausrichtung = 360 - Ausrichtung;
		}
		// Die Kugel ist am unteren Rand
		if (Position.getY() >= unten - Rand_Grenze) {
			Ausrichtung = 360 - Ausrichtung;
		}
		// Falls Location in 4 Zügen schwarz ist und Actors der Klasse Mauern
		// vorhanden sind:
		// Abbrechen, Leben vermindern
		// (siehe Jezzball --> interrupt() )
		if (gameGrid.getBg().getColor(moveLocation(4)).equals(Color.BLACK)
				&& gameGrid.getActors(Mauer.class).size() != 0) {
			mainClass.interrupt();

		}
		// Ausrichtung wird angewendet
		setDirection(Ausrichtung);

		// Lösche alte Kugel-Fläche
		for (int x = -2; x < 2; x++) {
			for (int y = -2; y < 2; y++) {
				gameGrid.getBg().fillCell(new Location(getLocation().x + x, getLocation().y + y), Color.WHITE);
			}
		}
		move(1);
		// Zeichne neue Kugel-Fläche
		for (int x = -2; x < 2; x++) {
			for (int y = -2; y < 2; y++) {
				gameGrid.getBg().fillCell(new Location(getLocation().x + x, getLocation().y + y), Color.RED);
			}
		}
		// Erhöhe Laufvariable, damit die Kugel nicht mehr eine zufällige
		// Richtung erhält
		var++;
	}
}