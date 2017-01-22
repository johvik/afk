import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PointComponent extends JPanel {
	private static final long serialVersionUID = 2893360897710721151L;

	private Robot robot;
	private String name;
	private SpinnerNumberModel xModel;
	private SpinnerNumberModel yModel;
	private PointImage pointImage;

	public PointComponent(String name, Robot robot, Properties props) {
		this.name = name;
		this.robot = robot;
		xModel = new SpinnerNumberModel(0, 0, 99999, 1);
		yModel = new SpinnerNumberModel(0, 0, 99999, 1);
		load(props);

		pointImage = new PointImage(50, 50);

		pointImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickSelectPosition();
			}
		});

		JSpinner xSpinner = new JSpinner(xModel);
		JSpinner ySpinner = new JSpinner(yModel);
		((DefaultEditor) xSpinner.getEditor()).getTextField().setEditable(false);
		((DefaultEditor) ySpinner.getEditor()).getTextField().setEditable(false);

		ChangeListener l = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				pointImage.repaint();
			}
		};
		xSpinner.addChangeListener(l);
		ySpinner.addChangeListener(l);

		add(new JLabel(name));
		add(xSpinner);
		add(ySpinner);
		add(pointImage);
	}

	private void onClickSelectPosition() {
		int option = JOptionPane.showConfirmDialog(getParent(), "Move mouse to " + name, "Select position",
				JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			Point point = MouseInfo.getPointerInfo().getLocation();
			xModel.setValue(point.x);
			yModel.setValue(point.y);
			pointImage.repaint();
		}
	}

	public int x() {
		return xModel.getNumber().intValue();
	}

	public int y() {
		return yModel.getNumber().intValue();
	}

	public Point getPoint() {
		return new Point(x(), y());
	}

	private void load(Properties props) {
		int x = Integer.parseInt(props.getProperty(name + " x", "0"));
		int y = Integer.parseInt(props.getProperty(name + " y", "0"));
		xModel.setValue(x);
		yModel.setValue(y);
	}

	public void save(Properties props) {
		props.setProperty(name + " x", Integer.toString(x()));
		props.setProperty(name + " y", Integer.toString(y()));
	}

	class PointImage extends JPanel {
		private static final long serialVersionUID = 9119897492538776495L;

		public PointImage(int width, int height) {
			setPreferredSize(new Dimension(width, height));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Dimension preferredSize = getPreferredSize();
			int x = x() - preferredSize.width / 2;
			int y = y() - preferredSize.height / 2;
			BufferedImage image = robot.createScreenCapture(new Rectangle(Math.max(0, x), Math.max(0, y),
					preferredSize.width + Math.min(0, x), preferredSize.height + Math.min(0, y)));
			g.drawImage(image, preferredSize.width - image.getWidth(), preferredSize.height - image.getHeight(), null);

			// Red cross
			g.setColor(Color.RED);
			g.drawLine(0, 0, preferredSize.width, preferredSize.height);
			g.drawLine(preferredSize.width, 0, 0, preferredSize.height);
		}
	}
}
