package GUI;

import main.Parameter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;
import java.io.PrintWriter;

public class ProbCogConfigPanel extends JPanel implements FocusListener, ActionListener
{
    SpringLayout layout = new SpringLayout();
    String[] labels = { "Mu value:"};
    JLabel[] label = new JLabel[1];
    JTextField[] textfield = new JTextField[1];
    JButton next_config;
    
    final String[] DEFAULT =
		{
			"For Example: 0.01"
		};
    
    public ProbCogConfigPanel()
    {
        this.setLayout(layout);
        
        for (int i = 0; i < label.length; i++) {
        	label[i] = new JLabel(labels[i]);
			textfield[i] = new JTextField(20);
	        
	        this.add(label[i]);
	        this.add(textfield[i]);
	        textfield[i].addFocusListener(this);
        }
        
        setTextField();
        
        
        next_config = new JButton("Config");
        this.add(next_config);
        next_config.addActionListener(this);
        
        layout.putConstraint(SpringLayout.WEST, label[0], 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, label[0], 5, SpringLayout.NORTH, this);
        
        layout.putConstraint(SpringLayout.WEST, textfield[0], 5, SpringLayout.EAST, label[0]);
        layout.putConstraint(SpringLayout.NORTH, textfield[0], 5, SpringLayout.NORTH, this);
        
        layout.putConstraint(SpringLayout.WEST, next_config, 0, SpringLayout.WEST, textfield[0]);
        layout.putConstraint(SpringLayout.NORTH, next_config, 5, SpringLayout.SOUTH, textfield[textfield.length - 1]);
        layout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, next_config);
        
        this.setVisible(false);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            File file = new File("ProbCogTrustModelConfiguration.ini");
            int i = 0;
            while(file.exists())
            {
                file = new File("ProbCogTrustModelConfiguration" + i + ".ini");
                i++;
            }
            
            PrintWriter output = new PrintWriter(file);
            output.print("Mu value =");
            output.println(this.textfield[0].getText());
            output.close();
        }
        catch (Exception ex)
        {
            System.out.println("IO Exception occured");    
        }
        this.textfield[0].setText("");
    }
    
    public void saveParam() {
		if (textfield[0].getText().equals(DEFAULT[0]) || textfield[0].getText() == null)
			Parameter.setMu_value((Double.parseDouble(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setMu_value(Double.parseDouble(textfield[0].getText()));
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