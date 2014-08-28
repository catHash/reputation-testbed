package GUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class OthersConfigPanel extends JPanel implements FocusListener, ActionListener
{
    SpringLayout layout = new SpringLayout();
    String[] labels;
    JLabel[] label;
    JTextField[] textfield;
    JButton next_config;
    
    final String[] DEFAULT;
    
    public OthersConfigPanel(String modelName) throws IOException
    {
        this.setLayout(layout);
        
        String filepath = "SavedConfiguration\\newModels\\"+modelName+".txt";
        File modelFile = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(modelFile));
        //first line reads model type;
        String line = br.readLine();
        //second line reads num of parameters
        line = br.readLine();
        String[] token = line.split(" = ");
        int num = Integer.parseInt(token[1].trim());        
        
        label = new JLabel[num];
        textfield = new JTextField[num];
        DEFAULT = new String[num];
        
        line = br.readLine();
        line = br.readLine();
        
        for (int i = 0; i < num; i++) {
        	line = br.readLine();
        	token = line.split("\t");
        	label[i] = new JLabel(token[0].trim());
			textfield[i] = new JTextField(20);
			DEFAULT[i] = "For Example: "+token[1].trim();
	        
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
    
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            File file = new File("BRSTrustModelConfiguration.ini");
            int i = 0;
            while(file.exists())
            {
                file = new File("BRSTrustModelConfiguration" + i + ".ini");
                i++;
            }
            
            PrintWriter output = new PrintWriter(file);
            output.print("quantile=");
            output.println(this.textfield[0].getText());
            output.close();
        }
        catch (Exception ex)
        {
            System.out.println("IO Exception occured");    
        }
        this.textfield[0].setText("");
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
       
