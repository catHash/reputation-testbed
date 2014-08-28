package GUI;

import main.Parameter;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AttackListSelection extends JFrame implements ActionListener {
	JTextArea output;
	JList list; 
	JTable table;
	JButton resetButton = new JButton("Reset");
	static DefaultListModel listModel;

	static List selected;
	String newline = "\n";
	ListSelectionModel listSelectionModel;

	static JFrame frame = new JFrame();
	JSplitPane splitPane;
	JPanel contentPane;
	int count=1;

	public AttackListSelection() throws IOException {

		ArrayList<String> data = new ArrayList<String>();
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		try {
			String dirName = "SavedConfiguration/atkModels.txt";
			File directoryName = new File ("SavedConfiguration");
			if (!directoryName.exists() || !directoryName.isDirectory())
			{
				directoryName.mkdirs();
			}
			File atkFile = new File(dirName);
			if (!atkFile.exists() || !atkFile.isFile())
			{
				atkFile.createNewFile();
				FileWriter fw = new FileWriter(atkFile);
				fw.write("SybilSelective\r\nWhitewashingSelective\r\nCamouflageSelective\r\nSelectiveUnfairRating\r\nNoAttack\r\nSybil\r\nAlwaysUnfair\r\nCamouflage\r\nWhitewashing\r\nSybil_Camouflage\r\nSybil_Whitewashing\r\n..");
				fw.close();
			}
			BufferedReader br = new BufferedReader(new FileReader("SavedConfiguration/atkModels.txt"));
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
        });
        */
		listSelectionModel = list.getSelectionModel();
		listSelectionModel.addListSelectionListener(
				new SharedListSelectionHandler());
		JScrollPane listPane = new JScrollPane(list);

		//Build output area.
		output = new JTextArea(1, 10);
		output.setEditable(false);
		JScrollPane outputPane = new JScrollPane(output,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//Do the layout.
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		JPanel centralPane = new JPanel(new BorderLayout());

		JPanel topHalf = new JPanel();
		topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));
		JPanel listContainer = new JPanel(new GridLayout(1,1));
		listContainer.setBorder(BorderFactory.createTitledBorder(
				"Choose Attack List"));
		listContainer.add(listPane);

		topHalf.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		topHalf.add(listContainer);

		topHalf.setPreferredSize(new Dimension(235, 250));
		centralPane.add(topHalf, BorderLayout.NORTH);

		JPanel bottomHalf = new JPanel();
		centralPane.add(bottomHalf, BorderLayout.SOUTH);
		splitPane.add(centralPane);

	}

	public JPanel getPanel(){
		return contentPane;
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
			}
		});
	}

	class SharedListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) { 
			count++;
			if(count%2==0){
				selected = list.getSelectedValuesList(); 
				int size = selected.size();

				ArrayList<String> selectedvalues = new ArrayList<String>();
				for(int i=0; i<selected.size(); i++){
					selectedvalues.add(selected.get(i).toString());
				}
				boolean check = false;
				if(Parameter.NO_OF_DISHONEST_BUYERS==0){
					for(int i=0; i<selected.size(); i++){
						if(!selected.get(i).toString().equalsIgnoreCase("noattack")){
							JOptionPane.showMessageDialog(null, "There are no dishonest buyers to perform attack! Please select No Attack model.");
							check = true;
							break;
						}
					}
					if(selected.get(0).toString().equalsIgnoreCase("noattack")){
						Parameter.ATK_EMPTY = false;
					}
					if(check==true){
						list.clearSelection();
						for(int i=0; i<size; i++){
							String s = selectedvalues.get(i);				
							if(s.equalsIgnoreCase("noattack")){
								list.setSelectedIndex(4);
								Parameter.ATK_EMPTY = false;
							}
						}
						check = false;
					}
				}
				else{
					if(size>0){
						Parameter.ATK_EMPTY = false;
					}
				}
				
				frame.setVisible(false);


				output.append(newline);
				output.setCaretPosition(output.getDocument().getLength());
			}
		}
	}

	public static List getList() {
		// TODO Auto-generated method stub
		return selected;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//		
	}
	
	public void clearContents(){
		list.clearSelection();
	}
}