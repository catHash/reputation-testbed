package GUI;

import main.Parameter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.io.PrintWriter;

public class MeTrustedGraphConfigPanel extends JPanel implements FocusListener, ActionListener {
	SpringLayout	layout		= new SpringLayout();
	String[]		labels		= { "Privileged Strategy:", "Sub Strategy:", "Lambda:"};
	JLabel[]		label		= new JLabel[3];
	JTextField[]	textfield	= new JTextField[3];
	
	final String[] DEFAULT =
		{
			"For Example: Belief, Plausibility or Hybrid (Default: Belief)",
			"For Example: All or Primary (Default: All)",
			"For Example: 0 to positive infinity (Default: 0)",
		};

	public MeTrustedGraphConfigPanel() {
		this.setLayout(layout);

		for (int i = 0; i < label.length; i++) {
			label[i] = new JLabel(labels[i]);
			textfield[i] = new JTextField(20);
			this.add(label[i]);
			this.add(textfield[i]);
			textfield[i].addFocusListener(this);
		}
		
		setTextField();

		layout.putConstraint(SpringLayout.WEST, label[0], 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[0], 5, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, textfield[0], 95, SpringLayout.EAST, label[0]);
		layout.putConstraint(SpringLayout.NORTH, textfield[0], 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, this, 10, SpringLayout.EAST, textfield[0]);
		

		for (int i = 1; i < label.length; i++) {
			layout.putConstraint(SpringLayout.WEST, label[i], 5, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.NORTH, label[i], 5, SpringLayout.SOUTH,
					textfield[i - 1]);

			layout.putConstraint(SpringLayout.WEST, textfield[i], 0, SpringLayout.WEST,
					textfield[i - 1]);
			layout.putConstraint(SpringLayout.NORTH, textfield[i], 5, SpringLayout.SOUTH,
					textfield[i - 1]);
			layout.putConstraint(SpringLayout.EAST, textfield[i], 0, SpringLayout.EAST,
					textfield[0]);
		}
		layout.putConstraint(SpringLayout.SOUTH, this, 10, SpringLayout.SOUTH, textfield[textfield.length - 1]);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			File file = new File("MeTrustedGraphConfigPanelTrustModelConfiguration.ini");
			int i = 0;
			while (file.exists()) {
				file = new File("MeTrustedGraphConfigPanelTrustModelConfiguration" + i + ".ini");
				i++;
			}

			PrintWriter output = new PrintWriter(file);
			output.print("pri=");
			output.println(this.textfield[0].getText());
			output.print("sub=");
			output.println(this.textfield[1].getText());
			output.print("lambda=");
			output.println(this.textfield[2].getText());
			output.close();
		} catch (Exception ex) {
			System.out.println("IO Exception occured");
		}
		for (int i = 0; i < textfield.length; i++) {
			this.textfield[i].setText("");
		}
	}

	public void saveParam() {
		if (textfield[0].getText().equals(DEFAULT[0]) || textfield[0].getText() == null)
			Parameter.privilegedStrat= "Belief";
		else 
			Parameter.privilegedStrat = textfield[0].getText().toString();
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.subStrat = "All";
		else 
			Parameter.subStrat = textfield[1].getText().toString();
		if (textfield[2].getText().equals(DEFAULT[2]) || textfield[2].getText() == null)
			Parameter.lambda = 0;
		else 
			Parameter.lambda = Double.parseDouble(textfield[2].getText());
	}

	public void focusGained(FocusEvent e) {
		for (int i = 0; i < textfield.length; i++) {
			if (e.getSource() == textfield[i]) {
				textfield[i].setForeground(Color.black);
				if (textfield[i].getText().equalsIgnoreCase(DEFAULT[i]))
				{
					textfield[i].setText("");
				}
				
			}
		}
	}

	public void focusLost(FocusEvent e) {
		for (int i = 0; i < textfield.length; i++) {
			if (e.getSource() == textfield[i]) {
				if (textfield[i].getText().isEmpty())
				{
					textfield[i].setText(DEFAULT[i]);
					textfield[i].setForeground(Color.gray);
				}
			}
		}
	}
	
	public void setTextField()
	{
		Color color = Color.gray;
		for (int i = 0; i < textfield.length; i++)
		{
			textfield[i].setForeground(color);
			textfield[i].setText(DEFAULT[i]);
			textfield[i].setToolTipText(DEFAULT[i]);
		}
	}
}