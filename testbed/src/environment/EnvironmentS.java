/**class EnvironmentS: Inherits properties from the Environment class. And uses the default
 * or user entered values to set the parameters involved in the virtual market environment.
 * 
 */
package environment;
import agent.*;
import weka.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import defenses.Reece;
import distributions.PseudoRandom;

import main.Account;
import main.CentralAuthority;
import main.Parameter;
import main.Product;
import main.Transaction;

public class EnvironmentS extends Environment{
	public CentralAuthority ca;
	public EnvironmentS(){
		super();
	}

	// sets the rating type to be used in the environment
	@Override
	public void eCommerceSetting(String attackName, String defenseName) {
		this.attackName = attackName;
		this.defenseName = defenseName;
		parameterSetting(attackName, defenseName);

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
		generateInstances();
		try {
			agentSetting(attackName, defenseName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	// creates the instances which store the transaction details and get stored in the .arff file
	@Override
	public Instances generateInstances() {
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
						for(int l=0; l<Parameter.NO_OF_CRITERIA; l++){
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
					for(int k=0; k<Parameter.NO_OF_CRITERIA; k++){
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
			return instTransactions;

		}
		else if (envType.equalsIgnoreCase("multinominal")){
			Instances header = initialInstancesHeader();
			Instances header2 = initialInstancesHeader2();
			instTransactions = new Instances(header);
			instBalances = new Instances(header2);

			assignTruth();

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
			return instTransactions;
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
			return instTransactions;
		}
		return null;
	}

	// initializes the buyer, seller objects with the necessary values and stores them in a list.
	private void agentSetting(String attackName, String defenseName) throws ClassNotFoundException, NoSuchMethodException, SecurityException{

		int numBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
		int numSellers = numOfSellers; 
		buyerList = new ArrayList<Buyer>();
		sellerList = new ArrayList<Seller>();
		productList = new ArrayList<Product>();
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
		//set up the global information for buyers;
		for(int i = 0; i < numBuyers; i++){	
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
		//for the evolutionary algorithm
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


	@Override
	void importConfigSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	void createEnvironment() {
		// TODO Auto-generated method stub

	}


	public void updateDailyReputationDiff(ArrayList<Double> trustVals, int sid, int criteriaid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eCommerceSetting(int noOfRuns, int defense, int attack,
			List<String> evaluateName, String attackName, String defenseName,
			String filename) {
		// TODO Auto-generated method stub

	}

	public void setUpEnvironment(String attackName, String defenseName)throws Exception{
		System.out.println("setup env");
		eCommerceSetting(attackName, defenseName);
	}

	public ArrayList simulateEnvironment(CentralAuthority ca, int noOfRuns, int defense, int attack, String attackName, String defenseName, boolean dailyPrint, List<String> evaluateName) throws ClassNotFoundException, NoSuchMethodException, SecurityException,Exception{
		ca.setAttackName(attackName);
		ca.setDefenseName(defenseName);
		this.ca = ca;
		setUpEnvironment(attackName, defenseName);   
		ArrayList transList = new ArrayList();
		ca.setBuyerList(buyerList);

		for (int day = 0; day <= Parameter.NO_OF_DAYS; day++){  
			System.out.println(day);
			if (day != 0)
			{
				for (int count = 0; count<buyerList.size();count++)
				{
					buyerList.get(count).getAccount().addToBalance(Parameter.CREDITS_PER_TURN);
				}
			}
			for (int a = 0; a<PseudoRandom.randInt(1, Parameter.transaction_limit);a++){
				DecimalFormat roundoff = new DecimalFormat("#.##");
				int buyerID = PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_BUYERS-1);
				Transaction t = buyerList.get(buyerID).addTransaction( day);
				transList.add(t);
			}

			//step 2: Attack model (dishonest buyers)    
			if(!attackName.equalsIgnoreCase("noattack"))
				ca.attack(day);      

			//step 3: Defense model (honest buyers)
			long defensetimeStart = new Date().getTime();
			ca.defense(day);   
			long defensetimeEnd = new Date().getTime();

			ca.getDefenseTime_day().set(day,(-defensetimeStart + defensetimeEnd) / 1000.0 );
			ca.getDailyRepDH().put(day, getDailyReputation().get(0));
			ca.getDailyRepH().put(day, getDailyReputation().get(1));
			ca.getDailyRepDiffDH().put(day, getDailyReputationDiff().get(0));
			ca.getDailyRepDiffH().put(day, getDailyReputationDiff().get(1));
			ca.getDailyMCCDH().put(day,  getMcc().getDailyMCC(day).get(0));
			ca.getDailyMCCH().put(day,  getMcc().getDailyMCC(day).get(1));
			ca.getDailyFNRDH().put(day, getDailyReputationDiff().get(0));
			ca.getDailyFNRH().put(day, getDailyReputationDiff().get(1));
			ca.getDailyAccDH().put(day,  getMcc().getDailyMCC(day).get(0));
			ca.getDailyAccH().put(day,  getMcc().getDailyMCC(day).get(1));
			ca.getDailyFPRDH().put(day, getDailyReputationDiff().get(0));
			ca.getDailyFPRH().put(day, getDailyReputationDiff().get(1));
			ca.getDailyPrecDH().put(day,  getMcc().getDailyMCC(day).get(0));
			ca.getDailyPrecH().put(day,  getMcc().getDailyMCC(day).get(1));
			ca.getDailyFDH().put(day, getDailyReputationDiff().get(0));
			ca.getDailyFH().put(day, getDailyReputationDiff().get(1));
			ca.getDailyTPRDH().put(day,  getMcc().getDailyMCC(day).get(0));
			ca.getDailyTPRH().put(day,  getMcc().getDailyMCC(day).get(1));
			ca.getRobustness().put(day, getRobustness().getRobustness(day));
			setDay(day); //update to next day
			ca.avgerWeights(day);
			ca.updateDailyResults( day,  attackName,  noOfRuns,  defense,  attack,  defenseName, evaluateName);


			try {
				String file1name = "data/results/" + defenseName + attackName + noOfRuns + "MAE";
				String file2name = "data/results/" + defenseName + attackName + noOfRuns + "MCC";
				String file3name = "data/results/" + defenseName + attackName + noOfRuns + "FNR";
				String file4name = "data/results/" + defenseName + attackName + noOfRuns + "FPR";
				String file5name = "data/results/" + defenseName + attackName + noOfRuns + "TPR";
				String file6name = "data/results/" + defenseName + attackName + noOfRuns + "Accuracy";
				String file7name = "data/results/" + defenseName + attackName + noOfRuns + "Precision";
				String file8name = "data/results/" + defenseName + attackName + noOfRuns + "F-Measure";

				File file1 = new File(file1name + ".txt");
				File file2 = new File(file2name + ".txt");
				File file3 = new File(file3name + ".txt");
				File file4 = new File(file4name + ".txt");
				File file5 = new File(file5name + ".txt");
				File file6 = new File(file6name + ".txt");
				File file7 = new File(file7name + ".txt");
				File file8 = new File(file8name + ".txt");


				// if file doesnt exists, then create it
				if (day==0) {
					file1.createNewFile();
					file2.createNewFile();
					file3.createNewFile();
					file4.createNewFile();
					file5.createNewFile();
					file6.createNewFile();
					file7.createNewFile();
					file8.createNewFile();
				}

				FileWriter fw1 = new FileWriter(file1.getAbsoluteFile(),true);
				FileWriter fw2 = new FileWriter(file2.getAbsoluteFile(),true);
				FileWriter fw3 = new FileWriter(file3.getAbsoluteFile(),true);
				FileWriter fw4 = new FileWriter(file4.getAbsoluteFile(),true);
				FileWriter fw5 = new FileWriter(file5.getAbsoluteFile(),true);
				FileWriter fw6 = new FileWriter(file6.getAbsoluteFile(),true);
				FileWriter fw7 = new FileWriter(file7.getAbsoluteFile(),true);
				FileWriter fw8 = new FileWriter(file8.getAbsoluteFile(),true);

				BufferedWriter bw1 = new BufferedWriter(fw1);
				bw1.write(dailyRepDiffDH.get(day) + " " + " " + dailyRepDiffH.get(day) + "\n");
				bw1.close();

				BufferedWriter bw2 = new BufferedWriter(fw2);
				bw2.write(getMcc().getDailyMCC(day).get(0) + " " + " " + getMcc().getDailyMCC(day).get(1) + "\n");
				bw2.close();

				BufferedWriter bw3 = new BufferedWriter(fw3);
				bw3.write(getMcc().getDailyFNR(day).get(0) + " " + " " + getMcc().getDailyFNR(day).get(1) + "\n");
				bw3.close();

				BufferedWriter bw4 = new BufferedWriter(fw4);
				bw4.write(getMcc().getDailyFPR(day).get(0) + " " + " " + getMcc().getDailyFPR(day).get(1) + "\n");
				bw4.close();

				BufferedWriter bw5 = new BufferedWriter(fw5);
				bw5.write(getMcc().getDailyTPR(day).get(0) + " " + " " + getMcc().getDailyTPR(day).get(1) + "\n");
				bw5.close();

				BufferedWriter bw6 = new BufferedWriter(fw6);
				bw6.write(getMcc().getDailyAcc(day).get(0) + " " + " " + getMcc().getDailyAcc(day).get(1) + "\n");
				bw6.close();

				BufferedWriter bw7 = new BufferedWriter(fw7);
				bw7.write(getMcc().getDailyPrec(day).get(0) + " " + " " + getMcc().getDailyPrec(day).get(1) + "\n");
				bw7.close();

				BufferedWriter bw8 = new BufferedWriter(fw8);
				bw8.write(getMcc().getDailyF(day).get(0) + " " + " " + getMcc().getDailyF(day).get(1) + "\n");
				bw8.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			if(dailyPrint){
				//print the transactions for different sellers
				System.out.print("Day " + day + ": ");					
				System.out.print("   |sellers' transactions|: ");
				for (int i = 0; i < Parameter.NO_OF_HONEST_SELLERS	+ Parameter.NO_OF_DISHONEST_SELLERS; i++) {
					if(i == Parameter.NO_OF_DISHONEST_SELLERS){
						System.out.print(", ");
					}						
					System.out.print(" ");					
				}		
				System.out.print("   |rep MAE|: " + dailyRepDH.get(day) + "  " + dailyRepH.get(day));
				System.out.print("   |repDiff MAE|: " + dailyRepDiffDH.get(day) + "  " + dailyRepDiffH.get(day));
				System.out.println(" avgWeights: " + ca.getDishonest_avgWt().get(day)+ "  " + ca.getHonest_avgWt().get(day));
			}	
		}

		if(defenseName.equalsIgnoreCase("reece")){
			Reece def = new Reece();
			def.displayMeanDirichlet(this);
		}
		return transList;
	}

}


