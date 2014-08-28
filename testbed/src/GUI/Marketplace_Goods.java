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

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class Marketplace_Goods extends JPanel implements FocusListener, ActionListener {
	SpringLayout	layout		= new SpringLayout();

	String[]		labels		= {"Number Of Product:", "Maximum Price:",
			"Minimum Price:", "Transaction Limit per day:", "Product Buy Limit:"};
	
	final String[] DEFAULT =
						{
							"For Example: 100",
							"For Example: 100",
							"For Example: 1",
							"For Example: 15",
							"For Example: 5",
						};
	JLabel[]		label		= new JLabel[5];
	JTextField[]	textfield	= new JTextField[5];


	public Marketplace_Goods() {
		this.setLayout(layout);

		for (int i = 0; i < label.length; i++) {
			label[i] = new JLabel(labels[i]);
			textfield[i] = new JTextField(20);
			this.add(label[i]);
			this.add(textfield[i]);
			textfield[i].addFocusListener(this);
		}

		setTextField();
		
		//browse.addActionListener(this);
		//this.add(browse);

		layout.putConstraint(SpringLayout.WEST, label[0], 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, label[0], 5, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, textfield[0], 120, SpringLayout.EAST, label[0]);
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
			
			if (i == label.length - 1)
			{
				layout.putConstraint(SpringLayout.EAST, textfield[i], 300, SpringLayout.EAST, label[i]);
				
			}
			else
			{
				layout.putConstraint(SpringLayout.EAST, textfield[i], 0, SpringLayout.EAST,
						textfield[0]);
			}
		}
		layout.putConstraint(SpringLayout.SOUTH, this, 10, SpringLayout.SOUTH, textfield[textfield.length - 1]);
		this.setVisible(true);
	}
	
	public void saveParam() {
		if (textfield[0].getText().equals(DEFAULT[0]) || textfield[0].getText() == null)
			Parameter.setProduct((Integer.parseInt(DEFAULT[0].substring(DEFAULT[0].indexOf(":")+2))));
		else 
			Parameter.setProduct(Integer.parseInt(textfield[0].getText()));
		if (textfield[1].getText().equals(DEFAULT[1]) || textfield[1].getText() == null)
			Parameter.setMax_price((Double.parseDouble(DEFAULT[1].substring(DEFAULT[1].indexOf(":")+2))));
		else 
			Parameter.setMax_price(Double.parseDouble(textfield[1].getText()));
		if (textfield[2].getText().equals(DEFAULT[2]) || textfield[2].getText() == null)
			Parameter.setMin_price((Double.parseDouble(DEFAULT[2].substring(DEFAULT[2].indexOf(":")+2))));
		else 
			Parameter.setMin_price(Double.parseDouble(textfield[2].getText()));
		if (textfield[3].getText().equals(DEFAULT[3]) || textfield[3].getText() == null)
			Parameter.setTransLimit((Integer.parseInt(DEFAULT[3].substring(DEFAULT[3].indexOf(":")+2))));
		else 
			Parameter.setTransLimit(Integer.parseInt(textfield[3].getText()));
		if (textfield[4].getText().equals(DEFAULT[4]) || textfield[4].getText() == null)
			Parameter.setProductBuyLimit((Integer.parseInt(DEFAULT[4].substring(DEFAULT[4].indexOf(":")+2))));
		else 
			Parameter.setProductBuyLimit(Integer.parseInt(textfield[4].getText()));
			
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
			File file = new File(fileS + "/ProductConfiguration.ini");
			PrintWriter output = new PrintWriter(file);
			output.print("numProducts=");
			output.println(this.textfield[0].getText());
			output.print("maxPrice=");
			output.println(this.textfield[1].getText());
			output.print("minPrice=");
			output.println(this.textfield[2].getText());
			output.print("TransLimit=");
			output.println(this.textfield[3].getText());
			output.print("prodBuyLimit=");
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
			textfield[0].setText(Integer.toString(Parameter.product));
			textfield[0].setToolTipText(Integer.toString(Parameter.product));
			textfield[1].setForeground(color);
			textfield[1].setText(Double.toString(Parameter.max_price));
			textfield[1].setToolTipText(Double.toString(Parameter.max_price));
			textfield[2].setForeground(color);
			textfield[2].setText(Double.toString(Parameter.min_price));
			textfield[2].setToolTipText(Double.toString(Parameter.min_price));
			textfield[3].setForeground(color);
			textfield[3].setText(Integer.toString(Parameter.transaction_limit));
			textfield[3].setToolTipText(Integer.toString(Parameter.transaction_limit));
			textfield[4].setForeground(color);
			textfield[4].setText(Integer.toString(Parameter.product_buy_limit));
			textfield[4].setToolTipText(Integer.toString(Parameter.product_buy_limit));
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String[] filename = initFileOpenChooser();
		textfield[8].setForeground(Color.black);
		textfield[8].setText(filename[0]);
	}
	
	public String[] initFileOpenChooser()
	{
		String[] filenameCombo = new String[2];
		
		try
		{
			JFileChooser fc = new JFileChooser();
			int value = fc.showOpenDialog(this);
			if (value == fc.APPROVE_OPTION)
			{
				filenameCombo[0] = fc.getSelectedFile().getAbsolutePath();
				filenameCombo[1] = fc.getSelectedFile().getName();
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		return filenameCombo;
	}
}