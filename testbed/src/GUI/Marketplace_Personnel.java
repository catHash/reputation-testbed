package GUI;

import main.Parameter;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class Marketplace_Personnel extends JPanel implements FocusListener {
	SpringLayout	layout		= new SpringLayout();
	String[]		labels		= { "Number Of Honest Buyer:", "Number Of Dishonest Buyer:", 
			"Number Of Honest Seller:", "Number Of Dishonest Seller:", "Initial Balance:"};
	
	final String[] DEFAULT =
		{
			"For Example: 100", 
			"For Example: 100",
			"For Example: 100",
			"For Example: 100",
			"For Example: 100.00"
		};
	JLabel[]		label		= new JLabel[DEFAULT.length];
	JTextField[]	textfield	= new JTextField[DEFAULT.length];

	public Marketplace_Personnel() {
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

		layout.putConstraint(SpringLayout.WEST, textfield[0], 113, SpringLayout.EAST, label[0]);
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
	
	public void saveParam() {
		if (textfield[0].getText().equals(DEFAULT[0]) || textfield[0].getText() == null)
			Parameter.setNO_OF_HONEST_BUYERS((Integer.parseInt(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_HONEST_BUYERS(Integer.parseInt(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setNO_OF_DISHONEST_BUYERS((Integer.parseInt(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_DISHONEST_BUYERS(Integer.parseInt(textfield[1].getText()));
		if (textfield[2].getText().equals(DEFAULT[2]) || textfield[2].getText() == null)
			Parameter.setNO_OF_HONEST_SELLERS((Integer.parseInt(DEFAULT[2].substring(DEFAULT[2].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_HONEST_SELLERS(Integer.parseInt(textfield[2].getText()));
		if (textfield[3].getText().equals(DEFAULT[3]) || textfield[3].getText() == null)
			Parameter.setNO_OF_DISHONEST_SELLERS((Integer.parseInt(DEFAULT[3].substring(DEFAULT[3].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_DISHONEST_SELLERS(Integer.parseInt(textfield[3].getText()));
		if (textfield[4].getText().equals(DEFAULT[4]) || textfield[4].getText() == null)
			Parameter.setINITIAL_BALANCE(Double.parseDouble((DEFAULT[4].substring(DEFAULT[4].indexOf(":")+2))));
		else 
			Parameter.setINITIAL_BALANCE(Double.parseDouble(textfield[4].getText()));

	}

	public String configuration(String filename) {
		String fileS = "SavedConfiguration/" + filename;
		Boolean success = new File(fileS).mkdirs();
		String defaultKey;
		for (int i = 0; i < textfield.length; i++)
		{
			if (textfield[i].getText().equals(DEFAULT[i]))
			{
				String[] defaultKeys = DEFAULT[i].split(": ", 0);
				defaultKey = defaultKeys[1];
				textfield[i].setText(defaultKey);
			}
		}
		try {
			File file = new File(fileS + "/AgentConfiguration.ini");
			PrintWriter output = new PrintWriter(file);
			output.print("honestBuyerNum=");
			output.println(this.textfield[0].getText());
			output.print("dishonestBuyerNum=");
			output.println(this.textfield[1].getText());
			output.print("honestSellerNum=");
			output.println(this.textfield[2].getText());
			output.print("dishonestSellerNum=");
			output.println(this.textfield[3].getText());
			output.print("initialBalance=");
			output.println(this.textfield[4].getText());
			output.close();
			fileS = file.getAbsolutePath();
		} catch (Exception ex) {
			System.out.println("IO Exception occured");
		}
		//this.setTextField();
		return fileS;
	}
	
	public void importConfig(String filename)
	{
		File file = new File(filename);
		String[] key = null;
		int i = 0;
		
		try
		{
			Scanner reader = new Scanner(file);
			
			while (reader.hasNext())
			{
				String data = reader.nextLine();
				key = data.split("=", 0);
				textfield[i].setForeground(Color.black);
				textfield[i].setText(key[1]);
				i++;
			}
			
			reader.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
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
	
	public void reset(){
		Color color = Color.gray;
		for (int i = 0; i < textfield.length; i++)
		{
			textfield[i].setForeground(color);
			textfield[i].setText(DEFAULT[i]);
			textfield[i].setToolTipText(DEFAULT[i]);
		}
	}
	
	public void setTextField()
	{
		if ((Parameter.ENV_IS_REAL&&Parameter.FIRST_CONFIG_RE)||((!Parameter.ENV_IS_REAL)&&Parameter.FIRST_CONFIG_SE)){
			Color color = Color.gray;
			for (int i = 0; i < textfield.length; i++)
			{
				textfield[i].setForeground(color);
				textfield[i].setText(DEFAULT[i]);
				textfield[i].setToolTipText(DEFAULT[i]);
			}
		}else{
			Color color = Color.black;
			textfield[0].setForeground(color);
			textfield[0].setText(Integer.toString(Parameter.NO_OF_HONEST_BUYERS));
			textfield[0].setToolTipText(Integer.toString(Parameter.NO_OF_HONEST_BUYERS));
			textfield[1].setForeground(color);
			textfield[1].setText(Integer.toString(Parameter.NO_OF_DISHONEST_BUYERS));
			textfield[1].setToolTipText(Integer.toString(Parameter.NO_OF_DISHONEST_BUYERS));
			textfield[2].setForeground(color);
			textfield[2].setText(Integer.toString(Parameter.NO_OF_HONEST_SELLERS));
			textfield[2].setToolTipText(Integer.toString(Parameter.NO_OF_HONEST_SELLERS));
			textfield[3].setForeground(color);
			textfield[3].setText(Integer.toString(Parameter.NO_OF_DISHONEST_SELLERS));
			textfield[3].setToolTipText(Integer.toString(Parameter.NO_OF_DISHONEST_SELLERS));
			textfield[4].setForeground(color);
			textfield[4].setText(Double.toString(Parameter.INITIAL_BALANCE));
			textfield[4].setToolTipText(Double.toString(Parameter.INITIAL_BALANCE));
		}
	
		

	}
}
