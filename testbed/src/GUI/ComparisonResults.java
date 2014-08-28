package GUI;

import java.util.ArrayList;

public class ComparisonResults {
	private ArrayList trustModelList = null;
	private ArrayList meanList = null;
	private ArrayList varList = null;

	//array list of evaluation matrix, each evaluation matrix stores arraylist of mean
	private ArrayList<ArrayList> dailyResults = null;
	
	public ComparisonResults() {
		this.trustModelList = new ArrayList();
		this.meanList = new ArrayList();
		this.varList = new ArrayList();
		this.dailyResults = new ArrayList< ArrayList>();
		for(int i=0; i<MainGUI.selectedEvaluate.size(); i++){
			dailyResults.add(new ArrayList());
		}
	}

	public ArrayList getTrustModelList() {
		return trustModelList;
	}

	public void setTrustModelList(ArrayList trustModelList) {
		this.trustModelList = trustModelList;
	}

	public ArrayList getMeanList() {
		return meanList;
	}

	public void setMeanList(ArrayList meanList) {
		this.meanList = meanList;
	}
	
	public ArrayList getVarList() {
		return varList;
	}

	public void setVarList(ArrayList varList) {
		this.varList = varList;
	}

	public ArrayList<ArrayList> getDailyResults() {
		return dailyResults;
	}

	public void setDailyResults(ArrayList<ArrayList> dailyResults) {
		this.dailyResults = dailyResults;
	}
	
	
}
