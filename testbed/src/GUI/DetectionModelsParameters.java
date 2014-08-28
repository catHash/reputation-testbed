package GUI;

import main.Parameter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class DetectionModelsParameters extends JFrame implements ActionListener {

	SpringLayout layout = new SpringLayout();
	String command = "";

	BRSConfigPanel brsConfig;
	MSRConfigPanel MSRConfig;
	MeTrustedGraphConfigPanel MeConfig;
	TRAVOSConfigPanel travosConfig;
	iclubConfigPanel iclubConfig;
	PersonalizedConfigPanel personalizedConfig;
	OthersConfigPanel othersConfig;
	ProbCogConfigPanel probCogConfig;
	WMAConfigPanel WMAConfig;
	JButton ok = new JButton("OK");
	JButton cancel = new JButton("Cancel");
	ArrayList<String> s = new ArrayList<String>();

	public DetectionModelsParameters(List<?> selected, ArrayList<?> dataList) throws IOException {

		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JScrollPane scrollPane = null;
		Border blackline = BorderFactory.createLineBorder(Color.BLACK);

		GridLayout g = new GridLayout(selected.size(), 1);

		ok.addActionListener(this);
		cancel.addActionListener(this);
		
		s.addAll((Collection<? extends String>) selected);

		File modelDirectory = new File ("SavedConfiguration\\newModels");
		ArrayList<String> moreModelNames = new ArrayList<String>();
		if (modelDirectory.isDirectory()){
			File modelFiles[] = modelDirectory.listFiles();
			for (int i = 0; i<modelFiles.length; i++){
				String[] line = modelFiles[i].toString().split("\\\\");
				line = line[line.length-1].split("\\.");
				moreModelNames.add(line[0]);
			}
		}

		panel1.setLayout(g);
		for (int i = 0; i < selected.size(); i++) {
			if(((String) selected.get(i)).equalsIgnoreCase("ebay")){
				continue;
			}
			else if (((String) selected.get(i)).equalsIgnoreCase("BRS")) {
				brsConfig = new BRSConfigPanel();
				TitledBorder brsTitle = BorderFactory.createTitledBorder(blackline,
						"BRS Trust Model Configuration");
				brsConfig.setBorder(brsTitle);
				panel1.add(brsConfig);
				brsConfig.setVisible(true);

			}
			else if (((String) selected.get(i)).equalsIgnoreCase("MSR")) {
				MSRConfig = new MSRConfigPanel();
				TitledBorder MSRTitle = BorderFactory.createTitledBorder(blackline,
						"MSR Trust Model Configuration");
				MSRConfig.setBorder(MSRTitle);
				panel1.add(MSRConfig);
				MSRConfig.setVisible(true);

			}
			else if (((String) selected.get(i)).equalsIgnoreCase("TRAVOS")) {
				travosConfig = new TRAVOSConfigPanel();
				TitledBorder travosTitle = BorderFactory.createTitledBorder(blackline,
						"TRAVOS Trust Model Configuration");
				travosConfig.setBorder(travosTitle);
				panel1.add(travosConfig);
				travosConfig.setVisible(true);
			}
			else if (((String) selected.get(i)).equalsIgnoreCase("IClub")) {
				iclubConfig = new iclubConfigPanel();
				TitledBorder iclubTitle = BorderFactory.createTitledBorder(blackline,
						"IClub Trust Model Configuration");
				iclubConfig.setBorder(iclubTitle);
				panel1.add(iclubConfig);
				iclubConfig.setVisible(true);
			}
			else if (((String) selected.get(i)).equalsIgnoreCase("Personalized")) {
				personalizedConfig = new PersonalizedConfigPanel();
				TitledBorder personalizedTitle = BorderFactory.createTitledBorder(
						blackline, "Personalized Trust Model Configuration");
				personalizedConfig.setBorder(personalizedTitle);
				panel1.add(personalizedConfig);
				personalizedConfig.setVisible(true);
				personalizedConfig.setSize(580,300);
			}else if (((String) selected.get(i)).equalsIgnoreCase("ProbCog")) {
				probCogConfig = new ProbCogConfigPanel();
				TitledBorder probCogTitle = BorderFactory.createTitledBorder(
						blackline, "ProbCog Trust Model Configuration");
				probCogConfig.setBorder(probCogTitle);
				panel1.add(probCogConfig);
				probCogConfig.setVisible(true);
			}
			else if (((String) selected.get(i)).equalsIgnoreCase("WMA")) {
				WMAConfig = new WMAConfigPanel();
				TitledBorder WMATitle = BorderFactory.createTitledBorder(
						blackline, "WMA Trust Model Configuration");
				WMAConfig.setBorder(WMATitle);
				panel1.add(WMAConfig);
				WMAConfig.setVisible(true);
			}
			else if (((String) selected.get(i)).equalsIgnoreCase("MeTrustedGraph")) {
				MeConfig = new MeTrustedGraphConfigPanel();
				TitledBorder MeTitle = BorderFactory.createTitledBorder(
						blackline, "Me Trusted Graph Trust Model Configuration");
				MeConfig.setBorder(MeTitle);
				panel1.add(MeConfig);
				MeConfig.setVisible(true);
			}
			else if (moreModelNames.contains(selected.get(i))){
				othersConfig = new OthersConfigPanel((String)selected.get(i));
				TitledBorder othersTitle = BorderFactory.createTitledBorder(blackline,
						selected.get(i).toString()+" Trust Model Configuration");
				othersConfig.setBorder(othersTitle);
				panel1.add(othersConfig);
				othersConfig.setVisible(true);
			}
		}

		panel2.setLayout(new FlowLayout());
		panel2.add(ok);
		panel2.add(cancel);
		panel2.setVisible(true);

		add(new JScrollPane(panel1), BorderLayout.CENTER);
		add(panel2, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setResizable(true);
		this.setVisible(true);
		this.setSize(600, (768>(selected.size())*250 ? (selected.size())*250:700));

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			Parameter.DEF_EMPTY = false;
			for(int i=0; i<s.size(); i++){
				if(s.get(i).toString().equalsIgnoreCase("brs")){
					brsConfig.saveParam();	}
				else if(s.get(i).toString().equalsIgnoreCase("iclub")){
					iclubConfig.saveParam();}
				else if(s.get(i).toString().equalsIgnoreCase("metrustedgraph")){
					MeConfig.saveParam();	}
				else if(s.get(i).toString().equalsIgnoreCase("personalized")){
					personalizedConfig.saveParam();	}
				else if(s.get(i).toString().equalsIgnoreCase("probcog")){
					probCogConfig.saveParam();}
				else if(s.get(i).toString().equalsIgnoreCase("travos")){
					travosConfig.saveParam(); }
				else if(s.get(i).toString().equalsIgnoreCase("wma")){
					WMAConfig.saveParam();
				}
				else if(s.get(i).toString().equalsIgnoreCase("msr")){
					MSRConfig.saveParam();
				}
			}
			dispose();
		} else if (e.getSource() == cancel) {
			dispose();
		} 
	}

}
