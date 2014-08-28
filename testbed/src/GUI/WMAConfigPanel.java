package GUI;

import main.Parameter;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class WMAConfigPanel extends JPanel implements FocusListener, ActionListener
{
    SpringLayout layout = new SpringLayout();
    String[] labels = { "Neighbour Limit", "Depth Limit"};
    JLabel[] label = new JLabel[2];
    JTextField[] textfield = new JTextField[2];
    
    final String[] DEFAULT =
		{
			"For Example: 4",
    		"For Example: 6"
		};
    
    public WMAConfigPanel()
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
            File file = new File("WMATrustModelConfiguration.ini");
            int i = 0;
            while(file.exists())
            {
                file = new File("WMATrustModelConfiguration" + i + ".ini");
                i++;
            }
            
            PrintWriter output = new PrintWriter(file);
            output.print("Neighbour Limit=");
            output.println(this.textfield[0].getText());
            output.print("Depth Limit=");
            output.println(this.textfield[1].getText());
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
			Parameter.setDepthLimit(Integer.parseInt(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2)));
		else 
			Parameter.setDepthLimit(Integer.parseInt(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setNeighbourLimit(Integer.parseInt(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2)));
		else 
			Parameter.setNeighbourLimit(Integer.parseInt(textfield[1].getText()));
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