/**
 * Weighted Majority Algorithm (WMA) model
 * Agents use referrals to find witnesses
 * then combine the witnesses�� testimonies to evaluate the party of interest
 * Two components considered: 
 * 1) local trust rating if direct interactions had happened
 * 2) testimonies from other agents if no transaction
 */
package defenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import main.Parameter;
import agent.Buyer;
import agent.Seller;
import weka.core.Instance;
import weka.core.Instances;
import distributions.PseudoRandom;
import environment.*;

public class WMA extends Defense{

	private Buyer buyer;
	private int depthLimit = Parameter.depthLimit;
	private int neighborSize = Parameter.neighbourLimit;
	private trustNet tn = null;
	private HashMap<String, HashMap<Integer, Integer>> bsr = null;
	private HashMap<String, HashMap<Integer, Double>> bel =null;
	private HashMap<String, HashMap<Integer, Double>> disbel = null;
	private HashMap<String, HashMap<Integer, Double>> unc = null;
	private HashMap<Integer, HashMap<Integer, Integer>> neighborFirstLevel = null;

	public void readInstances(){
		Instances transactions = ecommerce.getInstTransactions();
		totalBuyers = transactions.attribute(Parameter.m_bidIdx).numValues();
		totalSellers = transactions.attribute(Parameter.m_sidIdx).numValues();	
		m_NumInstances = transactions.numInstances();
		trustOfAdvisors = new ArrayList<Double>();
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}
		bsr = ecommerce.getBsr2();
		if(neighborSize > totalBuyers){
			neighborSize = totalBuyers;
		}
		neighborFirstLevel = new HashMap<Integer, HashMap<Integer, Integer>>();
		for(int i=0; i<totalBuyers; i++){
			HashMap<Integer, Integer> r = new HashMap<Integer, Integer>();
			for(int k=0; k<neighborSize; k++){
				int aid = PseudoRandom.randInt(0, totalBuyers-1);
				r.put(k,aid);
			}
			neighborFirstLevel.put(i, r);
		}

		bel = ecommerce.getBSnumBel();
		disbel = ecommerce.getBSnumDisBel();
		unc = ecommerce.getBSnumUnc();
	}

	@Override
	public double giveFairRating(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		if (bHonestVal == Parameter.agent_dishonest){
			System.out.println("error, must be honest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double fairRating = ecommerce.getSellersTrueRating(sVal,0);
		// add the rating to instances
		inst.setValue(Parameter.m_ratingIdx, fairRating);	
		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));	
		ecommerce.updateArray(inst);

		//update the trust net
		tn.update(buyer, sVal,0);
		return fairRating;
	}
	
	@Override
	public double[] giveFairRatingsMultiCriteria(Instance inst){

		// step 1: insert rating from honest buyer		
		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		// find the dishonest buyer in <day>, give unfair rating
		if (bHonestVal == Parameter.agent_dishonest){
			System.out.println("error, must be honest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));

		double[] fairRating = ecommerce.getSellersTrueRating().get(sVal);
		for(int i=0; i<Parameter.NO_OF_CRITERIA;i++){
			inst.setValue(Parameter.m_ratingIdx+i, fairRating[i]);
		}
		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));	
		ecommerce.updateArray(inst);
		tn.update(buyer, sVal,0);

		return fairRating;

	}
	/** INFO ON WMA mechanism:
	 * The basic idea is that all agents are created equal in principle to help each other out, so they form ratings of each other that they interact with, 
	 * and if they do not frequently interact, the agents can draw on knowledge of other agents, called witnesses in this model, on their opinion of the agent in question. 
	 * In other words, agents use referrals to find witnesses, 
	 * and then combine the witnesses’ testimonies to evaluate the party of interest. 
     * Hence in this trust model, it utilizes 2 components to evaluating the trustiness of an agent. 
     * First is the local trust rating which represents an agent’s personal rating  
     * of another agent if direct interactions had happened between the 2 agents. 
     * The second component is the testimonies from other agents in case the agent has no transaction with the agent of interest before. 
     * Combining these two components will form the total trust rating.
     * To find and determine who are the witnesses and their worthiness, 
     * the trust network has to be formed, where each agent has its own neighbours and acquaintances. 
     * Acquaintances refer to witnesses the agent has made own direct interaction with before, 
     * while neighbours are those where agent can seek testimonies from. Whether the testimony given by the witnesses are reliable or not is determined by 
     * the Dempster-Shafer Theory, or also known as the theory of belief functions, which computes a probability of belief based on each witness’s testimony.
     * As WMA deals with making an improving series of predictions based on a set of advisors, the above mentioned will first assign weights to the advisors and 
     * then make a prediction based on the weighted sum of the ratings provided by them. The second part of WMA fine-tunes the weights after an unsuccessful prediction 
     * so that the relative weight assigned to the successful advisors is increased and the relative weight assigned to the unsuccessful advisors is decrease.
	 * 
	 */

    //calculate trust of the seller based on the WMA mechanism
	@Override
	public double calculateTrust(Seller s, Buyer b, int criteria){
		readInstances();
		int bid = b.getId();
		int sid = s.getId();
		double rep_abs = 0.5;
		for(int k=0; k<totalBuyers; k++){
			int aid = k;
			trustOfAdvisors.set(aid, 1.0);
			String key = aid+"_"+sid;
			if(bel.get(key).get(criteria) + disbel.get(key).get(criteria) + unc.get(key).get(criteria)!=0.0){
				trustOfAdvisors.set(aid, 0.5);
			}
		}
		tn.update(b, sid,criteria);

		int depth =1;
		Vector<Integer> witnesses = new Vector<Integer>();
		buildTrustNet(bid, depth, witnesses);
		ArrayList<Double> bdu = new ArrayList<Double>();
		bdu.add(0.0);
		bdu.add(0.0);
		bdu.add(1.0);
		for(int k=0; k<witnesses.size(); k++){
			int aid = witnesses.get(k);
			String key = aid+"_"+sid;
			if(aid == bid) continue;
			if(bsr.get(key).get(criteria) ==0) continue;
			if(bel.get(key).get(criteria) + disbel.get(key).get(criteria) + unc.get(key).get(criteria) !=0){
				ArrayList<Double> bdu_as = new ArrayList<Double>();
				bdu_as.add(bel.get(key).get(criteria));
				bdu_as.add(disbel.get(key).get(criteria));
				bdu_as.add(unc.get(key).get(criteria));
				ArrayList<Double> wbdu_as = tn.weightBDU(trustOfAdvisors.get(aid), bdu_as);
				bdu = tn.DStheory(bdu, wbdu_as);
			}
		}
		rep_abs = (bdu.get(0) + bdu.get(2)) / (1.0 + bdu.get(2));

		double sum_ratings = 0;
		double sum_trust = 0;
		for(int k=0; k<witnesses.size(); k++){
			int aid = witnesses.get(k);
			if(aid ==bid) continue;
			String key = aid+"_"+sid;
			if(bsr.get(key).get(criteria) == 0) continue;
			sum_ratings += trustOfAdvisors.get(aid) * getRatings(aid, sid);
			sum_trust += trustOfAdvisors.get(aid);
		}
		if(sum_trust == 0.0){
			rep_abs = 0.5;
		}
		else {
			rep_abs = sum_ratings / sum_trust;
		}

		return rep_abs;
	}

	private void buildTrustNet(int bid, int depth, Vector<Integer> witnesses){
		if(depth >= depthLimit) return;
		HashMap<Integer,Integer> nfl = neighborFirstLevel.get(bid);
		for(int k=0; k<nfl.size(); k++){
			int aid = nfl.get(k);
			if(witnesses.contains(aid) == false){
				witnesses.add(aid);
				if(aid>=totalBuyers){
					aid = (aid - totalBuyers) % dhBuyer;
				}
				buildTrustNet(aid, depth+1, witnesses);
			}
		}
	}

	private class trustNet{
		private int neighborSize;
		private int depthLimit;		
		private ArrayList<Double> weights;
		public trustNet(int dl){			

			neighborSize = buyer.getTrustNetwork().size();
			depthLimit = dl;
			int numBuyers = ecommerce.getNumOfBuyers();
			weights = new ArrayList<Double>();
			for(int j = 0; j < numBuyers; j++){	
				weights.add(1.0);
			}	
			buyer.setTrusts(weights);
		}


		private ArrayList<Double> weightBDU(double w, ArrayList<Double> bdu){
			double sum = bdu.get(0) + bdu.get(1) + bdu.get(2);
			if(sum!=0){
				bdu.set(0, bdu.get(0)/sum);
				bdu.set(1,  bdu.get(1)/sum);
				bdu.set(2, bdu.get(2)/sum);
			}
			ArrayList<Double> weightbdu = new ArrayList<Double>();
			weightbdu.add(w*bdu.get(0));
			weightbdu.add(w*bdu.get(1));
			weightbdu.add(1.0-weightbdu.get(0) - weightbdu.get(1));
			return weightbdu;
		}

		private ArrayList<Double> DStheory(ArrayList<Double> bdubs, ArrayList<Double> bduas){
			ArrayList<Double> bducom = new ArrayList<Double>();
			double null_intersection = bdubs.get(0) * bduas.get(1) + bdubs.get(1) * bduas.get(0);
			if(null_intersection ==1.0){
				bducom.add(0.0);
				bducom.add(0.0);
				bducom.add(1.0);
			}
			else{
				bducom.add(bdubs.get(0) * bduas.get(0) + bdubs.get(0) * bduas.get(2) + bdubs.get(2) * bduas.get(0));
				bducom.add(bdubs.get(1) * bduas.get(1) + bdubs.get(1) * bduas.get(2) + bdubs.get(2) * bduas.get(1));
				bducom.add(bdubs.get(2) * bduas.get(2));
				for(int i=0; i<3; i++){
					bducom.set(i, (bducom.get(i)/(1.0-null_intersection)));
				}
			}
			return bducom;
		}

		private void buildTrustNet(Buyer buyer, int depth, Vector<Integer> witnesses){
			if(depth>=depthLimit) return;
			ArrayList<Integer> sn = buyer.getTrustNetwork();
			for(int i=0; i<sn.size(); i++){
				int aid = sn.get(i);
				if(witnesses.contains(aid) == false){
					witnesses.add(aid);
					int db = Parameter.NO_OF_DISHONEST_BUYERS;
					int hb = Parameter.NO_OF_HONEST_BUYERS;
					if(aid >= db+hb){
						aid = (aid - (db +hb)) %db;
					}
					Buyer advisor = buyer.getAdvisor(aid);
					buildTrustNet(advisor, depth+1, witnesses);
				}
			}
		}

		ArrayList<Integer> maxFastSort(ArrayList<Double> x, int m){
			int len = x.size();
			ArrayList<Integer> idx = new ArrayList<Integer>();
			for(int j=0; j<len; j++){
				idx.add(j);
			}
			for(int i=0; i<m; i++){
				for(int j=i+1; j<len; j++){
					if(x.get(idx.get(i)) < x.get(idx.get(j))){
						int id = idx.get(i);
						idx.set(i, idx.get(j));
						idx.set(j, id);
					}
				}
			}
			return idx;
		}

		private void updateWeight(int bid, int sid, Vector<Integer> witnesses, int criteria){
			double rho =0.5;
			String key = bid+"_"+sid;
		
			double repbs = (bel.get(key).get(criteria) + unc.get(key).get(criteria)) / (1.0+unc.get(key).get(criteria));
			if(repbs >= Parameter.m_omega[1]){
				rho = 1.0;
			}
			else if (repbs < Parameter.m_omega[0]){
				rho = 0.0;
			}
			else {
				rho = 0.5;
			}

			for(int k=0; k<witnesses.size(); k++){
				int aid = witnesses.get(k);
				if(aid == bid) continue;
				String key2 = aid+"_"+sid;
				double pias = (bel.get(key2).get(criteria) + unc.get(key2).get(criteria)) / (1.0 +unc.get(key2).get(criteria));
				double theta = 1.0 - Math.abs(pias - rho) /2.0;
				trustOfAdvisors.set(aid, (trustOfAdvisors.get(aid) * theta));
			}
		}

		private void selectReliableNeighbor(int bid){
			ArrayList<Integer> idx = maxFastSort(trustOfAdvisors, neighborSize);
			for(int k=0; k<neighborSize; k++){
				neighborFirstLevel.get(bid).put(k, idx.get(k));
			}
		}

		public void update(Buyer b, int sid, int criteria){
			int depth = 1;
			Vector<Integer> witnesses = new Vector<Integer>();
			buildTrustNet(b, depth, witnesses);
			updateWeight(b.getId(), sid, witnesses,criteria);
			tn.selectReliableNeighbor(b.getId());
		}
	}

	//choose seller based on the reputation predicted by WMA.
	public Instance chooseSeller(int day, Buyer honestBuyer, Environment ec) {
		this.ecommerce = ec;
		this.day = day;
		this.buyer = honestBuyer;
		if(day == 0){
			tn = new trustNet(depthLimit);
		}
		//calculate the trust values on target seller	
		ArrayList<Double> trustValues = new ArrayList<Double>();
		ArrayList<Double> mccValues = new ArrayList<Double>();
		ArrayList<Double> FNRValues = new ArrayList<Double>();
		ArrayList<Double> FPRValues = new ArrayList<Double>();
		ArrayList<Double> accValues = new ArrayList<Double>();
		ArrayList<Double> precValues = new ArrayList<Double>();
		ArrayList<Double> fValues = new ArrayList<Double>();
		ArrayList<Double> TPRValues = new ArrayList<Double>();

		ArrayList<Integer> chosenSeller = new ArrayList<Integer>(); 
		int criteria = Parameter.NO_OF_CRITERIA;
		double honestValuesMAE=0, dishonestValuesMAE=0,honestValuesMCC=0,dishonestValuesMCC=0;
		double honestValuesFNR=0,dishonestValuesFNR=0, honestValuesFPR=0,dishonestValuesFPR=0;
		double honestValuesAcurracy=0,dishonestValuesAcurracy=0, honestValuesPrecision=0,dishonestValuesPrecision=0;
		double honestValuesF=0, dishonestValuesF=0, honestValuesTPR=0, dishonestValuesTPR=0;
		int bid = honestBuyer.getId();

		for(int i=0; i<totalSellers; i++){
			int sid = i;
			double reputation =0, mcc=0, fnr=0, fpr=0, accuracy=0, prec=0, f=0, tpr=0;
			//calculate for each criteria
			for(int j=0; j<criteria; j++){
				reputation+=calculateTrust(honestBuyer.getListOfSellers().get(sid),honestBuyer, j);
				mcc += ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors);
				fnr += ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors);
				fpr += ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors);
				accuracy += ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors);
				prec += ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors);
				f += ecommerce.getMcc().calculateF(sid, trustOfAdvisors);
				tpr += ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors);
			}
			if(sid<Parameter.NO_OF_DISHONEST_SELLERS){
				dishonestValuesMAE += (reputation/criteria);
				dishonestValuesMCC += (mcc/criteria);
				dishonestValuesFNR += (fnr/criteria);
				dishonestValuesFPR += (fpr/criteria);
				dishonestValuesAcurracy += (accuracy/criteria);
				dishonestValuesPrecision += (prec/criteria);
				dishonestValuesF += (f/criteria);
				dishonestValuesTPR += (tpr/criteria);
			}
			else{
				honestValuesMAE += (reputation/criteria);
				honestValuesMCC += (mcc/criteria);
				honestValuesFNR += (fnr/criteria);
				honestValuesFPR += (fpr/criteria);
				honestValuesAcurracy += (accuracy/criteria);
				honestValuesPrecision += (prec/criteria);
				honestValuesF += (f/criteria);
				honestValuesTPR += (tpr/criteria);
			}
			if(reputation/criteria > 0.5){
				chosenSeller.add(sid);
			}
		}

		trustValues.add(dishonestValuesMAE/Parameter.NO_OF_DISHONEST_SELLERS);
		trustValues.add(honestValuesMAE/Parameter.NO_OF_HONEST_SELLERS);
		mccValues.add(dishonestValuesMCC/Parameter.NO_OF_DISHONEST_SELLERS);
		mccValues.add(honestValuesMCC/Parameter.NO_OF_HONEST_SELLERS);
		FNRValues.add(dishonestValuesFNR/Parameter.NO_OF_DISHONEST_SELLERS);
		FNRValues.add(honestValuesFNR/Parameter.NO_OF_HONEST_SELLERS);
		FPRValues.add(dishonestValuesFPR/Parameter.NO_OF_DISHONEST_SELLERS);
		FPRValues.add(honestValuesFPR/Parameter.NO_OF_HONEST_SELLERS);
		accValues.add(dishonestValuesAcurracy/Parameter.NO_OF_DISHONEST_SELLERS);
		accValues.add(honestValuesAcurracy/Parameter.NO_OF_HONEST_SELLERS);
		precValues.add(dishonestValuesPrecision/Parameter.NO_OF_DISHONEST_SELLERS);
		precValues.add(honestValuesPrecision/Parameter.NO_OF_HONEST_SELLERS);
		fValues.add(dishonestValuesF/Parameter.NO_OF_DISHONEST_SELLERS);
		fValues.add(honestValuesF/Parameter.NO_OF_HONEST_SELLERS);
		TPRValues.add(dishonestValuesTPR/Parameter.NO_OF_DISHONEST_BUYERS);
		TPRValues.add(honestValuesTPR/Parameter.NO_OF_HONEST_BUYERS);

		int sellerId=0;
		if(chosenSeller.size()>0){
			sellerId=chosenSeller.get(PseudoRandom.randInt(0, chosenSeller.size()-1));
		}
		else sellerId=PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);

		//update the daily reputation difference
		ecommerce.updateDailyReputationDiff(trustValues);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyTPR(TPRValues, ecommerce.getDay());

		int dVal = day + 1;
		int bVal = bid;
		String bHonestVal =Parameter.agent_honest;  
		int sVal = sellerId;

		double sHonestVal = ecommerce.getSellersTrueRating(sVal, 0);
		//add instance
		Instances transactions = ecommerce.getInstTransactions();
		Instance inst = new Instance(transactions.numAttributes());
		inst.setDataset(transactions);
		inst.setValue(Parameter.m_dayIdx, dVal);
		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);
		for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
			inst.setValue(Parameter.m_ratingIdx+j, Parameter.nullRating());
		}
		return inst;
	}

	//for real environment --- modified athirai
	public double predictRealSellerReputation(Buyer honestBuyer, Environment ec,Seller currentSeller, int criteriaid) {
		this.ecommerce = ec;
		this.buyer = honestBuyer;

		if(tn == null){
			tn = new trustNet(depthLimit);
		}
		//calculate the trust values on target seller		
		ArrayList<Double> trustValues = new ArrayList<Double>();
		ArrayList<Double> mccValues = new ArrayList<Double>();
		ArrayList<Double> FNRValues = new ArrayList<Double>();
		ArrayList<Double> FPRValues = new ArrayList<Double>();
		ArrayList<Double> accValues = new ArrayList<Double>();
		ArrayList<Double> precValues = new ArrayList<Double>();
		ArrayList<Double> fValues = new ArrayList<Double>();
		ArrayList<Double> TPRValues = new ArrayList<Double>();

		if (trustValues.size()==0){
			for(int i=0; i<2; i++){
				trustValues.add(0.0);
				mccValues.add(0.0);
				FNRValues.add(0.0);
				accValues.add(0.0);
				FPRValues.add(0.0);
				precValues.add(0.0);
				fValues.add(0.0);
				TPRValues.add(0.0);

			}
		}
		// consider all sellers as honest and calculate only MAE-honest
		int sid = currentSeller.getId();

		double trust=calculateTrust(ec.containsSeller(sid),honestBuyer, criteriaid);
		trustValues.set(0,trust);
		mccValues.set(0, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
		FNRValues.set(0, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
		accValues.set(0, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
		FPRValues.set(0, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
		precValues.set(0, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
		fValues.set(0, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
		TPRValues.set(0, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));

		//MAE-honest and dishonest is same for clarity
		trustValues.set(1,trust);
		mccValues.set(1, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
		FNRValues.set(1, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
		accValues.set(1, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
		FPRValues.set(1, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
		precValues.set(1, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
		fValues.set(1, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
		TPRValues.set(1, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));


		//update the daily reputation difference
		ecommerce.updateDailyReputationDiff(trustValues,sid,criteriaid);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay()); 
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyTPR(TPRValues, ecommerce.getDay());

		int dVal = day + 1;
		int bVal = honestBuyer.getId();
		String bHonestVal = Parameter.agent_honest;  
		int sVal = currentSeller.getId();
		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);	
		double rVal = Parameter.nullRating();				
		//add instance
		Instances transactions = ecommerce.getInstTransactions();

		Instance inst = new Instance(transactions.numAttributes());
		inst.setDataset(transactions);
		inst.setValue(Parameter.m_dayIdx, dVal);
		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);			
		inst.setValue(Parameter.m_ratingIdx, rVal);
		return trustValues.get(0);
	}

	public double getRatings(int bid, int sid){//play a trick, only for static seller behavior

		double rep = 0.0;
		double x = sid * 1.0 / (totalSellers - 1);
		//choice 1: uniform distribuiton
		rep = x;

		//choice 2: more sellers get reputation close to 0 and 1;		
		if(x < 0.5){
			rep = 2 * x * x;
		}else{
			rep = 1 - 2 * (x - 1) * (x - 1);
		}

		//choice 3: stepwise line
		double k = 0.5;   //k <= 1
		double porportion = 0.5; //how many are low reputation
		if(x < porportion){
			rep = k * x;
		}else{
			rep = k * (x - 1.0) + 1.0;
		}

		double rVal = rep;
		return rVal;
	}

}

