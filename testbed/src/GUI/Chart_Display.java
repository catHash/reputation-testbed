package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.CentralAuthority;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart_Display extends JPanel implements ActionListener {

	private CategoryDataset dataset;
	//private XYDataset datasetxy;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	// Label and dropbox
	private JLabel chartLabel;	
	private Chart_Table chartTable;
	private Container contentChartPane;
	private JComboBox chartComboBox;
	private Container contentlabelDropBoxPane;
	private String[] chartTitles;
	private ChartAnalyzer_Main chart_Main;
	private String[] attackList;
	private String attackName;
	private JButton okButton;

	private String evaluateName;

	public Chart_Display(int day ,Chart_Table chartTable, ChartAnalyzer_Main chart_Main,
			String[] attackList, String attackName, String evaluateName, ArrayList chartList, String type) {
		this.chart_Main = chart_Main;
		this.setLayout(new BorderLayout(10, 10));
		this.chartTable = chartTable;
		this.attackList = attackList;
		this.attackName = attackName;
		this.evaluateName = evaluateName;
		this.setPreferredSize(new Dimension(150, 150));
		initLabelAndDropBox(attackList);
		initChartData(day ,attackName, evaluateName, chartList, type);
		initOKButton();
	}

	public void setSelectedIndex(int index) {
		this.chartComboBox.setSelectedIndex(index);
	}

	// To create label and drop box for the chart
	public void initLabelAndDropBox(String[] attackList) {
		// Container to store label and dropbox
		contentlabelDropBoxPane = new Container();
		contentlabelDropBoxPane.setLayout(new FlowLayout());
		chartLabel = new JLabel("Attack Type: ");
		chartComboBox = new JComboBox(attackList);
		chartComboBox.setSelectedIndex(0); // Default selected drop box value
		chartComboBox.addActionListener(this);
		// Adding container to the panel
		contentlabelDropBoxPane.add(chartLabel);
		contentlabelDropBoxPane.add(chartComboBox);
		contentlabelDropBoxPane.setSize(this.getWidth(), 10);
		this.add(contentlabelDropBoxPane, BorderLayout.NORTH);
	}

	// To create chart diagram
	public void initChartData(int day, String attackName, String evaluateName, ArrayList chartList, String type) { // Std. var
		// Container to store the chart diagram
		contentChartPane = new Container();
		contentChartPane.setLayout(new FlowLayout());

			dataset = accessHashMap(attackName, evaluateName, type);

			chart = createChart(day, dataset, evaluateName);

		chartList.add(chart);
		chartPanel = new ChartPanel(chart);
		// Adding container to the panel
		contentChartPane.add(chartPanel);
		this.add(chartPanel, BorderLayout.CENTER);
	}

	// To create OKButton at the end (Return to main menu)
	private void initOKButton() {
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		contentChartPane.add(okButton);		
	}

	// To create a method to read from a file and display the chart content
	private CategoryDataset createDataset(ArrayList meanList, ArrayList varList,
			ArrayList trustModelList, String type) {

		String series = "Mean";
		String series1 = "Variance";
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (int j = 0; j < meanList.size(); j++) {
			dataset.addValue(Double.parseDouble((String) meanList.get(j)),
					series, (String) trustModelList.get(j));
		}

		for (int k = 0; k < varList.size(); k++) {
			dataset.addValue(Double.parseDouble((String) varList.get(k)),
					series1, (String) trustModelList.get(k));
		}

		return dataset;
	}


	private JFreeChart createChart(int day, final CategoryDataset dataset, String evalName) {
		String evaluateName ="";
		if(evalName.equalsIgnoreCase("Robustness ([-1,1])")){
			evaluateName = "Robustness";
		}  else if(evalName.equalsIgnoreCase("MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])")){
			evaluateName = "MAE-DS";
		} else if(evalName.equalsIgnoreCase("MAE-HS repDIff(reputation difference of honest seller ([0, 1])")){
			evaluateName = "MAE-HS";
		}
		else if(evalName.equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])")){
			evaluateName = "MCC-DS";
		}
		else if(evalName.equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
			evaluateName = "MCC-HS";
		}
		else if(evalName.equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "FNR-DS";
		}
		else if(evalName.equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
			evaluateName = "FNR-HS";
		}
		else if(evalName.equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "Accuracy-DS";
		}
		else if(evalName.equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
			evaluateName = "Accuracy-HS";
		}
		else if(evalName.equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "FPR-DS";
		}
		else if(evalName.equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
			evaluateName = "FPR-HS";
		}
		else if(evalName.equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "Precision-DS";
		}
		else if(evalName.equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
			evaluateName = "Precision-HS";
		}
		else if(evalName.equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "F-Measure-DS";
		}
		else if(evalName.equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
			evaluateName = "F-Measure-HS";
		}
		else if(evalName.equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])")){
			evaluateName = "TPR-DS";
		}
		else if(evalName.equalsIgnoreCase("TPR-HS (Classification of honest seller ([-1,1])")){
			evaluateName = "TPR-HS";
		}
		// create the chart...
		final JFreeChart chart = ChartFactory.createBarChart("Result on day " + day, // chart
				// title
				"Defense Models", // domain axis label
				evaluateName, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis.setLowerBound(-1.0);
		rangeAxis.setUpperBound(1.0);
		//rangeAxis.setTickUnit(new NumberTickUnit(10));
		// disable bar outlines...
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(true);

		CategoryPlot categoryPlot = chart.getCategoryPlot();
		BarRenderer br = (BarRenderer) categoryPlot.getRenderer();
		br.setMaximumBarWidth(.35); 

		// set up gradient paints for series...
		final GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue,
				0.0f, 0.0f, Color.lightGray);
		final GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green,
				0.0f, 0.0f, Color.lightGray);
		final GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
				0.0f, 0.0f, Color.lightGray);
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		renderer.setSeriesPaint(2, gp2);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.
		
		return chart;

	}




	// For action listener when clicking the drop box
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.chartComboBox)) {
			chart_Main.initPanel(this.chartComboBox.getSelectedIndex());
			// initChartData(attackName);
		}
		else if (e.getSource().equals(okButton)) {
			contentChartPane.setVisible(false);
		}
	}

	public CategoryDataset accessHashMap(String attackName, String evalName, String type) {
		//TODO not used?
		Object[] keySet = CentralAuthority.outputResult.keySet().toArray();
		ComparisonResults cr = new ComparisonResults();
		//TODO getStatistics

		int l = -1;
		if(evalName.equalsIgnoreCase("Robustness ([-1,1])")){
			l = 0;
		}  else if(evalName.equalsIgnoreCase("MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])")){
			l = 1;
		} else if(evalName.equalsIgnoreCase("MAE-HS repDIff(reputation difference of honest seller ([0, 1])")){
			l = 2;
		}
		else if(evalName.equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])")){
			l = 3;
		}
		else if(evalName.equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
			l = 4;
		}
		else if(evalName.equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])")){
			l = 5;
		} 
		else if(evalName.equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
			l = 6;
		}
		else if(evalName.equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])")){
			l = 7;
		}
		else if(evalName.equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
			l = 8;
		}

		else if(evalName.equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])")){
			l = 9;
		} 
		else if(evalName.equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
			l = 10;
		}
		else if(evalName.equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])")){
			l = 11;
		}
		else if(evalName.equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
			l = 12;
		}

		else if(evalName.equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
			l = 13;
		} 
		else if(evalName.equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
			l = 14;
		}
		else if(evalName.equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])")){
			l = 15;
		}
		else if(evalName.equalsIgnoreCase("TPR-HS (Classification of honest seller ([0,1])")){
			l = 16;
		}
		cr = (ComparisonResults) CentralAuthority.outputResult.get(attackName+"_"+l);
		return dataset = createDataset(cr.getMeanList(), cr.getVarList(), cr.getTrustModelList(), type);

	}


}
