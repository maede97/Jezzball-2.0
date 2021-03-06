// Jezzball.java

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;

import ch.aplu.jgamegrid.Actor;
import ch.aplu.jgamegrid.GGExitListener;
import ch.aplu.jgamegrid.GGKeyListener;
import ch.aplu.jgamegrid.GGMouse;
import ch.aplu.jgamegrid.GGMouseListener;
import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;

public class Jezzball extends GameGrid implements GGMouseListener, GGKeyListener {

	private static final String version = "1.6";

	private static final long serialVersionUID = 1L;
	private int Level = 1; // Aktueller Level
	private int Leben = Level - 1; // Aktuelle Anzahl Leben
	private int punkte_total = 0;
	private int prozent = 0;
	public static long startTime;

	public int KugelSlowDown = 2;

	public JRadioButtonMenuItem menuItemScreenshot;
	public static JRadioButtonMenuItem menuItemLogger;
	public JRadioButtonMenuItem menuItemStatus;
	public JRadioButtonMenuItem menuItemClose;

	public JRadioButtonMenuItem menu_fast;
	public JRadioButtonMenuItem menu_medium;
	public JRadioButtonMenuItem menu_slow;

	public JMenuItem info;
	public JMenuItem tastatur;
	public JMenuItem exit;

	public JMenuItem refresh_graphics;
	public JMenuItem kugel_fehler;

	public List<Actor> Kugeln = new ArrayList<Actor>();
	// Liste, in der alle Kugeln gespeicher sind

	public int ausrichtung = 0;

	// Ausrichtung der Mauer (Oben-Unten / Links-Rechts)

	public Jezzball() {
		// Erzeuge ein Feld mit 840x540 Pixel, der Zellengr�sse 1, dem
		// Hintergrund WHITE und unterdr�cke Spielsteuerungs-Leiste
		super(840, 540, 1, WHITE, false);
		setSimulationPeriod(5);
		setTitle("JezzBall 2.0");
		addStatusBar(120);
		// Erzeuge Status-Bar Ausgabe von Variabeln
		addKugeln();
		// F�ge Kugeln hinzu
		addMouseListener(this, GGMouse.lPress | GGMouse.rPress);
		// Setze Maus-Listener
		addKeyListener(this);

		JMenuBar menuBar = new JMenuBar();
		JMenu datei = new JMenu("Datei");
		JMenu menu = new JMenu("Einstellungen");
		JMenu menu2 = new JMenu("Fehlerbehebung");
		menuBar.add(datei);
		menuBar.add(menu);
		menuBar.add(menu2);

		info = new JMenuItem("Info");
		tastatur = new JMenuItem("Tastaturk�rzel");
		exit = new JMenuItem("Beenden");

		info.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show Info
				saveToLog("Hilfe angezeigt.");
				JOptionPane.showMessageDialog(null, "Wie wird Jezzball gespielt?" + "\n"
						+ "Das Spiel besteht aus Kugeln und Mauern. " + "Eine Kugel bewegt sich im Feld "
						+ "und kann mit Mauern eingegrenzt werden." + "\n"
						+ "Dazu hat der Spieler eine bestimmte Anzahl Leben zur Verf�gung." + "\n"
						+ "Mit Linksklick wird eine Mauer an dieser Stelle gebaut, an welcher geklickt wird." + "\n"
						+ "Die Mauer breitet sich gleichm�ssig nach 2 Richtungen aus. "
						+ "Die Richtung (horizontal oder vertikal) kann mit der rechten Maustaste gew�hlt werden."
						+ "\n" + "Die aktuelle Richtung kann in der Statusleiste unter dem Fenster abgelesen werden."
						+ "\n" + "Sobald die Mauer durch zwei Enden begrenzt wird "
						+ "(durch eine andere Mauer oder den Spielfeldrand), "
						+ "wird diese stabil. Hat es auf einer Seite keine Kugeln mehr, so wird diese Seite" + "\n"
						+ "bis zum n�chsten Hindernis (eine andere Mauer oder der Spielfeldrand) ausgef�llt."
						+ "Das Ziel des Spieles sind 75 Prozent ausgef�llte, schwarze Fl�chen." + "\n"
						+ "Falls eine Kugel die Mauer im Aufbau st�rt,"
						+ "wird der Bau der Mauer sofort unterbrochen, alle schon" + "\n"
						+ "abgeschlossenen Mauern werden entfernt und es wird ein Leben abgezogen. "
						+ "Falls der Spieler keine Leben mehr besitzt, hat er verloren." + "\n" + "\n"
						+ "Was wird alles in der Statusleiste angezeigt?" + "\n"
						+ "In der Statusleiste unter dem Spielfeld werden alle Informationen zum Spiel angezeigt."
						+ "\n" + "Spielzeit: Die aktuelle Spielzeit in Sekunden, Minuten, Stunden und Tagen" + "\n"
						+ "Prozent: Die bereits schwarz bedeckte Fl�che in Prozent, das Ziel pro Level sind 75." + "\n"
						+ "Punkte: Die erreichten Punkte. Wer wissen will, wie man diese berechnet, "
						+ "sollte meine Maturarbeit anschauen." + "\n" + "Leben: Die Anzahl restlicher Leben." + "\n"
						+ "Level: Der aktuelle Level. Falls man 75 Prozent erreicht hat, steigt man einen Level auf, "
						+ "der Schwierigkeitsgrad steigt, da eine zus�tzliche Kugel im Spiel ist." + "\n"
						+ "Ausrichtung: Die Richtung, in welche die Mauer gebaut wird." + "\n" + "\n"
						+ "Was macht man bei einem Fehler?" + "\n"
						+ "Leider passieren immer wieder kleinere (oder auch gr�ssere) Fehler. "
						+ "Falls ein Fehler passieren sollte, bitte im Menu auf Fehlerbehebung klicken." + "\n"
						+ "Falls eine Kugel am Rand des Spielfeldes oder einer Mauer hin und her f�hrt, "
						+ "und dabei komisch wackelt, bitte auf den Knopf \"Kugel in Wand\" klicken." + "\n"
						+ "Damit werden alle Kugeln wieder neu verteilt, das Problem sollte gel�st sein. "
						+ "Falls pl�tzlich alle Kugeln unsichtbar sind (das Spielfeld ist einfach weiss)," + "\n"
						+ "bitte auf den Knopf \"Grafik-Fehler\" dr�cken. "
						+ "Damit wird das ganze Spielfeld neu gezeichnet." + "\n"
						+ "Bei beiden Fehler kann es sein, dass das Problem nicht gel�st wird. "
						+ "In diesem Fall bitte einfach nochmals den Knopf dr�cken!" + "\n" + "\n"
						+ "Wer ist die Ansprechperson bei Fragen oder Unklarheiten?" + "\n"
						+ "Matthias Busenhart, matthias.busenhart@studmail.kzo.ch" + "\n" + "\n"
						+ "Vielen Dank f�rs Spielen und viel Spass!");
			}
		});

		tastatur.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame temp_frame = new JFrame("Tastaturk�rzel");
				JTable temp_table = new JTable(11, 2);
				temp_table.setEnabled(false);
				temp_frame.setSize(300, 210);

				temp_table.setValueAt("Escape", 0, 0);
				temp_table.setValueAt("Beenden", 0, 1);

				temp_table.setValueAt("F1", 1, 0);
				temp_table.setValueAt("Hilfe anzeigen", 1, 1);

				temp_table.setValueAt("F2", 2, 0);
				temp_table.setValueAt("Grafik-Fehler", 2, 1);

				temp_table.setValueAt("F3", 3, 0);
				temp_table.setValueAt("Kugel-in-Wand-Fehler", 3, 1);

				temp_table.setValueAt("1", 4, 0);
				temp_table.setValueAt("Geschwindigkeit Langsam", 4, 1);

				temp_table.setValueAt("2", 5, 0);
				temp_table.setValueAt("Geschwindigkeit Mittel", 5, 1);

				temp_table.setValueAt("3", 6, 0);
				temp_table.setValueAt("Geschwindigkeit Schnell", 6, 1);

				temp_table.setValueAt("C", 7, 0);
				temp_table.setValueAt("Statusbar anzeigen", 7, 1);

				temp_table.setValueAt("L", 8, 0);
				temp_table.setValueAt("Logger aktivieren", 8, 1);

				temp_table.setValueAt("S", 9, 0);
				temp_table.setValueAt("Screenshots aktivieren", 9, 1);

				temp_table.setValueAt("T", 10, 0);
				temp_table.setValueAt("Tastaturk�rzel anzeigen", 10, 1);

				temp_frame.add(temp_table);

				temp_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				temp_frame.setResizable(false);
				temp_frame.setVisible(true);

			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});
		datei.add(info);
		datei.add(tastatur);
		datei.add(exit);

		// MenuItems f�rs Aktiviern von Daten
		menuItemScreenshot = new JRadioButtonMenuItem("Screenshots");
		menuItemLogger = new JRadioButtonMenuItem("Logger");
		menuItemStatus = new JRadioButtonMenuItem("Zeige Statusleiste");
		menuItemClose = new JRadioButtonMenuItem("Beenden bei Verlieren");

		JMenu geschwindigkeit_menu = new JMenu("Geschwindigkeit");
		menu_fast = new JRadioButtonMenuItem("Schnell");
		menu_medium = new JRadioButtonMenuItem("Mitel");
		menu_slow = new JRadioButtonMenuItem("Langsam");
		menu_fast.setSelected(false);
		menu_medium.setSelected(true);
		menu_slow.setSelected(false);
		geschwindigkeit_menu.add(menu_fast);
		geschwindigkeit_menu.add(menu_medium);
		geschwindigkeit_menu.add(menu_slow);

		menuItemScreenshot.setSelected(false);
		menuItemLogger.setSelected(false);
		menuItemStatus.setSelected(true);
		menuItemClose.setSelected(true);
		menu.add(menuItemScreenshot);
		menu.add(menuItemLogger);
		menu.add(menuItemStatus);

		// menu.add(menuItemClose);

		menu.add(geschwindigkeit_menu);

		menu_fast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (menu_fast.isSelected()) {
					menu_medium.setSelected(false);
					menu_slow.setSelected(false);
					changeSpeed();
				} else {
					menu_fast.setSelected(true);
				}

			}
		});

		menu_medium.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (menu_medium.isSelected()) {
					menu_fast.setSelected(false);
					menu_slow.setSelected(false);
					changeSpeed();
				} else {
					menu_medium.setSelected(true);
				}

			}
		});

		menu_slow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (menu_slow.isSelected()) {
					menu_medium.setSelected(false);
					menu_fast.setSelected(false);
					changeSpeed();
				} else {
					menu_slow.setSelected(true);
				}

			}
		});

		menuItemStatus.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (menuItemStatus.isSelected()) {
					showStatusBar(true);
				} else {
					showStatusBar(false);
				}
			}
		});

		refresh_graphics = new JMenuItem("Grafik-Fehler");
		kugel_fehler = new JMenuItem("Kugel in Wand");
		refresh_graphics.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// refresh();
				// getBg().clear();
				// addKugeln();
				// getBg().clear();
				saveToLog("Grafik-Fehler.");
				removeActors(Mauer.class);
				getBg().clear();
				addKugeln();
				// Alle Bewegungs-Boxen der Kugeln wieder in den
				// Ursprungszustand
				// versetzen
				for (Actor kugel_actor : Kugeln) {
					Kugel kugel_kugel = (Kugel) kugel_actor;
					kugel_kugel.setBox(getHeight(), 0, 0, getWidth());
				}
				getBg().setPaintColor(WHITE);
				getBg().fillRectangle(new Point(0, 0), new Point(getWidth(), getHeight()));
			}
		});

		kugel_fehler.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (Actor k : Kugeln) {
					Location spawn = getRandomEmptyLocation();
					while (getBg().getColor(spawn).equals(BLACK)) {
						spawn = getRandomEmptyLocation();
					}
					for (int x = -4; x < 4; x++) {
						for (int y = -4; y < 4; y++) {
							if (getBg().getColor(new Location(k.getLocation().x + x, k.getLocation().y + y))
									.equals(RED)) {
								getBg().fillCell(new Location(k.getLocation().x + x, k.getLocation().y + y),
										Color.WHITE);
							}
						}
					}
					k.setLocation(spawn);
				}
				saveToLog("Kugel-in-Mauer-Fehler --> neu platziert.");

			}
		});
		menu2.add(refresh_graphics);
		menu2.add(kugel_fehler);

		getFrame().setJMenuBar(menuBar);
		menuBar.setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

		addExitListener(new GGExitListener() {
			@Override
			public boolean notifyExit() {
				onExit();
				return false;
			}
		});
		checkForUpdates();
		startTime = System.nanoTime();

		show();
		doRun();
	}

	protected void changeSpeed() {
		if (menu_fast.isSelected())
			KugelSlowDown = 0;
		if (menu_medium.isSelected())
			KugelSlowDown = 2;
		if (menu_slow.isSelected())
			KugelSlowDown = 5;
		for (Actor k : Kugeln) {
			k.setSlowDown(KugelSlowDown);
		}
	}

	public void onExit() {
		saveToLog("Exit-Knopf. Punkte: " + punkte_total);
		JOptionPane.showMessageDialog(null, "Du hast das Spiel beendet.\nDeine Punkte: " + punkte_total);
		System.exit(1);
	}

	public static void main(String[] args) {
		new Jezzball();
	}

	public static void saveToLog(String message) {
		if (menuItemLogger.isSelected())
			try {
				String time = String.valueOf((System.nanoTime() - startTime) / 1000000000);
				File folder = new File("Jezzball_Data" + File.separator + startTime);
				if (folder.exists()) {
					File out = new File("Jezzball_Data" + File.separator + startTime + File.separator + "Logger.txt");
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(out, true)));
					pw.println("[" + time + "]" + "    " + message);
					pw.close();

				} else {
					folder.mkdirs();
					File out = new File("Jezzball_Data" + File.separator + startTime + File.separator + "Logger.txt");
					out.createNewFile();
					saveToLog(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

	private void addKugeln() {
		getBg().clear(WHITE);
		removeActors(Kugel.class);
		Kugeln = new ArrayList<Actor>();
		for (int i = 0; i < Level; i++) {
			Kugel temp_Kugel = new Kugel(this, KugelSlowDown);
			addActor(temp_Kugel, getRandomEmptyLocation());
			while (temp_Kugel.getXStart() < 5 || temp_Kugel.getXStart() > getWidth() - 5 || temp_Kugel.getYStart() < 5
					|| temp_Kugel.getYStart() > getHeight() - 5) {
				removeActor(temp_Kugel);
				addActor(temp_Kugel, getRandomEmptyLocation());

			}
			Kugeln.add(temp_Kugel);
			temp_Kugel.setBox(getHeight() - 10, 0, 0, getWidth()); // Setze
																	// Bewegungs-Box
		}
		getBg().clear(WHITE);
		refresh();
	}

	public String Long2Date(long input) {
		String date = "";
		NumberFormat f = new DecimalFormat("00");

		long secs = input;
		long mins = 0;
		long hours = 0;
		long days = 0;

		while (secs > 60) {
			mins++;
			secs -= 60;
		}
		while (mins > 60) {
			hours++;
			mins -= 60;
		}
		while (hours > 24) {
			days++;
			hours -= 24;
		}

		date = f.format(days) + ":" + f.format(hours) + ":" + f.format(mins) + ":" + f.format(secs);
		return date;

	}

	public static Point getLocationOnCurrentScreen(final Component c) {
		final Point relativeLocation = c.getLocationOnScreen();

		final Rectangle currentScreenBounds = c.getGraphicsConfiguration().getBounds();

		relativeLocation.x -= currentScreenBounds.x;
		relativeLocation.y -= currentScreenBounds.y;

		return relativeLocation;
	}

	void takeSnapShot() {
		if (menuItemScreenshot.isSelected()) {
			update(getGraphics());

			String time = String.valueOf((System.nanoTime() - startTime) / 1000000000);
			String level = String.valueOf(getLevel());
			String name = "Sekunden_" + time + "_Level_" + level;
			try {
				Rectangle rect = getFrame().getBounds();
				if (menuItemStatus.isSelected())
					rect.setSize((int) rect.getWidth(), (int) rect.getHeight() + 120);
				else
					rect.setSize((int) rect.getWidth(), (int) rect.getHeight());
				Robot rob = new Robot();
				BufferedImage buf = rob.createScreenCapture(rect);
				File folder = new File("Jezzball_Data" + File.separator + startTime);
				if (folder.exists()) {
					File out = new File("Jezzball_Data" + File.separator + startTime + File.separator + name + ".jpeg");
					ImageIO.write(buf, "jpeg", out);
					saveToLog("Screenshot " + name + " erstellt.");
				} else {
					folder.mkdirs();
					File out = new File("Jezzball_Data" + File.separator + startTime + File.separator + name + ".jpeg");
					ImageIO.write(buf, "jpeg", out);
					saveToLog("Screenshot " + name + " erstellt.");
				}
			} catch (Exception e) {
				saveToLog("Screenshot nicht erstellt. Fehler.");
				e.printStackTrace();
			}
		}
	}

	public void StatusUpdate() {
		// Gibt in der Status-Bar (unter Frame)
		// diverse Spiel-Variablen aus
		// Je nach Ausrichtung gibt sie entweder
		// Rechts-Links oder Oben-Unten in
		// der 5. Zeile aus
		String dateFormatted = Long2Date((System.nanoTime() - startTime) / 1000000000);

		if (ausrichtung == 0) {
			setStatusText("Spielzeit: " + dateFormatted + "\nProzent: " + prozent + "\nPunkte: " + punkte_total
					+ "\nLeben: " + getLives() + "\nLevel: " + getLevel() + "\nAusrichtung: Links-Rechts");
		} else if (ausrichtung == 1) {
			setStatusText("Spielzeit: " + dateFormatted + "\nProzent: " + prozent + "\nPunkte: " + punkte_total
					+ "\nLeben: " + getLives() + "\nLevel: " + getLevel() + "\nAusrichtung: Oben-Unten");
		}
	}

	public void checkForWin() {
		takeSnapShot();
		// Berechnet bedeckte Fl�che, Ende falls gewonnen
		int total = getHeight() * getWidth();
		double sieg = 0.75 * total;
		// Sieg ist 75% des Spielfeldes
		double aktuell = 0;
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				if (getBg().getColor(new Location(x, y)).equals(BLACK)) {
					// Alle schawrzen Pixel z�hlen
					aktuell++;
				}
			}
		}

		prozent = (int) Math.round(aktuell / total * 100);
		StatusUpdate();

		if (aktuell >= sieg) {
			saveToLog("Levelaufstieg: " + getLevel() + " zu " + (getLevel() + 1));

			int punkte;
			punkte = (int) Math.round(2 * (aktuell / total * 100 - 75) * (getLevel() + 5) + (getLives() * 15));
			setLevel(getLevel() + 1); // Level erh�hen
			setLives(getLevel() - 1); // Leben neu setzen
			punkte_total += punkte;

			// Kugeln neu setzen
			removeActors(Kugel.class);
			addKugeln();
			// Gewonnen-Meldung anzeigen
			JOptionPane.showMessageDialog(null, "BRAVO!\nDu hast den Level erfolgreich "
					+ "abgeschlossen.\nDeine Punkte " + "f�r diesen Level:\n" + punkte + "\nDeine Punkte total:\n"
					+ punkte_total + "\nWeiter gehts!");
			ausrichtung = 0;
			refresh();
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else {
			// Nicht gewonnen
		}
	}

	public int getLevel() {
		// Gibt aktueller Level zur�ck
		return Level;
	}

	public void setLevel(int newLevel) {
		// Setzt neuer Level auf newLevel
		Level = newLevel;
	}

	public void setLives(int newLives) {
		// Setzt Anzahl Leben auf newLives
		Leben = newLives;
	}

	public int getLives() {
		// Gibt aktuelle Anzahl Leben zur�ck
		return Leben;
	}

	public String getFromURL(final String urlString) {
		BufferedInputStream in = null;
		String output = "";

		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			byte[] contents = new byte[1024];

			int bytesRead = 0;
			while ((bytesRead = in.read(contents)) != -1) {
				output = new String(contents, 0, bytesRead);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public void checkForUpdates() {
		saveToLog("Check for Updates");
		String url = "https://raw.githubusercontent.com/maede97/Jezzball-2.0/master/version.txt";
		String newVersion = getFromURL(url);
		if (!newVersion.equalsIgnoreCase(version)) {
			JOptionPane.showMessageDialog(null, "Eine neue Version von Jezzball ist verf�gbar!\n"
					+ "Aktuelle Version: " + version + "\n" + "Neue Version: " + newVersion + "\n"
					+ "Du kannst sie unter\n" + "https://github.com/maede97/Jezzball-2.0\n" + "herunterladen." + "\n\n"
					+ "Viel Spass!");
			saveToLog("Neue Version verf�gbar.");
		}
	}

	@Override
	public boolean mouseEvent(GGMouse mouse) {
		// Bei Maus-Klick --> Rechts oder Links
		switch (mouse.getEvent()) {
		case GGMouse.rPress:
			ausrichtung++;
			ausrichtung %= 2;
			if (ausrichtung == 1) {
				saveToLog("Rechte Maustaste. Aurichtung: Oben-Unten");
				setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			} else {
				saveToLog("Rechte Maustaste. Aurichtung: Links-Rechts");
				setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			}
			break;
		// Ausrichtung �ndern
		case GGMouse.lPress:
			saveToLog("Linke Maustaste.");
			if (getActors(Mauer.class).size() == 0) {
				// Eine Mauer bauen
				Actor mauer = new Mauer(ausrichtung, false, this);
				addActor(mauer, new Location(mouse.getX(), mouse.getY()));
			} else {
				// Falls schon eine Mauer am Bauen keine weitere
				setTitle("Du kannst nur eine Mauer aufs Mal bauen.");
			}
			break;
		}
		return true;
	}

	public void interrupt() {
		saveToLog("Interrupt.");
		takeSnapShot();
		// Wenn eine Kugel eine Mauer im Aufbau ber�hrt
		if (getLives() < 1) {
			saveToLog("Spiel verloren. Punkte: " + punkte_total);
			// Keine Leben mehr
			JOptionPane.showMessageDialog(null, "Du hast verloren.\nDeine Punkte: " + punkte_total);

			if (menuItemClose.isSelected()) {
				System.exit(1);
			} else {
				removeActors(Mauer.class);
				addKugeln();
				Level = 1;
				Leben = Level - 1;
				punkte_total = 0;
				prozent = 0;
				saveToLog("Neues Spiel gestartet.");
				startTime = System.nanoTime();
				getBg().fillRectangle(new Point(0, 0), new Point(getWidth(), getHeight()));
				StatusUpdate();
			}

		} else {
			saveToLog("Leben verloren. Rest-Leben: " + (getLives() - 1));

			removeActors(Mauer.class);
			getBg().clear();
			// Alle Bewegungs-Boxen der Kugeln wieder in den Ursprungszustand
			// versetzen
			for (Actor kugel_actor : Kugeln) {
				Kugel kugel_kugel = (Kugel) kugel_actor;
				kugel_kugel.setBox(getHeight(), 0, 0, getWidth());
			}
			setLives(getLives() - 1);

		}
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_F1:
			info.doClick();
			break;
		case KeyEvent.VK_ESCAPE:
			exit.doClick();
			break;
		case KeyEvent.VK_1:
			menu_slow.doClick();
			break;
		case KeyEvent.VK_2:
			menu_medium.doClick();
			break;
		case KeyEvent.VK_3:
			menu_fast.doClick();
			break;
		case KeyEvent.VK_S:
			menuItemScreenshot.doClick();
			break;
		case KeyEvent.VK_L:
			menuItemLogger.doClick();
			break;
		case KeyEvent.VK_C:
			menuItemStatus.doClick();
			break;
		case KeyEvent.VK_T:
			tastatur.doClick();
			break;
		case KeyEvent.VK_F2:
			refresh_graphics.doClick();
			break;
		case KeyEvent.VK_F3:
			kugel_fehler.doClick();
		default:
			break;
		}

		return false;
	}

	@Override
	public boolean keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}