package GUI;

import java.awt.GridLayout;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.MenuListener;

import main.Parameter;
import main.Transaction;
import environment.*;
import weka.core.Instance;
import weka.core.Instances;
import agent.Buyer;



public class SimulationAnalyzer_Main extends JPanel implements EventListener {

	private JTextArea simLog = new JTextArea();
	//For transaction details
	private Marketplace_Table transTable;
	private String columnData[] = {"Buyer","Seller","Product No","Product Price"};
	//For balance details
	private Marketplace_Table transBalTable;
	private String columnBalData[] = {"Buyer", "Balance"};
	//For rating details
	private Marketplace_Table transRatingTable;
	private ChartAnalyzer_Main[] chartMain = null;
	private String columnRatingData[] = {"Seller", "Rating"};
	private boolean checkdata = false;
	public ChartAnalyzer_Main[] getChartMain() {
		return chartMain;
	}

	public ChartAnalyzer_Main getChartMainIndex(int index){
		return chartMain[index];
	}

	// an array of ArrayList<Vector<Vector<Object>>> charts. each chart for each evaluation matrix
	private ArrayList<ArrayList<Vector<Vector<Object>>>> charts = new ArrayList<ArrayList<Vector<Vector<Object>>>>(); 
	// an array of chartsCols. each chartCols for each evaluation matrix
	private ArrayList<ArrayList<Vector<Object>>> chartCols = new ArrayList<ArrayList<Vector<Object>>>();
	private String [] chartTitles;
	// an array of data. each data for each evaluation matrix
	private ArrayList<Vector<Vector <Object>>> data = new ArrayList<Vector<Vector <Object>>>();
	private boolean isResult = false;

	public boolean isResult() {
		return isResult;
	}

	public void setResult(boolean isResult) {
		this.isResult = isResult;
	}

	public SimulationAnalyzer_Main()
	{

		initTable();
		initPanel();
	}

	public void initTable()
	{
		transTable = new Marketplace_Table();
		transTable.setInitTable(columnData.length, this.columnData);
		transBalTable = new Marketplace_Table();
		transBalTable.setInitTable(this.columnBalData.length, this.columnBalData);
		transRatingTable = new Marketplace_Table();
		transRatingTable.setInitTable(columnRatingData.length, this.columnRatingData);
	}

	public void initPanel()
	{
		this.setLayout(new GridLayout(3,1,10,10));
		simLog.setSize(10,10);
		simLog.setEditable(false);

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1,0));
		textPanel.add(new JScrollPane(simLog));
		textPanel.setBorder(BorderFactory.createTitledBorder("Log Details"));
		add(textPanel);

		textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1,0));
		textPanel.setBorder(BorderFactory.createTitledBorder("Rating Details"));
		textPanel.add(transTable);
		add(textPanel);

		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new GridLayout(1,2));

		textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1,0));
		textPanel.setBorder(BorderFactory.createTitledBorder("Buyer Details"));
		textPanel.add(transBalTable);
		tablePanel.add(textPanel);

		textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1,0));
		textPanel.setBorder(BorderFactory.createTitledBorder("Seller Details"));
		textPanel.add(transRatingTable);
		tablePanel.add(textPanel);

		add(tablePanel);
	}

	public void setChartAnalyzer(ChartAnalyzer_Main[] chartMain)
	{
		this.chartMain = chartMain;
	}

	public void setChartAnalyzerIndex(ChartAnalyzer_Main chartMain, int index)
	{
		this.chartMain[index] = chartMain;
	}



	public void readTrans(ArrayList transList, ArrayList<Buyer> buyers, Environment commerce){
		Instances instList = commerce.getInstTransactions();
		Instance inst = null;
		for (int a =0; a<transList.size(); a++){
			Vector<String> transData = new Vector<String>();
			transData.addElement("b"+((Transaction)transList.get(a)).getBuyer().getId());
			transData.addElement("s"+((Transaction)transList.get(a)).getSeller().getId());
			//getProductNo to getQuantity
			transData.addElement(String.valueOf(((Transaction)transList.get(a)).getProduct().getId()));
			transData.addElement(String.valueOf(((Transaction)transList.get(a)).getPrice()));
			setRowData(transData,'A');
		}
		for (int b =0; b<buyers.size(); b++)
		{
			DecimalFormat roundoff = new DecimalFormat("0.00");
			Vector<String> buyersData = new Vector<String>();
			buyersData.addElement("b"+String.valueOf(buyers.get(b).getId()));
			buyersData.addElement(String.valueOf(roundoff.format(buyers.get(b).getAccount().getBalance())));
			setRowData(buyersData,'B');
		}
		double[] sellerRating = new double[Parameter.TOTAL_NO_OF_SELLERS];
		double[] sellerRatingCount = new double[Parameter.TOTAL_NO_OF_SELLERS];
		for (int e = 0; e<sellerRating.length; e++)
		{
			sellerRating[e] = 0;
			sellerRatingCount[e] = 0;
		}
		for (int c = 0; c<instList.numInstances(); c++)
		{
			inst = instList.instance(c);
			if(commerce instanceof EnvironmentR){
				int sellerID = (int) inst.value(Parameter.m_sidIdx);
				if(sellerID > Parameter.NO_OF_HONEST_SELLERS+Parameter.NO_OF_DISHONEST_SELLERS) continue;
				sellerRating[sellerID] += inst.value(Parameter.m_ratingIdx);
				sellerRatingCount[sellerID]++;
			}
			else{
				int sellerID = (int) inst.value(Parameter.m_sidIdx);
				sellerRating[sellerID] += inst.value(Parameter.m_ratingIdx);
				sellerRatingCount[sellerID]++;
			}

		}
		for (int f = 0; f<sellerRating.length; f++)
		{
			DecimalFormat roundoff = new DecimalFormat("0.000");
			Vector<String> sellerData = new Vector<String>();
			if (sellerRating[f] == 0.0 && sellerRatingCount[f] ==0)
			{
				sellerRating[f] = commerce.getSellersTrueRating(f,0);
			} else {
				sellerRating[f] /= sellerRatingCount[f];
			}
			sellerData.addElement("s"+f);
			sellerData.addElement(String.valueOf(roundoff.format(sellerRating[f])));
			setRowData(sellerData, 'C');			
		}

	}

	public void setChartTableData(int day,int j, int k, String evaluateName, double mean, double std, boolean check)
	{
		if (charts.size()==0){
			for(int i=0; i<MainGUI.selectedEvaluate.size(); i++){
				charts.add(new ArrayList<Vector<Vector<Object>>>());
				data.add(new Vector<Vector <Object>>());
				chartCols.add(new ArrayList<Vector<Object>>());
			}
		}

		int l = -1; 
		for(int i=0; i<MainGUI.selectedEvaluate.size(); i++){
			if(evaluateName.equalsIgnoreCase(MainGUI.selectedEvaluate.get(i))){
				l = i;
			}  
		}

		Vector set = null;
		set = new Vector();
		set.add(MainGUI.selectedAttack.get(k).toString());
		set.add(MainGUI.selectedDetect.get(j).toString());
		set.add(mean);
		set.add(std);
		if(data.get(l).size()< MainGUI.selectedDetect.size()){
			data.get(l).add(set);
			charts.get(l).add(data.get(l));			
		}
		else if (check==false)
		{	
			 data.get(l).set(j, set);
			 charts.get(l).set(j, data.get(l));
		}
		else {
			if(checkdata==false){
				for(int i=0; i<data.size(); i++){
					if(data.get(i).size()==MainGUI.selectedDetect.size())
						data.get(i).clear();
				}
				checkdata=true;
			}
			data.get(l).add(set);
			charts.get(l).add(data.get(l));
		}
		//data.get
		set = new Vector();
		set.add("Atk Model");
		set.add("Detection Model");
		set.add("Mean");
		set.add("Std. Variance");
		chartCols.get(l).add(set);
		if(!charts.get(l).isEmpty()){
			if ( chartMain == null){
				chartMain = new ChartAnalyzer_Main[MainGUI.selectedEvaluate.size()];
				for(int i=0; i<chartMain.length; i++){
					chartMain[i] = new ChartAnalyzer_Main();
				}
			}
			if(chartMain[l] == null){
				chartMain[l] = new ChartAnalyzer_Main(day,charts.get(l), chartCols.get(l), chartTitles, evaluateName, "bar");
			}
			else{
				chartMain[l].setChartData(day,charts.get(l), chartCols.get(l), chartTitles, evaluateName, "bar", k);
			}
		}

	}



	public Vector<Object> processChartData(String line)
	{
		if(line.split("[,]").length < 1)
			return null;

		String[] columns = line.split(",");
		Vector<Object> row = new Vector<Object>();

		for(String column : columns)
			row.add(column.split(":")[1].trim());

		return row;
	}

	public Vector<Object> processChartColumns(String line)
	{
		if(line.split("[,]").length < 1)
			return null;

		String[] columns = line.split(",");
		Vector<Object> col = new Vector<Object>();

		for(String column : columns)
			col.add(column.split(":")[0].trim());

		return col;
	}

	public Vector<String> splitLine(String line)
	{
		Vector<String> listofWords = new Vector<String>();
		String word = "";

		for(char curChar : line.toCharArray())
		{

			if(curChar!=' ' && curChar!=':')
				word += curChar;
			else if(curChar == ' ')
			{
				if(!word.isEmpty())
					listofWords.addElement(word);
				word = "";
			}
		}
		if(!word.isEmpty())
			listofWords.addElement(word);
		return listofWords;

	}

	public void setText(String text)
	{
		simLog.append(text + "\n");
		simLog.setCaretPosition(simLog.getDocument().getLength());
	}

	public void setDouble(Double text)
	{
		simLog.append(text + "\n");
		simLog.setCaretPosition(simLog.getDocument().getLength());
	}

	public ArrayList<ArrayList<Vector<Vector<Object>>>> getCharts()
	{
		return charts;
	}

	public ArrayList<ArrayList<Vector<Object>>> getChartColumns()
	{
		return chartCols;
	}

	public void setRowData(Vector<String> transData, char type)
	{
		if(type=='A')
			transTable.addRowData(transData);
		else if(type=='B')
			transBalTable.addRowData(transData);
		else
			transRatingTable.addRowData(transData);
	}

	public void onRecvMyEvent(MyEvent event) {
		simLog.append(event.text + "\n");
		simLog.setCaretPosition(simLog.getDocument().getLength());
	}

}
