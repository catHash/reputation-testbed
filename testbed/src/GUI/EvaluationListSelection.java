package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import main.Parameter;


public class EvaluationListSelection  extends JFrame implements ActionListener {
	JTextArea output;
	JList list; 
	JTable table;
	static List selected;
	String newline = "\n";
	ListSelectionModel listSelectionModel;

	static JFrame frame = new JFrame();
	JSplitPane splitPane; int count=1;
	JPanel contentPane;
	ArrayList<String> data;
	public EvaluationListSelection() {
		data = new ArrayList<String>();
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		try {
			String dirName = "SavedConfiguration/evalModels.txt";
			File directoryName = new File ("SavedConfiguration");
			if (!directoryName.exists() || !directoryName.isDirectory())
			{
				directoryName.mkdirs();
			}
			File defFile = new File(dirName);
			if (!defFile.exists() || !defFile.isFile())
			{
				defFile.createNewFile();
				FileWriter fw = new FileWriter(defFile);
				fw.write("Robustness ([-1,1])\r\nMAE-DS repDiff(Reputation difference of dishonest seller ([0, 1])\r\nMAE-HS repDiff(Reputation difference of honest seller ([0, 1])\r\nMCC-DS (Classification of dishonest seller ([-1,1])\r\nMCC-HS (Classification of honest seller ([-1,1])\r\nFNR-DS (Classification of dishonest seller ([0,1])\r\nFNR-HS (Classification of honest seller ([0,1])\r\nAccuracy-DS (Classification of dishonest seller ([0,1])\r\nAccuracy-HS (Classification of honest seller ([0,1])\r\nFPR-DS (Classification of dishonest seller ([0,1])\r\nFPR-HS (Classification of honest seller ([0,1])\r\nTPR-DS (Classification of dishonest seller ([0,1])\r\nTPR-HS (Classification of honest seller ([0,1])\r\nPrecision-DS (Classification of dishonest seller ([0,1])\r\nPrecision-HS (Classification of honest seller ([0,1])\r\nF-Measure-DS (Classification of dishonest seller ([0,1])\r\nF-Measure-HS (Classification of honest seller ([0,1])\r\n..");
				fw.close();
			}
			BufferedReader br = new BufferedReader(new FileReader(dirName));
			String line = br.readLine();
			while (!line.equalsIgnoreCase("..")){
				data.add(line);
				line = br.readLine();
			}
			br.close();
		}
		catch (IOException ex) {
			ex.getMessage();
		}

		list = new JList(data.toArray());

		list.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if(super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				}
				else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});
/*
		list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
              if(Parameter.NO_OF_DISHONEST_BUYERS==0){
            	  JOptionPane.showMessageDialog(null,"Can not select attack model if no. of dishonest buyer is 0!");
            	  list.clearSelection();
              }
            }
        });*/
		
		listSelectionModel = list.getSelectionModel();
		listSelectionModel.addListSelectionListener(
				new SharedListSelectionHandler());

		JScrollPane listPane = new JScrollPane(list);


		//Build output area.
		output = new JTextArea(1, 10);
		output.setEditable(false);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel topHalf = new JPanel();
		topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));
		JPanel listContainer = new JPanel(new GridLayout(1,1));
		listContainer.setBorder(BorderFactory.createTitledBorder(
				"Choose Evaluation Metrics"));
		listContainer.add(listPane);

		topHalf.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		topHalf.add(listContainer);

		topHalf.setMinimumSize(new Dimension(460, 50));
		topHalf.setPreferredSize(new Dimension(460, 150));
		splitPane.add(topHalf);


	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private void createAndShowGUI() {
		//Create and set up the window.
		frame = new JFrame("EvaluationListSelection");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		//Create and set up the content pane.
		frame.setContentPane(contentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public void initialise() {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				list.clearSelection();
				//createAndShowGUI();              
				//DetectionModelsParameters DMP = new DetectionModelsParameters();
			}
		});
	}

	class SharedListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) { 
			count++;
			if(count%2==0){

				selected = list.getSelectedValuesList(); 
				ArrayList<String> selectedvalues = new ArrayList<String>();
				
				output.append(newline);
				output.setCaretPosition(output.getDocument().getLength());

				Parameter.EVA_EMPTY = false;
				frame.setVisible(false);

				output.append(newline);
				output.setCaretPosition(output.getDocument().getLength());
				boolean check = false;
				int size = selected.size();
				for(int i=0; i<selected.size(); i++){
					selectedvalues.add(selected.get(i).toString());
				}
				for(int i=0; i<selected.size(); i++){
					String s = selected.get(i).toString();
					if(Parameter.ENV_IS_REAL==true){
						if(s.equalsIgnoreCase("Robustness ([-1,1])") || s.equalsIgnoreCase("MAE-DS repDiff(Reputation difference of dishonest seller ([0, 1])") || s.equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])") || s.equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])") || s.equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
							JOptionPane.showMessageDialog(null, "Option not available in Real Environment!");
							check = true;
							break;
						}
						
					}

				}
				if(check==true){
					list.clearSelection();
					for(int i=0; i<size; i++){
						String s = selectedvalues.get(i);				
						if(s.equalsIgnoreCase("MAE-HS repDiff(Reputation difference of honest seller ([0, 1])")){
							list.setSelectedIndex(2);
						}
						else if(s.equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
							list.setSelectedIndex(4);
						}
						else if(s.equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(6);
						}
						else if(s.equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(8);
						}
						else if(s.equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(10);
						}
						else if(s.equalsIgnoreCase("TPR-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(12);
						}
						else if(s.equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(14);
						}
						else if(s.equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
							list.setSelectedIndex(16);
						}
					}
					check = false;
				}
			}


		}
	}

	public static List getList() {
		// TODO Auto-generated method stub
		return selected;
	}

	public ArrayList getDataList() {
		// TODO Auto-generated method stub
		return data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}
	public void clearContents(){		
		list.clearSelection();
	}
}
