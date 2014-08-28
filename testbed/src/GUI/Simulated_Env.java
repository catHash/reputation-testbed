package GUI;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;




public class Simulated_Env extends JFrame{
	//for real environment
    public String realFileName=""; 
	Boolean isReal=false;
	public JPanel panels[]=new JPanel[1]; 
	JPanel mainPanel = new JPanel(new GridLayout(1,1));
	String title[] = {"Marketplace Setup"};	
	
	
	
    
   public void setIsReal(Boolean value){
	   this.isReal=value;
   }
   
   public String getRealFileName(){
	   return this.realFileName;
   }
	
	public Simulated_Env(String title, Boolean realValue)
	{

		super(title);
		setIsReal(realValue);
		this.panels[0]=new Marketplace_Main(this);	
		createTabs();
		this.add(mainPanel);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
		this.setPanelsSize(650,720);

		//this.pack();
	}
	public void createTabs()
	{
        JTabbedPane tabbedPane = new JTabbedPane();    
        
        for(int i = 0;i < panels.length;i++)
        {
        	tabbedPane.addTab(title[i],panels[i]);
        	this.realFileName=((Marketplace_Main)panels[i]).getRealName();
        }
        
        mainPanel.add(tabbedPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	public void changeTab(int index)
	{
		JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (JTabbedPane)mainPanel.getComponent(i);
				break;
			}
		tabbedPane.setSelectedIndex(index%tabbedPane.getComponentCount());		
	}
	
	public int getTab()
	{
		JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (JTabbedPane)mainPanel.getComponent(i);
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
