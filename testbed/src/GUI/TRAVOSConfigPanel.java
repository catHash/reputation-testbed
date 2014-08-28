package GUI;

import main.Parameter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.io.PrintWriter;

public class TRAVOSConfigPanel extends JPanel implements FocusListener, ActionListener {
	SpringLayout	layout		= new SpringLayout();
	String[]		labels		= { "Number Of Bins", "Error Threadshold:",
			"Minimum Accuracy Value:" };
	JLabel[]		label		= new JLabel[3];
	JTextField[]	textfield	= new JTextField[3];
	
	final String[] DEFAULT =
		{
			"For Example: 3",
			"For Example: 0.2",
			"For Example: 0.5"
		};

	public TRAVOSConfigPanel() {
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
			File file = new File("TRAVOSTrustModelConfiguration.ini");
			int i = 0;
			while (file.exists()) {
				file = new File("TRAVOSTrustModelConfiguration" + i + ".ini");
				i++;
			}

			PrintWriter output = new PrintWriter(file);
			output.print("numBins=");
			output.println(this.textfield[0].getText());
			output.print("errorThredshold=");
			output.println(this.textfield[1].getText());
			output.print("minAccuracyValue=");
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
			Parameter.setNO_OF_BINS((Integer.parseInt(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_BINS(Integer.parseInt(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setError_threshold((Double.parseDouble(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2))));
		else 
			Parameter.setError_threshold(Double.parseDouble(textfield[1].getText()));
		if (textfield[2].getText().equals(DEFAULT[2]) || textfield[2].getText() == null)
			Parameter.setMinAccValue((Double.parseDouble(DEFAULT[2].substring(DEFAULT[2].indexOf(":")+2))));
		else 
			Parameter.setMinAccValue(Double.parseDouble(textfield[2].getText()));
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