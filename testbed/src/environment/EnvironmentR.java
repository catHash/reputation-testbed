/**The real environment uses data from an external file source to generate agents and transactions
 * in the simulated environment. It also inherits attributes and methods from the Environment class.
 */
package environment;


import weka.core.*;

import java.io.*;
import java.util.*;

import weka.core.Instances;
import defenses.Reece;
import distributions.PseudoRandom;
import agent.*;
import main.*;
import environment.Environment;

/* modified - athirai */
public class EnvironmentR extends Environment{

	public static String filename;
	private CentralAuthority ca=null;

	public EnvironmentR(CentralAuthority ca){
		super();
		this.ca = ca;
		mcc = new MCC(this);
		ca.setBankbalance(new BankBalance());
		ca.setTransactionList(transactionList);

	}
    
    /*method to setup the real environment.
     * The number of agents, attack and defense models are initialized.
     * The method to create the header section of the .arff files storing
     * information on the real environment transactions is also called from this method.
     */
	public void eCommerceSetting(int noOfRuns, int defense, int attack, List<String> evaluateName, String attackName,String defenseName, String filename){
		this.attackName = new String(attackName);
		this.defenseName = new String(defenseName);
		this.filename=filename;

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

		if(Parameter.includeICLUB(defenseName)){			
			if (Parameter.RATING_TYPE.equalsIgnoreCase("binary")) {
				envType = "binary";
			} else if (Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")) {
				envType = "multinominal";
			} else{
				envType = "multinominal";
			}
		} else if(Parameter.includeWMA(defenseName) || defenseName.equalsIgnoreCase("metrustedgraph")){
			envType = "real";
		} else{
			envType = "binary";
		}
		agentSetting();
		Instances insts=generateInstances( attackName,  noOfRuns,  defense,  attack,  defenseName,  evaluateName);
	}

   //method to setup the values stored by the hashmaps used for containing buyer seller ratings.
	public void agentSetting(){
		int numBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		int numSellers = numOfSellers; 
		buyerList = new ArrayList<Buyer>();
		sellerList = new ArrayList<Seller>();
		productList = new ArrayList<Product>();
		mcc = new MCC(this);
		buyerList = new ArrayList<Buyer>();
		sellerList = new ArrayList<Seller>();
		//transactionList = new ArrayList<Transaction>();
		productList = new ArrayList<Product>();
		sellersTrueRating = new HashMap<Integer, double[]>();
		sellersTrueRep = new HashMap<Integer, double[]>();
		day =0;
		if(envType.equalsIgnoreCase("binary")){
			//initialize the header information of instances
			Instances header = initialInstancesHeader();
			Instances header2 = initialInstancesHeader2();
			instTransactions = new Instances(header);
			instBalances = new Instances(header2);

			assignTruth();

			bsr = new HashMap<String, HashMap<String, Double>>();
			sr = new HashMap<Integer, HashMap<String, Double>>();
			for(int i=0; i<numOfBuyers; i++){
				for(int j=0; j<numOfSellers; j++){
					String key = i+"_"+j;
					HashMap<String, Double> r = new HashMap<String, Double>();
					for(int k=0; k<2; k++){
						for(int l=0; l<Parameter.NO_OF_CRITERIA;l++){
							String critkey = k+"_"+l;
							r.put(critkey, 0.0);
						}
					}
					bsr.put(key, r);
				}
			}
			for(int i=0; i<numOfSellers; i++){
				HashMap<String, Double> r = new HashMap<String, Double>();
				for(int j=0; j<2; j++){
					for(int k=0; k <Parameter.NO_OF_CRITERIA; k++){
						String critkey = j+"_"+k;
						r.put(critkey, 0.0);
					}
				}
				sr.put(i, r);
			}

			if(Parameter.includeTRAVOS(defenseName)){
				BScurrRating = new HashMap<String, Double>();
				for(int i=0; i<numOfBuyers; i++){
					for(int j=0; j<numOfSellers; j++){
						String key = i+"_"+j;
						BScurrRating.put(key, (double)Parameter.RATING_BINARY[1]);
					}
				}
			}		

			if(Parameter.includePersonalized(defenseName)){	
				BAsameRatings = new HashMap<String, HashMap<Integer, Double>>();
				for(int i=0; i<numOfBuyers; i++){
					for(int j=0; j<numOfBuyers; j++){
						String key = i+"_"+j;
						HashMap<Integer, Double> r= new HashMap<Integer, Double>();
						for(int k=0; k<2; k++){
							r.put(k,0.0);
						}
						BAsameRatings.put(key, r);
					}
				}
			}
		}
		else if (envType.equalsIgnoreCase("multinominal")){
			Instances header = initialInstancesHeader();
			Instances header2 = initialInstancesHeader2();
			instTransactions = new Instances(header);
			instBalances = new Instances(header2);

			bsr = new HashMap<String, HashMap<String, Double>>();
			for(int i=0; i<numOfBuyers; i++){
				for(int j=0; j<numOfSellers; j++){
					String key = i+"_"+j;
					HashMap<String, Double> r = new HashMap<String, Double>();
					for(int k=0; k<mnrLen; k++){
						for(int l=0; l<Parameter.NO_OF_CRITERIA; l++){
							String critkey = k+"_"+l;
							r.put(critkey, 0.0);
						}
					}
					bsr.put(key, r);
				}
			}
		}
		else if (envType.equalsIgnoreCase("real")){
			//initialize the header information of instances
			Instances header = initialInstancesHeader();
			Instances header2 = initialInstancesHeader2();
			instTransactions = new Instances(header);
			instBalances = new Instances(header2);

			assignTruth();

			//initialize the double array;
			bsr = new HashMap<String, HashMap<String, Double>>();
			BSnumR = new HashMap<String, Integer>();
			BSnumBDU = new HashMap<String, HashMap<Integer, Double>>();

			BSnumBel = new HashMap<String,HashMap<Integer, Double>>();
			BSnumDisBel = new HashMap<String,HashMap<Integer, Double>>();
			BSnumUnc = new HashMap<String,HashMap<Integer, Double>>();
			bsr2 = new HashMap<String, HashMap<Integer, Integer>>();
			for(int i=0; i<instTransactions.attribute(Parameter.m_bidIdx).numValues(); i++){
				for(int j=0; j< instTransactions.attribute(Parameter.m_sidIdx).numValues();	 j++){
					String key = i+"_"+j;
					HashMap<String, Double> r = new HashMap<String, Double>();
					HashMap<Integer, Double> bdu = new HashMap<Integer, Double>();
					HashMap<Integer, Double> bel = new HashMap<Integer, Double>();
					HashMap<Integer, Double> disbel = new HashMap<Integer, Double>();
					HashMap<Integer, Double> unc = new HashMap<Integer, Double>();
					HashMap<Integer, Integer> b = new HashMap<Integer, Integer>();

					for(int k=0; k<3; k++){
						bdu.put(k,0.0);
					}
					for(int k=0; k<Parameter.NO_OF_CRITERIA;k++){
						String critkey = 0+"_"+k;
						r.put(critkey, 0.0);
						bel.put(k,0.0);
						disbel.put(k,0.0);
						unc.put(k,0.0);
						b.put(k,0);
					}
					BSnumBel.put(key, bel);
					BSnumDisBel.put(key, disbel);
					BSnumUnc.put(key, unc);
					bsr2.put(key, b);
					bsr.put(key, r);
					BSnumR.put(key, 0);
					BSnumBDU.put(key, bdu);
				}
			}	
		}

		for(int i = 0; i < numBuyers; i++){
			int bid = i;
			if(bid < Parameter.NO_OF_DISHONEST_BUYERS || bid >= numBuyers){
				Buyer b = new Buyer(this);
				b.setId(i);
				b.setIshonest(false);
				b.setAccount(new Account());
				b.getAccount().setBalance(Parameter.INITIAL_BALANCE);
				b.setAttackName(attackName);
				b.setEcommerce(this, true);
				b.setAttackModel(b.attackSetting(attackName));
				buyerList.add(b);
			} else{
				Buyer b = new Buyer(this);
				b.setId(i);
				b.setIshonest(true);
				b.setAccount(new Account());
				b.getAccount().setBalance(Parameter.INITIAL_BALANCE);
				b.setDefenseName(defenseName);
				b.setEcommerce(this, true);
				b.setDefenseModel(b.defenseSetting(defenseName));
				buyerList.add(b);		
			}
		}
		for(int k = 0; k < numSellers; k++){
			int sid = k;
			if(sid < Parameter.NO_OF_DISHONEST_SELLERS){
				Seller s = new Seller(this);
				s.setId(sid);
				s.setIshonest(false);
				s.setAccount(new Account());
				s.getAccount().setBalance(Parameter.INITIAL_BALANCE);
				s.setEcommerce(this, false);
				Product p = new Product();
				p.setId(sid);
				p.setPrice(Math.round(PseudoRandom.randDouble(Parameter.min_price, Parameter.max_price)*100)/100);
				s.getProductsOnSale().add(p);
				p.setS(s);
				sellerList.add(s);		
				productList.add(p);

			} else{
				Seller s = new Seller(this);
				s.setId(sid);
				s.setIshonest(true);
				s.setAccount(new Account());
				s.getAccount().setBalance(Parameter.INITIAL_BALANCE);
				s.setEcommerce(this, false);
				Product p = new Product();
				p.setId(sid);
				p.setPrice(Math.round(PseudoRandom.randDouble(Parameter.min_price, Parameter.max_price)*100)/100);
				p.setS(s);
				s.getProductsOnSale().add(p);
				sellerList.add(s);		
				productList.add(p);
			}			
		}
		for(int i=0; i<numBuyers; i++){
			buyerList.get(i).setListOfBuyers(buyerList);
			buyerList.get(i).setListOfSellers(sellerList);
		}
		for(int i=0; i<numSellers; i++){
			sellerList.get(i).setListOfBuyers(buyerList);
			sellerList.get(i).setListOfSellers(sellerList);
		}


		for(int i=0; i<productList.size(); i++){
			productList.get(i).setListOfProducts(productList);
		}			
		for(int i=0; i<numOfSellers; i++){
			double[] rate = new double[Parameter.NO_OF_CRITERIA];
			for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
				rate[j] = (double)Parameter.RATING_BINARY[2];
			}
			sellersTrueRating.put(i, rate);
		}
		ca.setBuyerList(buyerList);

		if(Parameter.includeEA(defenseName) || Parameter.includeWMA(defenseName) || defenseName.equalsIgnoreCase("metrustedgraph")){
			int tnType = 1;
			for(int i = 0; i < numBuyers; i++){
				buyerList.get(i).InitialTrustNetwork(tnType);
			}
			for(int i = 0; i < numBuyers; i++){		
				int day = 0;
				buyerList.get(i).resetWitness(day);
			}
		}	
	}

	//method to create the header section of the .arff file for real environment.
	public Instances initialInstancesHeaderReal(){

		FastVector attInfo = new FastVector(); 							
		attInfo.addElement(new Attribute("day"));			

		FastVector attSeller = new FastVector(); 											
		for(int i = 0; i < Parameter.TOTAL_NO_OF_SELLERS; i++){
			String str = "s" + Integer.toString(i);
			attSeller.addElement(str);
		}
		attInfo.addElement(new Attribute("seller", attSeller));

		FastVector attBuyer = new FastVector(); 						
		for(int i = 0; i < Parameter.TOTAL_NO_OF_BUYERS; i++){
			String str = "b" + Integer.toString(i);
			attBuyer.addElement(str);			
		}
		attInfo.addElement(new Attribute("buyer", attBuyer));

		for(int i = 0; i < Parameter.NO_OF_CRITERIA; i++){
			attInfo.addElement(new Attribute("true_rep"+"_c"+i));
		}

		for(int i = 0; i < Parameter.NO_OF_CRITERIA; i++){									
			String str = "criteria" + Integer.toString(i);
			attInfo.addElement(new Attribute("predicted_rep"+"_c"+i));		
		}

		String instsName = new String("eCommerce.arff");									
		Instances header = new Instances(instsName, attInfo, Parameter.TOTAL_RATINGS);
		return header;
	}

	private void attack(int day){
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

    // the data section containing the details of the transactions of the .arff file are created through this method
	public Instances generateInstances(String attackName, int noOfRuns, int defense, int attack, String defenseName, List<String> evaluateName){
		System.out.println("In generate instances real");
		Instances header = initialInstancesHeader();
		Instances header2 = initialInstancesHeader2();
		instTransactions = new Instances(header);
		instBalances = new Instances(header2);
		data = new Instances(header);
		String relationName = "nS_" + Parameter.TOTAL_NO_OF_SELLERS + "nD_" + Parameter.NO_OF_DAYS + "nB_" + Parameter.TOTAL_NO_OF_BUYERS;
		data.setRelationName(relationName);
		try{
			//read each rating in the real data file
			FileInputStream fstream = new FileInputStream(this.filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			br.readLine();  //for skipping the meta data in the real daa text file
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();
			String strLine;
			int day2=0;
			while ((strLine = br.readLine()) != null)   {
				StringTokenizer st = new StringTokenizer(strLine,", ");
				int i=0;int day1=0,bVal=0,sVal=0;
				Instance inst = new Instance(data.numAttributes());
				inst.setDataset(data);
				double realRating[]=new double[Parameter.NO_OF_CRITERIA];
				if(st.hasMoreTokens())st.nextToken();
				while (st.hasMoreTokens()) {
					if(i==1)
					{//day of transaction
						day1=Integer.parseInt(st.nextToken())-1;
						inst.setValue(Parameter.m_dayIdx, day1);
						if(day1>day2){ //a new day
							System.out.println(day1);
							//same as setDay() method in environment class.
							if(day2 < Parameter.NO_OF_DAYS){
								dailyRepDiffDH.put(day1, dailyRepDiffDH.get(day2));
								dailyRepDiffH.put(day1, dailyRepDiffH.get(day2));
							}
							if(day1< Parameter.NO_OF_DAYS){

								for(int i1=0; i1<numOfSellers; i1++){
									sellerList.get(i1).getDailySales().put(day1, sellerList.get(i1).getDailySales().get(day2));
								}
							}

							day2=day1;
							if (day != 0)
							{
								for (int count = 0; count<buyerList.size();count++)
								{
									buyerList.get(count).getAccount().addToBalance(Parameter.CREDITS_PER_TURN);
								}
							}

							//update gui
							ca.getDailyRepDH().put(day, getDailyReputation().get(0));
							ca.getDailyRepH().put(day, getDailyReputation().get(1));
							ca.getDailyRepDiffDH().put(day, getDailyReputationDiff().get(0));
							ca.getDailyRepDiffH().put(day, getDailyReputationDiff().get(1));
							ca.getDailyMCCDH().put(day,  getMcc().getDailyMCC(day).get(0));
							ca.getDailyMCCH().put(day,  getMcc().getDailyMCC(day).get(1));
							ca.getRobustness().put(day, getRobustness().getRobustness(day));
							ca.getDailyFNRDH().put(day,  getMcc().getDailyFNR(day).get(0));
							ca.getDailyFNRH().put(day,  getMcc().getDailyFNR(day).get(1));
							ca.getDailyAccDH().put(day,  getMcc().getDailyAcc(day).get(0));
							ca.getDailyAccH().put(day,  getMcc().getDailyAcc(day).get(1));
							ca.getDailyFPRDH().put(day,  getMcc().getDailyFPR(day).get(0));
							ca.getDailyFPRH().put(day,  getMcc().getDailyFPR(day).get(1));
							ca.getDailyPrecDH().put(day,  getMcc().getDailyPrec(day).get(0));
							ca.getDailyPrecH().put(day,  getMcc().getDailyPrec(day).get(1));
							ca.getDailyFDH().put(day,  getMcc().getDailyF(day).get(0));
							ca.getDailyFH().put(day,  getMcc().getDailyF(day).get(1));
							ca.getDailyTPRDH().put(day,  getMcc().getDailyTPR(day).get(0));
							ca.getDailyTPRH().put(day,  getMcc().getDailyTPR(day).get(1));
							ca.updateDailyResults(day2-1, attackName, noOfRuns, defense, attack, defenseName, evaluateName);

							attack(day);

						}
						this.day = day1;
						numOfRepPerDay = 0;

					}
					if(i==2)
					{ //seller id
						sVal=Integer.parseInt(st.nextToken())-1;
						inst.setValue(Parameter.m_sidIdx, sVal);
					}
					if(i==3)
					{ //buyer id
						bVal=Integer.parseInt(st.nextToken())-1+Parameter.NO_OF_DISHONEST_BUYERS;
						inst.setValue(Parameter.m_bidIdx, bVal);
					}
					if(i>3 && i<=Parameter.NO_OF_CRITERIA+3)
					{ //true rating of the seller for the various criteria
						double value=Double.parseDouble(st.nextToken());

						//convert from multinominal rating to binary rating
						//because MAE uses values from 0 to 1
						realRating[i-4]=(value+1)/5;
					}
					i++;
					if(i>(Parameter.NO_OF_CRITERIA+3))break;
				}
				Seller s = containsSeller(sVal);

				for(int k=0;k<Parameter.NO_OF_CRITERIA;k++){
					sellersTrueRep.put(s.getId(),realRating); //for a single criteria, you can extend to multiple criteria
				}
				if(bVal >=500) continue;
				Buyer b = containsBuyer(bVal);
				b.setDefenseModel(b.defenseSetting(defenseName));
				this.day=day1;
				//predict the real rating
				double prediction=0;
				double[] pred = new double[Parameter.NO_OF_CRITERIA];
				numOfRep ++; numOfRepPerDay ++;
				if(defenseName.equalsIgnoreCase("metrustedgraph") || defenseName.equalsIgnoreCase("MSR") || defenseName.equalsIgnoreCase("reece")){
					prediction = b.getDefenseModel().predictRealSellerReputation(b,this,s,0);

				}
				else{
					for(int k=0;k<Parameter.NO_OF_CRITERIA;k++){
						prediction = b.getDefenseModel().predictRealSellerReputation(b,this,s,k);
						pred[k] = prediction*(Parameter.RATING_MULTINOMINAL.length);
						inst.setValue(Parameter.m_ratingIdx+k, prediction);
					}
				}
				inst.setValue(Parameter.m_bHonestIdx, "honest");
				inst.setValue(Parameter.m_sHonestIdx, 1);
				Transaction t = new Transaction();
				Product p =s.getProductsOnSale().get(0);
				int productQty = PseudoRandom.randInt(1,Parameter.product_buy_limit);
				double 	price = p.getPrice();
				double cost = p.getPrice() * productQty;
				t.create(b, s, p, productQty, p.getPrice(), day1, cost);
				t.updateTransRatings(pred);
				if (cost<b.getAccount().getBalance()){
					b.getAccount().editBalance(cost, t);
					s.getAccount().addToBalance(cost);
					b.getProductsPurchased().add(p);
				}
				transactionList.add(t);
				b.addTrans(t);
				String bHonestVal=Parameter.agent_dishonest;
				double sHonestVal=0;
				String sHonesty =Parameter.agent_dishonest;
				if(bVal>=Parameter.NO_OF_DISHONEST_BUYERS)bHonestVal=Parameter.agent_honest;
				if(sVal>=Parameter.NO_OF_DISHONEST_SELLERS){sHonestVal=1;
				sHonesty=Parameter.agent_dishonest;
				}
				instTransactions.add(inst);
				this.createData(day1, String.valueOf(bVal), bHonestVal, String.valueOf(sVal), sHonestVal, pred);
				updateArray(inst);
				this.createData2(day1, String.valueOf(bVal), b.getAccount().getBalance(), Integer.toString(this.getSellerList().get(sVal).getProductsOnSale().get(0).getId()),this.getSellerList().get(sVal).getProductsOnSale().get(0).getPrice(), String.valueOf(sVal), sHonesty, s.getAccount().getBalance());

			}

			in.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		data.sort(Parameter.m_dayIdx);

		return data;
	}


	@Override
	public void importConfigSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createEnvironment() {
		// TODO Auto-generated method stub

	}



	@Override
	public void eCommerceSetting(String attackName, String defenseName) {


	}

	@Override
	public void updateDailyReputationDiff(ArrayList<Double> trustVals, int sid, int i){
		dailyRepDH.put(day, dailyRepDH.get(day) + trustVals.get(0));
		dailyRepH.put(day, dailyRepH.get(day)+trustVals.get(1));
		double avgTrueRep =0;
		avgTrueRep += sellersTrueRep.get(sid)[i];

		dailyRepDiffDH.put(day, dailyRepDiffDH.get(day) + Math.abs(Math.abs(avgTrueRep) - Math.abs((trustVals.get(0)))));
		avgTrueRep = 0;
		avgTrueRep += sellersTrueRep.get(sid)[i];

		dailyRepDiffH.put(day, dailyRepDiffH.get(day) + Math.abs((avgTrueRep) - Math.abs((trustVals.get(1)))));
	}

	//
	@Override
	public ArrayList<Double> getDailyReputation(){
		ArrayList<Double> dr = new ArrayList<Double>();
		if(defenseName.equalsIgnoreCase("metrustedgraph") || defenseName.equalsIgnoreCase("MSR") || defenseName.equalsIgnoreCase("reece")){
			double val0 = dailyRepDH.get(day)/(numOfRep) ;
			double val1 = dailyRepH.get(day)/(numOfRep );
			dr.add(0, val0);
			dr.add(1, val1);
		}
		else{
			double val0 = dailyRepDH.get(day)/(numOfRep * Parameter.NO_OF_CRITERIA) ;
			double val1 = dailyRepH.get(day)/(numOfRep * Parameter.NO_OF_CRITERIA) ;
			dr.add(0, val0);
			dr.add(1, val1);
		}
		return dr;
	}

	@Override
	public ArrayList<Double> getDailyReputationDiff(){
		ArrayList<Double> drd = new ArrayList<Double>();
		//because not all buyers evaluate reputation of seller everyday (unlike simulated env),
		//have to divide by the number of buyers that have evaluated reputation of sellers.
		if(defenseName.equalsIgnoreCase("metrustedgraph") || defenseName.equalsIgnoreCase("MSR") || defenseName.equalsIgnoreCase("reece")){
			double val0 = dailyRepDiffDH.get(this.day) / (numOfRep) ;
			double val1 = dailyRepDiffH.get(this.day) / (numOfRep) ;
			drd.add(0, val0);
			drd.add(1,val1);
		}
		else {
			double val0 = dailyRepDiffDH.get(this.day) / (numOfRep * Parameter.NO_OF_CRITERIA) ;
			double val1 = dailyRepDiffH.get(this.day) / (numOfRep * Parameter.NO_OF_CRITERIA) ;
			drd.add(0, val0);
			drd.add(1,val1);
		}


		return drd;
	}



	@Override
	Instances generateInstances() {
		// TODO Auto-generated method stub
		return null;
	}



}//class