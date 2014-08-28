/** class Buyer: inherits properties from Agent class. Contains the variables and methods performed on/by the 
 *  buyers part of the environment.
 */

package agent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;
import environment.Environment;
import weka.core.*;
import main.Product;
import main.Transaction;


import defenses.Defense;
import distributions.PseudoRandom;
import main.Parameter;

import attacks.*;

public class Buyer extends Agent{
	private String attackName;
	private String defenseName;
	private Attack attackModel;
	private Defense defenseModel;

	private ArrayList<Product> productsPurchased = null;
	private ArrayList<Seller> sellersRated = null;

	//for building buyer's trust network
	private Vector<Integer> advisors = null;
	private ArrayList<Double> trusts = null;
	//store the average trust for dishonest/honest advisors according to the dishonest/honest duopoly sellers;
	private double[][] m_SaverTA ;
	private int depthLimit = 4;
	private int neighborSize = 3;
	private ArrayList<Integer> trustNetwork = null;
	private double[] bounds = {0.0, 1.0};// trust network bounds{0.0; 1.0}
	private double fitness;
	//true/false whether the dishonest agents are collusive or not	
	private int TNtype = 1; //0/1/2 means honest trust network/noise/collusive

	private double[] currentRating = new double[Parameter.NO_OF_CRITERIA];

	public Buyer(Environment env){
		this.ecommerce = env;
		sellersRated = new ArrayList<Seller>();
		productsPurchased = new ArrayList<Product>();
		advisors = new Vector<Integer>();
		trusts = new ArrayList<Double>();
		for(int i=0; i<ecommerce.getNumOfBuyers(); i++)
			trusts.add(0.0);
		m_SaverTA = new double [ecommerce.getNumOfBuyers()][2];
	}

	public ArrayList<Product> getProductsPurchased() {
		return productsPurchased;
	}

	public void setProductsPurchased(ArrayList<Product> productsPurchased) {
		this.productsPurchased = productsPurchased;
	}

	public ArrayList<Product> getPurchasedProducts(){
		return productsPurchased;
	}

	// method to select seller's rating and invoke method to create .arff file
	public void rateSeller(int day){
		this.day = day;
		if(this.day > 0){//scan all the history information,
			for (int i = 0; i < history.numInstances(); i++) {
				Instance inst = history.instance(i);
				int dVal = (int) (inst.value(Parameter.m_dayIdx));
				// only complete the current day transaction
				if (dVal != this.day)continue;				
				if (this.ishonest == false) {
					if(Parameter.NO_OF_CRITERIA==1){
						currentRating[0] = attackModel.giveUnfairRating(inst);
					}
					else{
						currentRating = attackModel.giveUnfairRatingMultiCriteria(inst);
					}
				} else {
					if(Parameter.NO_OF_CRITERIA==1){
						currentRating[0] = defenseModel.giveFairRating(inst);
					}
					else{
						currentRating = defenseModel.giveFairRatingsMultiCriteria(inst);
					}
				}				

				//update sellers' history
				int sVal = (int)(inst.value(Parameter.m_sidIdx));
				listOfSellers.get(sVal).addInstance(new Instance(inst));
				String bHonestVal ="";
				if(ishonest == true)
					bHonestVal = Parameter.agent_honest;
				else
					bHonestVal = Parameter.agent_dishonest;  
				double sHonestVal;
				String sellerhonesty="";

				if(ecommerce.getSellerList().get(sVal).isIshonest()==true){
					sellerhonesty = Parameter.agent_honest;
					sHonestVal = 0.0;
				}
				else{
					sHonestVal = 1.0;
					sellerhonesty = Parameter.agent_dishonest;
				}
				ecommerce.createData(dVal,Integer.toString(id),bHonestVal,Integer.toString(sVal),sHonestVal,currentRating);				
				ecommerce.createData2(dVal,Integer.toString(id),account.getBalance(),Integer.toString(ecommerce.getSellerList().get(sVal).getProductsOnSale().get(0).getId()),ecommerce.getSellerList().get(sVal).getProductsOnSale().get(0).getPrice(), Integer.toString(sVal),sellerhonesty,ecommerce.getSellerList().get(sVal).getAccount().getBalance());

				try{
					ecommerce.createARFFfile();
					ecommerce.createBalanceArff();
				}
				catch(Exception e){e.printStackTrace();}
			}
		}		
	}

	//set attack model
	public Attack attackSetting(String attackName){
		Attack attackModel= null;		
		try{
			if(attackName.equalsIgnoreCase("NoAttack")){
				return null;
			}
			Class<?> class1 = Class.forName("attacks."+attackName.trim());
			Constructor<?> cons = class1.getDeclaredConstructor();
			cons.setAccessible(true);
			attackModel = (Attack) cons.newInstance();
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (InstantiationException ex) {
			ex.printStackTrace();
		}
		catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		attackModel.setEcommerce(ecommerce);
		return attackModel;
	}


	//set defense model
	public Defense defenseSetting(String defenseName) {
		Defense defenseModel= null;

		try{
			Class class1 = Class.forName("defenses."+defenseName.trim());

			Constructor<?> cons = class1.getDeclaredConstructor();
			cons.setAccessible(true);
			defenseModel = (Defense) cons.newInstance();
		}
		catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (InstantiationException ex) {
			ex.printStackTrace();
		}
		catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		catch (InvocationTargetException ex) {
			ex.getCause().printStackTrace();
		}
		catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		defenseModel.setEcommerce(ecommerce);
		return defenseModel;
	}

	/* stores the transactions in the form of Transaction objects and instances are created
	 in this method to contain the details of every transaction, later to be stored in .arff file.
	*/
	public Transaction addTransaction(int day){
		int	sellerID = PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);
		Seller s = ecommerce.getSellerList().get(sellerID);
		Product p = s.getProductsOnSale().get(0);
		int productQty = PseudoRandom.randInt(1,Parameter.product_buy_limit);
		double 	price = p.getPrice();
		double cost = p.getPrice() * productQty;
		Transaction t = new Transaction();
		t.create(this, s, p, productQty, p.getPrice(), day, cost);
		t.updateTransRatings(currentRating);
		trans.add(t);
		ecommerce.getTransactionList().add(t);
		if (cost<account.getBalance()){
			this.account.editBalance(cost, t);
			s.getAccount().addToBalance(cost);
			sellersRated.add(s);
			productsPurchased.add(p);
		}

		//store buyer's balance after the transaction
		Instances balances = ecommerce.getInstBalances();
		double sHonestVal = ecommerce.getSellersTrueRating(s.getId(), 0);	

		Instance inst1 = new Instance(balances.numAttributes());
		inst1.setDataset(balances);
		inst1.setValue(Parameter.m_dayIdx, day+1);
		inst1.setValue(Parameter.m_bidIdx, "b" + Integer.toString(id)); 
		inst1.setValue(Parameter.m_bbalIdx, this.getAccount().getBalance());
		inst1.setValue(Parameter.m_pIdx, "p" + Integer.toString(p.getId()));
		inst1.setValue(Parameter.m_ppriceIdx, p.getPrice());			
		inst1.setValue(Parameter.m_sidIdx2, s.getId());	
		inst1.setValue(Parameter.m_sHonestIdx2, sHonestVal);			
		inst1.setValue(Parameter.m_sbalIdx, s.getAccount().getBalance());	
		return t;
	}

	//create transaction that includes buyer, seller and product
	public void perform_model(int day){
		this.day = day;	
		//step 2: perform action == attack/defense
		Instance inst = null;
		if (this.ishonest == false) {
			inst = attackModel.chooseSeller(day, this, ecommerce);			
		} else{
			inst = defenseModel.chooseSeller(day, this, ecommerce);
		}
		this.addInstance(new Instance(inst));
	}

	public void calculateAverageTrusts(int sid){
		int index = sid;
		m_SaverTA[index][0] = 0.0;
		m_SaverTA[index][1] = 0.0;
		int numDA = 0; //number of dishonest advisors;
		int numHA = 0; //number of honest advisors;
		int hb = Parameter.NO_OF_HONEST_BUYERS;	
		for(int j = 0; j < advisors.size(); j++){					
			int aid = advisors.get(j);
			if(aid == this.id)continue;
			if(aid < Parameter.NO_OF_DISHONEST_BUYERS || aid >= Parameter.NO_OF_DISHONEST_BUYERS + hb){
				m_SaverTA[index][0] += trusts.get(aid);
				numDA++;
			} 
			else{
				m_SaverTA[index][1] += trusts.get(aid);
				numHA++;
			}
		}				
		if (numDA != 0) {
			m_SaverTA[index][0] /= (numDA);
		}
		if (numHA != 0) {
			m_SaverTA[index][1] /= (numHA);		
		}
	}

	private ArrayList<Integer> maxFastSort(ArrayList<Double> x, int m) {
		trusts.set(this.id, 0.0);
		int len = x.size();
		if(len > Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS){
			len = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS + (day - 1) * Parameter.NO_OF_DISHONEST_BUYERS;
		}
		ArrayList<Integer> idx = new ArrayList<Integer>();
		for (int j = 0; j < len; j++) {
			idx.add(j);
		}
		for (int i = 0; i < m; i++) {
			for (int j = i + 1; j < len; j++) {
				if (x.get(idx.get(i)) < x.get(idx.get(j))) {
					int id = idx.get(i);
					idx.set(i, idx.get(j));
					idx.set(j, id);
				}
			}
		} 
		trusts.set(id, 1.0);		
		return idx;
	}

	private void selectReliableNeighbor(){
		//select neighbor with high weight from the witness	
		trusts.set(id, 0.0);		
		ArrayList<Integer> idx = maxFastSort(trusts, neighborSize);
		setTrustNetwork(idx);
		trusts.set(id, 1.0);
	}

	public void resetWitness(int day){
		if(day > 0){
			selectReliableNeighbor();
		}
		advisors.clear();
		int depth = 1;
		buildTrustNet(this, depth, advisors);
	}

	public void setAverageTrusts(int sid, double[] averTA){
		int index = sid;
		m_SaverTA[index][0] = averTA[0];
		m_SaverTA[index][1] = averTA[1];
	}

	public double[] getAverageTrusts(int sid){
		int index = sid;
		return m_SaverTA[index];
	}

	public Buyer getAdvisor(int aid){
		int db = Parameter.NO_OF_DISHONEST_BUYERS;
		int hb = Parameter.NO_OF_HONEST_BUYERS;
		if(aid >= db + hb){
			aid = (aid - (db + hb)) % db;
		}
		return listOfBuyers.get(aid);
	}

	public void setTrustNetwork(ArrayList<Integer> sn){
		for(int i = 0; i < neighborSize; i++){
			trustNetwork.set(i, sn.get(i));
		}
	}

	public ArrayList<Integer> getTrustNetwork(){
		return trustNetwork;
	}

	public void setTrustAdvisor(int aid, double trust){

		trusts.set(aid, trust);
	}

	public void setTrusts(ArrayList<Double> ws){
		for(int i = 0; i < trusts.size(); i++){
			trusts.set(i,ws.get(i));
		}
	}

	public ArrayList<Double> getTrusts(){
		return trusts;
	}

	private void findNeighbors(int type){
		//two types = 0/1, from the limit/whole
		Vector<Integer> neighbor = new Vector<Integer>();
		int numBuyers = ecommerce.getNumOfBuyers(); 	
		if(type == 0){//for dishonest buyers; collusion
			Vector<Integer> limit = new Vector<Integer>();
			int db = Parameter.NO_OF_DISHONEST_BUYERS;
			int hb = Parameter.NO_OF_HONEST_BUYERS;	
			if(TNtype == 0){//honest trust network
				for (int i = 0; i < numBuyers; i++) {						
					if (i >= db && i < db + hb) {
						limit.add(i);
					}
				}
			} 
			else { //dishonest trust network
				for (int i = 0; i < numBuyers; i++) {
					if (i < db || i >= db + hb) {
						limit.add(i);
					}
				}
			}
			int numLimit = limit.size();	
			for (int i = 0; i < neighborSize; i++) {
				int addID = 0;
				do {
					int rnd = PseudoRandom.randInt(0, numLimit - 1);
					addID = limit.get(rnd);
				} while (neighbor.contains(addID));
				neighbor.add(addID);
				trustNetwork.set(i, addID);
			}
		} 
		else{//for honest buyers
			numBuyers = Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS;
			for (int i = 0; i < neighborSize; i++) {
				int addID = 0;
				do {
					addID = PseudoRandom.randInt(0, numBuyers- 1);
				} while (neighbor.contains(addID));
				neighbor.add(addID);
				trustNetwork.set(i, addID);
			}
		}		
	}

	private void buildTrustNet(Buyer buyer, int depth, Vector<Integer> advisors){ 
		if(depth > depthLimit)return;			
		ArrayList<Integer> sn = buyer.getTrustNetwork();
		for(int i = 0; i < sn.size(); i++){
			int aid = sn.get(i);				
			if(advisors.contains(aid) == false){
				advisors.add(aid);
				Buyer advisor = buyer.getAdvisor(aid);
				buildTrustNet(advisor, depth + 1, advisors);
			}
		}	
	}

	public void setDepthNeighborSize(int depth, int neighborSize){
		depthLimit = depth;
		this.neighborSize = neighborSize;
	}

	public void InitialTrustNetwork(int snType){
		if(Parameter.includeEA(ecommerce.getDefenseName()) || Parameter.includeWMA(ecommerce.getDefenseName()) || ecommerce.getDefenseName().equalsIgnoreCase("metrustedgraph")){
			TNtype = snType;
			//set the trust Network, trusts, and fitness
			trustNetwork = new ArrayList<Integer>();
			for (int i=0; i<neighborSize; i++){
				trustNetwork.add(0);
			}
			int numBuyers = ecommerce.getNumOfBuyers();
			trusts = new ArrayList<Double>();	
			for(int i=0; i<numBuyers; i++){
				trusts.add(0.0);
			}
			if(this.ishonest == false){
				findNeighbors(0);			
				if (TNtype == 0) { // honest weight and fitness;
					int db = Parameter.NO_OF_DISHONEST_BUYERS;
					int hb = Parameter.NO_OF_HONEST_BUYERS;
					for (int i = 0; i < numBuyers; i++) {
						if (i < db || i >= db + hb) {
							trusts.set(i, 0.0);
						} 
						else {
							trusts.set(i,1.0);
						}					
					}
					trusts.set(id,1.0);
					fitness = 1.0;
				} 
				else if (TNtype == 1) { // noise weight and fitness;
					for (int i = 0; i < numBuyers; i++) {					
						trusts.set(i,PseudoRandom.randDouble(bounds[0], bounds[1]));						
					}
					trusts.set(id,1.0);
					fitness = PseudoRandom.randDouble(0, 1);
				} 
				else {//collusive	
					int db = Parameter.NO_OF_DISHONEST_BUYERS;
					int hb = Parameter.NO_OF_HONEST_BUYERS;
					for (int i = 0; i < numBuyers; i++) {								
						if (i < db || i >= db + hb) {
							trusts.set(i, 1.0);
						} 
						else {
							trusts.set(i, 0.0);
						}
					}
					fitness = 1.0;
				}
			} 
			else{
				findNeighbors(1);

				for (int i = 0; i <numBuyers; i++) {		
					trusts.set(i,PseudoRandom.randDouble(bounds[0], bounds[1]));
				}
				trusts.set(id,1.0);
				fitness = 0.0;

			}		
			//initial witnesses
			advisors = new Vector<Integer>();			
		}
	}

	public Vector<Integer> getAdvisors() {
		return advisors;
	}

	public void setAdvisors(Vector<Integer> advisors) {
		this.advisors = advisors;
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
	public Attack getAttackModel() {
		return attackModel;
	}
	public void setAttackModel(Attack attackModel) {
		this.attackModel = attackModel;
	}
	public Defense getDefenseModel() {
		return defenseModel;
	}
	public void setDefenseModel(Defense defenseModel) {
		this.defenseModel = defenseModel;
	}


}
