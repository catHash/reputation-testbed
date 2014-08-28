package GUI;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import main.CentralAuthority;
import main.Parameter;

public class MainGUI extends JPanel {

	public static ArrayList<String> selectedAttack = null;
	public static ArrayList<String> selectedDetect = null;
	public static ArrayList<String> selectedEvaluate = null;
	public static Simulated_Env se = null;
	public static Simulated_Env re = null;
	public static JPanel  attackPanel;

	JLabel label;
	public static JFrame frame;
	String simpleDialogDesc = "Please select option";
	public static DetectionListSelection LSD;
	public static AttackListSelection ALS;
	public static EvaluationListSelection ELS;
	public static JPanel frequentPanel;
	JOptionPane pane = new JOptionPane();
	Reset reset = new Reset();

	/** Creates the GUI shown inside the frame's content pane. */
	public MainGUI(JFrame frame) {
		super(new BorderLayout());
		this.frame = frame;

		// Create the components.
		JPanel frequentPanel = createSimpleDialogBox();

		Border padding = BorderFactory.createEmptyBorder(0, 20, 0, 20);
		frequentPanel.setBorder(padding);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("MainGUI", null, frequentPanel, simpleDialogDesc); // tooltip

		add(tabbedPane, BorderLayout.CENTER);

	}

	public DetectionListSelection getLSD() {
		return LSD;
	}


	public void setLSD(DetectionListSelection lSD) {
		LSD = lSD;
	}


	public AttackListSelection getALS() {
		return ALS;
	}


	public void setALS(AttackListSelection aLS) {
		ALS = aLS;
	}


	public EvaluationListSelection getELS() {
		return ELS;
	}


	public void setELS(EvaluationListSelection eLS) {
		ELS = eLS;
	}
	/** Creates the panel shown by the first tab. */
	private JPanel createSimpleDialogBox() {

		final JButton runBtn;
		final JButton importBtn;
		final JButton resetBtn;

		final String BTN_IMPORT = "Import";
		final String BTN_RUN = "Run";
		final String BTN_RESET = "Reset";

		JPanel envPanel = new JPanel(new FlowLayout());
		attackPanel = new JPanel();
		JPanel defensePanel = new JPanel();
		JPanel matrixPanel = new JPanel(new BorderLayout());
		JPanel runPanel = new JPanel();
		JSplitPane splitPane;

		JLabel envLabel = new JLabel("Select the desired environment: ");
		final JList envList; 
		DefaultListModel listModel;
		listModel = new DefaultListModel();
		listModel.addElement("Simulated Environment");
		listModel.addElement("Real Environment");
		envList = new JList(listModel);
		envList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JButton envConfBtn = new JButton("Config");
		envConfBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					if (envList.getSelectedValue().toString().equalsIgnoreCase("Simulated Environment")){
						if(Parameter.ENV_IS_REAL) Parameter.FIRST_CONFIG_SE = true;
						Parameter.ENV_IS_REAL = false;
						se = new Simulated_Env("Simulate Environment",false);			
					}
					else if (envList.getSelectedValue().toString().equalsIgnoreCase("Real Environment")){
						if(!Parameter.ENV_IS_REAL) Parameter.FIRST_CONFIG_RE = true;
						Parameter.ENV_IS_REAL = true;

						re = new Simulated_Env("Real Environment",true);
					}
				}catch (NullPointerException ex)
				{	
					JOptionPane.showMessageDialog(null, "Please Config the environment.");
				}
			}
		});


		JScrollPane listPane = new JScrollPane(envList);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		envPanel.add(splitPane, BorderLayout.CENTER);
		JPanel topHalf = new JPanel();
		topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));
		JPanel listContainer = new JPanel(new GridLayout(1,1));
		listContainer.setBorder(BorderFactory.createTitledBorder(
				"Select the desired environment: "));
		listContainer.add(listPane);

		topHalf.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		topHalf.setPreferredSize(new Dimension(500, 70));
		topHalf.add(listContainer);	
		topHalf.add(envConfBtn);	
		splitPane.add(topHalf);
		runBtn = new JButton(BTN_RUN);
		importBtn = new JButton(BTN_IMPORT);
		resetBtn = new JButton(BTN_RESET);

		runBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				//make getlist return arraylist
				selectedDetect = (ArrayList<String>) LSD.getList();
				selectedAttack = (ArrayList<String>) ALS.getList();
				selectedEvaluate = (ArrayList<String>) ELS.getList();

				Parameter.TOTAL_NO_OF_BUYERS = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
				Parameter.TOTAL_NO_OF_SELLERS = Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS;
				Parameter.changeTargetValues();
				Parameter.updateValues();
				Parameter.atkNameList = selectedAttack;
				Parameter.defNameList = selectedDetect;
				Parameter.evaNameList = selectedEvaluate;

				try {

					if (Parameter.ENV_EMPTY) {
						JOptionPane.showMessageDialog(null, "There is no input for environment paremeters.\nYou may continue the simulation with default environment parameter values by pressing \"Run\" again, or go to \"Environment\" to input your parameters.");
						Parameter.ENV_EMPTY = false;
					} else if (Parameter.ATK_EMPTY)
					{ JOptionPane.showMessageDialog(null, "Please choose an attack model from \"Attack Model\"");
					} else if (Parameter.DEF_EMPTY)
					{
						JOptionPane.showMessageDialog(null, "Please choose a detecion model from \"Detection\"");
					} else if (Parameter.EVA_EMPTY)
					{
						JOptionPane.showMessageDialog(null, "Please choose an evaluation metric from \"Evaluation Metric\"");
					} else {
						if (se != null){
							System.out.println("Simulated Env");
							Thread t = new Thread() {
								@Override
								public void run() {  // override the run() to specify the running behavior
									CentralAuthority ca = new CentralAuthority();
									try {
										ca.evaluateDefenses(selectedDetect, selectedAttack, selectedEvaluate, null);
									} catch (ClassNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (NoSuchMethodException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (SecurityException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							};
							t.start();
						}
						
						else if (re !=null){
							System.out.println("Real Env");
							boolean check = true;	

							for(int i=0; i<selectedEvaluate.size(); i++){
								String s = selectedEvaluate.get(i).toString();
								if(s.equalsIgnoreCase("Robustness ([-1,1])") || s.equalsIgnoreCase("MAE-DS repDiff(Reputation difference of dishonest seller ([0, 1])") || s.equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])") || s.equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
									JOptionPane.showMessageDialog(null, "There are no dishonest sellers in real environment!");
									check = false;
									break;
								}
							}
							if(check == false) check = true;
							else check = false;

							if(check==false){
								Thread t = new Thread() {
									@Override
									public void run() {  // override the run() to specify the running behavior
										CentralAuthority ca = new CentralAuthority();
										try {
											ca.evaluateDefenses(selectedDetect, selectedAttack, selectedEvaluate, re.getRealFileName());
										} catch (ClassNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (NoSuchMethodException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (SecurityException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								};
								t.start();
							}
						}
					}
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				//Display display = new Display("Display");

			}
		});

		importBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Import importfunc = new Import();

				importfunc.initialise();
			}
		});

		resetBtn.addActionListener(new ActionListener() {
			// reset the configurations
			public void actionPerformed(ActionEvent e) {
				selectedAttack = null;
				selectedDetect = null;
				selectedEvaluate = null;
				Parameter.ATK_EMPTY=true;
				Parameter.DEF_EMPTY=true;
				Parameter.ENV_EMPTY=true;
				Parameter.EVA_EMPTY=true;
				Parameter.FIRST_CONFIG_RE=true;
				Parameter.FIRST_CONFIG_SE=true;
				ELS.initialise();
				LSD.initialise();
				ALS.initialise();
				//reset.initialise();
				envList.clearSelection();
				//createAndShowGUI();
			
				se = null;
				re = null;
			}
		});

		runPanel.add(runBtn);
		runPanel.add(importBtn);
		runPanel.add(resetBtn);

		try {
			ALS = new AttackListSelection();
			ELS = new EvaluationListSelection();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		//JPanel attP = ALS.contentPane;
		attackPanel = ALS.contentPane;   	
		matrixPanel = ELS.contentPane;
		//ELS = new EvaluationListSelection();
		LSD = new DetectionListSelection();	
		defensePanel = LSD.contentPane;

		JPanel adPane = new JPanel(new FlowLayout());
		adPane.add(attackPanel);
		adPane.add(defensePanel);
		adPane.add(matrixPanel);

		JPanel topPane = new JPanel(new BorderLayout());
		topPane.add(envPanel, BorderLayout.NORTH);
		topPane.add(adPane, BorderLayout.SOUTH);

		JPanel bottomPane = new JPanel(new BorderLayout());		

		bottomPane.add(matrixPanel, BorderLayout.NORTH);
		bottomPane.add(runPanel, BorderLayout.SOUTH);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(topPane, BorderLayout.PAGE_START);
		contentPane.add(bottomPane, BorderLayout.PAGE_END);

		return contentPane;
	}

	/* Open file chooser dialog and return the filename chosen */
	public String selectFile() {
		String filename = "";
		JFileChooser fc = new JFileChooser();
		int value = fc.showOpenDialog(this);
		if (value == JFileChooser.APPROVE_OPTION)
			filename = fc.getSelectedFile().getAbsolutePath();
		return filename;

	}


	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TestBed Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		MainGUI newContentPane = new MainGUI(frame);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}