package GUI;

import main.Parameter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class Marketplace_Simulation extends JPanel implements FocusListener, ActionListener{
	SpringLayout layout = new SpringLayout();
	String[] labels = {"Credit per turn:", "Rating Type:", "No of Criteria:"};
	final String[] DEFAULT = {
			"For Example: 100","","For Example: 1"};
	//"For Example: Real/Binary/Multinominal"

	JLabel[] label = new JLabel[3];
	JTextField textfield = new JTextField();
	Marketplace_Personnel agentConfig; 
	Marketplace_Goods productConfig;
	Marketplace_Schedule schedConfig; 
	String[] ratingType = {"Real", "Binary", "Multinominal"};
	JTextField criteriano = new JTextField();
	JComboBox<String> combo = new JComboBox<String>();
	String command = "real";//default
	Marketplace_Main mm;


	public Marketplace_Simulation(Marketplace_Main mm)
	{
		this.setLayout(layout);
		this.mm=mm;
		for (int i = 0; i < label.length; i++) {
			label[i] = new JLabel(labels[i]);
			this.add(label[i]);
			// this.setText(textfield[i], i);

		}
		this.add(textfield);
		this.add(criteriano);		
		textfield.addFocusListener(this);
		criteriano.addFocusListener(this);
		this.setTextField();
		this.setCriteriaNo();
		combo = new JComboBox<String>(ratingType);
		combo.addActionListener(this);
		this.setRatingType();
		
		
		
		Border blackline = BorderFactory.createLineBorder(Color.BLACK);

		agentConfig = new Marketplace_Personnel();
		TitledBorder agentTitle = BorderFactory
				.createTitledBorder(blackline, "Agent Configuration");
		agentConfig.setBorder(agentTitle);

		productConfig = new Marketplace_Goods();
		TitledBorder productTitle = BorderFactory.createTitledBorder(blackline,
				"Product Configuration");
		productConfig.setBorder(productTitle);

		schedConfig = new Marketplace_Schedule();
		TitledBorder schedTitle = BorderFactory.createTitledBorder(blackline,
				"Scheduler Configuration");
		schedConfig.setBorder(schedTitle);

		this.add(combo);
		this.add(agentConfig);
		this.add(productConfig);
		this.add(schedConfig);


		layout.putConstraint(SpringLayout.WEST, label[0], 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[0], 5, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, textfield, 138, SpringLayout.EAST, label[0]);
		layout.putConstraint(SpringLayout.NORTH, textfield, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, this, 25, SpringLayout.EAST, textfield);

		layout.putConstraint(SpringLayout.WEST, label[1], 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[1], 5, SpringLayout.SOUTH, textfield);

		layout.putConstraint(SpringLayout.WEST, combo, 0, SpringLayout.WEST, textfield);
		layout.putConstraint(SpringLayout.NORTH, combo, 5, SpringLayout.SOUTH, textfield);
		layout.putConstraint(SpringLayout.EAST, combo, 0, SpringLayout.EAST, textfield);

		layout.putConstraint(SpringLayout.WEST, label[2], 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[2], 5, SpringLayout.NORTH, criteriano);

		layout.putConstraint(SpringLayout.WEST, criteriano, 0, SpringLayout.WEST, combo);
		layout.putConstraint(SpringLayout.NORTH, criteriano, 5, SpringLayout.SOUTH, combo);
		layout.putConstraint(SpringLayout.EAST, criteriano, 0, SpringLayout.EAST, combo);

		layout.putConstraint(SpringLayout.WEST, agentConfig, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, agentConfig, 10, SpringLayout.SOUTH, criteriano);
		layout.putConstraint(SpringLayout.EAST, agentConfig, -10, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.WEST, productConfig, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, productConfig, 10, SpringLayout.SOUTH, agentConfig);
		layout.putConstraint(SpringLayout.EAST, productConfig, 0, SpringLayout.EAST, agentConfig);

		layout.putConstraint(SpringLayout.WEST, schedConfig, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, schedConfig, 10, SpringLayout.SOUTH, productConfig);
		layout.putConstraint(SpringLayout.EAST, schedConfig, 0, SpringLayout.EAST, agentConfig);

		layout.putConstraint(SpringLayout.SOUTH, this, 10, SpringLayout.SOUTH, schedConfig);

		//this.setSize(700, 700);
		this.setVisible(true);


	}

	public void actionPerformed(ActionEvent e)
	{ 					
		//do nothing
	}

	public void focusGained(FocusEvent e) {
		for (int i = 0; i < 2; i++) {
			if (e.getSource() == textfield) {
				textfield.setForeground(Color.black);
				if (textfield.getText().equalsIgnoreCase(DEFAULT[0]))
				{
					textfield.setText("");
				}
			}
			if (e.getSource() == criteriano) {
				criteriano.setForeground(Color.black);
				if (criteriano.getText().equalsIgnoreCase(DEFAULT[2]))
				{
					criteriano.setText("");
				}
			}

		}
	}

	public void focusLost(FocusEvent e) {
		for (int i = 0; i <2; i++) {
			if (e.getSource() == textfield) {
				if (textfield.getText().isEmpty())
				{
					textfield.setText(DEFAULT[0]);
					textfield.setForeground(Color.gray);
				}
			}
			if (e.getSource() == criteriano) {
				if (criteriano.getText().isEmpty())
				{
					criteriano.setText(DEFAULT[2]);
					criteriano.setForeground(Color.gray);
				}
			}
		}
	}

	public void setTextField()
	{
		if((Parameter.ENV_IS_REAL&&Parameter.FIRST_CONFIG_RE)||((!Parameter.ENV_IS_REAL)&&Parameter.FIRST_CONFIG_SE)){
			Color color = Color.gray;
			for (int i = 0; i < 1; i++)
			{
				textfield.setForeground(color);
				textfield.setText(DEFAULT[0]);
				textfield.setToolTipText(DEFAULT[0]);
			}	
		}else{
			textfield.setForeground(Color.black);
			textfield.setText(Integer.toString(Parameter.CREDITS_PER_TURN));
			textfield.setToolTipText(Integer.toString(Parameter.CREDITS_PER_TURN));

		}
	}
	
	public void reset(){
		Color color = Color.gray;
		textfield.setForeground(color);
		textfield.setText(DEFAULT[0]);
		textfield.setToolTipText(DEFAULT[0]);
		combo.setSelectedIndex(0);
		criteriano.setForeground(color);
		criteriano.setText(DEFAULT[2]);
		criteriano.setToolTipText(DEFAULT[2]);
	}
	
	public void setRatingType(){
		if((Parameter.ENV_IS_REAL&&Parameter.FIRST_CONFIG_RE)||((!Parameter.ENV_IS_REAL)&&Parameter.FIRST_CONFIG_SE)){
			combo.setSelectedIndex(0);
		}else{
			if(Parameter.RATING_TYPE.equalsIgnoreCase("real"))
				combo.setSelectedIndex(0);
			else if (Parameter.RATING_TYPE.equalsIgnoreCase("binary"))
				combo.setSelectedIndex(1);
			else
				combo.setSelectedIndex(2);
		}
	}
	
	public void setCriteriaNo()
	{
		if((Parameter.ENV_IS_REAL&&Parameter.FIRST_CONFIG_RE)||((!Parameter.ENV_IS_REAL)&&Parameter.FIRST_CONFIG_SE)){
			Color color = Color.gray;
			for (int i = 0; i < 1; i++)
			{
				criteriano.setForeground(color);
				criteriano.setText(DEFAULT[2]);
				criteriano.setToolTipText(DEFAULT[2]);
			}
		}else{
			criteriano.setForeground(Color.black);
			criteriano.setText(Integer.toString(Parameter.NO_OF_CRITERIA));
			criteriano.setToolTipText(Integer.toString(Parameter.NO_OF_CRITERIA));
		}
	}
	public void saveParam() {
		command = (String) combo.getSelectedItem().toString().toLowerCase();	
		if (textfield.getText().equals(DEFAULT[0]) || textfield.getText() == null)
			Parameter.setCREDITS_PER_TURN(Integer.parseInt((DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setCREDITS_PER_TURN(Integer.parseInt(textfield.getText()));
		if (criteriano.getText().equals(DEFAULT[2]) || criteriano.getText() == null || criteriano.getText().equalsIgnoreCase(""))
			Parameter.NO_OF_CRITERIA = 1;
		else 
			Parameter.NO_OF_CRITERIA = Integer.parseInt(criteriano.getText());
		if (command.equals(DEFAULT[1]) || command == null)
			Parameter.setRATING_TYPE("real");
		else 
			Parameter.setRATING_TYPE(command);
	}

	public String[] configuration(String filename)
	{
		String fileS = "SavedConfiguration/" + filename;
		String[] key = new String[5];
		String defaultKey;
		for (int i = 0; i < 1; i++)
		{
			if (textfield.getText().equals(DEFAULT[i]))
			{
				String[] defaultKeys = DEFAULT[i].split(": ", 0);
				defaultKey = defaultKeys[1];
				textfield.setText(defaultKey);
			}
			if (criteriano.getText().equals(DEFAULT[2]))
			{
				String[] defaultKeys = DEFAULT[2].split(": ", 0);
				defaultKey = defaultKeys[1];
				criteriano.setText(defaultKey);
			}


		}
		boolean success = new File(fileS).mkdirs();
		try {
			File file = new File(fileS + "/SimulationConfiguration.ini");
			PrintWriter output = new PrintWriter(file);
			output.print("creditPerTurn=");
			output.println(this.textfield.getText());
			output.print("ratingType=");
			output.println((String) combo.getSelectedItem().toString().toLowerCase());
			output.print("criteriano=");
			output.println(this.criteriano.getText());
			if(Parameter.ENV_IS_REAL == true){
				output.print("realData=");
				output.println(mm.textfield.getText());
			}
			output.print("agentConfigFile=");
			key[0] = agentConfig.configuration(filename);
			output.println(key[0]);
			output.print("productConfigFile=");
			key[1] = productConfig.configuration(filename);
			output.println(key[1]);
			output.print("schedulerConfigFile=");
			key[3] = schedConfig.configuration(filename);
			output.println(key[3]);
			output.close();
			key[4] = file.getAbsolutePath();
		} catch (Exception ex) {
			System.out.println("IO Exception occured");
		}
		//this.setTextField();
		//this.setCriteriaNo();
		return key;	
	}

	public void importConfig(String[] configFile)
	{
		File file = new File(configFile[0]);
		String key = null;
		int i = 0;
		boolean isRating = false;
		try
		{
			Scanner input = new Scanner(file);
			while (input.hasNext())
			{
				key = input.nextLine();
				String[] partKey = key.split("=", 0);
				if (partKey[0].equalsIgnoreCase("ratingType"))
				{
					textfield.setForeground(Color.black);
					criteriano.setForeground(Color.black);

					for (int j = 0; j < ratingType.length; j++) {
						if (partKey[1].equalsIgnoreCase(ratingType[j])) {
							isRating = true;
							combo.setSelectedIndex(j);
						}
					}				
				}
				else if (partKey[0].equalsIgnoreCase("creditPerTurn"))
				{
					textfield.setText(partKey[1]);
				}
				else if (partKey[0].equalsIgnoreCase("criteriano"))
				{
					criteriano.setText(partKey[1]);
				}
				else if (partKey[0].equalsIgnoreCase("realData"))
				{
					mm.textfield.setText(partKey[1]);
				}
				else
				{
					agentConfig.importConfig(configFile[1]);
					productConfig.importConfig(configFile[2]);
					schedConfig.importConfig(configFile[3]);
				}
				i++;
			}
		}
		catch (Exception e)
		{	
			System.out.println("File Not Found!!!");
		}
	}
}