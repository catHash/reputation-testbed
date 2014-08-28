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
import main.Parameter;

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

public class LineChart_Display extends JPanel implements ActionListener {

	private XYDataset datasetxy;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	// Label and dropbox
	private String title;
	private JLabel chartLabel;	

	private Chart_Table chartTable;
	private Container contentChartPane;
	private String[] chartTitles;
	private ChartAnalyzer_Main chart_Main;
	private String[] attackList;
	private String attackName;
	private JButton okButton;

	private String evaluateName;

	public LineChart_Display(int envDay, Chart_Table chartTable, ChartAnalyzer_Main chart_Main,
			String[] attackList, String attackName, String evaluateName, ArrayList chartList, String type) {
		this.chart_Main = chart_Main;
		this.setLayout(new BorderLayout(10, 10));
		this.chartTable = chartTable;
		this.attackList = attackList;
		this.attackName = attackName;
		this.evaluateName = evaluateName;
		this.setPreferredSize(new Dimension(150, 150));
		initLabelAndDropBox(attackList);
		initChartData(envDay, attackName, evaluateName, chartList, type);
		initOKButton();
	}



	// To create label and drop box for the chart
	public void initLabelAndDropBox(String[] attackList) {

		chartLabel = new JLabel("Attack Type: ");

	}

	// To create chart diagram
	public void initChartData(int envDay, String attackName, String evaluateName, ArrayList chartList, String type) { // Std. var
		// Container to store the chart diagram
		contentChartPane = new Container();
		contentChartPane.setLayout(new FlowLayout());
		// Creating of chart

		datasetxy = accessLineHashMap( envDay, attackName, evaluateName, type);
		chart = createLineChart(datasetxy, evaluateName, attackName);

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
	private XYDataset createXYDataset(int envDay, ArrayList meanList, ArrayList varList,
			ArrayList trustModelList, String type) {

		String series = "Mean";
		String series1 = "Variance";
		XYSeriesCollection dataset = new XYSeriesCollection();
		int loop = meanList.size()/trustModelList.size(); // 40/4
		int start =0;
		int day=0;


		for(int j=0; j<trustModelList.size(); j++){
			day=0;
			XYSeries xyseries = new XYSeries(trustModelList.get(j).toString());
			for (int x = start; x < loop; x++) {

				if(x < meanList.size()){
					xyseries.add(day, Double.parseDouble((String)meanList.get(x)));
					day++;
				}
			}
			start =loop;
			loop += (envDay+1);

			dataset.addSeries(xyseries);
		}
		return dataset;
	}

	private JFreeChart createLineChart(final XYDataset dataset, String evalName, String attackName) {
		String evaluateName ="";
		if(evalName.equalsIgnoreCase("Robustness ([-1,1])")){
			evaluateName = "Robustness";
		}  
		else if(evalName.equalsIgnoreCase("MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])")){
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
		final JFreeChart chart = ChartFactory.createXYLineChart("", "Day", evaluateName, dataset, PlotOrientation.VERTICAL, true, true, false);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		rangeAxis.setLowerBound(-1.0);
		rangeAxis.setUpperBound(1.0);

		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setLowerBound(0);
		domainAxis.setUpperBound(Parameter.NO_OF_DAYS+1);
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		//final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawOutlines(true);
		for(int i=0; i<MainGUI.selectedDetect.size(); i++){
			renderer.setSeriesLinesVisible(i, true);
			renderer.setSeriesShapesVisible(i, true);
		}
		XYPlot xyPlot = chart.getXYPlot();
		XYLineAndShapeRenderer br = (XYLineAndShapeRenderer) xyPlot.getRenderer();


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


		return chart;

	}


	public XYDataset accessLineHashMap(int envDay, String attackName, String evalName, String type) {
		Object[] keySet = CentralAuthority.outputResult.keySet().toArray();
		ComparisonResults cr = new ComparisonResults();
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
		int index = -1;
		for(int i=0; i<MainGUI.selectedEvaluate.size(); i++){
			if (evalName.equalsIgnoreCase(MainGUI.selectedEvaluate.get(i).toString())){
				index = i;
				break;
			}
		}
		System.out.println();
		cr = (ComparisonResults) CentralAuthority.outputResult.get(attackName+"_"+l);
		return datasetxy = createXYDataset(envDay, cr.getDailyResults().get(index), cr.getVarList(), cr.getTrustModelList(), type);



	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
