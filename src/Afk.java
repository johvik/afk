import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Afk implements Runnable {
	private static boolean running = true;
	private static Thread thread;
	private SimpleRobot robot;
	private Ui ui;

	/**
	 * Slot size in pixels
	 */
	private static final int SLOT_SIZE = 34;

	private boolean attacking;
	private Date lastSpearPickup;
	private Date lastPaladinDance;
	private Date lastMageDance;
	private int runesMade;

	public Afk(SimpleRobot robot, Ui ui) {
		thread = Thread.currentThread();
		this.robot = robot;
		this.ui = ui;

		attacking = false;
		lastSpearPickup = new Date();
		lastPaladinDance = new Date();
		lastMageDance = new Date();
		runesMade = 0;
	}

	private boolean useMana(Point p) {
		// Check if mana pixel is blue
		return robot.getPixelColor(p.x, p.y).equals(new Color(114, 96, 255));
	}

	private void paladinClient() throws SimpleRobot.MouseMovedException, InterruptedException {
		Date now = new Date();
		// Click to bring client to foreground
		robot.leftClick(ui.paladinFood.getPoint());
		Thread.sleep(100);

		if (robot.closePixelColor(ui.monkHealth.getPoint(), new Color(38, 35, 31))) {
			// Stop attacking
			robot.sendKey(KeyEvent.VK_ESCAPE);
			attacking = false;
			Thread.sleep(100);
		} else if (!attacking) {
			// Start attacking
			robot.leftClick(ui.monkHealth.getPoint());
			attacking = true;
			Thread.sleep(100);
		}

		if (now.getTime() - lastSpearPickup.getTime() > 1000 * 7) {
			// Pick up spears
			robot.controlLeftDrag(ui.spearDrop.getPoint(), ui.spearHand.getPoint());
			lastSpearPickup = now;
			Thread.sleep(100);
		}

		if (useMana(ui.paladinMana.getPoint())) {
			// Cast spell
			robot.sendKey(KeyEvent.VK_F11);
			Thread.sleep(500);

			// Eat conjured food
			for (int i = 0; i < 2; i++) {
				robot.rightClick(ui.paladinFood.getPoint());
				Thread.sleep(100);
			}
		}

		if (now.getTime() - lastPaladinDance.getTime() > 1000 * 60 * 8) {
			// Dance
			robot.dance();
			lastPaladinDance = now;
			Thread.sleep(100);
		}
	}

	private void mageClient() throws SimpleRobot.MouseMovedException, InterruptedException {
		Date now = new Date();
		// Click to bring client to foreground
		robot.leftClick(ui.mageFood.getPoint());
		Thread.sleep(100);

		if (useMana(ui.mageMana.getPoint())) {
			// TODO Add support for more than one backpack
			// Move a blank rune
			robot.leftDrag(ui.mageRunes.getPoint(), ui.mageHand.getPoint());
			Thread.sleep(250);

			// Cast spell
			robot.sendKey(KeyEvent.VK_F12);
			Thread.sleep(500);

			// Move rune to backpack
			robot.leftDrag(ui.mageHand.getPoint(), new Point(ui.mageRunes.x() - SLOT_SIZE, ui.mageRunes.y()));
			Thread.sleep(250);

			runesMade += 1;

			if (runesMade >= 19) {
				// Open next backpack
				robot.leftClick(new Point(ui.mageRunes.x() + SLOT_SIZE, ui.mageRunes.y()));
				runesMade = 0;
				Thread.sleep(100);

				// Scroll to bottom
				for (int i = 0; i < 5; i++) {
					robot.mouseWheel(5);
					Thread.sleep(100);
				}
			}

			// Eat food
			for (int i = 0; i < 4; i++) {
				robot.rightClick(ui.mageFood.getPoint());
				Thread.sleep(100);
			}
		}

		if (now.getTime() - lastMageDance.getTime() > 1000 * 60 * 8) {
			// Dance
			robot.dance();
			lastMageDance = now;
			Thread.sleep(100);
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000); // Wait a bit before starting
			while (running) {
				try {
					if (ui.paladinEnabled()) {
						paladinClient();
						Thread.sleep(100);
					}
					if (ui.mageEnabled()) {
						mageClient();
						Thread.sleep(100);
					}
					Thread.sleep(100);
				} catch (SimpleRobot.MouseMovedException e) {
					// Wait until the user stopped moving the mouse
					Thread.sleep(10000);
				}
			}
		} catch (InterruptedException e) {
		}
	}

	public static void stop() {
		running = false;
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		try {
			Properties props = new Properties();

			try {
				props.loadFromXML(new FileInputStream("afk_config.xml"));
			} catch (FileNotFoundException e) {
				// Use the defaults since file does not exist
			}

			SimpleRobot robot = new SimpleRobot();
			Ui ui = new Ui(robot, props);
			ui.setVisible(true);
			new Afk(robot, ui).run();
		} catch (AWTException | IOException e) {
			e.printStackTrace();
		}
	}
}
