package main;

import java.util.ArrayList;

public class DailyResults {

	//accumulated
	private ArrayList<Double> robustness = new ArrayList<Double>();
	private ArrayList<Double> maehs = new ArrayList<Double>();
	private ArrayList<Double> maeds = new ArrayList<Double>();
	private ArrayList<Double> mcchs = new ArrayList<Double>();
	private ArrayList<Double> mccds = new ArrayList<Double>();
	
	// *** added by amanda
	private ArrayList<Double> fnrhs = new ArrayList<Double>();
	private ArrayList<Double> fnrds = new ArrayList<Double>();
	private ArrayList<Double> acchs = new ArrayList<Double>();
	private ArrayList<Double> accds = new ArrayList<Double>();
	private ArrayList<Double> fprhs = new ArrayList<Double>();
	private ArrayList<Double> fprds = new ArrayList<Double>();
	private ArrayList<Double> precds = new ArrayList<Double>();
	private ArrayList<Double> prechs = new ArrayList<Double>();
	private ArrayList<Double> fhs = new ArrayList<Double>();
	private ArrayList<Double> fds = new ArrayList<Double>();
	private ArrayList<Double> tprhs = new ArrayList<Double>();
	private ArrayList<Double> tprds = new ArrayList<Double>();
	
	private ArrayList<Double> dailyrobustness = new ArrayList<Double>();
	private ArrayList<Double> dailymaehs = new ArrayList<Double>();
	private ArrayList<Double> dailymaeds = new ArrayList<Double>();
	private ArrayList<Double> dailymcchs = new ArrayList<Double>();
	private ArrayList<Double> dailymccds = new ArrayList<Double>();
	
	// *** added by amanda
	private ArrayList<Double> dailyfnrhs = new ArrayList<Double>();
	private ArrayList<Double> dailyfnrds = new ArrayList<Double>();
	private ArrayList<Double> dailyacchs = new ArrayList<Double>();
	private ArrayList<Double> dailyaccds = new ArrayList<Double>();
	private ArrayList<Double> dailyfprhs = new ArrayList<Double>();
	private ArrayList<Double> dailyfprds = new ArrayList<Double>();
	private ArrayList<Double> dailyprecds = new ArrayList<Double>();
	private ArrayList<Double> dailyprechs = new ArrayList<Double>();
	private ArrayList<Double> dailyfds = new ArrayList<Double>();
	private ArrayList<Double> dailyfhs = new ArrayList<Double>();
	private ArrayList<Double> dailytprds = new ArrayList<Double>();
	private ArrayList<Double> dailytprhs = new ArrayList<Double>();

	public DailyResults(){
		for (int i=0; i<Parameter.NO_OF_DAYS+1; i++){
			robustness.add(0.0);
			maehs.add(0.0);
			maeds.add(0.0);
			mccds.add(0.0);
			mcchs.add(0.0);
			// *** added by amanda
			fnrhs.add(0.0);
			fnrds.add(0.0);
			accds.add(0.0);
			acchs.add(0.0);
			fprhs.add(0.0);
			fprds.add(0.0);
			precds.add(0.0);
			prechs.add(0.0);
			fhs.add(0.0);
			fds.add(0.0);
			tprds.add(0.0);
			tprhs.add(0.0);
		}
	}
	
	public ArrayList<Double> getRobustness() {
		return robustness;
	}
	public void setRobustness(ArrayList<Double> robustness) {
		this.robustness = robustness;
	}
	public ArrayList<Double> getMaehs() {
		return maehs;
	}
	public void setMaehs(ArrayList<Double> maehs) {
		this.maehs = maehs;
	}
	public ArrayList<Double> getMaeds() {
		return maeds;
	}
	public void setMaeds(ArrayList<Double> maeds) {
		this.maeds = maeds;
	}
	public ArrayList<Double> getMcchs() {
		return mcchs;
	}
	public void setMcchs(ArrayList<Double> mcchs) {
		this.mcchs = mcchs;
	}
	public ArrayList<Double> getMccds() {
		return mccds;
	}
	public void setMccds(ArrayList<Double> mccds) {
		this.mccds = mccds;
	}

	public ArrayList<Double> getDailyrobustness() {
		return dailyrobustness;
	}

	public void setDailyrobustness(ArrayList<Double> dailyrobustness) {
		this.dailyrobustness = dailyrobustness;
	}

	public ArrayList<Double> getDailymaehs() {
		return dailymaehs;
	}

	public void setDailymaehs(ArrayList<Double> dailymaehs) {
		this.dailymaehs = dailymaehs;
	}

	public ArrayList<Double> getDailymaeds() {
		return dailymaeds;
	}

	public void setDailymaeds(ArrayList<Double> dailymaeds) {
		this.dailymaeds = dailymaeds;
	}

	public ArrayList<Double> getDailymcchs() {
		return dailymcchs;
	}

	public void setDailymcchs(ArrayList<Double> dailymcchs) {
		this.dailymcchs = dailymcchs;
	}

	public ArrayList<Double> getDailymccds() {
		return dailymccds;
	}

	public void setDailymccds(ArrayList<Double> dailymccds) {
		this.dailymccds = dailymccds;
	}

	public ArrayList<Double> getFnrhs() {
		return fnrhs;
	}

	public void setFnrhs(ArrayList<Double> fnrhs) {
		this.fnrhs = fnrhs;
	}

	public ArrayList<Double> getFnrds() {
		return fnrds;
	}

	public void setFnrds(ArrayList<Double> fnrds) {
		this.fnrds = fnrds;
	}

	public ArrayList<Double> getAcchs() {
		return acchs;
	}

	public void setAcchs(ArrayList<Double> acchs) {
		this.acchs = acchs;
	}

	public ArrayList<Double> getAccds() {
		return accds;
	}

	public void setAccds(ArrayList<Double> accds) {
		this.accds = accds;
	}

	public ArrayList<Double> getFprhs() {
		return fprhs;
	}

	public void setFprhs(ArrayList<Double> fprhs) {
		this.fprhs = fprhs;
	}

	public ArrayList<Double> getFprds() {
		return fprds;
	}

	public void setFprds(ArrayList<Double> fprds) {
		this.fprds = fprds;
	}

	public ArrayList<Double> getPrecds() {
		return precds;
	}

	public void setPrecds(ArrayList<Double> precds) {
		this.precds = precds;
	}

	public ArrayList<Double> getPrechs() {
		return prechs;
	}

	public void setPrechs(ArrayList<Double> prechs) {
		this.prechs = prechs;
	}

	public ArrayList<Double> getFhs() {
		return fhs;
	}

	public void setFhs(ArrayList<Double> fhs) {
		this.fhs = fhs;
	}

	public ArrayList<Double> getFds() {
		return fds;
	}

	public void setFds(ArrayList<Double> fds) {
		this.fds = fds;
	}

	public ArrayList<Double> getTprhs() {
		return tprhs;
	}

	public void setTprhs(ArrayList<Double> tprhs) {
		this.tprhs = tprhs;
	}

	public ArrayList<Double> getTprds() {
		return tprds;
	}

	public void setTprds(ArrayList<Double> tprds) {
		this.tprds = tprds;
	}

	public ArrayList<Double> getDailyfnrhs() {
		return dailyfnrhs;
	}

	public void setDailyfnrhs(ArrayList<Double> dailyfnrhs) {
		this.dailyfnrhs = dailyfnrhs;
	}

	public ArrayList<Double> getDailyfnrds() {
		return dailyfnrds;
	}

	public void setDailyfnrds(ArrayList<Double> dailyfnrds) {
		this.dailyfnrds = dailyfnrds;
	}

	public ArrayList<Double> getDailyacchs() {
		return dailyacchs;
	}

	public void setDailyacchs(ArrayList<Double> dailyacchs) {
		this.dailyacchs = dailyacchs;
	}

	public ArrayList<Double> getDailyaccds() {
		return dailyaccds;
	}

	public void setDailyaccds(ArrayList<Double> dailyaccds) {
		this.dailyaccds = dailyaccds;
	}

	public ArrayList<Double> getDailyfprhs() {
		return dailyfprhs;
	}

	public void setDailyfprhs(ArrayList<Double> dailyfprhs) {
		this.dailyfprhs = dailyfprhs;
	}

	public ArrayList<Double> getDailyfprds() {
		return dailyfprds;
	}

	public void setDailyfprds(ArrayList<Double> dailyfprds) {
		this.dailyfprds = dailyfprds;
	}

	public ArrayList<Double> getDailyprecds() {
		return dailyprecds;
	}

	public void setDailyprecds(ArrayList<Double> dailyprecds) {
		this.dailyprecds = dailyprecds;
	}

	public ArrayList<Double> getDailyprechs() {
		return dailyprechs;
	}

	public void setDailyprechs(ArrayList<Double> dailyprechs) {
		this.dailyprechs = dailyprechs;
	}

	public ArrayList<Double> getDailyfds() {
		return dailyfds;
	}

	public void setDailyfds(ArrayList<Double> dailyfds) {
		this.dailyfds = dailyfds;
	}

	public ArrayList<Double> getDailyfhs() {
		return dailyfhs;
	}

	public void setDailyfhs(ArrayList<Double> dailyfhs) {
		this.dailyfhs = dailyfhs;
	}

	public ArrayList<Double> getDailytprds() {
		return dailytprds;
	}

	public void setDailytprds(ArrayList<Double> dailytprds) {
		this.dailytprds = dailytprds;
	}

	public ArrayList<Double> getDailytprhs() {
		return dailytprhs;
	}

	public void setDailytprhs(ArrayList<Double> dailytprhs) {
		this.dailytprhs = dailytprhs;
	}

	
}
