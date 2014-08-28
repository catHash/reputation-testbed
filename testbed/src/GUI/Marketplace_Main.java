package GUI;

import main.Parameter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Marketplace_Main extends JPanel implements ActionListener, WindowListener {
	SpringLayout layout	= new SpringLayout();
	Marketplace_Simulation	simConfig;
	JButton save_Config;
	JButton import_Config;
	JButton reset;
	JButton ok = new JButton("OK");
	JButton cancel = new JButton("Cancel");

	//real environment
	String name = "";
	String importFileDirectory = "";
	JButton import_file;
	JTextField	textfield;	

	public String getRealName(){
		return this.name;
	}


	Simulated_Env main;
	//Marketplace_Controls marketControls;

	public Marketplace_Main(Simulated_Env main) {
		this.main = main;
		this.setLayout(layout);
		Border blackline = BorderFactory.createLineBorder(Color.BLACK);

		simConfig = new Marketplace_Simulation(this);
		TitledBorder simTitle = BorderFactory
				.createTitledBorder(blackline, "Marketplace Configuration");
		simConfig.setBorder(simTitle);

		save_Config = new JButton("Save Configuration");
		save_Config.addActionListener(this);
		import_Config = new JButton("Import Configuration");
		import_Config.addActionListener(this);
		reset = new JButton("Reset");
		reset.addActionListener(this);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		import_file = new JButton("Import Real File");
		import_file.addActionListener(this);
		SpringLayout.Constraints okCst =
				layout.getConstraints(import_file);
		okCst.setX(Spring.constant(457));
		okCst.setY(Spring.constant(5));
		this.add(import_file);
		textfield	= new JTextField(33);
		if(this.main.isReal){
			if(Parameter.FIRST_CONFIG_RE){
				textfield.setText("");
			}else{
				textfield.setForeground(Color.black);
				textfield.setText(Parameter.IMPORT_REAL_FILE);

			}
		}
		
		okCst =
				layout.getConstraints(textfield);
		okCst.setX(Spring.constant(20));
		okCst.setY(Spring.constant(5));
		this.add(textfield);


		this.add(simConfig);
		this.add(save_Config);
		this.add(import_Config);
		this.add(reset);

		this.add(ok);
		this.add(cancel);

		import_file.setVisible(false);
		textfield.setVisible(false);

		layout.putConstraint(SpringLayout.WEST, simConfig, 10, SpringLayout.WEST, this);
		if(this.main.isReal == true)
			layout.putConstraint(SpringLayout.NORTH, simConfig, 35, SpringLayout.NORTH, this);
		else
			layout.putConstraint(SpringLayout.NORTH, simConfig, 10, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.EAST, simConfig, -10, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.WEST, save_Config, 10, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, save_Config, 0, SpringLayout.SOUTH, simConfig);

		layout.putConstraint(SpringLayout.WEST, import_Config, 10, SpringLayout.EAST, save_Config);
		layout.putConstraint(SpringLayout.NORTH, import_Config, 0, SpringLayout.NORTH, save_Config);

		layout.putConstraint(SpringLayout.WEST, reset, 10, SpringLayout.EAST, import_Config);
		layout.putConstraint(SpringLayout.NORTH, reset, 0, SpringLayout.NORTH, import_Config);


		layout.putConstraint(SpringLayout.WEST, ok, 10, SpringLayout.EAST, reset);
		layout.putConstraint(SpringLayout.NORTH, ok, 0, SpringLayout.NORTH, import_Config);
		layout.putConstraint(SpringLayout.SOUTH, ok, 0, SpringLayout.SOUTH, import_Config);

		layout.putConstraint(SpringLayout.WEST, cancel, 10, SpringLayout.EAST, ok);
		layout.putConstraint(SpringLayout.NORTH, cancel, 0, SpringLayout.NORTH, import_Config);
		layout.putConstraint(SpringLayout.SOUTH, cancel, 0, SpringLayout.SOUTH, import_Config);


		//real environment
		if(this.main.isReal==true){
			import_file.setVisible(true);
			textfield.setVisible(true);
		}

	}





	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save_Config)
		{
			File f = initFileSaveChooser();
			if (f==null){
			}else{	
				String[] filename = new String[2];
				filename[0]= f.getAbsolutePath();
				filename[1]=f.getName();
				String[] check = filename[1].split("\\.", 0);
				String file = check[0];
				String[] key = simConfig.configuration(file); //Store temp file

				if ((check.length == 1) || (!check[check.length - 1].equalsIgnoreCase("dat")))
				{
					filename[0] = filename[0] + ".dat";
					filename[1] = filename[1] + ".dat";
				}
				try
				{
					PrintWriter output = new PrintWriter(filename[0]);
					output.print("simConfig=");
					output.println(key[4]);
					output.print("agentConfig=");
					output.println(key[0]);
					output.print("productConfig=");
					output.println(key[1]);
					output.print("schedConfig=");
					output.println(key[3]);
					output.print("masterConfig=");
					output.println(key[2]);
					output.close();
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}

			}
		}
		else if (e.getSource() == import_Config)
		{
			File f = initFileOpenChooser();
			if (f==null){
			}else{	
				String[] filename = new String[2];
				filename[0]= f.getAbsolutePath();
				filename[1]=f.getName();
				File file = new File(filename[0]);
				try
				{
					String[] check = filename[1].split("\\.", 0);
					if (check[check.length-1].equalsIgnoreCase("dat"))
					{
						Scanner input = new Scanner(file);
						String[] configFile = new String[5];
						String key = null;
						int i = 0;
						while (input.hasNext())
						{
							key = input.nextLine();
							String[] partKey = key.split("=", 0);
							configFile[i] = partKey[1];
							i++;
						}
						simConfig.importConfig(configFile);
					}
					else
					{
						System.out.println("Wrong File Format!!!");
					}
				}
				catch (Exception ex)
				{
					System.out.println("File Not Found!!!");
				}
			}
		}
		else if (e.getSource() == reset)
		{
			simConfig.reset();
			simConfig.agentConfig.reset();
			simConfig.productConfig.reset();
			simConfig.schedConfig.reset();

		}
		else if (e.getSource() == ok) 
		{
			if(Parameter.ENV_IS_REAL){
				this.saveParam();
			}
			simConfig.saveParam();
			simConfig.agentConfig.saveParam();
			simConfig.productConfig.saveParam();
			simConfig.schedConfig.saveParam();
			Parameter.ENV_EMPTY = false;
			if (main.isReal == true)
				Parameter.FIRST_CONFIG_RE = false;
			else
				Parameter.FIRST_CONFIG_SE = false;
			if(main.isReal==true){
				boolean flagA = fileCopy(importFileDirectory, name);

				if (flagA ){
					JOptionPane.showMessageDialog(null, "File has been imported successfully to realdata folder");
				}
			}
			main.dispose();

		}
		else if (e.getSource() == cancel) 
		{
			main.dispose();
		}

		//real environment
		else if (e.getSource().equals(import_file)) {
			importFileDirectory = selectFile();

			if (importFileDirectory.length() > 0) {
				if (importFileDirectory.substring(importFileDirectory.lastIndexOf(".") + 1).equals("txt")) {
				}
				else {
					JOptionPane.showMessageDialog(this, "Please make sure that a .txt file is imported!", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}			
		}


	}


	//Import file method
	public File initFileOpenChooser()
	{

		try
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".dat", "dat");
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);
			int value = fc.showOpenDialog(this);
			if (value == fc.APPROVE_OPTION)
			{
				return fc.getSelectedFile();
			}

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	public void saveParam() {
		if (textfield.getText() == null)
			Parameter.IMPORT_REAL_FILE = null;
		else 
			Parameter.IMPORT_REAL_FILE = textfield.getText();
		
	}

	//Auto save the file in the backend, execute only during run button
	public String autoSave()
	{
		//Configuration file name parameters
		String tempFileName="Default";
		String configurationFileName="SavedConfiguration/";

		//Create temp file
		String[] key = simConfig.configuration(tempFileName); 

		return configurationFileName+tempFileName+"/SimulationConfiguration.ini";	
	}

	//To save file method
	public File initFileSaveChooser()
	{

		try
		{			
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".dat", "dat");
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(filter);

			int value = fc.showSaveDialog(this);
			if (value == fc.APPROVE_OPTION)
			{
				return fc.getSelectedFile();
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	//real environment functions
	// Open file chooser dialog and return the filename chosen
	public String selectFile() {
		String filename = "";
		JFileChooser fc = new JFileChooser();
		int value = fc.showOpenDialog(this);
		if (value == JFileChooser.APPROVE_OPTION){

			filename = fc.getSelectedFile().getAbsolutePath();
			name=fc.getSelectedFile().getName();
			textfield.setText(filename);
		}
		return filename;
	}

	//FileCopy to destination
	public boolean fileCopy(String srcFileName, String filename) {
		srcFileName = textfield.getText();
		String desFileName = "";
		File temporaryFile = null;
		temporaryFile = new File("");

		String[] f = srcFileName.split("\\\\");
		name = f[f.length-1];
		File srcFile;
		if(f[1].equalsIgnoreCase("data"))
			 srcFile = new File(temporaryFile.getAbsolutePath()+srcFileName);
		else 
			srcFile = new File(srcFileName);
		desFileName = temporaryFile.getAbsolutePath()+ "\\data\\realdata\\"+name;
		main.realFileName=desFileName;
		System.out.println(desFileName);

		if(!f[1].equalsIgnoreCase("data")){
		try {
			File experimentDirectory = new File("data\\realdata\\");
			if (!experimentDirectory.exists()) {
				boolean result = experimentDirectory.mkdirs();
				System.out.println("Creating " + desFileName);
			}

			InputStream in = new FileInputStream(srcFile);			
			FileOutputStream out = new FileOutputStream (desFileName);
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
			JOptionPane.showMessageDialog(this,f[0], 
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (IOException ex) {
			ex.printStackTrace();JOptionPane.showMessageDialog(this,"Please import a file!" , 
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
		return true;
	}
}
