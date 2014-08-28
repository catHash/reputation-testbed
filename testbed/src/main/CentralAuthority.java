/**class CentralAuthority: Links the MainGUI with the Environment. Contains the lists of
 * metric measures used to evaluate the performance of the defense models.
 * 
 */
		
package main;

import GUI.MainGUI;
import GUI.SimulationAnalyzer_Main;
import GUI.ComparisonResults;
import GUI.Display;
import agent.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.*;

import javax.swing.JDialog;
import javax.swing.JFrame;

import defenses.Reece;
import distributions.*;
import environment.*;


public class CentralAuthority implements Runnable {

	private Environment env;
	private String defenseName;
	private String attackName;
	private ArrayList<Buyer> buyerList;
	private BankBalance bankbalance ;
	public ArrayList<Double> defenseTime_day;

	private ArrayList<Double> honest_avgWt;
	private ArrayList<Double> dishonest_avgWt;
	Thread t;
	private HashMap<Integer, Double> dailyRepH;
	private HashMap<Integer, Double> dailyRepDH;
	private HashMap<Integer, Double> dailyRepDiffH;
	private HashMap<Integer, Double> dailyRepDiffDH; 
	private HashMap<Integer, Double> dailyMCCH;
	private HashMap<Integer, Double> dailyMCCDH;
	private HashMap<Integer, Double> dailyFNRH;
	private HashMap<Integer, Double> dailyFNRDH; 
	private HashMap<Integer, Double> dailyAccH;
	private HashMap<Integer, Double> dailyAccDH;
	private HashMap<Integer, Double> dailyFPRH;
	private HashMap<Integer, Double> dailyFPRDH; 
	private HashMap<Integer, Double> dailyPrecH;
	private HashMap<Integer, Double> dailyPrecDH;
	private HashMap<Integer, Double> dailyFH;
	private HashMap<Integer, Double> dailyFDH; 
	private HashMap<Integer, Double> dailyTPRH;
	private HashMap<Integer, Double> dailyTPRDH;
	private HashMap<Integer, Double> robustness;

	private String realdata_filename;
	public static int curAttack = -1;
	public static HashMap outputResult = new HashMap(); 
	SimulationAnalyzer_Main analysis = new SimulationAnalyzer_Main();
	ArrayList transactionList = null;
	Display display;
	boolean check = true;

	private double[][][][] results = new double[MainGUI.selectedAttack.size()][MainGUI.selectedDetect.size()][17][Parameter.NO_OF_DAYS+1];
	private double [][][][] stds = new double[MainGUI.selectedAttack.size()][MainGUI.selectedDetect.size()][17][Parameter.NO_OF_DAYS+1];


	private HashMap<String, DailyResults> dailyResults = new HashMap<String, DailyResults>();

	public CentralAuthority(){ 
		Thread t = new Thread(this);
		t.start();
		robustness = new HashMap<Integer, Double>();
		dailyRepH = new HashMap<Integer, Double>();
		dailyRepDH = new HashMap<Integer, Double>();
		dailyRepDiffH = new HashMap<Integer, Double>();
		dailyRepDiffDH = new HashMap<Integer, Double>();
		dailyMCCH = new HashMap<Integer, Double>();
		dailyMCCDH = new HashMap<Integer, Double>();
		defenseTime_day = new ArrayList<Double>();
		honest_avgWt = new ArrayList<Double>();
		dishonest_avgWt = new ArrayList<Double>();
		dailyFNRH = new HashMap<Integer, Double>();
		dailyFNRDH = new HashMap<Integer, Double>();
		dailyAccH = new HashMap<Integer, Double>();
		dailyAccDH = new HashMap<Integer, Double>();
		dailyFPRH = new HashMap<Integer, Double>();
		dailyFPRDH = new HashMap<Integer, Double>();
		dailyPrecH = new HashMap<Integer, Double>();
		dailyPrecDH = new HashMap<Integer, Double>();
		dailyFH = new HashMap<Integer, Double>();
		dailyFDH = new HashMap<Integer, Double>();
		dailyTPRH = new HashMap<Integer, Double>();
		dailyTPRDH = new HashMap<Integer, Double>();
		for(int i=0; i<Parameter.NO_OF_DAYS+1; i++){
			defenseTime_day.add(i,0.0);
			dishonest_avgWt.add(i,0.0);
			honest_avgWt.add(i,0.0);
		}
		bankbalance = new BankBalance();
	}





 /* Stores the results obtained from the different evaluation metrics for 
  * attack and defense models on a daily basis.
  */   
	public void updateDailyResults(int day, String attackName, int noOfRuns, int defense, int attack, String defenseName, List<String> evaluateName){
		bankbalance.updateDailyBankBalance(day, buyerList);

		results[attack][defense][0][day] = robustness.get(day);
		results[attack][defense][1][day] = dailyRepDiffDH.get(day);
		results[attack][defense][2][day] = dailyRepDiffH.get(day)	;		
		results[attack][defense][3][day] = dailyMCCDH.get(day);
		results[attack][defense][4][day] =dailyMCCH.get(day)	;	
		results[attack][defense][5][day] = dailyFNRDH.get(day);
		results[attack][defense][6][day] = dailyFNRH.get(day)	;		
		results[attack][defense][7][day] = dailyAccDH.get(day);
		results[attack][defense][8][day] =dailyAccH.get(day)	;	
		results[attack][defense][9][day] = dailyFPRDH.get(day);
		results[attack][defense][10][day] = dailyFPRH.get(day)	;		
		results[attack][defense][11][day] = dailyPrecDH.get(day);
		results[attack][defense][12][day] =dailyPrecH.get(day)	;	
		results[attack][defense][13][day] = dailyFDH.get(day);
		results[attack][defense][14][day] = dailyFH.get(day)	;		
		results[attack][defense][15][day] = dailyTPRDH.get(day);
		results[attack][defense][16][day] =dailyTPRH.get(day)	;	
		if(day>0){
			stds[attack][defense][0][day] = Math.sqrt((robustness.get(day-1) - robustness.get(day)) * (robustness.get(day-1) - robustness.get(day))); 
			stds[attack][defense][1][day] = Math.sqrt((dailyRepDiffDH.get(day-1) - dailyRepDiffDH.get(day)) * (dailyRepDiffDH.get(day-1) - dailyRepDiffDH.get(day))); 
			stds[attack][defense][2][day] = Math.sqrt((dailyRepDiffH.get(day-1) - dailyRepDiffH.get(day)) * (dailyRepDiffH.get(day-1) - dailyRepDiffH.get(day))); 
			stds[attack][defense][3][day] = Math.sqrt((dailyMCCDH.get(day-1) - dailyMCCDH.get(day)) * (dailyMCCDH.get(day-1) - dailyMCCDH.get(day))); 
			stds[attack][defense][4][day] = Math.sqrt((dailyMCCH.get(day-1) - dailyMCCH.get(day)) * (dailyMCCH.get(day-1) - dailyMCCH.get(day))); 
			stds[attack][defense][5][day] = Math.sqrt((dailyFNRDH.get(day-1) - dailyFNRDH.get(day)) * (dailyFNRDH.get(day-1) - dailyFNRDH.get(day))); 
			stds[attack][defense][6][day] = Math.sqrt((dailyFNRH.get(day-1) - dailyFNRH.get(day)) * (dailyFNRH.get(day-1) - dailyFNRH.get(day))); 
			stds[attack][defense][7][day] = Math.sqrt((dailyAccDH.get(day-1) - dailyAccDH.get(day)) * (dailyAccDH.get(day-1) - dailyAccDH.get(day))); 
			stds[attack][defense][8][day] = Math.sqrt((dailyAccH.get(day-1) - dailyAccH.get(day)) * (dailyAccH.get(day-1) - dailyAccH.get(day))); 
			stds[attack][defense][9][day] = Math.sqrt((dailyFPRDH.get(day-1) - dailyFPRDH.get(day)) * (dailyFPRDH.get(day-1) - dailyFPRDH.get(day))); 
			stds[attack][defense][10][day] = Math.sqrt((dailyFPRH.get(day-1) - dailyFPRH.get(day)) * (dailyFPRH.get(day-1) - dailyFPRH.get(day))); 
			stds[attack][defense][11][day] = Math.sqrt((dailyPrecDH.get(day-1) - dailyPrecDH.get(day)) * (dailyPrecDH.get(day-1) - dailyPrecDH.get(day))); 
			stds[attack][defense][12][day] = Math.sqrt((dailyPrecH.get(day-1) - dailyPrecH.get(day)) * (dailyPrecH.get(day-1) - dailyPrecH.get(day))); 
			stds[attack][defense][13][day] = Math.sqrt((dailyFDH.get(day-1) - dailyFDH.get(day)) * (dailyFDH.get(day-1) - dailyFDH.get(day))); 
			stds[attack][defense][14][day] = Math.sqrt((dailyFH.get(day-1) - dailyFH.get(day)) * (dailyFH.get(day-1) - dailyFH.get(day))); 
			stds[attack][defense][15][day] = Math.sqrt((dailyTPRH.get(day-1) - dailyTPRDH.get(day)) * (dailyTPRDH.get(day-1) - dailyTPRDH.get(day))); 
			stds[attack][defense][16][day] = Math.sqrt((dailyTPRH.get(day-1) - dailyTPRH.get(day)) * (dailyTPRH.get(day-1) - dailyTPRH.get(day))); 
		}
		String key = defenseName + "_" + attackName;

		//for results of each runtime
		if(day ==0){
			dailyResults.get(key).setDailyrobustness(new ArrayList<Double>());
			dailyResults.get(key).setDailymaeds(new ArrayList<Double>());
			dailyResults.get(key).setDailymaehs(new ArrayList<Double>());
			dailyResults.get(key).setDailymccds(new ArrayList<Double>());
			dailyResults.get(key).setDailymcchs(new ArrayList<Double>());
			dailyResults.get(key).setDailyfnrds(new ArrayList<Double>());
			dailyResults.get(key).setDailyfnrhs(new ArrayList<Double>());
			dailyResults.get(key).setDailyaccds(new ArrayList<Double>());
			dailyResults.get(key).setDailyacchs(new ArrayList<Double>());
			dailyResults.get(key).setDailyfprds(new ArrayList<Double>());
			dailyResults.get(key).setDailyfprhs(new ArrayList<Double>());
			dailyResults.get(key).setDailyprecds(new ArrayList<Double>());
			dailyResults.get(key).setDailyprechs(new ArrayList<Double>());
			dailyResults.get(key).setDailyfds(new ArrayList<Double>());
			dailyResults.get(key).setDailyfhs(new ArrayList<Double>());
			dailyResults.get(key).setDailytprds(new ArrayList<Double>());
			dailyResults.get(key).setDailytprhs(new ArrayList<Double>());

		}
		dailyResults.get(key).getDailyrobustness().add(robustness.get(day));
		dailyResults.get(key).getDailymaeds().add(dailyRepDiffDH.get(day));
		dailyResults.get(key).getDailymaehs().add(dailyRepDiffH.get(day));
		dailyResults.get(key).getDailymccds().add(dailyMCCDH.get(day));
		dailyResults.get(key).getDailymcchs().add(dailyMCCH.get(day));
		dailyResults.get(key).getDailyfnrds().add(dailyFNRDH.get(day));
		dailyResults.get(key).getDailyfnrhs().add(dailyFNRH.get(day));
		dailyResults.get(key).getDailyaccds().add(dailyAccDH.get(day));
		dailyResults.get(key).getDailyacchs().add(dailyAccH.get(day));
		dailyResults.get(key).getDailyfprds().add(dailyFPRDH.get(day));
		dailyResults.get(key).getDailyfprhs().add(dailyFPRH.get(day));
		dailyResults.get(key).getDailyprecds().add(dailyPrecDH.get(day));
		dailyResults.get(key).getDailyprechs().add(dailyPrecH.get(day));
		dailyResults.get(key).getDailyfds().add(dailyFDH.get(day));
		dailyResults.get(key).getDailyfhs().add(dailyFH.get(day));
		dailyResults.get(key).getDailytprds().add(dailyTPRDH.get(day));
		dailyResults.get(key).getDailytprhs().add(dailyTPRH.get(day));

		//for overall results
		dailyResults.get(key).getRobustness().set(day, dailyResults.get(key).getRobustness().get(day) + robustness.get(day));
		dailyResults.get(key).getMaeds().set(day, dailyResults.get(key).getMaeds().get(day) + dailyRepDiffDH.get(day));
		dailyResults.get(key).getMaehs().set(day, dailyResults.get(key).getMaehs().get(day) + dailyRepDiffH.get(day));
		dailyResults.get(key).getMccds().set(day, dailyResults.get(key).getMccds().get(day) + dailyMCCDH.get(day));
		dailyResults.get(key).getMcchs().set(day, dailyResults.get(key).getMcchs().get(day) + dailyMCCH.get(day));
		dailyResults.get(key).getFnrds().set(day, dailyResults.get(key).getFnrds().get(day) + dailyFNRDH.get(day));
		dailyResults.get(key).getFnrhs().set(day, dailyResults.get(key).getFnrhs().get(day) + dailyFNRH.get(day));
		dailyResults.get(key).getAccds().set(day, dailyResults.get(key).getAccds().get(day) + dailyAccDH.get(day));
		dailyResults.get(key).getAcchs().set(day, dailyResults.get(key).getAcchs().get(day) + dailyAccH.get(day));
		dailyResults.get(key).getFprds().set(day, dailyResults.get(key).getFprds().get(day) + dailyFPRDH.get(day));
		dailyResults.get(key).getFprhs().set(day, dailyResults.get(key).getFprhs().get(day) + dailyFPRH.get(day));
		dailyResults.get(key).getPrecds().set(day, dailyResults.get(key).getPrecds().get(day) + dailyPrecDH.get(day));
		dailyResults.get(key).getPrechs().set(day, dailyResults.get(key).getPrechs().get(day) + dailyPrecH.get(day));
		dailyResults.get(key).getFds().set(day, dailyResults.get(key).getFds().get(day) + dailyFDH.get(day));
		dailyResults.get(key).getFhs().set(day, dailyResults.get(key).getFhs().get(day) + dailyFH.get(day));
		dailyResults.get(key).getTprds().set(day, dailyResults.get(key).getTprds().get(day) + dailyTPRDH.get(day));
		dailyResults.get(key).getTprhs().set(day, dailyResults.get(key).getTprhs().get(day) + dailyTPRH.get(day));
	}

	private String formatResults(double result){
		if(result>=0){
			DecimalFormat df = new DecimalFormat("0.00000000000000000");			
			return df.format(result);
		}
		else
		{
			DecimalFormat df = new DecimalFormat("0.0000000000000000");			
			return df.format(result);
		}
	}


	// method to invoke the attack, based on the attack model selected by the user.
	public void attack(int day){
		int numBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		//Attack model (dishonest buyers), give rating/ perform attack		
		if(!attackName.equalsIgnoreCase("noattack")){
			for(int i = 0; i < numBuyers; i++){
				int bid = i;
				if(buyerList.get(bid).isIshonest() == false){
					buyerList.get(bid).rateSeller(day);
				}
			}
			for(int i = 0; i < numBuyers; i++){
				int bid = i;
				if(buyerList.get(bid).isIshonest() == false){
					buyerList.get(bid).perform_model(day);
				}
			}
		}
	}

	//method to invoke the defense model selected by the user.
	public void defense(int day){
		int numBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		//Attack model (dishonest buyers), give rating/ perform defense				
		for(int i = 0; i < numBuyers; i++){
			int bid = i;
			if(buyerList.get(bid).isIshonest() == true){
				buyerList.get(bid).rateSeller(day);
			}
		}
		for(int i = 0; i < numBuyers; i++){
			int bid = i;
			if(buyerList.get(bid).isIshonest() == true){
				buyerList.get(bid).perform_model(day);
			}
		}
	}

	public void avgerWeights(int day){

		int db = Parameter.NO_OF_DISHONEST_BUYERS;
		int hb = Parameter.NO_OF_HONEST_BUYERS; 

		if(Parameter.includeWhitewashing()){
			db = db + (day) * db;
		}		

		//these code for trust models: trustworthiness for local/partial advisors;
		if(Parameter.includeWMA(defenseName) || Parameter.includeEA(defenseName)){
			int numDA = 0; //number of dishonest advisors;
			int numHA = 0; //number of honest advisors
			for(int i = Parameter.NO_OF_DISHONEST_BUYERS; i < Parameter.NO_OF_DISHONEST_BUYERS + hb; i++){
				int bid = i;
				ArrayList<Double> weights_BA = buyerList.get(bid).getTrusts();
				Vector<Integer> advisors = buyerList.get(bid).getAdvisors();
				for(int j=0; j<advisors.size(); j++){
					int aid = advisors.get(j);
					if(aid == bid)continue;
					if(aid < Parameter.NO_OF_DISHONEST_BUYERS || aid >= Parameter.NO_OF_DISHONEST_BUYERS + hb){
						dishonest_avgWt.set(day, dishonest_avgWt.get(day) + weights_BA.get(aid));
						numDA++;
					} else{
						honest_avgWt.set(day, honest_avgWt.get(day) + weights_BA.get(aid));
						numHA++;
					}
				}
			}
			if (numDA != 0) {
				dishonest_avgWt.set(day,(dishonest_avgWt.get(day)/ (numDA)));
			}           
			if (numHA != 0) {
				honest_avgWt.set(day,(honest_avgWt.get(day)/(numHA)));
			}
		}
		else{
			//these code for trust models: trustworthiness for all advisors;
			int numDA = 0;
			int numHA = 0;
			for(int i = Parameter.NO_OF_DISHONEST_BUYERS; i < Parameter.NO_OF_DISHONEST_BUYERS + hb; i++){
				int bid = i;      
				for (int k = 0; k < Parameter.TOTAL_NO_OF_SELLERS; k++) {                
					int sid = k;
					double[] SaverTA = buyerList.get(bid).getAverageTrusts(sid);
					if(SaverTA[0] >= 0){//dishonest advisors
						dishonest_avgWt.set(day, dishonest_avgWt.get(day) + SaverTA[0]);
						numDA++;
					}
					if(SaverTA[1] >= 0){//dishonest advisors
						honest_avgWt.set(day, honest_avgWt.get(day) + SaverTA[1]);
						numHA++;
					}
				}
			}       
			if (numDA != 0) {
				dishonest_avgWt.set(day,(dishonest_avgWt.get(day)/ (numDA*Parameter.NO_OF_DISHONEST_SELLERS)));
			}           
			if (numHA != 0) {
				honest_avgWt.set(day,(honest_avgWt.get(day)/ (numHA*Parameter.NO_OF_HONEST_SELLERS)));
			}   
		}       

	}
	public void evaluateDefenses(ArrayList<String> defenseNames, ArrayList<String> attackNames, List<String> evaluateName, String realdata) throws Exception,ClassNotFoundException, NoSuchMethodException, SecurityException{
		HashMap<String, ArrayList<Double>> overallResults = new HashMap<String, ArrayList<Double>>();
		File theDir = new File("data");

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			boolean result = theDir.mkdir();  
			if(result) {    
				System.out.println("DIR created");  
			}
		}

		File theDir2 = new File("data/results");

		// if the directory does not exist, create it
		if (!theDir2.exists()) {
			boolean result = theDir2.mkdir();  
			if(result) {    
				System.out.println("DIR created");  
			}
		}
		int runtimes = Parameter.NO_OF_RUNTIMES;

		double[][][][] results = new double[runtimes][defenseNames.size()][attackNames.size()][2];
		String key = "";
		for(int i = 0; i < runtimes; i++){
			for(int j = 0; j < defenseNames.size(); j++){  
				for(int k = 0; k < attackNames.size(); k++){
					System.err.print("  runtimes = " + i + ",   defense = " + defenseNames.get(j) + ",   attack = " + attackNames.get(k));

					dailyRepH = new HashMap<Integer, Double>();
					dailyRepDH = new HashMap<Integer, Double>();
					dailyRepDiffH = new HashMap<Integer, Double>();
					dailyRepDiffDH = new HashMap<Integer, Double>();

					robustness = new HashMap<Integer, Double>();
					honest_avgWt = new ArrayList<Double>();
					dishonest_avgWt = new ArrayList<Double>();
					dailyMCCH = new HashMap<Integer, Double>();
					dailyMCCDH = new HashMap<Integer, Double>();
					dailyFNRH = new HashMap<Integer, Double>();
					dailyFNRDH = new HashMap<Integer, Double>();
					dailyAccH = new HashMap<Integer, Double>();
					dailyAccDH = new HashMap<Integer, Double>();
					dailyFPRH = new HashMap<Integer, Double>();
					dailyFPRDH = new HashMap<Integer, Double>();
					dailyPrecH = new HashMap<Integer, Double>();
					dailyPrecDH = new HashMap<Integer, Double>();
					dailyFH = new HashMap<Integer, Double>();
					dailyFDH = new HashMap<Integer, Double>();
					dailyTPRH = new HashMap<Integer, Double>();
					dailyTPRDH = new HashMap<Integer, Double>();
					for(int i1=0; i1<Parameter.NO_OF_DAYS+1; i1++){
						defenseTime_day.add(i1,0.0);
						dishonest_avgWt.add(i1,0.0);
						honest_avgWt.add(i1,0.0);
					}
					bankbalance = new BankBalance();
					if (realdata != null){
						env = new EnvironmentR(this);
						env.setCheck(true);
					}
					else {
						env = new EnvironmentS();
						env.setCheck(false);
					}
					key = defenseNames.get(j)+"_"+attackNames.get(k);
					if(dailyResults.get(key) == null){

						dailyResults.put(key, new DailyResults());
					}
					ArrayList<Double> metrics = new ArrayList<Double>();
					for(int p=0; p<16; p++){
						metrics.add(0.0);
					}
					overallResults.put(key, metrics);

					long start = new Date().getTime();
					if(env instanceof EnvironmentS){
						EnvironmentS e = (EnvironmentS) env;
						transactionList = e.simulateEnvironment(this,i, j, k, attackNames.get(k), defenseNames.get(j), false, evaluateName);
					}
					else if(env instanceof EnvironmentR){
						env.eCommerceSetting(i, j, k, evaluateName, attackNames.get(k),defenseNames.get(j),realdata);
					}				
					long end = new Date().getTime();
					System.err.println("   time =  " + (end - start)/1000.0 + " s");

					if(env.isCheck()==false){
						overallResults.get(key).set(0, overallResults.get(key).get(0) + dailyRepDiffDH.get(Parameter.NO_OF_DAYS));
						overallResults.get(key).set(1, overallResults.get(key).get(1) +dailyRepDiffH.get(Parameter.NO_OF_DAYS));
						overallResults.get(key).set(2, overallResults.get(key).get(2) + env.getMcc().getDailyMCC(env.getDay()-1).get(0)); 
						overallResults.get(key).set(3, overallResults.get(key).get(3) + env.getMcc().getDailyMCC(env.getDay()-1).get(1));
						overallResults.get(key).set(4, overallResults.get(key).get(4) + env.getMcc().getDailyFNR(env.getDay()-1).get(0));
						overallResults.get(key).set(5, overallResults.get(key).get(5) + env.getMcc().getDailyFNR(env.getDay()-1).get(1));
						overallResults.get(key).set(6, overallResults.get(key).get(6) + env.getMcc().getDailyFPR(env.getDay()-1).get(0)); 
						overallResults.get(key).set(7, overallResults.get(key).get(7) + env.getMcc().getDailyFPR(env.getDay()-1).get(1));
						overallResults.get(key).set(8, overallResults.get(key).get(8) + env.getMcc().getDailyAcc(env.getDay()-1).get(0));
						overallResults.get(key).set(9, overallResults.get(key).get(9) + env.getMcc().getDailyAcc(env.getDay()-1).get(1));
						overallResults.get(key).set(10, overallResults.get(key).get(10) + env.getMcc().getDailyPrec(env.getDay()-1).get(0)); 
						overallResults.get(key).set(11, overallResults.get(key).get(11) + env.getMcc().getDailyPrec(env.getDay()-1).get(1));
						overallResults.get(key).set(12, overallResults.get(key).get(12) + env.getMcc().getDailyF(env.getDay()-1).get(0)) ;
						overallResults.get(key).set(13, overallResults.get(key).get(13) + env.getMcc().getDailyF(env.getDay()-1).get(1));
						overallResults.get(key).set(14, overallResults.get(key).get(14) + env.getMcc().getDailyTPR(env.getDay()-1).get(0)) ;
						overallResults.get(key).set(15, overallResults.get(key).get(15) + env.getMcc().getDailyTPR(env.getDay()-1).get(1));
						if(i==(Parameter.NO_OF_RUNTIMES-1)){
							overallResults(overallResults.get(key));
						}
					}
					results[i][j][k] = generate_report(i); //report include the run count
				}
			}
			
		}
		getDailyStatistic(defenseNames, attackNames, evaluateName);
		getStatistic(results, defenseNames, attackNames, evaluateName);
	}

	//displays relevant ouputs on the console and generates necessary files.
	public double[] generate_report(int runCount){

		try{
			PrintWriter pw;

			String pathName = "output";
			if(Parameter.RATING_TYPE.equalsIgnoreCase("binary")){
				pathName = "outputB";
			}else if(Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")){
				pathName = "outputM";
			}else if(Parameter.RATING_TYPE.equalsIgnoreCase("real")){
				pathName = "outputR";
			}
			String outputPath = pathName + "/" + defenseName ;
			File experimentDirectory = new File(outputPath);
			if (!experimentDirectory.exists()) {
				boolean result = new File(outputPath).mkdirs();
			}
			String outputName = experimentDirectory + "/" + defenseName + "2" + attackName + "Head.txt";			
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outputName)));

			double[] results = new double[17];
			results[0] = robustness.get(robustness.size()-1);
			results[1] = dailyRepDiffDH.get(dailyRepDiffDH.size()-1);
			results[2] = dailyRepDiffH.get(dailyRepDiffH.size()-1);		
			results[3] = dailyMCCDH.get(dailyMCCDH.size()-1);
			results[4] =dailyMCCH.get(dailyMCCH.size()-1)	;	
			results[5] = dailyFNRDH.get(dailyFNRDH.size()-1);
			results[6] = dailyFNRH.get(dailyFNRH.size()-1);		
			results[7] = dailyAccDH.get(dailyAccDH.size()-1);
			results[8] =dailyAccH.get(dailyAccH.size()-1)	;	
			results[9] = dailyFPRDH.get(dailyFPRDH.size()-1);
			results[10] = dailyFPRH.get(dailyFPRH.size()-1);		
			results[11] = dailyPrecDH.get(dailyPrecDH.size()-1);
			results[12] =dailyPrecH.get(dailyPrecH.size()-1)	;	
			results[13] = dailyFDH.get(dailyFDH.size()-1);
			results[14] = dailyFH.get(dailyFH.size()-1);		
			results[15] = dailyTPRDH.get(dailyTPRDH.size()-1);
			results[16] =dailyTPRH.get(dailyTPRH.size()-1)	;	

			pw.println("Attack: " + attackName + " Defense: " + defenseName);
			pw.println();
			pw.println("=======================PERFORMANCE=========================");		
			pw.println("target honest sales: " + env.getRobustness().getNoOfTrans_HS().get(Parameter.NO_OF_DAYS));
			pw.println("target dishonest sales: " + env.getRobustness().getNoOfTrans_DS().get(Parameter.NO_OF_DAYS));
			pw.println("target dishonest:honest sales: " + env.getRobustness().getNoOfTrans_DS().get(Parameter.NO_OF_DAYS) + ":" + env.getRobustness().getNoOfTrans_HS().get(Parameter.NO_OF_DAYS));
			pw.println("target dishonest-honest sales: " + (env.getRobustness().getNoOfTrans_DS().get(Parameter.NO_OF_DAYS) - env.getRobustness().getNoOfTrans_HS().get(Parameter.NO_OF_DAYS)));			
			pw.println();	
			pw.println("======================PARAMETERS==========================");				
			pw.println("NO_OF_DISHONEST_BUYERS             : " + Parameter.NO_OF_DISHONEST_BUYERS);
			pw.println("NO_OF_HONEST_BUYERS                : " + Parameter.NO_OF_HONEST_BUYERS);			
			pw.println("NO_OF_DISHONEST_SELLERS            : " + Parameter.NO_OF_DISHONEST_SELLERS);
			pw.println("NO_OF_HONEST_SELLERS               : " + Parameter.NO_OF_HONEST_SELLERS);
			pw.println("NO_OF_DAYS                         : " + Parameter.NO_OF_DAYS);						
			pw.println("TARGET_DISHONEST_SELLER            : " + Parameter.TARGET_DISHONEST_SELLER);
			pw.println("TARGET_HONEST_SELLER               : " + Parameter.TARGET_HONEST_SELLER);
			pw.println("honestBuyerOntargetSellerRatio     : " + Parameter.m_honestBuyerOntargetSellerRatio);
			pw.println("dishonestBuyerOntargetSellerRatio  : " + Parameter.m_dishonestBuyerOntargetSellerRatio);
			pw.println();
			pw.println("=======================DAILY INFO=========================");	
			pw.println("sellers' sale");	
			pw.println("==========================================================");
			pw.close();					



			outputName = pathName + "/" + defenseName + "2" + attackName + ".txt";
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outputName, true)));			
			pw.println(results[0] + "  " + results[1] + "   " + results[2]+ "  " + results[3]+ "  " + results[4] + "   " + results[5]);			
			pw.close();

			//output the detail sellers' sale
			outputName = pathName + "/" + defenseName + "/" + defenseName;			
			outputName += "2" + attackName + runCount;		
			outputName += "Detail";
			//Detail 1: |transactions|
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outputName + "_trans.txt")));
			for(int j = 0; j < Parameter.NO_OF_DAYS; j++){
				for (int i = 0; i < Parameter.NO_OF_HONEST_SELLERS	+ Parameter.NO_OF_DISHONEST_SELLERS; i++) {											
					pw.print(env.getSellerList().get(i).getDailySales().get(j) + " ");
				}
				pw.println();
			}			
			pw.close();
			//Detail 2: MAE
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outputName + "_MAE.txt")));
			for(int j = 0; j <= Parameter.NO_OF_DAYS; j++){	
				pw.print(dailyRepDH.get(j) + " " );
				pw.print(dailyRepH.get(j) + " " );
				pw.print(dailyRepDiffDH.get(j) + " " );
				pw.print(dailyRepDiffH.get(j) + " " );
				pw.print( dishonest_avgWt.get(j) + "   ");
				pw.print( honest_avgWt.get(j)+ "   ");
				pw.println();
			}			
			pw.close();	

			return results;
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}

		return null;
	}

    //returns statistical information on the different evaluation metrics
	private void getDailyStatistic(ArrayList<String>defenseName, ArrayList<String>attackName, List<String> evaluateName){
		int noIndicators = 17;
		double [][][][] means = new double[attackName.size()][defenseName.size()][noIndicators][Parameter.NO_OF_DAYS];
		int day=0;
		means = results;
		do{
			String hmKey = "";
			ComparisonResults cr = null;
			for(int j=0; j<attackName.size(); j++){
				for(int i = 0; i<evaluateName.size(); i++){
					int l = -1;
					if(evaluateName.get(i).equalsIgnoreCase("Robustness ([-1,1])")){
						l = 0;
					}  
					else if(evaluateName.get(i).equalsIgnoreCase("MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])")){
						l = 1;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase("MAE-HS repDIff(reputation difference of honest seller ([0, 1])")){
						l = 2;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])")){
						l = 3;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
						l = 4;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])")){
						l = 5;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
						l = 6;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])")){
						l = 7;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
						l = 8;
					}

					else if(evaluateName.get(i).equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])")){
						l = 9;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
						l = 10;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])")){
						l = 11;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
						l = 12;
					}

					else if(evaluateName.get(i).equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
						l = 13;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
						l = 14;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])")){
						l = 15;
					}
					else if(evaluateName.get(i).equalsIgnoreCase("TPR-HS (Classification of honest seller ([0,1])")){
						l = 16;
					}
					hmKey = attackName.get(j)+"_"+l;
					cr = new ComparisonResults();

					for(int k=0; k<defenseName.size(); k++){
						cr.getTrustModelList().add(defenseName.get(k));					
					}
					outputResult.put(hmKey, cr);
				}
			}

			Object[] possibilities = {
					"Robustness ([-1,1])",
					"MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])",
					"MAE-HS repDiff(reputation difference of honest seller ([0, 1])", 
					"MCC-DS (Classification of dishonest seller ([-1,1])",
					"MCC-HS (Classification of honest seller ([-1,1])",		
					"FNR-DS (Classification of dishonest seller ([0,1])",
					"FNR-HS (Classification of honest seller ([0,1])"	,
					"Accuracy-DS (Classification of dishonest seller ([0,1])",
					"Accuracy-HS (Classification of honest seller ([0,1])"	,
					"FPR-DS (Classification of dishonest seller ([0,1])",
					"FPR-HS (Classification of honest seller ([0,1])"	,
					"Precision-DS (Classification of dishonest seller ([0,1])",
					"Precision-HS (Classification of honest seller ([0,1])",	
					"F-Measure-DS (Classification of dishonest seller ([0,1])",
					"F-Measure-HS (Classification of honest seller ([0,1])",	
					"TPR-DS (Classification of dishonest seller ([0,1])",
					"TPR-HS (Classification of honest seller ([0,1])"	
			};

			for(int i = 0; i<evaluateName.size(); i++){
				int l = -1; String evalName = evaluateName.get(i);
				if(evaluateName.get(i).equalsIgnoreCase(possibilities[0].toString())){
					l = 0; 
				} 
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[1].toString())){
					l = 1;
				} 
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[2].toString())){
					l = 2;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[3].toString())){
					l = 3;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[4].toString())){
					l = 4;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[5].toString())){
					l = 5;
				} 
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[6].toString())){
					l = 6;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[7].toString())){
					l = 7;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[8].toString())){
					l = 8;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[9].toString())){
					l = 9;
				} 
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[10].toString())){
					l = 10;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[11].toString())){
					l = 11;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[12].toString())){
					l = 12;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[13].toString())){
					l = 13;
				} 
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[14].toString())){
					l = 14;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[15].toString())){
					l = 15;
				}
				else if(evaluateName.get(i).equalsIgnoreCase(possibilities[16].toString())){
					l = 16;
				}


				analysis.setResult(true);
				for(int k=0; k<attackName.size(); k++){
					cr = (ComparisonResults) outputResult.get(attackName.get(k)+"_"+l);

					
					for (int m = 0; m < cr.getTrustModelList().size(); m++) {
						
						int size = dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().size();
						DecimalFormat df = new DecimalFormat("#.###");
						if(Double.isNaN(means[k][m][l][day])){
							means[k][m][l][day]=0.0;
						}
						if(Double.isNaN(stds[k][m][l][day])){
							stds[k][m][l][day]=0.0;
						}
						cr.getMeanList().add( df.format(means[k][m][l][day]));
						cr.getVarList().add( df.format(stds[k][m][l][day]));
						if(l==0){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().size() <=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().size()-1)));
								}
								else{ 
									
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyrobustness().get(g)));
								}
							}
						}
						else if(l==1){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaeds().get(g)));
								
								}
							}
						}
						else if(l==2){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().size() <= g){
									
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymaehs().get(g)));
								
								}
							}
						}
						else if(l==3){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymccds().get(g)));
								
								}
							}
						}
						else if(l==4){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailymcchs().get(g)));
								
								}
							}
						}
						else if(l==5){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().size()-1)){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrds().get(g)));
								}
							}
						}
						else if(l==6){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfnrhs().get(g)));
							
								}
							}
						}

						else if(l==7){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().size()-1)));
								}
								else{ 
									
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyaccds().get(g)));
								}
							}
						}
						else if(l==8){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyacchs().get(g)));
								
								}
							}
						}

						else if(l==9){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprds().get(g)));
								}
							}
						}
						else if(l==10){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfprhs().get(g)));
								
								}
							}
						}

						else if(l==11){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().size()-1)));
								}
								else{ 
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprecds().get(g)));
								
								}
							}
						}
						else if(l==12){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyprechs().get(g)));
								
								}
							}
						}

						else if(l==13){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfds().get(g)));
								}
								
							}
						}
						else if(l==14){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailyfhs().get(g)));
								}
								
							}
						}

						else if(l==15){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprds().get(g)));
								}
								
							}
						}
						else if(l==16){
							for(int g=0; g<=day; g++){
								if(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().size()<=g){
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().size()-1))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().get(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().size()-1)));
								}
								else{
									if(Double.isNaN(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().get(g))){
										cr.getDailyResults().get(i).add(df.format(0));
									}
									else
									cr.getDailyResults().get(i).add(df.format(dailyResults.get(defenseName.get(m)+"_"+attackName.get(k)).getDailytprhs().get(g)));
								}
								
							}
						}
					System.out.println(means[k][m][l][day]);
						analysis.setChartTableData(day, m, k, evalName, means[k][m][l][day], stds[k][m][l][day], false);
					}

				}		
			}
			if(check==true){

				t = new Thread() {
					@Override
					public void run() {  // override the run() to specify the running behavior
						try {
							display = new Display("Visualization Module" , analysis);
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				t.start();

				check = false;
			}
			else{
				display.revalidate();
			}

			day++;
			try {
				Thread.sleep(200);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		} while(day<=Parameter.NO_OF_DAYS);

	}

	//returns statistical information of the different evaluation metrics
	private void getStatistic(double[][][][] results, ArrayList<String> defenseNames, ArrayList<String> attackNames, List<String> evaluateName){
		//		results : [run times][defense][attack] [|transactions|, time]
		int runtimes = Parameter.NO_OF_RUNTIMES;
		int noIndicators = 17;
		double[][][] means = new double[defenseNames.size()][attackNames.size()][noIndicators];
		double[][][] stds = new double[defenseNames.size()][attackNames.size()][noIndicators];
		//get the mean and standard deviation
		System.out.println(runtimes);

		for(int i = 0; i < runtimes; i++){
			for(int j = 0; j < defenseNames.size(); j++){			
				for(int k = 0; k < attackNames.size(); k++){
					for(int l = 0; l < noIndicators; l++){
						means[j][k][l] += results[i][j][k][l];
					}					
				}
			}
		}

		for(int i = 0; i < runtimes; i++){
			for(int j = 0; j < defenseNames.size(); j++){			
				for(int k = 0; k < attackNames.size(); k++){
					for(int l = 0; l < noIndicators; l++){
						if (i==0)
							means[j][k][l] /=  runtimes;
						stds[j][k][l] += (results[i][j][k][l] - means[j][k][l]) * (results[i][j][k][l] - means[j][k][l]);
					}
				}
			}
		}

		String hmKey = "";
		ComparisonResults cr = null;
		for (int a = 0; a < attackNames.size(); a++) {
			for(int i = 0; i<evaluateName.size(); i++){
				int l = -1;
				if(evaluateName.get(i).equalsIgnoreCase("Robustness ([-1,1])")){
					l = 0;
				}  
				else if(evaluateName.get(i).equalsIgnoreCase("MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])")){
					l = 1;
				} else if(evaluateName.get(i).equalsIgnoreCase("MAE-HS repDIff(reputation difference of honest seller ([0, 1])")){
					l = 2;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("MCC-DS (Classification of dishonest seller ([-1,1])")){
					l = 3;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("MCC-HS (Classification of honest seller ([-1,1])")){
					l = 4;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("FNR-DS (Classification of dishonest seller ([0,1])")){
					l = 5;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("FNR-HS (Classification of honest seller ([0,1])")){
					l = 6;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("Accuracy-DS (Classification of dishonest seller ([0,1])")){
					l = 7;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("Accuracy-HS (Classification of honest seller ([0,1])")){
					l = 8;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("FPR-DS (Classification of dishonest seller ([0,1])")){
					l = 9;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("FPR-HS (Classification of honest seller ([0,1])")){
					l = 10;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("Precision-DS (Classification of dishonest seller ([0,1])")){
					l = 11;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("Precision-HS (Classification of honest seller ([0,1])")){
					l = 12;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("F-Measure-DS (Classification of dishonest seller ([0,1])")){
					l = 13;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("F-Measure-HS (Classification of honest seller ([0,1])")){
					l = 14;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("TPR-DS (Classification of dishonest seller ([0,1])")){
					l = 15;
				}
				else if(evaluateName.get(i).equalsIgnoreCase("TPR-HS (Classification of honest seller ([0,1])")){
					l = 16;
				}

				hmKey = attackNames.get(a)+"_"+l;
				cr = new ComparisonResults();
				for (int b = 0; b < defenseNames.size(); b++) {
					cr.getTrustModelList().add(defenseNames.get(b));	
				}
				outputResult.put(hmKey, cr);
			}
		}

		Object[] possibilities = {
				"Robustness ([-1,1])",
				"MAE-DS repDiff(reputation difference of dishonest seller ([0, 1])",
				"MAE-HS repDiff(reputation difference of honest seller ([0, 1])", 
				"MCC-DS (Classification of dishonest seller ([-1,1])",
				"MCC-HS (Classification of honest seller ([-1,1])"	,
				"FNR-DS (Classification of dishonest seller ([0,1])", // *** added by amanda
				"FNR-HS (Classification of honest seller ([0,1])"	,
				"Accuracy-DS (Classification of dishonest seller ([0,1])",
				"Accuracy-HS (Classification of honest seller ([0,1])"	,
				"FPR-DS (Classification of dishonest seller ([0,1])",
				"FPR-HS (Classification of honest seller ([0,1])"	,
				"Precision-DS (Classification of dishonest seller ([0,1])",
				"Precision-HS (Classification of honest seller ([0,1])",	
				"F-Measure-DS (Classification of dishonest seller ([0,1])",
				"F-Measure-HS (Classification of honest seller ([0,1])",	
				"TPR-DS (Classification of dishonest seller ([0,1])",
				"TPR-HS (Classification of honest seller ([0,1])"	
		};

		System.out.printf(String.format("%-16s	", "          "));
		for (int k = 0; k < attackNames.size(); k++) {
			System.out.print(String.format("%-2s	", attackNames.get(k)));
		}
		System.out.println();
		for (int j = 0; j < defenseNames.size(); j++) {
			for (int k = 0; k < attackNames.size(); k++) {	
				String key = defenseNames.get(j)+"_"+attackNames.get(k);
				for(int i = 0; i<evaluateName.size(); i++){
					int l = -1; String evalName = evaluateName.get(i);
					if(evaluateName.get(i).equalsIgnoreCase(possibilities[0].toString())){
						l = 0; 
					}  else if(evaluateName.get(i).equalsIgnoreCase(possibilities[1].toString())){
						l = 1;
					} else if(evaluateName.get(i).equalsIgnoreCase(possibilities[2].toString())){
						l = 2;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[3].toString())){
						l = 3;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[4].toString())){
						l = 4;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[5].toString())){
						l = 5;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[6].toString())){
						l = 6;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[7].toString())){
						l = 7;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[8].toString())){
						l = 8;
					}

					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[9].toString())){
						l = 9;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[10].toString())){
						l = 10;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[11].toString())){
						l = 11;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[12].toString())){
						l = 12;
					}

					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[13].toString())){
						l = 13;
					} 
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[14].toString())){
						l = 14;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[15].toString())){
						l = 15;
					}
					else if(evaluateName.get(i).equalsIgnoreCase(possibilities[16].toString())){
						l = 16;
					}
					DecimalFormat roundoff = new DecimalFormat("0.000");
					if(Double.isNaN(means[j][k][l])){
						means[j][k][l]=0;
					}
					if(Double.isNaN(stds[j][k][l] )){
						stds[j][k][l] =0;
					}
					means[j][k][l] = Double.parseDouble(roundoff.format(means[j][k][l]));
					if(runtimes > 0){
						stds[j][k][l] = Math.sqrt(stds[j][k][l] / (runtimes));
					}
					
					stds[j][k][l] = Double.parseDouble(roundoff.format(stds[j][k][l]));
					//using the "Tab" key to get spaces, it is easy to translate in word
					System.out.printf("%2.2f+%2.2f	", means[j][k][l], stds[j][k][l]);
					analysis.setResult(true);
					analysis.setText("-- Trust Model Used: " + defenseNames.get(j) + " --");
					analysis.setText("-- Attack Model Used: " + attackNames.get(k) + " --");
					analysis.setText("*** Total Rating ***");
					analysis.setText(evaluateName.get(i));
					analysis.setText("Mean: " + String.valueOf(means[j][k][l]) + ", Std. variance: "
							+ String.valueOf(stds[j][k][l]));
					cr = (ComparisonResults) outputResult.get(attackNames.get(k)+"_"+l);
					for (int m = 0; m < cr.getTrustModelList().size(); m++) {
						if (defenseNames.get(j).equalsIgnoreCase((String) cr.getTrustModelList().get(m))) {
							DecimalFormat df = new DecimalFormat("#.###");
							cr.getMeanList().add( df.format(means[j][k][l]));
							cr.getVarList().add( df.format(stds[j][k][l]));

						}
					}
					for(int g=0; g<dailyResults.get(key).getMaeds().size(); g++){
						//get average of runtimes
						DecimalFormat df = new DecimalFormat("#.###");
						if(l==0){
							dailyResults.get(key).getRobustness().set(g, dailyResults.get(key).getRobustness().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getRobustness().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getRobustness().get(g)));
						}
						else if (l==1){
							dailyResults.get(key).getMaeds().set(g, dailyResults.get(key).getMaeds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getMaeds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getMaeds().get(g)));
						}
						else if (l==2){
							dailyResults.get(key).getMaehs().set(g, dailyResults.get(key).getMaehs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getMaehs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getMaehs().get(g)));

						}
						else if (l==3){
							dailyResults.get(key).getMccds().set(g, dailyResults.get(key).getMccds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getMccds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getMccds().get(g)));

						}
						else if (l==4){
							dailyResults.get(key).getMcchs().set(g, dailyResults.get(key).getMcchs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getMcchs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getMcchs().get(g)));

						}

						else if (l==5){
							dailyResults.get(key).getFnrds().set(g, dailyResults.get(key).getFnrds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFnrds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFnrds().get(g)));

						}
						else if (l==6){
							dailyResults.get(key).getFnrhs().set(g, dailyResults.get(key).getFnrhs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFnrhs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFnrhs().get(g)));

						}
						else if (l==7){
							dailyResults.get(key).getAccds().set(g, dailyResults.get(key).getAccds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getAccds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getAccds().get(g)));

						}
						else if (l==8){
							dailyResults.get(key).getAcchs().set(g, dailyResults.get(key).getAcchs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getAcchs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getAcchs().get(g)));

						}
						else if (l==9){
							dailyResults.get(key).getFprds().set(g, dailyResults.get(key).getFprds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFprds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFprds().get(g)));

						}
						else if (l==10){
							dailyResults.get(key).getFprhs().set(g, dailyResults.get(key).getFprhs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFprhs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFprhs().get(g)));

						}
						else if (l==11){
							dailyResults.get(key).getPrecds().set(g, dailyResults.get(key).getPrecds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getPrecds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getPrecds().get(g)));

						}
						else if (l==12){
							dailyResults.get(key).getPrechs().set(g, dailyResults.get(key).getPrechs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getPrechs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getPrechs().get(g)));

						}
						else if (l==13){
							dailyResults.get(key).getFds().set(g, dailyResults.get(key).getFds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFds().get(g)));

						}
						else if (l==14){
							dailyResults.get(key).getFhs().set(g, dailyResults.get(key).getFhs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getFhs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getFhs().get(g)));

						}
						else if (l==15){
							dailyResults.get(key).getTprds().set(g, dailyResults.get(key).getTprds().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getTprds().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getTprds().get(g)));

						}
						else if (l==16){
							dailyResults.get(key).getTprhs().set(g, dailyResults.get(key).getTprhs().get(g)/Parameter.NO_OF_RUNTIMES);
							if(Double.isNaN(dailyResults.get(key).getTprhs().get(g))){
								cr.getDailyResults().get(i).add(df.format(0));
							}
							else
							cr.getDailyResults().get(i).add(df.format(dailyResults.get(key).getTprhs().get(g)));

						}
					}
					analysis.setText("*** # Success Transactions ***");
					analysis.setText("*** Simulation has finished! ***");
					analysis.setText("");
					analysis.setChartTableData(Parameter.NO_OF_DAYS,j, k, evalName, means[j][k][l], stds[j][k][l], true);
				}
				System.out.println();
			}		

			analysis.readTrans(transactionList, env.getBuyerList(), env);
			System.out.println();
		}
		System.out.println("=====Come to Display=====");
	}

   //generates file storing results obtained from the selected attack and defense models.
	public void overallResults(ArrayList<Double> overallResults){


		try {

			String file1name = "data/results/" + defenseName + attackName  + "Overall";

			File file1 = new File(file1name + ".txt");


			// if file doesnt exists, then create it
			if (!file1.exists()) {
				file1.createNewFile();
			}

			//editted by AMANDA (copy entire try block)

			FileWriter fw = new FileWriter(file1.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Defense Name: " + defenseName + " " + " " + "Attack Name: " + attackName + "\n");
			bw.write("Average Results of Evaluation Metrics for " + Parameter.NO_OF_DAYS + " days \n\n");
			bw.write(" _______________________________________________________________________________________________________\n");
			bw.write("|     Evaluation Metrics      " + "|          Dishonest Seller          " + "|           Honest Seller            |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|            MAE              " + "|         " + formatResults(overallResults.get(0)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(1)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|            MCC              " + "|         " + formatResults(overallResults.get(2)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(3)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|            FNR              " + "|         " + formatResults(overallResults.get(4)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(5)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|            FNR              " + "|         " + formatResults(overallResults.get(6)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(7)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|            TPR              " + "|         " + formatResults(overallResults.get(14)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(15)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|          Accuracy           " + "|         " + formatResults(overallResults.get(8)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(9)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|         Precision           " + "|         " + formatResults(overallResults.get(10)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(11)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n");
			bw.write("|         F-Measure           " + "|         " + formatResults(overallResults.get(12)/Parameter.NO_OF_RUNTIMES)  + "        |         " + formatResults(overallResults.get(13)/Parameter.NO_OF_RUNTIMES) +"        |\n");
			bw.write("|_____________________________|____________________________________|____________________________________|\n\n");

			bw.close();



		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	public void showOutput(){

	}

	public void exportToDB(){

	}

	public void importFromDB(){

	}

	public void getCentralReputation(){

	}

	public void displaySuggestions(){

	}


	public Environment getEnv() {
		return env;
	}


	public void setEnv(Environment env) {
		this.env = env;
	}



	public String getRealdata_filename() {
		return realdata_filename;
	}



	public void setRealdata_filename(String realdata_filename) {
		this.realdata_filename = realdata_filename;
	}



	public HashMap<Integer, Double> getDailyRepH() {
		return dailyRepH;
	}



	public void setDailyRepH(HashMap<Integer, Double> dailyRepH) {
		this.dailyRepH = dailyRepH;
	}



	public HashMap<Integer, Double> getDailyRepDH() {
		return dailyRepDH;
	}



	public void setDailyRepDH(HashMap<Integer, Double> dailyRepDH) {
		this.dailyRepDH = dailyRepDH;
	}



	public HashMap<Integer, Double> getDailyRepDiffH() {
		return dailyRepDiffH;
	}



	public void setDailyRepDiffH(HashMap<Integer, Double> dailyRepDiffH) {
		this.dailyRepDiffH = dailyRepDiffH;
	}



	public HashMap<Integer, Double> getDailyRepDiffDH() {
		return dailyRepDiffDH;
	}



	public void setDailyRepDiffDH(HashMap<Integer, Double> dailyRepDiffDH) {
		this.dailyRepDiffDH = dailyRepDiffDH;
	}



	public HashMap<Integer, Double> getDailyMCCH() {
		return dailyMCCH;
	}



	public void setDailyMCCH(HashMap<Integer, Double> dailyMCCH) {
		this.dailyMCCH = dailyMCCH;
	}



	public HashMap<Integer, Double> getDailyMCCDH() {
		return dailyMCCDH;
	}



	public void setDailyMCCDH(HashMap<Integer, Double> dailyMCCDH) {
		this.dailyMCCDH = dailyMCCDH;
	}



	public HashMap<Integer, Double> getRobustness() {
		return robustness;
	}



	public void setRobustness(HashMap<Integer, Double> robustness) {
		this.robustness = robustness;
	}



	public ArrayList<Buyer> getBuyerList() {
		return buyerList;
	}



	public void setBuyerList(ArrayList<Buyer> buyerList) {
		this.buyerList = buyerList;
	}



	public BankBalance getBankbalance() {
		return bankbalance;
	}



	public void setBankbalance(BankBalance bankbalance) {
		this.bankbalance = bankbalance;
	}



	public ArrayList getTransactionList() {
		return transactionList;
	}



	public void setTransactionList(ArrayList transactionList) {
		this.transactionList = transactionList;
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub

	}



	public HashMap<Integer, Double> getDailyFNRH() {
		return dailyFNRH;
	}



	public void setDailyFNRH(HashMap<Integer, Double> dailyFNRH) {
		this.dailyFNRH = dailyFNRH;
	}



	public HashMap<Integer, Double> getDailyFNRDH() {
		return dailyFNRDH;
	}



	public void setDailyFNRDH(HashMap<Integer, Double> dailyFNRDH) {
		this.dailyFNRDH = dailyFNRDH;
	}



	public HashMap<Integer, Double> getDailyAccH() {
		return dailyAccH;
	}



	public void setDailyAccH(HashMap<Integer, Double> dailyAccH) {
		this.dailyAccH = dailyAccH;
	}



	public HashMap<Integer, Double> getDailyAccDH() {
		return dailyAccDH;
	}



	public void setDailyAccDH(HashMap<Integer, Double> dailyAccDH) {
		this.dailyAccDH = dailyAccDH;
	}



	public HashMap<Integer, Double> getDailyFPRH() {
		return dailyFPRH;
	}



	public void setDailyFPRH(HashMap<Integer, Double> dailyFPRH) {
		this.dailyFPRH = dailyFPRH;
	}



	public HashMap<Integer, Double> getDailyFPRDH() {
		return dailyFPRDH;
	}



	public void setDailyFPRDH(HashMap<Integer, Double> dailyFPRDH) {
		this.dailyFPRDH = dailyFPRDH;
	}



	public HashMap<Integer, Double> getDailyPrecH() {
		return dailyPrecH;
	}



	public void setDailyPrecH(HashMap<Integer, Double> dailyPrecH) {
		this.dailyPrecH = dailyPrecH;
	}



	public HashMap<Integer, Double> getDailyPrecDH() {
		return dailyPrecDH;
	}



	public void setDailyPrecDH(HashMap<Integer, Double> dailyPrecDH) {
		this.dailyPrecDH = dailyPrecDH;
	}



	public HashMap<Integer, Double> getDailyFH() {
		return dailyFH;
	}



	public void setDailyFH(HashMap<Integer, Double> dailyFH) {
		this.dailyFH = dailyFH;
	}



	public HashMap<Integer, Double> getDailyFDH() {
		return dailyFDH;
	}



	public void setDailyFDH(HashMap<Integer, Double> dailyFDH) {
		this.dailyFDH = dailyFDH;
	}



	public HashMap<Integer, Double> getDailyTPRH() {
		return dailyTPRH;
	}



	public void setDailyTPRH(HashMap<Integer, Double> dailyTPRH) {
		this.dailyTPRH = dailyTPRH;
	}



	public HashMap<Integer, Double> getDailyTPRDH() {
		return dailyTPRDH;
	}



	public void setDailyTPRDH(HashMap<Integer, Double> dailyTPRDH) {
		this.dailyTPRDH = dailyTPRDH;
	}



	public HashMap<String, DailyResults> getDailyResults() {
		return dailyResults;
	}



	public void setDailyResults(HashMap<String, DailyResults> dailyResults) {
		this.dailyResults = dailyResults;
	}

	public ArrayList<Double> getDefenseTime_day() {
		return defenseTime_day;
	}

	public ArrayList<Double> getHonest_avgWt() {
		return honest_avgWt;
	}

	public void setHonest_avgWt(ArrayList<Double> honest_avgWt) {
		this.honest_avgWt = honest_avgWt;
	}

	public ArrayList<Double> getDishonest_avgWt() {
		return dishonest_avgWt;
	}

	public void setDishonest_avgWt(ArrayList<Double> dishonest_avgWt) {
		this.dishonest_avgWt = dishonest_avgWt;
	}

	public String getDefenseName() {
		return defenseName;
	}

	public void setDefenseName(String defenseName) {
		this.defenseName = defenseName;
	}

	public String getAttackName() {
		return attackName;
	}

	public void setAttackName(String attackName) {
		this.attackName = attackName;
	}

	public void setDefenseTime_day(ArrayList<Double> defenseTime_day) {
		this.defenseTime_day = defenseTime_day;
	}


}