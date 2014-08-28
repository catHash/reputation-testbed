/**
 * Trust and Reputation for Agent-based Virtual Organizations(TRAVOS) model
 * The truster can make assessment based on previous direct interactions with the trustee
 * The truster may assess trustworthiness based on the reputation of the trustee.
 */
package defenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import main.Parameter;
import distributions.BetaDistribution;
import distributions.PseudoRandom;
import agent.Buyer;
import agent.Seller;
import environment.Environment;


public class TRAVOS extends Defense{

	private static int numBins = 3;
	private HashMap<String, Double> BS_currRating;
	//method to get the details of the environment setting from the instances.
	public void readInstances(int criteriaid){

		Instances transactions = ecommerce.getInstTransactions();
		totalBuyers = transactions.attribute(Parameter.m_bidIdx).numValues();
		totalSellers = transactions.attribute(Parameter.m_sidIdx).numValues();	
		m_NumInstances = transactions.numInstances();
		trustOfAdvisors = new ArrayList<Double>();   //the initial trust values is 0;
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}
		//for statistic features
		bsr = ecommerce.getBsr();
		rtimes = new ArrayList<Double>();
		for(int i=0; i<Parameter.NO_OF_DAYS; i++){
			rtimes.add(0.0);
		}
		BS_currRating = ecommerce.getBScurrRating();
	}
/**
*The TRAVOS model works by allowing an agent (truster) to look into two methods for assessing the trustworthiness of another agent (the trustee).
* First the truster can make assessment based on previous direct interactions with the trustee. 
* Second, the truster may assess trustworthiness based on the reputation of the trustee.
* In general, trust can be accumulated and gained over time through personal interaction and experience with others, so if the truster has personal interaction with the trustee,
*  the trust is calculated based on its previous experiences with the trustee. However, when there is a lack of personal experience between the truster and trustee, 
*  there is a need to draw upon the general opinion of the trustee from the public, otherwise called reputation in this model, 
* which is gathered from other agents or advisors, for use as assessment of the trustee’s trustworthiness.
* In this model, three requirements must be observed. First is that the model provides a standard metric scale of comparison 
* in terms of level of trust in an agent, with or without personal experience. Second requirement is an individual’s level of confidence in his trust towards another agent.
*  This confidence level is necessary to determine if there is a need for the truster to utilize the reputation of the trustee based on general opinion of others. 
* If the confidence level threshold is fulfilled, the truster’s level of trust in the trustee is sufficed. 
* Lastly the model must be able to calculate and conduct its own discounting of other agents’ opinions based on the past reliability of the agents.
**/
	
	
	@Override
	//predicts the reputation of the seller based on the TRAVOS approach
	public double calculateTrust(Seller seller, Buyer honestBuyer, int criteriaid) {
		readInstances(criteriaid);
		int sid = seller.getId();
		int bid = honestBuyer.getId();
		
		//get the positive/negative rating for pairs of buyer and seller
		double pos_BAforS = 0;
		double neg_BAforS = 0;	

		//step 1: positive and negative form the buyer

		Vector<Integer> stroedAdvisors = honestBuyer.getAdvisors();
		stroedAdvisors.clear();
		//step 2: add all the advisors's adjusted positive and negative rating
		int numBuyers =ecommerce.getNumOfBuyers();
		double mu_uniform = 0.5;  												//expectation of the uniform distribution
		double sigma_uniform = Math.sqrt(1.0 / 12);									//Variance of the uniform distribution		
		for(int i = 0; i < numBuyers; i++){
			int aid = i;			
			if(aid == bid)continue;			
			String key2 = aid+"_"+sid;
			//calculate the reputation and variance of seller based on advisor
			double neg_AS, pos_AS;
			neg_AS = bsr.get(key2).get(0+"_"+criteriaid);

			pos_AS = bsr.get(key2).get(1+"_"+criteriaid);

			if(pos_AS == 0 && neg_AS == 0) 	{
				trustOfAdvisors.set(aid, 0.5);//non suggestion for seller
				continue;
			}

			double mu_AS = (pos_AS + 1.0) / (pos_AS + neg_AS + 2.0);
			double sigma_AS = Math.sqrt( (pos_AS + 1) * (neg_AS + 1) / ((pos_AS + neg_AS + 2) * (pos_AS + neg_AS + 2) * (pos_AS + neg_AS + 3) ) );
			//adjust the alpha and beta values	
			double rho = relationshipBA_S( bid,  aid,  sid,criteriaid);	
			trustOfAdvisors.set(aid, rho);
			double mu_adj = (1 - rho) * mu_uniform  + rho * mu_AS;
			double sigma_adj = (1 - rho) * sigma_uniform  + rho * sigma_AS;
			double tmp = (mu_adj * (1 - mu_adj)) / (sigma_adj * sigma_adj) - 1.0;
			double pos_AS_adj =  mu_adj * tmp - 1.0;
			double neg_AS_adj =  (1.0 - mu_adj) * tmp - 1.0;

			//add the adjust positive and negative values into the buyers.
			pos_BAforS += pos_AS_adj;
			neg_BAforS += neg_AS_adj;

			//set the trust for advisors;
			stroedAdvisors.add(aid);
			honestBuyer.setTrustAdvisor(aid, rho);
		}
		honestBuyer.calculateAverageTrusts(sid);  //get the average trust of advisors based on seller

		double rep_BAforS = (pos_BAforS + 1.0 * Parameter.m_laplace) / (pos_BAforS + neg_BAforS + 2.0 * Parameter.m_laplace);
		return rep_BAforS;
	}

	//choosing a seller based on the reputation predicted by the TRAVOS model.
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


	private double relationshipBA_S(int bid, int aid, int sid, int criteriaid) {

		//get the positive/negative rating for pairs of buyer and seller
		String key2 = aid+"_"+sid;
		// step 1: find the reputation bin for advisor to seller
		double neg_AS = bsr.get(key2).get(0+"_"+criteriaid);
		double pos_AS = bsr.get(key2).get(1+"_"+criteriaid);
		double rep_AS = ((double) (pos_AS + 1))	/ ((double) (pos_AS + neg_AS + 2));

		// find the bin that the expected reputation falls into
		double lb = 0;
		double ub = 0;
		for (int i = 0; i < numBins; i++) {
			lb = ((double) (i)) / ((double) (numBins));
			ub = ((double) (i + 1)) / ((double) (numBins));
			if (rep_AS >= lb  && rep_AS < ub)
				break;
		}

		// step 2: find the relationship between the seller and advisor, (1. reputation \in [bin of rep_AS], 2. number of transactions)
		// go through the sellers that ever rated by the advisor calculate the expected reputation of those sellers and
		// find out the ones that fall between lb and ub		
		int neg_BAforSref = 0;
		int pos_BAforSref = 0;
		// for each seller, the number of positive & negative ratings
		HashMap<Integer, HashMap<String, Double>> rating = new HashMap<Integer, HashMap<String, Double>>();

		for(int i=0; i<totalSellers; i++){
			String key3 = aid+"_"+i;
			rating.put(i, bsr.get(key3));
		}

		for(int i=0; i<rating.size(); i++){
			int Sref = i;
			//only consider the reference seller; ignore the null rating for reference seller based on advisor
			if(Sref == sid || (rating.get(Sref).get(0+"_"+criteriaid) ==0 && rating.get(Sref).get(1+"_"+criteriaid) == 0)) continue;
			//the recently rating for reference seller based on buyer
			String key4 = bid+"_"+Sref;
			double rBSref_recently = BS_currRating.get(key4);
			if(rBSref_recently == Parameter.RATING_BINARY[1])
				continue;	

			String key5 = aid+"_"+Sref;
			double neg_ASref = bsr.get(key5).get(0+"_"+criteriaid);
			double pos_ASref = bsr.get(key5).get(1+"_"+criteriaid);
			double rep_ASref = ((double) (pos_ASref + 1)) / ((double) (pos_ASref + neg_ASref + 2));
			// if the reputation (advisor to the reference seller)is between the bin
			//1. reputation \in [bin of rep_AS]
			if (rep_ASref >= lb && rep_ASref <= ub) {				 
				// 2. the rating from the buyer's view
				if (rBSref_recently == Parameter.RATING_BINARY[0]) {
					neg_BAforSref++;
				} else if (rBSref_recently == Parameter.RATING_BINARY[2]) { 
					pos_BAforSref++;
				}
			}
		}

		BetaDistribution distBA_S = new BetaDistribution(pos_BAforSref + 1, neg_BAforSref + 1);
		double relationshipBA_S = distBA_S.CDF(ub) - distBA_S.CDF(lb);

		return relationshipBA_S;
	}
}
