package GUI;

import main.Parameter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DetectionListSelection extends JFrame implements ActionListener {
	JTextArea output;
	JList list; 
	JTable table;
	static DefaultListModel listModel;

	static List selected;
	String newline = "\n";
	ListSelectionModel listSelectionModel;
	static JFrame frame = new JFrame();
	JSplitPane splitPane;
	JPanel contentPane;
	ArrayList<String> data;
	int count=1;
	ArrayList<String> track = new ArrayList<String>();

	public boolean brs = false;
	public boolean msr = false;
	public boolean ebay = true;
	public boolean iclub = false;
	public boolean metrustedgraph = false;
	public boolean personalized = false;
	public boolean probcog = false;
	public boolean reece = true;
	public boolean travos = false;
	public boolean wma = false;
	public DetectionListSelection() {
		data = new ArrayList<String>();
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		try {	// if the directory does not exist, create it
	
			
			String dirName = "SavedConfiguration/defModels.txt";
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
				fw.write("BRS\r\nMSR\r\nReece\r\nMeTrustedGraph\r\neBay\r\nTRAVOS\r\nIClub\r\nPersonalized\r\nProbCog\r\nWMA\r\n..");
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

		listModel=new DefaultListModel();
		for (int i=0; i<data.size(); i++) {
			listModel.addElement(data.get(i));
		}
		list=new JList(listModel);
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
				"Choose Defense List"));
		listContainer.add(listPane);

		topHalf.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		topHalf.add(listContainer);

		topHalf.setPreferredSize(new Dimension(235, 260));
		splitPane.add(topHalf);


	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private void createAndShowGUI() {
		//Create and set up the window.
		frame = new JFrame("DetectionListSelection");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

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
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();

				selected = list.getSelectedValuesList(); 
				ArrayList<String> s = new ArrayList<String>();
				boolean checkbrs =false, checkmsr = false, checkiclub=false, checkme = false, checkper= false, checkprobcog=false, checkreece=false, checktravos=false, checkwma=false;
				for(int i=0; i<selected.size(); i++){
					if(selected.get(i).toString().equalsIgnoreCase("brs")){
						if( brs == false){
							s.add("brs");
							brs = true;
						}
						checkbrs=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("msr")){
						if(msr == false){
							s.add("msr");
							msr = true;
						}
						checkmsr=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("iclub")){
						if(iclub == false){
							s.add("iclub");
							iclub = true;
						}
						checkiclub=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("metrustedgraph")){
						if(metrustedgraph == false){
							s.add("metrustedgraph");
							metrustedgraph = true;
						}
						checkme=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("personalized")){
						if(personalized == false){
							s.add("personalized");
							personalized = true;
						}
						checkper=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("probcog")){
						if(probcog == false){
							s.add("probcog");
							probcog = true;
						}
						checkprobcog=true;
					}

					else if(selected.get(i).toString().equalsIgnoreCase("travos")){
						if( travos == false){
							s.add("travos");
							travos = true;
						}
						checktravos=true;
					}
					else if(selected.get(i).toString().equalsIgnoreCase("wma")){
						if(wma == false){
							s.add("wma");
							wma = true;
						}
						checkwma=true;
					}
				}


				if(checkbrs == false && brs==true){
					brs = false;
				}
				if(checkmsr == false && msr==true){
					msr = false;
				}
				if(checkiclub == false && iclub==true){
					iclub = false;
				}
				if(checkme == false && metrustedgraph==true){
					metrustedgraph = false;
				}
				if(checkper == false && personalized==true){
					personalized = false;
				}
				if(checkprobcog == false && probcog==true){
					probcog = false;
				}

				if(checktravos == false && travos==true){
					travos = false;
				}
				if(checkwma == false && wma==true){
					wma = false;
				}

				String[] selectedItems = new String[selected.size()];	
				if(s.size()>0){
					try {

						DetectionModelsParameters dmp = new DetectionModelsParameters(s, getDataList());

						Parameter.DEF_EMPTY = false;
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					output.append(newline);
					output.setCaretPosition(output.getDocument().getLength());
				}
				for(int i=0; i<selected.size(); i++){
					if(selected.get(i).toString().equalsIgnoreCase("ebay") || selected.get(i).toString().equalsIgnoreCase("reece") )
						Parameter.DEF_EMPTY = false;
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