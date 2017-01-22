import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Ui extends JFrame {
	private static final long serialVersionUID = -7656625449604764076L;

	public static final String CONFIG_FILE = "afk_config.xml";

	private JCheckBox paladinEnabled;
	private JCheckBox mageEnabled;

	public PointComponent spearHand;
	public PointComponent spearDrop;
	public PointComponent monkHealth;
	public PointComponent paladinMana;
	public PointComponent paladinFood;
	public PointComponent mageMana;
	public PointComponent mageHand;
	public PointComponent mageRunes;
	public PointComponent mageFood;

	private ArrayList<PointComponent> paladinPointComponents;
	private ArrayList<PointComponent> magePointComponents;

	public Ui(Robot robot, Properties props) {
		super("Afk");

		paladinEnabled = new JCheckBox("Paladin enabled");
		mageEnabled = new JCheckBox("Mage enabled");

		boolean paladinEnabledProp = Boolean.parseBoolean(props.getProperty("paladin enabled", "true"));
		paladinEnabled.setSelected(paladinEnabledProp);

		boolean mageEnabledProp = Boolean.parseBoolean(props.getProperty("mage enabled", "true"));
		mageEnabled.setSelected(mageEnabledProp);

		spearHand = new PointComponent("Spear hand", robot, props);
		spearDrop = new PointComponent("Spear drop", robot, props);
		monkHealth = new PointComponent("Monk health", robot, props);
		paladinMana = new PointComponent("Paladin mana", robot, props);
		paladinFood = new PointComponent("Paladin food", robot, props);

		mageMana = new PointComponent("Mage mana", robot, props);
		mageHand = new PointComponent("Mage hand", robot, props);
		mageRunes = new PointComponent("Mage runes", robot, props);
		mageFood = new PointComponent("Mage food", robot, props);

		paladinPointComponents = new ArrayList<>();
		magePointComponents = new ArrayList<>();

		paladinPointComponents.add(spearHand);
		paladinPointComponents.add(spearDrop);
		paladinPointComponents.add(monkHealth);
		paladinPointComponents.add(paladinMana);
		paladinPointComponents.add(paladinFood);

		magePointComponents.add(mageMana);
		magePointComponents.add(mageHand);
		magePointComponents.add(mageRunes);
		magePointComponents.add(mageFood);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(panel);

		panel.add(paladinEnabled);
		panel.add(mageEnabled);

		for (PointComponent p : paladinPointComponents) {
			panel.add(p);
		}
		for (PointComponent p : magePointComponents) {
			panel.add(p);
		}

		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		timer.start();

		pack();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				timer.stop();
				onClose();
			}
		});
	}

	public boolean paladinEnabled() {
		return paladinEnabled.isSelected();
	}

	public boolean mageEnabled() {
		return mageEnabled.isSelected();
	}

	private void saveConfiguration() {
		try {
			Properties props = new Properties();

			props.setProperty("paladin enabled", Boolean.toString(paladinEnabled()));
			props.setProperty("mage enabled", Boolean.toString(mageEnabled()));

			for (PointComponent p : paladinPointComponents) {
				p.save(props);
			}
			for (PointComponent p : magePointComponents) {
				p.save(props);
			}

			props.storeToXML(new FileOutputStream(CONFIG_FILE), "Configuration for ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onClose() {
		Afk.stop();
		saveConfiguration();
	}
}
