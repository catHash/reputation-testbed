/**class Environment: The abstract class which defines methods and variables to be used by the real
 * and simulated environment. The environment encompasses the interaction between the buyers and sellers
 * on a daily basis and sets the necessary parameters required for the transactions on a daily basis. 
 * 
 */
package environment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.*;
import weka.core.converters.*; //for arffsaver

import java.io.*;

import main.Parameter;
import main.Transaction;
import agent.Buyer;
import agent.Seller;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import weka.core.*;

import java.lang.*;

import distributions.PseudoRandom;

public abstract class Environment {
	//count the number of buyers that have evaluated reputation of seller 
	protected int numOfRep =0, numOfRepPerDay=0;
	public abstract void eCommerceSetting(String attackName,String defenseName);
	public abstract void eCommerceSetting(int noOfRuns, int defense, int attack, List<String> evaluateName, String attackName,String defenseName, String filename);
	abstract Instances generateInstances();
	abstract void importConfigSettings();
	abstract void createEnvironment();
	public abstract void updateDailyReputationDiff(ArrayList<Double> trustVals, int sid, int criteriaid);
	protected FastVector attInfo;
	protected FastVector attInfo2;
	protected FastVector attBuyer;

	protected FastVector attbuyerHonest;
	protected FastVector attSeller;
	protected FastVector attProduct;
	protected FastVector attSellerHonest;
	protected Instances data;
	protected Instances data2;

	protected int numOfDays;
	protected int numOfBuyers;
	protected int numOfSellers;
	protected int numOfRatings;

	protected Instances instTransactions;
	protected Instances instBalances;
	protected String attackName;
	protected String defenseName;

	protected HashMap<Integer, double[]> sellersTrueRating;
	protected HashMap<Integer, double[]> sellersTrueRep;

	protected int day;
	protected String envType;

	protected HashMap<Integer, Double> dailyRepH;
	protected HashMap<Integer, Double> dailyRepDH;
	protected HashMap<Integer, Double> dailyRepDiffH;
	protected HashMap<Integer, Double> dailyRepDiffDH;
	protected Robustness robustness;

	protected ArrayList<Buyer> buyerList;
	protected ArrayList<Seller> sellerList;
	protected ArrayList<Product> productList;
	protected ArrayList<Transaction> transactionList;

	//for real env
	protected String baseDIR="data/";
	protected boolean check;

	protected MCC mcc;
	protected int criteriano = Parameter.NO_OF_CRITERIA;

	//string of hashmap denotes buyer/seller pair
	//Hashmap key in inner hashmap denotes criteria
	//for WMA
	protected HashMap<String, HashMap<Integer,Double>> BSnumBel = null;
	protected HashMap<String, HashMap<Integer,Double>> BSnumDisBel= null;
	protected HashMap<String, HashMap<Integer,Double>> BSnumUnc = null;
	protected HashMap<String, HashMap<Integer,Integer>> bsr2 = null;

	//binary environment
	//string of hashmap denotes buyer/seller pair
	//string in inner hashmap denotes rating/criteria pair
	protected HashMap<String, HashMap<String, Double>> bsr;
	protected HashMap<Integer, HashMap<String, Double>> sr;
	protected HashMap<String, Double> BScurrRating;
	protected HashMap<String, HashMap<Integer, Double>> BAsameRatings;

	//multinominal
	protected int mnrLen = Parameter.RATING_MULTINOMINAL.length;

	//real
	protected HashMap<String, Integer> BSnumR;
	protected HashMap<String, HashMap<Integer, Double>> BSnumBDU;

	public Environment(){
		transactionList = new ArrayList<Transaction>();
		numOfDays = Parameter.NO_OF_DAYS;
		numOfBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		numOfSellers = Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS;
		robustness = new Robustness(this);
		sellersTrueRating = new HashMap<Integer, double[]>();
		sellersTrueRep = new HashMap<Integer, double[]>();
		day = 0;
		dailyRepH = new HashMap<Integer, Double>();
		dailyRepDH = new HashMap<Integer, Double>();
		dailyRepDiffH = new HashMap<Integer, Double>();
		dailyRepDiffDH = new HashMap<Integer, Double>();
		for(int i=0; i<Parameter.NO_OF_DAYS+1; i++){
			dailyRepH.put(i, 0.0);
			dailyRepDH.put(i, 0.0);
			dailyRepDiffH.put(i, 0.0);
			dailyRepDiffDH.put(i, 0.0);

		}
		mcc = new MCC(this);
	}

	public MCC getMcc() {
		return mcc;
	}

	public void setMcc(MCC mcc) {
		this.mcc = mcc;
	}


	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public ArrayList<Double> getDailyReputation(){
		ArrayList<Double> dr = new ArrayList<Double>();
		dr.add(dailyRepDH.get(day) / (Parameter.NO_OF_HONEST_BUYERS));
		dr.add(dailyRepH.get(day) / (Parameter.NO_OF_HONEST_BUYERS));
		return dr;
	}

	public ArrayList<Double> getDailyReputationDiff(){
		ArrayList<Double> drd = new ArrayList<Double>();
		drd.add(dailyRepDiffDH.get(day) / ((day+1) * Parameter.NO_OF_HONEST_BUYERS));
		drd.add(dailyRepDiffH.get(day) / ((day+1) * Parameter.NO_OF_HONEST_BUYERS));
		return drd;
	}

	public void setDay(int day){
		if(this.day < Parameter.NO_OF_DAYS){
			dailyRepDiffDH.put(this.day+1, dailyRepDiffDH.get(this.day));
			dailyRepDiffH.put(this.day+1, dailyRepDiffH.get(this.day));
		}
		if(this.day< Parameter.NO_OF_DAYS){

			for(int i=0; i<numOfSellers; i++){
				sellerList.get(i).getDailySales().put(this.day+1, sellerList.get(i).getDailySales().get(this.day));
			}
		}
		this.day = day +1;
	}

	public HashMap<Integer, Double> getBuyersAdvisorsSameRatingsArray(int bid, int aid) { 

		if(BAsameRatings == null) 
			return null;   
		String key = bid+"_"+aid;
		return BAsameRatings.get(key); 
	} 

	//method to initialize the variables, hashmaps to be used for computational purposes
	protected void parameterSetting(String attackName, String defenseName){
		//set the number of dishonest/honest buyers
		int db = Parameter.NO_OF_DISHONEST_BUYERS;
		int hb = Parameter.NO_OF_HONEST_BUYERS;
		Parameter.NO_OF_DISHONEST_BUYERS = (db < hb) ? db : hb;
		Parameter.NO_OF_HONEST_BUYERS = (db > hb) ? db : hb;
		//setting the attack model
		if(Parameter.includeSybil(attackName)){
			Parameter.NO_OF_DISHONEST_BUYERS = (db > hb) ? db : hb;
			Parameter.NO_OF_HONEST_BUYERS = (db < hb) ? db : hb;
		}

		numOfDays = Parameter.NO_OF_DAYS;
		numOfBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		numOfSellers = Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS;

		sellersTrueRating = new HashMap<Integer, double[]>();
		sellersTrueRep = new HashMap<Integer, double[]>();
		day = 0;
		dailyRepH = new HashMap<Integer, Double>();
		dailyRepDH = new HashMap<Integer, Double>();
		dailyRepDiffH = new HashMap<Integer, Double>();
		dailyRepDiffDH = new HashMap<Integer, Double>();
		for(int i=0; i<Parameter.NO_OF_DAYS+1; i++){
			dailyRepH.put(i, 0.0);
			dailyRepDH.put(i, 0.0);
			dailyRepDiffH.put(i, 0.0);
			dailyRepDiffDH.put(i, 0.0);

		}
		mcc = new MCC(this);
		attackName = new String(attackName);
		defenseName = new String(defenseName);

	}

	//sets the default rating values, based on the chosen rating system.
	public void assignTruth(){

		if(Parameter.RATING_TYPE.compareTo("binary") == 0){
			for(int i = 0; i < Parameter.NO_OF_DISHONEST_SELLERS; i++){
				double[] rating = new double[criteriano];
				double[] rep = new double[criteriano];
				for(int j=0; j<criteriano; j++){
					//dishonest seller, rating = -1
					rating[j] = (double)Parameter.RATING_BINARY[0];
					rep[j] =0.0;
				}
				sellersTrueRating.put(i, rating);
				sellersTrueRep.put(i, rep);
			}
			for(int i = Parameter.NO_OF_DISHONEST_SELLERS; i < numOfSellers; i++){
				double[] rating = new double[criteriano];
				double[] rep = new double[criteriano];
				for(int j=0; j<criteriano; j++){
					//honest seller, rating = 1
					rating[j] = (double)Parameter.RATING_BINARY[2];
					rep[j] =1.0;
				}
				sellersTrueRating.put(i, rating);
				sellersTrueRep.put(i, rep);
			}
		} else if(Parameter.RATING_TYPE.compareTo("multinominal") == 0){
			int interval = numOfSellers / (Parameter.RATING_MULTINOMINAL.length - 1);	
			double[] trueRep = new double[]{0, 0.25, 0.5, 0.75, 1.0};
			int halfPos = Parameter.RATING_MULTINOMINAL.length / 2;
			for(int i = 0; i < numOfSellers; i++){
				double[] rating = new double[criteriano];
				double[] rep = new double[criteriano];
				for(int j=0; j<criteriano; j++){
					int ratingIdx = i / interval;
					//if(ratingIdx >= halfPos)ratingIdx++;
					//[1..1, 2...,2, 4...4, 5...5]
					rating[j] = (double)Parameter.RATING_MULTINOMINAL[ratingIdx];
					rep[j] = trueRep[ratingIdx];
				}
				sellersTrueRating.put(i, rating);
				sellersTrueRep.put(i, rep);
			}
		} else if(Parameter.RATING_TYPE.compareTo("real") == 0){
			for(int i = 0; i < numOfSellers; i++){
				double[] rating = new double[criteriano];
				double[] rep = new double[criteriano];
				for(int j=0; j<criteriano; j++){
					if(i < numOfSellers/2){
						double interval = (Parameter.m_omega[0] - Parameter.RATING_REAL[0]) / (numOfSellers/2 - 1);
						rating[j] = i*interval;
						rep[j] = i*interval;
					} else{
						double interval = (Parameter.RATING_REAL[1] - Parameter.m_omega[1]) / (numOfSellers/2 - 1);
						rating[j] = Parameter.m_omega[1] + (i - numOfSellers/2) * interval;
						rep[j] = Parameter.m_omega[1] + (i - numOfSellers/2) * interval;
					}
				}
				sellersTrueRating.put(i, rating);
				sellersTrueRep.put(i, rep);
			}
		} else{
			System.out.println("not such type of rating");
		}
	}

	public void updateDailyReputationDiff(ArrayList<Double> trustVals){		
		dailyRepDH.put(day, dailyRepDH.get(day) + trustVals.get(0));
		dailyRepH.put(day, dailyRepH.get(day)+trustVals.get(1));
		double avgTrueRep =0;
		for(int i=0; i<criteriano; i++){
			if(defenseName.equalsIgnoreCase("ebay"))
				avgTrueRep += sellersTrueRating.get(Parameter.TARGET_DISHONEST_SELLER)[i];
			else
				avgTrueRep += sellersTrueRep.get(Parameter.TARGET_DISHONEST_SELLER)[i];
		}
		dailyRepDiffDH.put(day, dailyRepDiffDH.get(day) + Math.abs(Math.abs(avgTrueRep/criteriano) - ((trustVals.get(0)))));
		avgTrueRep = 0;
		for(int i=0; i<criteriano; i++){
			avgTrueRep += sellersTrueRep.get(Parameter.TARGET_HONEST_SELLER)[i];
		}
		dailyRepDiffH.put(day, dailyRepDiffH.get(day) + Math.abs((avgTrueRep/criteriano) - ((trustVals.get(1)))));
	}


	public void updateArray(Instance inst) {
		if(envType.equalsIgnoreCase("binary")){
			int bVal = (int)inst.value(Parameter.m_bidIdx);
			int sVal = (int)inst.value(Parameter.m_sidIdx);
			String key = bVal+"_"+sVal;
			//translate the ratings to binary;
			for(int k=Parameter.m_ratingIdx; k<Parameter.m_ratingIdx+Parameter.NO_OF_CRITERIA; k++){
				int rVal = translate2BinaryRating(inst.value(k));
				if (rVal == Parameter.RATING_BINARY[0]) {
					String critkey = 0+"_"+(k-Parameter.m_ratingIdx);
					bsr.get(key).put(critkey, bsr.get(key).get(critkey)+1);
					sr.get(sVal).put(critkey, sr.get(sVal).get(critkey)+1);
					sellerList.get(sVal).addneg(bVal);
					if(k==inst.numValues()-1)
						sellerList.get(sVal).addSales(day);
				} else if (rVal == Parameter.RATING_BINARY[1]) {			
					// nothing need to do;
				} else if (rVal == Parameter.RATING_BINARY[2]) {
					String critkey = 1+"_"+(k-Parameter.m_ratingIdx);
					bsr.get(key).put(critkey, bsr.get(key).get(critkey)+1);
					sr.get(sVal).put(critkey, sr.get(sVal).get(critkey)+1);
					sellerList.get(sVal).addPos(bVal);

					if(k==inst.numValues()-1)
						sellerList.get(sVal).addSales(day);
				}	
				/***TRAVOS: update information for travos***/
				if(Parameter.includeTRAVOS(defenseName)){
					BScurrRating.put(key, (double)rVal);
				}
				/***TRAVOS: update information for travos***/

				/***personalized approach: update information***/
				if(Parameter.includePersonalized(defenseName) ){	
					int dayInterval = (int)(Parameter.NO_OF_DAYS / Parameter.m_timewindows);
					int dVal_B = (int) inst.value(Parameter.m_dayIdx);   
					int bVal_B = bVal;
					int sVal_B = sVal;
					int rVal_B = rVal;
					if (dayInterval ==0) dayInterval = 1;
					int tw_B = dVal_B / dayInterval;
					if (rVal_B == Parameter.RATING_BINARY[1]) {
						return;
					}
					//only use the recently ratings
					ArrayList<Boolean> advisor_scanned = new ArrayList<Boolean>();
					for(int i = 0; i < numOfBuyers; i++){
						advisor_scanned.add(false);
					}	
					//sort the instances by data, ascending;
					for (int j = instTransactions.numInstances() - 1; j >=0 ; j--) {
						Instance inst_A = instTransactions.instance(j);				
						int dVal_A = (int) inst_A.value(Parameter.m_dayIdx);
						int bVal_A = (int) inst_A.value(Parameter.m_bidIdx);
						int sVal_A = (int) inst_A.value(Parameter.m_sidIdx);
						//translate the ratings to binary;
						int rVal_A = translate2BinaryRating(inst_A.value(Parameter.m_ratingIdx));						
						int tw_A = dVal_A / dayInterval;
						if(rVal_A == Parameter.RATING_BINARY[1] || advisor_scanned.get(bVal_A))continue;
						//same seller, same time window, prior to buyer day 
						if (sVal_B != sVal_A || tw_B != tw_A  || dVal_A > dVal_B)continue;				
						if (rVal_B != rVal_A) { 
							String key1 = bVal_B+"_"+bVal_A;
							String key2 = bVal_A+"_"+bVal_B;
							BAsameRatings.get(key1).put(0, BAsameRatings.get(key1).get(0)+1);
							BAsameRatings.get(key2).put(0, BAsameRatings.get(key2).get(0)+1);
						} else {
							String key1 = bVal_B+"_"+bVal_A;
							String key2 = bVal_A+"_"+bVal_B;
							BAsameRatings.get(key1).put(1, BAsameRatings.get(key1).get(1)+1);
							BAsameRatings.get(key2).put(1, BAsameRatings.get(key2).get(1)+1);
						}
						advisor_scanned.set(bVal_A, true);
					}
				}	
			}
			/***personalized approach: update information***/
		}

		else if (envType.equalsIgnoreCase("multinominal")){
			int bVal = (int)inst.value(Parameter.m_bidIdx);
			int sVal = (int)inst.value(Parameter.m_sidIdx);
			String key = bVal+"_"+sVal;
			//translate the ratings to multinominal;
			for(int z=5; z<inst.numValues(); z++){
				int rVal = translate2MultinominalRating(inst.value(z));	
				if(rVal != Parameter.RATING_MULTINOMINAL[mnrLen/2]){
					if(this instanceof EnvironmentR){
						if(bVal < Parameter.NO_OF_DISHONEST_BUYERS) rVal -=1;
						String critkey = (rVal)+"_"+(z-Parameter.m_ratingIdx);
						bsr.get(key).put(critkey, bsr.get(key).get(critkey)+1);
					}
					else {
						String critkey = (rVal-1)+"_"+(z-Parameter.m_ratingIdx);

						bsr.get(key).put(critkey, bsr.get(key).get(critkey)+1);
					}
				}
				if(z==inst.numValues()-1)
					sellerList.get(sVal).addSales(day);
			}
		}
		else if (envType.equalsIgnoreCase("real")){
			int bVal = (int)inst.value(Parameter.m_bidIdx);;
			int sVal = (int)inst.value(Parameter.m_sidIdx);
			String key = bVal+"_"+sVal;

			//translate the ratings to real;
			for(int z=Parameter.m_ratingIdx; z<Parameter.m_ratingIdx+Parameter.NO_OF_CRITERIA; z++){
				double rating = inst.value(z);
				bsr2.get(key).put(z-Parameter.m_ratingIdx, bsr2.get(key).get(z-Parameter.m_ratingIdx)+1);
				if (rating >= Parameter.m_omega[1]) {
					BSnumBel.get(key).put(z-Parameter.m_ratingIdx, BSnumBel.get(key).get(z-Parameter.m_ratingIdx) + 1);
				} else if (rating < Parameter.m_omega[0]) {
					BSnumDisBel.get(key).put(z-Parameter.m_ratingIdx, BSnumDisBel.get(key).get(z-Parameter.m_ratingIdx) + 1);
				} else {
					BSnumUnc.get(key).put(z-Parameter.m_ratingIdx, BSnumUnc.get(key).get(z-Parameter.m_ratingIdx) + 1);
				}

				double rVal = translate2RealRating(inst.value(Parameter.m_ratingIdx));	
				if(rVal <0.5) sellerList.get(sVal).addneg(bVal);
				else sellerList.get(sVal).addPos(bVal);
				//not null rating;	
				String critkey = 0+"_"+(z-Parameter.m_ratingIdx);
				bsr.get(key).put(critkey, (bsr.get(key).get(critkey) * BSnumR.get(key) + rVal) / (BSnumR.get(key) + 1.0));
				BSnumR.put(key, BSnumR.get(key)+1);
				if(z==(Parameter.m_ratingIdx+Parameter.NO_OF_CRITERIA-1))
					sellerList.get(sVal).addSales(day);

				if(rVal >= Parameter.m_omega[1]){ 
					BSnumBDU.get(key).put(0, BSnumBDU.get(key).get(0)+1);
				} else if(rVal <= Parameter.m_omega[1]){ 
					BSnumBDU.get(key).put(1, BSnumBDU.get(key).get(1)+1);
				} else{
					BSnumBDU.get(key).put(2, BSnumBDU.get(key).get(2)+1);
				}
			}
		}
	}

	//method to initialize the header section of the transaction .arff
	public Instances initialInstancesHeader(){
		attInfo = new FastVector();
		//for day information
		attInfo.addElement(new Attribute(Parameter.dayString)); 
		attBuyer = new FastVector();
		if(Parameter.includeWhitewashing()){
			//more buyer in database			
			numOfBuyers = numOfBuyers + Parameter.NO_OF_DAYS * Parameter.NO_OF_DISHONEST_BUYERS;
			for(int i = 0; i < numOfBuyers; i++){
				String str = "b" + Integer.toString(i);
				attBuyer.addElement(str);			
			}			
		}else{			
			for(int i = 0; i < numOfBuyers; i++){
				String str = "b" + Integer.toString(i);
				attBuyer.addElement(str);			
			}
		}		
		attInfo.addElement(new Attribute(Parameter.buyerIdString, attBuyer));
		//for buyer/advisor dishonest/honest
		attbuyerHonest = new FastVector();
		attbuyerHonest.addElement(Parameter.agent_dishonest);                            
		attbuyerHonest.addElement(Parameter.agent_honest);
		attInfo.addElement(new Attribute(Parameter.buyerHonestyString, attbuyerHonest));
		//for sellers id, nominal
		attSeller = new FastVector();       
		for(int i = 0; i < numOfSellers; i++){
			String str = "s" + Integer.toString(i);
			attSeller.addElement(str);
		}
		attInfo.addElement(new Attribute(Parameter.sellerIdString, attSeller));

		attSellerHonest = new FastVector();
		attSellerHonest.addElement(Parameter.agent_dishonest);                            
		attSellerHonest.addElement(Parameter.agent_honest);
		attInfo.addElement(new Attribute(Parameter.sellerHonestyString, attSellerHonest));
		//attInfo.addElement(new Attribute(Parameter.sellerHonestyString));
		for(int i = 0; i < Parameter.NO_OF_CRITERIA; i++){									
			attInfo.addElement(new Attribute("criteria".concat(Integer.toString(i))));	
		}
		String instsName = new String("eCommerce.arff");
		Instances header = new Instances(instsName, attInfo, numOfDays * (numOfBuyers));
		data=new Instances("eCommerce.arff",attInfo,0);
		return header;
	}

	//method to initialize header section of the balance .arff
	public Instances initialInstancesHeader2(){
		System.out.println("enters instance header 2");
		attInfo2 = new FastVector();
		attInfo2.addElement(new Attribute(Parameter.dayString)); 
		attBuyer = new FastVector();
		for(int i = 0; i < numOfBuyers; i++){
			String str = "b" + Integer.toString(i);
			attBuyer.addElement(str);
		}
		attInfo2.addElement(new Attribute(Parameter.buyerIdString, attBuyer));
		attInfo2.addElement(new Attribute(Parameter.buyerBalString));
		attProduct = new FastVector();               
		for(int i = 0; i < numOfSellers; i++){
			String str = "p" + Integer.toString(i);
			attProduct.addElement(str);
		}
		attInfo2.addElement(new Attribute(Parameter.productString, attProduct));
		attInfo2.addElement(new Attribute(Parameter.salePriceString));
		attSeller = new FastVector();
		for(int i = 0; i < numOfSellers; i++){
			String str = "s" + Integer.toString(i);
			attSeller.addElement(str);
		}
		attInfo2.addElement(new Attribute(Parameter.sellerIdString, attSeller));
		attSellerHonest = new FastVector(); 				
		attSellerHonest.addElement(Parameter.agent_dishonest);   
		attSellerHonest.addElement(Parameter.agent_honest);
		attInfo2.addElement(new Attribute(Parameter.sellerHonestyString, attSellerHonest));
		attInfo2.addElement(new Attribute(Parameter.sellerBalString));
		String instsName = new String("eCommerce2.arff");
		Instances header = new Instances(instsName, attInfo2, numOfDays * (numOfBuyers+numOfSellers));
		data2=new Instances("eCommerce2.arff",attInfo2,0);
		return header;
	}



	public void createData(int dVal,String bVal,String bHonestVal,String sVal,double sHonestVal,double rVal[]){
		double vals[] = new double[data.numAttributes()];
		vals[0]=dVal;
		vals[1]=attBuyer.indexOf("b"+bVal);
		vals[2]=attbuyerHonest.indexOf(bHonestVal);
		vals[3]=attSeller.indexOf("s"+sVal);
		vals[4]=sHonestVal;
		for(int i=0; i<rVal.length; i++){
			vals[5+i]=rVal[i];
		}
		data.add(new Instance(1.0,vals));
	}

	//method to setup the array of double type to store 
	//the details of respective attributes of instances (for balance .arff)
	public void createData2(int dVal,String bVal,double buyerBal,String product,double saleprice, String sVal, String ishonest, double sellerBal){
		double vals[] = new double[data2.numAttributes()];
		vals[0]=dVal;
		vals[1]=attBuyer.indexOf("b"+bVal);
		vals[2]=buyerBal;
		vals[3]=attProduct.indexOf("p"+product);
		vals[4]=saleprice;
		vals[5]=attSeller.indexOf("s"+sVal);
		vals[6]=attSellerHonest.indexOf(ishonest);
		vals[7]=sellerBal;
		data2.add(new Instance(1.0,vals));
	}

	//method to create the .arff file to contain transaction details and store in predefined directory
	public void createARFFfile()throws Exception{
		ArffSaver  saver = new ArffSaver();
		saver.setInstances(data);
		String att = this.getAttackName(); String def = this.getDefenseName();
		String filename = "";
		if(this instanceof EnvironmentS) 
			filename = "data/"+att+"_"+def+"_"+"simulated_transaction.arff";
		else{
			filename = "data/"+att+"_"+def+"_"+"real_transaction.arff";
		}
		saver.setFile(new File(filename));//(new File("data/FYP.arff"));
		saver.writeBatch();
	}

	//method to create .arff file for balance information and store it in predefined directory
	public void createBalanceArff()throws Exception{
		ArffSaver saver2 = new ArffSaver();
		saver2.setInstances(data2);
		String filename = "";
		String att = this.getAttackName(); String def = this.getDefenseName();
		if(this instanceof EnvironmentS)
			filename = "data/"+att+"_"+def+"_"+"simulated_balance.arff";
		else
			filename = "data/"+att+"_"+def+"_"+"real_balance.arff";
		saver2.setFile(new File(filename));
		saver2.writeBatch();
	}


	public final static void saveWekaInstances(Instances insts, String directory,
			String fileName) {

		Instances dataSet = insts;
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		File experimentDirectory = new File(directory);
		if (!experimentDirectory.exists()) {
			boolean result = new File(directory).mkdirs();
			System.out.println("Creating " + directory);
		}

		try {
			saver.setFile(new File(directory + "/" + fileName));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//added -athirai

	//method to check whether the seller list contains another seller's information (object)
	public Seller containsSeller(int  sid){
		Seller s = new Seller(this);
		s.setId(sid);
		if(sellerList.contains(s)){
			return sellerList.get(sellerList.indexOf(s));
		}
		else return null;
	}

	//method to check whether buyer list contains another buyer' onformation (object)
	public Buyer containsBuyer(int  bid){
		Buyer b = new Buyer(this);
		b.setId(bid);
		if(buyerList.contains(b))return buyerList.get(buyerList.indexOf(b));
		else return null;
	}

	//converting real/multinomial rating to binary
	public int translate2BinaryRating(double originalRating){

		int rVal = (int)originalRating;
		if(Parameter.RATING_TYPE.compareTo("multinominal") == 0){
			rVal = Parameter.rating_multinominal2binary(originalRating);
		}else if(Parameter.RATING_TYPE.compareTo("real") == 0){
			rVal = Parameter.rating_real2binary(originalRating);			
		}

		return rVal;
	}
	//transalting binary/real rating to multinomial
	public int translate2MultinominalRating(double originalRating){

		int rVal = (int)originalRating;
		if(Parameter.RATING_TYPE.compareTo("binary") == 0){
			rVal = Parameter.rating_binary2multinominal(originalRating);
		}else if(Parameter.RATING_TYPE.compareTo("real") == 0){
			rVal = Parameter.rating_real2multinominal(originalRating);			
		}

		return rVal;
	}
	//converting real/multinomial rating to binary
	public double translate2RealRating(double originalRating){

		double rVal = (int)originalRating;
		if(Parameter.RATING_TYPE.compareTo("binary") == 0){
			rVal = Parameter.rating_binary2real(originalRating);
		}else if(Parameter.RATING_TYPE.compareTo("multinominal") == 0){
			rVal = Parameter.rating_multinominal2real(originalRating);			
		}

		return rVal;
	}

	public FastVector getAttInfo() {
		return attInfo;
	}

	public void setAttInfo(FastVector attInfo) {
		this.attInfo = attInfo;
	}

	public FastVector getAttInfo2() {
		return attInfo2;
	}

	public void setAttInfo2(FastVector attInfo2) {
		this.attInfo2 = attInfo2;
	}

	public FastVector getAttBuyer() {
		return attBuyer;
	}

	public void setAttBuyer(FastVector attBuyer) {
		this.attBuyer = attBuyer;
	}

	public FastVector getAttbuyerHonest() {
		return attbuyerHonest;
	}

	public void setAttbuyerHonest(FastVector attbuyerHonest) {
		this.attbuyerHonest = attbuyerHonest;
	}

	public FastVector getAttSeller() {
		return attSeller;
	}

	public void setAttSeller(FastVector attSeller) {
		this.attSeller = attSeller;
	}

	public FastVector getAttProduct() {
		return attProduct;
	}

	public void setAttProduct(FastVector attProduct) {
		this.attProduct = attProduct;
	}

	public FastVector getAttSellerHonest() {
		return attSellerHonest;
	}

	public void setAttSellerHonest(FastVector attSellerHonest) {
		this.attSellerHonest = attSellerHonest;
	}

	public Instances getData() {
		return data;
	}

	public void setData(Instances data) {
		this.data = data;
	}

	public Instances getData2() {
		return data2;
	}

	public void setData2(Instances data2) {
		this.data2 = data2;
	}

	public int getNumOfDays() {
		return numOfDays;
	}

	public void setNumOfDays(int numOfDays) {
		this.numOfDays = numOfDays;
	}

	public int getNumOfBuyers() {
		return numOfBuyers;
	}

	public void setNumOfBuyers(int numOfBuyers) {
		this.numOfBuyers = numOfBuyers;
	}

	public int getNumOfSellers() {
		return numOfSellers;
	}

	public void setNumOfSellers(int numOfSellers) {
		this.numOfSellers = numOfSellers;
	}

	public int getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(int numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public Instances getInstTransactions() {
		return instTransactions;
	}

	public void setInstTransactions(Instances instTransactions) {
		this.instTransactions = instTransactions;
	}

	public Instances getInstBalances() {
		return instBalances;
	}

	public void setInstBalances(Instances instBalances) {
		this.instBalances = instBalances;
	}

	public String getAttackName() {
		return attackName;
	}

	public void setAttackName(String attackName) {
		this.attackName = attackName;
	}

	public String getDefenseName() {
		return defenseName;
	}

	public void setDefenseName(String defenseName) {
		this.defenseName = defenseName;
	}

	public double getSellersTrueRating(int id, int criteriaid) {
		return sellersTrueRating.get(id)[criteriaid];
	}

	public void setSellersTrueRating(HashMap<Integer, double[]> sellersTrueRating) {
		this.sellersTrueRating = sellersTrueRating;
	}

	public HashMap<Integer, double[]> getSellersTrueRep() {
		return sellersTrueRep;
	}

	public void setSellersTrueRep(HashMap<Integer, double[]> sellersTrueRep) {
		this.sellersTrueRep = sellersTrueRep;
	}

	public int getDay() {
		return day;
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

	//	public MCC getMcc() {
	//		return mcc;
	//	}
	//
	//	public void setMcc(MCC mcc) {
	//		this.mcc = mcc;
	//	}

	public ArrayList<Buyer> getBuyerList() {
		return buyerList;
	}

	public void setBuyerList(ArrayList<Buyer> buyerList) {
		this.buyerList = buyerList;
	}

	public ArrayList<Seller> getSellerList() {
		return sellerList;
	}

	public void setSellerList(ArrayList<Seller> sellerList) {
		this.sellerList = sellerList;
	}

	public ArrayList<Product> getProductList() {
		return productList;
	}

	public void setProductList(ArrayList<Product> productList) {
		this.productList = productList;
	}

	public String getBaseDIR() {
		return baseDIR;
	}

	public void setBaseDIR(String baseDIR) {
		this.baseDIR = baseDIR;
	}

	public HashMap<String, HashMap<String, Double>> getBsr() {
		return bsr;
	}

	public void setBsr(HashMap<String, HashMap<String, Double>> bsr) {
		this.bsr = bsr;
	}

	public HashMap<Integer, HashMap<String, Double>> getSr() {
		return sr;
	}

	public void setSr(HashMap<Integer, HashMap<String, Double>> sr) {
		this.sr = sr;
	}

	public HashMap<String, Double> getBScurrRating() {
		return BScurrRating;
	}

	public void setBScurrRating(HashMap<String, Double> bScurrRating) {
		BScurrRating = bScurrRating;
	}

	public HashMap<String, HashMap<Integer, Double>> getBAsameRatings() {
		return BAsameRatings;
	}

	public void setBAsameRatings(
			HashMap<String, HashMap<Integer, Double>> bAsameRatings) {
		BAsameRatings = bAsameRatings;
	}

	public int getMnrLen() {
		return mnrLen;
	}

	public void setMnrLen(int mnrLen) {
		this.mnrLen = mnrLen;
	}

	public HashMap<String, Integer> getBSnumR() {
		return BSnumR;
	}

	public void setBSnumR(HashMap<String, Integer> bSnumR) {
		BSnumR = bSnumR;
	}

	public Robustness getRobustness() {
		return robustness;
	}

	public HashMap<String, HashMap<Integer, Double>> getBSnumBDU() {
		return BSnumBDU;
	}

	public void setBSnumBDU(HashMap<String, HashMap<Integer, Double>> bSnumBDU) {
		BSnumBDU = bSnumBDU;
	}

	public void setRobustness(Robustness robustness) {
		this.robustness = robustness;
	}

	public ArrayList<Transaction> getTransactionList() {
		return transactionList;
	}

	public void setTransactionList(ArrayList<Transaction> transactionList) {
		this.transactionList = transactionList;
	}

	public HashMap<Integer, double[]> getSellersTrueRating() {
		return sellersTrueRating;
	}




	public HashMap<String, HashMap<Integer, Double>> getBSnumBel() {
		return BSnumBel;
	}

	public void setBSnumBel(HashMap<String, HashMap<Integer, Double>> bSnumBel) {
		BSnumBel = bSnumBel;
	}

	public HashMap<String, HashMap<Integer, Double>> getBSnumDisBel() {
		return BSnumDisBel;
	}

	public void setBSnumDisBel(HashMap<String, HashMap<Integer, Double>> bSnumDisBel) {
		BSnumDisBel = bSnumDisBel;
	}

	public HashMap<String, HashMap<Integer, Double>> getBSnumUnc() {
		return BSnumUnc;
	}

	public void setBSnumUnc(HashMap<String, HashMap<Integer, Double>> bSnumUnc) {
		BSnumUnc = bSnumUnc;
	}

	public HashMap<String, HashMap<Integer, Integer>> getBsr2() {
		return bsr2;
	}

	public void setBsr2(HashMap<String, HashMap<Integer, Integer>> bsr2) {
		this.bsr2 = bsr2;
	}
	public int getNumOfRep() {
		return numOfRep;
	}
	public void setNumOfRep(int numOfRep) {
		this.numOfRep = numOfRep;
	}
	public int getNumOfRepPerDay() {
		return numOfRepPerDay;
	}
	public void setNumOfRepPerDay(int numOfRepPerDay) {
		this.numOfRepPerDay = numOfRepPerDay;
	}

}

