package GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;


import org.jfree.chart.JFreeChart;

//For Chart Analyzer GUI
public class ChartAnalyzer_Main extends JPanel {
	private ArrayList<Chart_Display> charts = new ArrayList<Chart_Display>(); // For
	// chart
	// display
	private ArrayList<LineChart_Display> linecharts = new ArrayList<LineChart_Display>();
	private ArrayList<Chart_Table> tables = new ArrayList<Chart_Table>(); // For
	// chart
	// table
	// display
	private ArrayList<Vector<Vector<Object>>> chartData;
	private ArrayList<Vector<Object>> chartCols;
	private String[] chartTitles;
	public static ArrayList chartList;
	private String evaluateName;
	public ChartAnalyzer_Main() {
	}

	public ChartAnalyzer_Main(int day, ArrayList<Vector<Vector<Object>>> chartData,
			ArrayList<Vector<Object>> chartCols, String[] chartTitles, String evaluateName, String type) {
		this.chartTitles = chartTitles;
		this.chartData = chartData;
		this.chartCols = chartCols;
		this.evaluateName = evaluateName;
		initChartAndTable(day, type);
		initPanel(0);
	}

	public void setChartData(int day, ArrayList<Vector<Vector<Object>>> chartData,
			ArrayList<Vector<Object>> chartCols, String[] chartTitles, String evaluateName, String type, int attack) {
		this.chartTitles = chartTitles;
		this.chartData = chartData;
		this.chartCols = chartCols;
		this.evaluateName = evaluateName;
		initChartAndTable( day, type);
				initPanel(attack);
	}

	
	public String getEvaluateName() {
		return evaluateName;
	}

	public void setEvaluateName(String evaluateName) {
		this.evaluateName = evaluateName;
	}

	// To display the chart
	public void initChartAndTable(int day, String type) {
		int index = 0;
		String[] attackNames = new String[MainGUI.selectedAttack.size()];
		String[] evalNames = new String[MainGUI.selectedEvaluate.size()];

		for (int i = 0; i < MainGUI.selectedAttack.size(); i++) {
			attackNames[i] = MainGUI.selectedAttack.get(i).toString();

		}

		// runs for each dataSet found in chartData
		chartList = new ArrayList();
		charts = new ArrayList<Chart_Display>(); 
		linecharts = new ArrayList<LineChart_Display>();
		tables = new ArrayList<Chart_Table>();
		
		for (Vector<Vector<Object>> dataSet : chartData) {
			// there is only one dataset in chartdata
			for (int j = 0; j < MainGUI.selectedAttack.size(); j++) {
				Chart_Table table = new Chart_Table( dataSet, chartCols.get(index));
				Chart_Display chart = new Chart_Display(day, table, this, attackNames, attackNames[j], evaluateName, chartList, type);
				LineChart_Display linechart = new LineChart_Display(day,table, this, attackNames, attackNames[j], evaluateName, chartList, type);

				tables.add(table);
				charts.add(chart);
				linecharts.add(linechart);
			}
		}
	}

	// To segment the panel into 2 sections
	public void initPanel(int index) {
		this.removeAll();
		// Overview of the main layout
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		charts.get(index).setSelectedIndex(index);
		linecharts.get(index);
		
		// Segment the panel for chart
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1, 0));
		textPanel.setBorder(BorderFactory.createTitledBorder("Chart"));
		textPanel.add(charts.get(index));	
		this.add(textPanel);
		
		
		JPanel textPanel2 = new JPanel();
		textPanel2.setLayout(new GridLayout(1, 0));
		textPanel2.setBorder(BorderFactory.createTitledBorder("Line Chart"));
		textPanel2.add(linecharts.get(index));
		this.add(textPanel2);

		
		// Segment the panel for table
		JPanel textPanel1 = new JPanel();
		textPanel1.setLayout(new GridLayout(1, 0));
		textPanel1.setBorder(BorderFactory.createTitledBorder("Table"));
		textPanel1.add(tables.get(index));
		this.add(textPanel1);

		if (this.getComponentCount() == 6) {
			this.remove(0);
			this.remove(0);
			this.remove(0);			
		}
	}
}
