import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SimpleRobot extends Robot {
	private static final int TOLERANCE_SQUARE = 40 * 40;

	public class MouseMovedException extends Exception {
		private static final long serialVersionUID = -1972882950303643080L;

		public MouseMovedException(String message) {
			super(message);
		}
	}

	private Point lastMousePosition;

	public SimpleRobot() throws AWTException {
		super();
		lastMousePosition = MouseInfo.getPointerInfo().getLocation();
		setAutoDelay(50);
	}

	@Override
	public synchronized void mouseMove(int x, int y) {
		super.mouseMove(x, y);
		lastMousePosition = new Point(x, y);
	}

	private void checkMousePosition() throws MouseMovedException {
		Point currentPosition = MouseInfo.getPointerInfo().getLocation();
		if (!currentPosition.equals(lastMousePosition)) {
			lastMousePosition = currentPosition;
			throw new MouseMovedException("Mouse position has changed");
		}
	}

	public synchronized void leftClick(Point p) throws MouseMovedException {
		checkMousePosition();
		mouseMove(p.x, p.y);
		mousePress(InputEvent.BUTTON1_DOWN_MASK);
		mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	public synchronized void leftDrag(Point from, Point to) throws MouseMovedException {
		checkMousePosition();
		mouseMove(from.x, from.y);
		mousePress(InputEvent.BUTTON1_DOWN_MASK);
		delay(200);
		mouseMove(to.x, to.y);
		mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	public synchronized void controlLeftDrag(Point from, Point to) throws MouseMovedException {
		checkMousePosition();
		keyPress(KeyEvent.VK_CONTROL);
		leftDrag(from, to);
		keyRelease(KeyEvent.VK_CONTROL);
	}

	public synchronized void rightClick(Point p) throws MouseMovedException {
		checkMousePosition();
		mouseMove(p.x, p.y);
		mousePress(InputEvent.BUTTON3_DOWN_MASK);
		mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}

	public synchronized void sendKey(int keycode) throws MouseMovedException {
		checkMousePosition();
		setAutoDelay(10);
		keyPress(keycode);
		keyRelease(keycode);
		setAutoDelay(50);
	}

	private boolean numlock() {
		return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
	}

	public synchronized void dance() throws MouseMovedException {
		checkMousePosition();
		final boolean numlock = numlock();

		// Disable numlock if active
		if (numlock) {
			sendKey(KeyEvent.VK_NUM_LOCK);
		}
		keyPress(KeyEvent.VK_CONTROL);
		sendKey(KeyEvent.VK_UP);
		delay(50);
		sendKey(KeyEvent.VK_DOWN);
		keyRelease(KeyEvent.VK_CONTROL);

		// Restore original numlock value
		if (numlock) {
			sendKey(KeyEvent.VK_NUM_LOCK);
		}
	}

	private static double distance(double a, double b) {
		return (a - b) * (a - b);
	}

	private static boolean isCloseSquare(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = (rgb1 >> 0) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = (rgb2 >> 0) & 0xff;
		return distance(r1, r2) + distance(g1, g2) + distance(b1, b2) <= TOLERANCE_SQUARE;
	}

	public synchronized boolean closePixelColor(Point p, Color color) {
		return isCloseSquare(color.getRGB(), getPixelColor(p.x, p.y).getRGB());
	}
}
