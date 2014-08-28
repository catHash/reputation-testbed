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

public class Marketplace_Schedule extends JPanel implements FocusListener {
	SpringLayout	layout		= new SpringLayout();
	//String[]		labels		= { "Maximum Time Step:", "Warm Up Period:" };
	String[]		labels		= { "No of Days:" , "No of Runtimes:" };
	
	final String[] DEFAULT =
						{"For Example: 100" ,
			             "For Example: 100"
		
						};
	
	JLabel[]		label		= new JLabel[2];
	JTextField[]	textfield	= new JTextField[2];

	public Marketplace_Schedule() {
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
		
		layout.putConstraint(SpringLayout.WEST, label[1], 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[1], 5, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, textfield[1], 95, SpringLayout.EAST, label[0]);
		layout.putConstraint(SpringLayout.NORTH, textfield[1], 5, SpringLayout.NORTH, this);
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
			Parameter.setNO_OF_DAYS((Integer.parseInt(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_DAYS(Integer.parseInt(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setNO_OF_RUNTIMES((Integer.parseInt(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2))));
		else 
			Parameter.setNO_OF_RUNTIMES(Integer.parseInt(textfield[1].getText()));
			
	}

	public String configuration(String filename) {
		String fileS = "SavedConfiguration/" + filename;
		boolean success = new File(fileS).mkdirs();
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
			File file = new File(fileS + "/SchedulerConfiguration.ini");
			PrintWriter output = new PrintWriter(file);
			output.print("noOfDays=");
			output.println(this.textfield[0].getText());
			output.print("noOfRuntimes=");
			output.println(this.textfield[1].getText());
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
			textfield[0].setText(Integer.toString(Parameter.NO_OF_DAYS));
			textfield[0].setToolTipText(Integer.toString(Parameter.NO_OF_DAYS));
			textfield[1].setForeground(color);
			textfield[1].setText(Integer.toString(Parameter.NO_OF_RUNTIMES));
			textfield[1].setToolTipText(Integer.toString(Parameter.NO_OF_RUNTIMES));
			
		}
			
	}
}