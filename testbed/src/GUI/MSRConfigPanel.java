package GUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import main.Parameter;
import distributions.PseudoRandom;

public class MSRConfigPanel extends JPanel implements FocusListener, ActionListener
{
    SpringLayout layout = new SpringLayout();
    int inputNo = Parameter.NO_OF_CRITERIA*2;
    String[] labels = new String[inputNo];   
    JLabel[] label = new JLabel[inputNo];
    JTextField[] textfield = new JTextField[inputNo];
    JButton next_config;
    ArrayList<String[]> defaults = new ArrayList<String[]>();
    String[] DEFAULT_1C = {"For example: 1"};
    String[] DEFAULT_2C = {"For example: 0.3","For example: 0.7"};
    String[] DEFAULT_3C = {"For example: 0.3","For example: 0.45","For example: 0.25" };
    String[] DEFAULT_4C = {"For example: 0.16","For example: 0.28","For example: 0.35","For example: 0.21"};
    String[] DEFAULT_5C = {"For example: 0.33","For example: 0.17","For example: 0.22","For example: 0.12","For example: 0.16"};
    String[] DEFAULT_6C = {"For example: 0.28","For example: 0.09","For example: 0.23","For example: 0.1","For example: 0.14","For example: 0.16"};
    String[] DEFAULT_7C = {"For example: 0.3","For example: 0.1","For example: 0.15","For example: 0.2","For example: 0.05","For example: 0.07","For example: 0.13"};
    String[] DEFAULT_8C = {"For example: 0.1","For example: 0.13","For example: 0.16","For example: 0.08","For example: 0.06","For example: 0.21","For example: 0.15","For example: 0.11"};
    
    
    public MSRConfigPanel()
    {
        
    	this.setLayout(layout);
        
        for (int i = 0; i< Parameter.NO_OF_CRITERIA; i++){
        	labels[i] = "Importance weight ["+i+"]";
        }
        for (int j = 0; j< Parameter.NO_OF_CRITERIA; j++){
        	labels[j+Parameter.NO_OF_CRITERIA] = "Preferential weight ["+j+"]";
        }
        
        for (int i = 0; i < label.length; i++) {
        	label[i] = new JLabel(labels[i]);
			textfield[i] = new JTextField(20);
	        
	        this.add(label[i]);
	        this.add(textfield[i]);
	        textfield[i].addFocusListener(this);
        }
        defaults.add(DEFAULT_1C);
        defaults.add(DEFAULT_2C);
        defaults.add(DEFAULT_3C);
        defaults.add(DEFAULT_4C);
        defaults.add(DEFAULT_5C);
        defaults.add(DEFAULT_6C);
        defaults.add(DEFAULT_7C);
        defaults.add(DEFAULT_8C);
        
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
    
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            File file = new File("MSRTrustModelConfiguration.ini");
            int i = 0;
            while(file.exists())
            {
                file = new File("MSRTrustModelConfiguration" + i + ".ini");
                i++;
            }
            
            PrintWriter output = new PrintWriter(file);
            for(int m=0;m<inputNo;m++){
            	output.print(labels[m]+"=");
                output.println(this.textfield[m].getText());
            }
            
            output.close();
        }
        catch (Exception ex)
        {
            System.out.println("IO Exception occured");    
        }
        for (int i = 0; i < textfield.length; i++) {
			this.textfield[i].setText("");
		}
    }
    
    public void saveParam() {
    	ArrayList<Double> imp_weight = new ArrayList<Double>();
    	ArrayList<Double> pre_weight = new ArrayList<Double>();
    	
    	for(int n=0;n<Parameter.NO_OF_CRITERIA;n++){
    		if (textfield[n].getText().equals(defaults.get(Parameter.NO_OF_CRITERIA-1)[n]) || textfield[n].getText() == null)
    			imp_weight.add(Double.parseDouble(defaults.get(Parameter.NO_OF_CRITERIA-1)[n].substring(defaults.get(Parameter.NO_OF_CRITERIA-1)[n].indexOf(":")+2)));
    		else 
    			imp_weight.add(Double.parseDouble(textfield[n].getText()));
    	}
    	for(int p=0;p<Parameter.NO_OF_CRITERIA;p++){
    		int q = p+Parameter.NO_OF_CRITERIA;
    		if (textfield[q].getText().equals(defaults.get(Parameter.NO_OF_CRITERIA-1)[p]) || textfield[q].getText() == null)
    			pre_weight.add(Double.parseDouble(defaults.get(Parameter.NO_OF_CRITERIA-1)[p].substring(defaults.get(Parameter.NO_OF_CRITERIA-1)[p].indexOf(":")+2)));
    		else 
    			pre_weight.add(Double.parseDouble(textfield[q].getText()));
    	}
    	Parameter.setMSR_imp_weight(imp_weight);
    	Parameter.setMSR_pre_weight(pre_weight);
	}

	public void focusGained(FocusEvent e) {
		for (int i = 0; i < textfield.length; i++) {
			if (e.getSource() == textfield[i]) {
				textfield[i].setForeground(Color.black);
				if(i<Parameter.NO_OF_CRITERIA){
				if (textfield[i].getText().equalsIgnoreCase(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]))
				{
					textfield[i].setText("");
				}
				}else{
					if (textfield[i].getText().equalsIgnoreCase(defaults.get(Parameter.NO_OF_CRITERIA-1)[i-Parameter.NO_OF_CRITERIA-1]))
					{
						textfield[i].setText("");
					}
				}
			}
		}
	}

	public void focusLost(FocusEvent e) {
		for (int i = 0; i < textfield.length; i++) {
			if (e.getSource() == textfield[i]) {
				if (textfield[i].getText().isEmpty())
				{
					if(i<Parameter.NO_OF_CRITERIA){
						textfield[i].setText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]);
						textfield[i].setForeground(Color.gray);
					}else{
						
						textfield[i].setText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i-Parameter.NO_OF_CRITERIA-1]);
						textfield[i].setForeground(Color.gray);
						
					}
				}
			}
		}
	}
	
	public void setTextField()
	{
		Color color = Color.gray;
		for (int i = 0; i < Parameter.NO_OF_CRITERIA; i++)
		{
			textfield[i].setForeground(color);
			textfield[i].setText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]);
			textfield[i].setToolTipText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]);
		}
		for (int i = 0; i < Parameter.NO_OF_CRITERIA; i++)
		{
			textfield[i+Parameter.NO_OF_CRITERIA].setForeground(color);
			textfield[i+Parameter.NO_OF_CRITERIA].setText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]);
			textfield[i+Parameter.NO_OF_CRITERIA].setToolTipText(defaults.get(Parameter.NO_OF_CRITERIA-1)[i]);
		}
	}
    
       
}