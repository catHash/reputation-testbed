package GUI;

import main.Parameter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.io.PrintWriter;

public class PersonalizedConfigPanel extends JPanel implements FocusListener, ActionListener {
	SpringLayout	layout		= new SpringLayout();
	String[]		labels		= { "Epsilon:", "Gamma:", "Forgetting:", "Time Window:" };
	JLabel[]		label		= new JLabel[4];
	JTextField[]	textfield	= new JTextField[4];
	
	final String[] DEFAULT =
		{
			"For Example: 0.25",
			"For Example: 8",
			"For Example: 0.5",
			"For Example: 10"
		};

	public PersonalizedConfigPanel() {
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
			File file = new File("PersonalizedTrustModelConfiguration.ini");
			int i = 0;
			while (file.exists()) {
				file = new File("PersonalizedTrustModelConfiguration" + i + ".ini");
				i++;
			}

			PrintWriter output = new PrintWriter(file);
			output.print("epsilon=");
			output.println(this.textfield[0].getText());
			output.print("gamme=");
			output.println(this.textfield[1].getText());
			output.print("forgetting=");
			output.println(this.textfield[2].getText());
			output.print("timeWindow=");
			output.println(this.textfield[3].getText());
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
			Parameter.setP_Epsilon((Double.parseDouble(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setP_Epsilon(Double.parseDouble(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setP_Gamma((Double.parseDouble(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2))));
		else 
			Parameter.setP_Gamma(Double.parseDouble(textfield[1].getText()));
		if (textfield[2].getText().equals(DEFAULT[2]) || textfield[2].getText() == null)
			Parameter.setP_Forgetting((Double.parseDouble(DEFAULT[2].substring(DEFAULT[2].indexOf(":")+2))));
		else 
			Parameter.setP_Forgetting(Double.parseDouble(textfield[2].getText()));
		if (textfield[3].getText().equals(DEFAULT[3]) || textfield[3].getText() == null)
			Parameter.setP_TimeWindow((Integer.parseInt(DEFAULT[3].substring(DEFAULT[3].indexOf(":")+2))));
		else 
			Parameter.setP_TimeWindow(Integer.parseInt(textfield[3].getText()));
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