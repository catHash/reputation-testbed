/**class ProbCog: The Prob-Cog trust model involves a two layered filtering algorithm that examines the honesty
of the buyer’s advisors through means of probability and cognitions.
 * 
 */
package defenses;

import java.util.*;

import weka.core.Instance;
import weka.core.Instances;

import distributions.PseudoRandom;
import agent.*;
import environment.*;
import main.*;

public class ProbCog extends Defense{


	private double m_mu = Parameter.m_mu;               //parameter in ProbCog, default is 0.6
	private HashMap<String, Double> BS_expVal;
	private HashMap<String, Double> BS_relVal;
	private double popQuality;

	public void readInstances(int criteriaid){

		//get the number of dishonest/honest buyers;
		Instances transactions = ecommerce.getInstTransactions();
		totalBuyers = transactions.attribute(Parameter.m_bidIdx).numValues();
		totalSellers = transactions.attribute(Parameter.m_sidIdx).numValues();		

		m_NumInstances = transactions.numInstances();
		trustOfAdvisors = new ArrayList<Double>();
		//the initial trust values is 0;
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}
		BS_expVal = new HashMap<String, Double>();
		BS_relVal = new HashMap<String, Double>();
		bsr = ecommerce.getBsr();
	

		for(int i = 0; i < totalBuyers; i++){
			int bid = i;
			for(int j = 0; j < totalSellers; j++){
				int sid = j;
				String key = i+"_"+j;
				double value = getExpectedValue(bid, sid,criteriaid);
				BS_expVal.put(key, value);
				double value2 = getBuyerReliabilityValue(bid, sid,criteriaid);
				BS_relVal.put(key, value2);
			}
		}
	}
	//for read instances
	public double getExpectedValue(int bid,int sid, int criteriaid){

		String key = bid+"_"+sid;
		double neg_b = bsr.get(key).get(0+"_"+criteriaid);
		double pos_b = bsr.get(key).get(1+"_"+criteriaid);
		double ev = 0.5;
		if(pos_b + neg_b != 0){
			ev = (pos_b + 1.0)/ (pos_b + neg_b + 2.0);
		}
		return ev;
	}
	//for read instances
	public double getBuyerReliabilityValue(int bid,int sid, int criteriaid){

		int q = 100;
		String key = bid+"_"+sid;
		ArrayList<Double> interval = new ArrayList<Double>();
		ArrayList<Double> value = new ArrayList<Double>();

		for(int i=0; i<q+1; i++){
			interval.add(0.0);
			value.add(0.0);
		}
		double innerRes = 0.0;
		double outerRes = 0.0;
		// init interval variable.
		interval.set(0, 0.0);//interval[0] = 0;
		for (int k = 1; k <= q; k++) {
			interval.set(k, interval.get(k-1)+1.0/q);
		}
		// *** For buyer(itself)
		double s = bsr.get(key).get(0+"_"+criteriaid);
		double r = bsr.get(key).get(1+"_"+criteriaid);
		if ((r != 0) || (s != 0)) {
			for (int i = 0; i < interval.size(); i++) {
				value.set(i, Math.pow(interval.get(i),r)* Math.pow(1-interval.get(i),s));
			}
			// **Apply the rule for inner integral
			innerRes = value.get(0)+value.get(q);
			for (int i = 1; i < interval.size() - 1; i++) {
				innerRes += 2.0*value.get(i);
			}
			innerRes *= (0.5 * 1.0 / q);

			// ** outer integral Calculation: 1/2*abs(1/innerRes*x^r(1-x)^s -1)
			for (int i = 0; i < interval.size(); i++) {
				double inverse = 1 / innerRes;
				value.set(i,0.5 * Math.abs(inverse * (Math.pow(interval.get(i),r) * Math.pow(1-interval.get(i),s))-1));//value[i] = 0.5 * Math.abs(inverse * (Math.pow(interval[i], r) * Math.pow(1 - interval[i], s)) - 1);
			}
			// **Apply the rule for outer integral
			outerRes = value.get(0)+value.get(q);
			for (int i = 1; i < interval.size() - 1; i++) {
				outerRes += 2.0 * value.get(i);
			}
			outerRes *= (0.5 * 1.0 / q);
		}

		return outerRes;
	}
	//used in FirstLayerClassification
	private double calcCompetency(int bid, int aid) {
		double competency;
		double disbelief = 0.0;
		double uncertainty = 0.0;

		int validateSellers = 0;
		for(int j = 0; j < totalSellers ; j++){
			int sid = j;
			String key = bid+"_"+sid;

			String key2 = aid+"_"+sid;
			if(BS_relVal.get(key) == 0 || BS_relVal.get(key2)== 0){
				continue;
			}
			validateSellers++;
			disbelief += Math.abs(BS_expVal.get(key)- BS_expVal.get(key2));
			uncertainty += Math.abs(BS_relVal.get(key)- BS_relVal.get(key2));
		}
		if(validateSellers != 0){
			competency = (1 - disbelief / validateSellers) * (1 - uncertainty / validateSellers);
		} else{
			competency = -9999;
		}

		return competency;
	}
	/*used in calculate reputation
	 * For the first layer: buyers attempt to ensure its advisors are honest by eliminating those
     * deemed as dishonest or with insufficient market place experience through probability.
	 */
	private void FirstLayerClassification(int bid) {

		int firstlayerDishonestyPred = 0;
		//default setting from zeinab, 1.2 * (beta + epsilon); //0.6
		for (int k = 0; k < totalBuyers; k++) {
			int aid =k;
			if(bid == aid)continue;
			double A_comeptenceVal = calcCompetency(bid, aid);
			if(A_comeptenceVal < 0){
				//buyer and advisor has no common rating pair
				trustOfAdvisors.set(aid, 0.5);
				continue;
			}
			if (1 - A_comeptenceVal > m_mu) {// predict as dishonest advisors;
				firstlayerDishonestyPred++;
				trustOfAdvisors.set(aid, 0.0);
			} else {
				trustOfAdvisors.set(aid, 1.0);
			}

		}
		popQuality = firstlayerDishonestyPred / totalBuyers;
	}

	/** Calculates the reputation of the sellers based on the first layer classification
	 * and second layer classification - The second layer involves buyers conducting further evaluations based 
	 * on multiple criteria to
     * examine if the honesty of the remaining advisors is similar with their calculated honesty value
     * in the first layer. 
	 */
	public double calculateTrust(Seller seller,Buyer honestBuyer, int criteriaid){
		readInstances(criteriaid);

		int bid = honestBuyer.getId();
		trustOfAdvisors = new ArrayList<Double>();
		for(int k=0; k<totalBuyers; k++){
			trustOfAdvisors.add(0.0);
		}
		for(int k=0; k<totalBuyers; k++){
			int aid = k;
			String key = aid+"_"+seller.getId();
			if(bsr.get(key).get(0+"_"+criteriaid) == 0 && bsr.get(key).get(1+"_"+criteriaid)==0){
				trustOfAdvisors.set(aid,0.5);
			}
		}

		//get the negative/positive rating for pairs of buyer and seller with advisors help
		double neg_BAforS = 0;
		double pos_BAforS = 0;

		FirstLayerClassification(bid);

		for (int k = 0; k < totalBuyers; k++) {
			int aid = k;
			String key = aid+"_"+seller.getId();
			if(bid == aid)continue;
			if(bsr.get(key).get(0+"_"+criteriaid)== 0 && bsr.get(key).get(1+"_"+criteriaid)== 0){
				//no transaction with seller
				trustOfAdvisors.set(aid, 0.5);
				continue;
			}
			neg_BAforS += trustOfAdvisors.get(aid) * bsr.get(key).get(0+"_"+criteriaid);
			pos_BAforS += trustOfAdvisors.get(aid) * bsr.get(key).get(1+"_"+criteriaid);
		}		

		double rep_BAforS = 0.5;
		if(pos_BAforS + neg_BAforS == 0.0){
			rep_BAforS = 0.5;
		}else{
			rep_BAforS = pos_BAforS / (pos_BAforS + neg_BAforS);
		}


		return rep_BAforS;
	}
    // sellers chosen based on the reputation calculated by the defense model.
	public Instance chooseSeller(int day, Buyer honestBuyer, Environment ec) {
		this.ecommerce = ec;
		this.day = day;
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

		//select seller with the maximum trust values from the two target sellers
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

	public void calculateReputation1(Buyer buyer,Seller seller,ArrayList<Boolean> trustAdvisors){}
	public ArrayList<Boolean> calculateReputation2(Buyer buyer, Seller seller,ArrayList<Boolean> trustAdvisors){return new ArrayList<Boolean>(); }

}//class
