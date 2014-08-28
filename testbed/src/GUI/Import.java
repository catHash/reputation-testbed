package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class Import extends JFrame implements ActionListener, FocusListener {
	Border blackline = BorderFactory.createLineBorder(Color.BLACK);
	JLabel[] labelArray;
	JTextField[] textFieldArray;
	JTextField[] defaultValueArray;

	//mainContentPane
	SpringLayout layout = new SpringLayout();
	JPanel mainContentPane;
	JLabel modelSelectionLabel;
	JComboBox modelSelectionBox;
	String[] listOfChoices = new String[]{"Attack Model", "Defence Model"};
	JLabel importFile;
	JTextField importFileDirectory;
	JButton importButton;
	JLabel noOfAddParamLabel;
	JTextField addParamTextField;	
	JButton check;
	String text ="";
	final String addParamText = "Choose from 1-5";
	final String parameterName = "Parameter Name";
	final String defaultName = "Default Value";

	//paramFieldsPanel
	SpringLayout layout2 = new SpringLayout();
	JPanel paramFieldsPanel = new JPanel(layout2);

	//bottomPanel
	JPanel bottomPanel = new JPanel(new FlowLayout());	
	JButton okButton;
	JButton cancelButton;

	public Import() {
		this.setLayout(new BorderLayout());
		mainContentPane = new JPanel(layout);
		mainContentPane.setPreferredSize(new Dimension(400, 130));
		initDisplay();
	}

	public void initialise() {
		this.add(mainContentPane,BorderLayout.NORTH);
		this.add(paramFieldsPanel,BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.setResizable(true); 
		this.setSize(550,200);
		this.setLocation(200, 100);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	public void initialise(int width, int height) {
		this.add(mainContentPane,BorderLayout.NORTH);
		this.add(paramFieldsPanel,BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.setResizable(false); 
		this.setSize(width,height);
		this.setLocation(200, 100);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	public void initDisplay() {
		TitledBorder mainTitle = BorderFactory.createTitledBorder(blackline, "Import New Feature");		
		modelSelectionLabel = new JLabel("Import Type:");
		modelSelectionBox = new JComboBox(listOfChoices);
		modelSelectionBox.addActionListener(this);
		mainContentPane.setBorder(mainTitle);
		mainContentPane.add(modelSelectionLabel);
		mainContentPane.add(modelSelectionBox);

		importFile = new JLabel("Import file:");
		importFileDirectory = new JTextField(10);
		importFileDirectory.setPreferredSize(new Dimension(10, 22));
		importButton = new JButton("Import");
		importButton.addActionListener(this);
		importButton.setPreferredSize(new Dimension(80, 22));
		mainContentPane.add(importFile);
		mainContentPane.add(importFileDirectory);
		mainContentPane.add(importButton);

		noOfAddParamLabel = new JLabel("No. of additional parameters:");
		addParamTextField = new JTextField(addParamText, 10);
		addParamTextField.setForeground(Color.GRAY);		
		addParamTextField.addFocusListener(this);
		addParamTextField.setPreferredSize(new Dimension(10, 22));
		noOfAddParamLabel.setVisible(false);
		addParamTextField.setVisible(false);
		mainContentPane.add(noOfAddParamLabel);
		mainContentPane.add(addParamTextField);

		check = new JButton("Check");
		check.addActionListener(this);
		check.setPreferredSize(new Dimension(80, 22));
		check.setVisible(false);
		mainContentPane.add(check);

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);

		//Components Alignment
		layout.putConstraint(SpringLayout.WEST, modelSelectionLabel, 50, SpringLayout.WEST, mainContentPane);
		layout.putConstraint(SpringLayout.WEST, modelSelectionBox, 200, SpringLayout.WEST, modelSelectionLabel);

		layout.putConstraint(SpringLayout.NORTH, importFile, 40, SpringLayout.NORTH, mainContentPane);
		layout.putConstraint(SpringLayout.NORTH, importFileDirectory, 0, SpringLayout.NORTH, importFile);
		layout.putConstraint(SpringLayout.NORTH, importButton, 0, SpringLayout.NORTH, importFileDirectory);

		layout.putConstraint(SpringLayout.WEST, importFile, 50, SpringLayout.WEST, mainContentPane);
		layout.putConstraint(SpringLayout.WEST, importFileDirectory, 200, SpringLayout.WEST, importFile);
		layout.putConstraint(SpringLayout.WEST, importButton, 130, SpringLayout.WEST, importFileDirectory);

		layout.putConstraint(SpringLayout.NORTH, noOfAddParamLabel, 80, SpringLayout.NORTH, mainContentPane);
		layout.putConstraint(SpringLayout.NORTH, addParamTextField, 0, SpringLayout.NORTH, noOfAddParamLabel);
		layout.putConstraint(SpringLayout.NORTH, check, 0, SpringLayout.NORTH, addParamTextField);

		layout.putConstraint(SpringLayout.WEST, noOfAddParamLabel, 50, SpringLayout.WEST, mainContentPane);
		layout.putConstraint(SpringLayout.WEST, addParamTextField, 200, SpringLayout.WEST, noOfAddParamLabel);
		layout.putConstraint(SpringLayout.WEST, check, 130, SpringLayout.WEST, addParamTextField);
	}

	private void generateTextFields(int noOfTextFields) {	
		paramFieldsPanel.removeAll();

		if (noOfTextFields > 0 && noOfTextFields < 6) {
			JLabel paramLabels;
			JTextField paramFields;
			JTextField defaultValueField;
			labelArray = new JLabel[noOfTextFields];
			textFieldArray = new JTextField[noOfTextFields];
			defaultValueArray = new JTextField[noOfTextFields];
			TitledBorder subTitle = BorderFactory.createTitledBorder(blackline, "Additional Parameters");
			paramFieldsPanel.setBorder(subTitle);			

			for (int i = 0; i < noOfTextFields; i++) {
				paramLabels = new JLabel("Parameter " +(i+1)+": ");
				paramFields = new JTextField(parameterName, 10);
				defaultValueField = new JTextField(defaultName, 10);
				labelArray[i] = paramLabels;
				textFieldArray[i] = paramFields;
				defaultValueArray[i] = defaultValueField;
				textFieldArray[i].addFocusListener(this);
				defaultValueArray[i].addFocusListener(this);
				textFieldArray[i].setForeground(Color.GRAY);
				defaultValueArray[i].setForeground(Color.GRAY);
				paramFieldsPanel.add(labelArray[i]);
				paramFieldsPanel.add(textFieldArray[i]);
				paramFieldsPanel.add(defaultValueArray[i]);

				//Components Alignment
				layout2.putConstraint(SpringLayout.WEST, labelArray[i], 50, SpringLayout.WEST, paramFieldsPanel);
				layout2.putConstraint(SpringLayout.WEST, textFieldArray[i], 150, SpringLayout.WEST, labelArray[i]);
				layout2.putConstraint(SpringLayout.WEST, defaultValueArray[i], 150, SpringLayout.WEST, textFieldArray[i]);
				if (i == 0)
				{
					layout2.putConstraint(SpringLayout.NORTH, labelArray[i], 20, SpringLayout.NORTH, paramFieldsPanel);
					layout2.putConstraint(SpringLayout.NORTH, textFieldArray[i], 20, SpringLayout.NORTH, paramFieldsPanel);
					layout2.putConstraint(SpringLayout.NORTH, defaultValueArray[i], 20, SpringLayout.NORTH, paramFieldsPanel);
				} else {
					layout2.putConstraint(SpringLayout.NORTH, labelArray[i], 50*(i)+20, SpringLayout.NORTH, paramFieldsPanel);
					layout2.putConstraint(SpringLayout.NORTH, textFieldArray[i], 50*(i)+20, SpringLayout.NORTH, paramFieldsPanel);
					layout2.putConstraint(SpringLayout.NORTH, defaultValueArray[i], 50*(i)+20, SpringLayout.NORTH, paramFieldsPanel);
				}
			}
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(addParamTextField)) {
			if (addParamTextField.getText().equals(addParamText)) {
				addParamTextField.setText("");
				addParamTextField.setForeground(Color.BLACK);
			}
		}
		else {
			for (int i = 0; i < textFieldArray.length; i++) {
				if (e.getSource().equals(textFieldArray[i])) {
					if (textFieldArray[i].getText().equals(parameterName)) {
						textFieldArray[i].setText("");
						textFieldArray[i].setForeground(Color.BLACK);
					}
				}
				if (e.getSource().equals(defaultValueArray[i])) {
					if (defaultValueArray[i].getText().equals(defaultName)) {
						defaultValueArray[i].setText("");
						defaultValueArray[i].setForeground(Color.BLACK);
					}
				}
			}
		}		
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(addParamTextField)) {
			if (addParamTextField.getText().isEmpty()) {
				addParamTextField.setText(addParamText);
				addParamTextField.setForeground(Color.GRAY);
			}
		}	
		else {
			for (int i = 0; i < textFieldArray.length; i++) {
				if (e.getSource().equals(textFieldArray[i])) {
					if (textFieldArray[i].getText().isEmpty()) {
						textFieldArray[i].setText(parameterName);
						textFieldArray[i].setForeground(Color.GRAY);
					}
				}
				if (e.getSource().equals(defaultValueArray[i])) {
					if (defaultValueArray[i].getText().isEmpty()) {
						defaultValueArray[i].setText(defaultName);
						defaultValueArray[i].setForeground(Color.GRAY);
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(importButton)) {
			String configFileName = selectFile();
			if (configFileName.length() > 0) {
				if (configFileName.substring(configFileName.lastIndexOf(".") + 1).equals("class")) {
					importFileDirectory.setText(configFileName);	
				}
				else {
					JOptionPane.showMessageDialog(this, "Please make sure that a .class file is imported!", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}			
		}
		else if (e.getSource().equals(check)) {
			int noOfTextFields = 0;
			try {
				if (addParamTextField.getText().isEmpty() || addParamTextField.getText() == null || addParamTextField.getText().equalsIgnoreCase("0")){
					noOfTextFields = 0;
				}
				else
					noOfTextFields = Integer.parseInt(addParamTextField.getText());
			}
			catch(NumberFormatException err) {
				JOptionPane.showMessageDialog(this, "Please enter a numerical value from 0-5, or leave blank with no additional parameter is needed.", 
						"Please Enter Correct Value", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception err) {
				err.printStackTrace();
			}
			if (noOfTextFields < 0 || noOfTextFields > 5)
			{
				JOptionPane.showMessageDialog(this, "You may only enter a minimum of 0 or a maximum of 5 parameters", 
						"Please only enter from 0 - 5", JOptionPane.INFORMATION_MESSAGE);
			} else {
				generateTextFields(noOfTextFields);
				this.initialise(550, 220+(noOfTextFields*55));
			}
		}
		else if (e.getSource().equals(okButton)) {
			//NewFeature newFeature = new NewFeature(modelSelectionBox.getSelectedItem().toString(), labelArray, textFieldArray);
			
			if(importFileDirectory.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "Import file path cannot be empty!");
				this.initialise();
				importFileDirectory.setText("");
				addParamTextField.setText(addParamText);
				addParamTextField.setForeground(Color.GRAY);
			}
			else{
			boolean flagA = false;
			try {
				flagA = fileCopy(modelSelectionBox.getSelectedItem().toString(), importFileDirectory.getText());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			boolean flagB = false;;
			try {
				if (flagA)
					flagB = writeFile(importFileDirectory.getText());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (flagA && flagB){
				JOptionPane.showMessageDialog(null, "File has been imported successfully");

				this.dispose();

			}
			else if (!flagA){
				JOptionPane.showMessageDialog(null, "File already exists!");
				this.initialise();
				importFileDirectory.setText("");
				addParamTextField.setText(addParamText);
				addParamTextField.setForeground(Color.GRAY);
			}
			else if (!flagB){
				this.initialise();
				addParamTextField.setText(addParamText);
				addParamTextField.setForeground(Color.GRAY);
			}
			}
		}
		else if (e.getSource().equals(cancelButton)) {
			this.dispose();
		}
		else if (e.getSource().equals(modelSelectionBox)){
			if (modelSelectionBox.getSelectedItem().toString().equalsIgnoreCase("Attack Model")){
				noOfAddParamLabel.setVisible(false);
				addParamTextField.setVisible(false);
				check.setVisible(false);
				paramFieldsPanel.setVisible(false);
				
				this.initialise();
				importFileDirectory.setText("");
			} else {
				noOfAddParamLabel.setVisible(true);
				addParamTextField.setVisible(true);
				addParamTextField.setText(addParamText);
				addParamTextField.setForeground(Color.GRAY);
				check.setVisible(true);
				importFileDirectory.setText("");
			}
		}
	}

	// Open file chooser dialog and return the filename chosen
	public String selectFile() {
		String filename = "";
		JFileChooser fc = new JFileChooser();
		int value = fc.showOpenDialog(this);
		if (value == JFileChooser.APPROVE_OPTION)
			filename = fc.getSelectedFile().getAbsolutePath();
		return filename;
	}

	//FileCopy to destination
	public boolean fileCopy(String importType, String srcFileName) throws IOException {
		File srcFile = new File(srcFileName);
		String[] strArray1 = importType.split(" ");
		String[] strArray2 = srcFileName.split("\\\\");
		String desFileName = "";



		text = strArray2[strArray2.length-1];
		String[] test = text.split("\\\\");
		String[] temp = test[0].split(".class");
		String newModelName = temp[0];
		
		boolean check = false;
		if (strArray1[0].equalsIgnoreCase("attack")){
			for(int i=0; i<MainGUI.ALS.listModel.size(); i++){
				if(MainGUI.ALS.listModel.get(i).toString().equalsIgnoreCase(newModelName)){
					check = true;
					break;
				}
			}

		}
		else {
			for(int i=0; i<MainGUI.LSD.listModel.size(); i++){
				if(MainGUI.LSD.listModel.get(i).toString().equalsIgnoreCase(newModelName)){
					check = true;
					break;
				}
			}
		}

		if(check==false){



			if (strArray1[0].equalsIgnoreCase("attack")) {
				File temporaryFile = null;
				temporaryFile = new File("");

				desFileName = temporaryFile.getAbsolutePath()+"\\bin\\attacks\\" + strArray2[strArray2.length-1];
			}
			else {
				File temporaryFile = null;
				temporaryFile = new File("");

				desFileName = temporaryFile.getAbsolutePath()+"\\bin\\defenses\\" + strArray2[strArray2.length-1];
			}

			try {
				if (!srcFile.exists()) {
					srcFile.createNewFile();
				}



				InputStream in = new FileInputStream(srcFile);
				OutputStream out = new FileOutputStream(new File(desFileName));

				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.close();
				System.out.println("File has been copied.");
				return true;
			}
			catch (FileNotFoundException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "File is not found!" + desFileName, 
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		else
			return false;
	}

	//Write to txt file
	public boolean writeFile(String srcFileName) throws IOException {
		String[] strArray1 = srcFileName.split("\\\\");

		String[] strArray2 = strArray1[strArray1.length-1].split("\\.");

		if (modelSelectionBox.getSelectedItem().toString().equalsIgnoreCase("Attack Model"))
		{
			boolean check = updateModelFiles(modelSelectionBox.getSelectedItem().toString(), strArray2[0]);
			return check;
		}

		String fileName = "SavedConfiguration/newModels";
		try {			
			File directory = new File(fileName);
			if (!directory.isDirectory()){
				boolean yea = directory.mkdirs();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName + "/" + strArray2[0] + ".txt"));
			bw.write("Model Type = " + modelSelectionBox.getSelectedItem().toString());
			bw.write("\r\n");
			if (addParamTextField.getText().equals(addParamText) || addParamTextField.getText().equals("0")) {
				bw.write("No. of additional parameters = 0");
			}
			else {				
				bw.write("No. of additional parameters = " + addParamTextField.getText());
				bw.write("\r\n");
				bw.write("\r\n");
				bw.write("Parameter Name               Parameter Value");
				bw.write("\r\n");
				for (int i = 0; i < textFieldArray.length; i++) {
					if (textFieldArray[i].getText().equals(parameterName) || textFieldArray[i].getText().equals("")) {
						throw new ParameterNameMissingException();
					}
					else {
						bw.write(textFieldArray[i].getText()+"\t");
					}	

					for (int j = 0; j < 29 - textFieldArray[i].getText().length(); j++) {
						bw.write(" ");
					}				

					if (defaultValueArray[i].getText().equals(defaultName) || defaultValueArray[i].getText().equals("")) {
						bw.write("");
					}
					else {
						bw.write(defaultValueArray[i].getText());
					}					
					bw.write("\r\n");
				}
			}						
			System.out.println("Class file has been written to the bin directory");

			bw.close();

		}
		catch (NullPointerException ex) {
			JOptionPane.showMessageDialog(this, "Please ensure that the fields are filled up correctly " +
					"and click on Check button to add additional parameters", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (ParameterNameMissingException ex) {
			JOptionPane.showMessageDialog(this, "All Parameter Names must be filled up!", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "File is not found!", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		boolean check = updateModelFiles(modelSelectionBox.getSelectedItem().toString(), strArray2[0]);

		return check;
	}

	private boolean updateModelFiles(String modelType, String modelName) throws IOException {
		// TODO Auto-generated method stub
		boolean check = false;
		if (modelType.equalsIgnoreCase("Attack Model")){
			for(int i=0; i<MainGUI.ALS.listModel.size(); i++){
				if(MainGUI.ALS.listModel.get(i).toString().equalsIgnoreCase(modelName)){
					check = true;
					break;
				}
			}
			if(check == false)
				MainGUI.ALS.listModel.addElement(modelName);
		}
		else {
			for(int i=0; i<MainGUI.LSD.listModel.size(); i++){
				if(MainGUI.LSD.listModel.get(i).toString().equalsIgnoreCase(modelName)){
					check = true;
					break;
				}
			}
			if(check == false)

				MainGUI.LSD.listModel.addElement(modelName);
		}

		if(check == false){
			File outputFile, tempFile = new File("SavedConfiguration/temp.txt");
			Boolean flag = false;
			String atkURL = "SavedConfiguration/atkModels.txt";
			String defURL = "SavedConfiguration/defModels.txt";
			if (modelType.equalsIgnoreCase("Attack Model")){
				outputFile =  new File(atkURL);
			}
			else {
				outputFile =  new File(defURL);
			}

			//make a temp copy if the model file already exists with existing model names
			if (outputFile.exists()){
				BufferedReader br = new BufferedReader(new FileReader(outputFile));
				PrintWriter pw = new PrintWriter(tempFile);
				String line = br.readLine();
				while (!line.equalsIgnoreCase("..")){
					pw.write(line);
					pw.write("\r\n");
					line = br.readLine();
				}
				pw.write("..");
				br.close();
				pw.close();

			}
			else {
				File directory = new File("SavedConfiguration");
				if (!directory.isDirectory()){
					boolean yea = directory.mkdirs();
				}
				PrintWriter pw = new PrintWriter(tempFile);
				if (modelType.equalsIgnoreCase("Attack Model"))
					pw.write("Sybil\r\nAlwaysUnfair\r\nCamouflage\r\nWhitewashing\r\nSybil_Camouflage\r\nSybil_Whitewashing\r\n..");
				else
					pw.write("MeTrustedGraph\r\nReece\r\nBRS\r\neBay\r\nTRAVOS\r\nIClub\r\nPersonalized\r\nProbCog\r\nWMA\r\n..");
				pw.close();
			}

			//writing of the new atkModels or defModels file
			PrintWriter pw = new PrintWriter(outputFile);
			BufferedReader br = new BufferedReader(new FileReader(tempFile));
			String line = br.readLine();
			while (!line.equalsIgnoreCase("..")){
				pw.write(line);
				pw.write("\r\n");
				line = br.readLine();
			}
			br.close();
			pw.write(modelName);
			pw.write("\r\n");
			pw.write("..");
			pw.close();
			tempFile.delete();

			return true;

		}
		else return false;
	}


}



class ParameterNameMissingException extends Exception {
	public ParameterNameMissingException() {
		super();
	}
}
