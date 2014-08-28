package GUI;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.ClosableTabbedPane;




public class Display extends JFrame{

	JPanel mainPanel = new JPanel(new GridLayout(1,1));
	public JPanel panels[];
	String title[];	

	public Display(String title, SimulationAnalyzer_Main simMain)
	{	
		super(title);
		this.setAlwaysOnTop(true);
		panels = new JPanel[MainGUI.selectedEvaluate.size() +1];
		this.title = new String[MainGUI.selectedEvaluate.size()+ 1];
		panels[0] = simMain;
		this.title[0] = "Simulation Analyzer  ";
		int countt =0;
		for(int i=1; i<= MainGUI.selectedEvaluate.size(); i++){
			panels[i] = simMain.getChartMainIndex(countt);
			countt++;
		}
		int count=1;
		for(int i=0; i< MainGUI.selectedEvaluate.size(); i++){
			String m = MainGUI.selectedEvaluate.get(i);
			if(m.equalsIgnoreCase("Robustness ([-1,1])")){
				this.title[count] = "Chart Analyzer: Robustness  ";
			}
			else if (m.equalsIgnoreCase("MAE-DS repDiff(Reputation difference of dishonest seller ([0, 1])")){
				this.title[count] = "Chart Analyzer: MAE-DS  ";
			}
			else if (m.equalsIgnoreCase("MAE-HS repDiff(Reputation difference of honest seller ([0, 1])")){
				this.title[count] = "Chart Analyzer: MAE-HS  ";
			}
			else if (m.equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])")){
				this.title[count] = "Chart Analyzer: MCC-DS  ";
			}
			else if (m.equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
				this.title[count] = "Chart Analyzer: MCC-HS  ";
			}
			else if (m.equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: FNR-DS  ";
			}
			else if (m.equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: FNR-HS  ";
			}
			else if (m.equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: Accuracy-DS  ";
			}
			else if (m.equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: Accuracy-HS  ";
			}
			else if (m.equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: FPR-DS  ";
			}
			else if (m.equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: FPR-HS  ";
			}
			else if (m.equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: Precision-DS  ";
			}
			else if (m.equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: Precision-HS  ";
			}
			else if (m.equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: F-Measure-DS  ";
			}
			else if (m.equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: F-Measure-HS  ";
			}
			else if (m.equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: TPR-DS  ";
			}
			else if (m.equalsIgnoreCase("TPR-HS (Classification of honest seller ([0,1])")){
				this.title[count] = "Chart Analyzer: TPR-HS  ";
			}
			
			count++;
		}
		
		
		createTabs();
		this.add(mainPanel);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(true);
		this.setVisible(true);
		this.setPanelsSize(650,750);
		//this.pack();
	}
	public void createTabs()
	{
		ClosableTabbedPane tabbedPane = new ClosableTabbedPane();//JTabbedPane tabbedPane = new JTabbedPane(); 
        
		
		for(int i = 0;i < panels.length;i++)
		{	
			tabbedPane.insertTab(title[i], null, panels[i], null, i);

		}

		mainPanel.add(tabbedPane);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setSelectedIndex(1);
	}

	public void changeTab(int index)
	{
		ClosableTabbedPane tabbedPane = null;//JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (ClosableTabbedPane)mainPanel.getComponent(i);//(JTabbedPane)mainPanel.getComponent(i);
				break;
			}
		tabbedPane.setSelectedIndex(index%tabbedPane.getComponentCount());		
	}

	public int getTab()
	{
		ClosableTabbedPane tabbedPane = null;//JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (ClosableTabbedPane)mainPanel.getComponent(i);//(JTabbedPane)mainPanel.getComponent(i);
				break;
			}
		return tabbedPane.getSelectedIndex();	
	}

	public void setPanelsSize(int width,int height)
	{
		this.setSize(width,height);
		for(int i = 0;i < panels.length; i++)
		{
			if(panels[i] instanceof MarketTabPanels)
				((MarketTabPanels) panels[i]).setPanelSize(width,height);
			else
				panels[i].setSize(width, height);
		}

	}

	public JPanel getPanels(int index)
	{
		return panels[index];
	}

	public int getPanelCount()
	{
		return panels.length;
	}

}
