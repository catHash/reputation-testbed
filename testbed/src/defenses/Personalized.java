/**class Personalized: Inherits the Defense class. To evaluate the private trust of the advisor, the buyer compares his individual ratings against
*the advisor�s ratings with regard to their commonly rated sellers. When the buyer has
*insufficient knowledge of the advisor, the reputation of the advisor agent will also be measured.
*A large difference would then result in the buyer discounting the advisor�s trustworthiness
to a large extent.
* 
*/

package defenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;

import distributions.PseudoRandom;
import main.Parameter;
import agent.Buyer;
import agent.Seller;
import environment.Environment;


public class Personalized extends Defense {


	private double gamma = 0.8;
	private double epsilon = 0.25;
	private double inconsistency = 0.1;
	private double numratingBA = 0.0;
	public HashMap<Integer, HashMap<String, Double>> sr =null;

	/*Computes private trust of advisor: the buyer compares his/her individual ratings against 
	the advisor's rating with regard to their commonly rated sellers.*/
	private double privateTrustAdvisor(int bid, int aid) {
		HashMap<Integer, Double> BAsameRatings = ecommerce.getBuyersAdvisorsSameRatingsArray(bid, aid);
		double priTr_BA = 0.0;
		double neg_BA =BAsameRatings.get(0);
		double pos_BA = BAsameRatings.get(1);
		priTr_BA = (pos_BA + 1.0) / (pos_BA + neg_BA + 2.0);
		numratingBA = pos_BA + neg_BA;
		
		return priTr_BA;
	}

	/*computing public trust: Determined by comparing an advisor's rating with the majortiy 
	of the other advisor's rating with regard to their commonly rated sellers. */
	private double publicTrustAdvisor(int aid, int criteriaid){		
		HashMap<String, HashMap<String, Double>> bsr = ecommerce.getBsr();
		HashMap<Integer, HashMap<String, Double>> sr = ecommerce.getSr();
		//calculate the public trust for advisor based on buyer
		double pos_BA = 0.0;	//number of consistent ratings
		double neg_BA = 0.0;	//number of all ratings
		//estimate every sellers average rating		
		for(int i = 0; i < ecommerce.getSellerList().size(); i++){
			int Sref = i;

			//step 1: calculate the reputation for reference seller by advisor	
			String key = aid+"_"+Sref;
			double rep_ASref = (bsr.get(key).get(1+"_"+criteriaid) + 1.0) / (bsr.get(key).get(0+"_"+criteriaid) + bsr.get(key).get(1+"_"+criteriaid) + 2.0);

			//step 2: estimate the average rating for seller in current day, from all buyers.	
			double neg_aBSref = sr.get(Sref).get(0+"_"+criteriaid) - bsr.get(key).get(0+"_"+criteriaid);
			double pos_aBSref = sr.get(Sref).get(1+"_"+criteriaid) - bsr.get(key).get(1+"_"+criteriaid);
			double rep_aBSref = (pos_aBSref + 1.0) / (pos_aBSref + neg_aBSref + 2.0);

			//step 3: compare the reputation based on advisor and reputation based on other buyers
			if(Math.abs(rep_ASref - rep_aBSref) < inconsistency){
				pos_BA++;
			}else{
				neg_BA++;
			}
		}
		double pubTr_BA = (pos_BA + 1.0) / (pos_BA + neg_BA + 2.0);

		return pubTr_BA;
	}

    /*method to calculate weighted trust of advisor after taking public and private trust 
    *into consideration. Trustworthiness of the advisor.
	*/
	private double trustAdvisor(int bid, int aid, int criteriaid){		

		double trustAdvisor = 0.0;	
		//rigorous according to the paper
		double priTr_BA = privateTrustAdvisor(bid, aid);

		double Nmin = -(1.0 / (2.0 * epsilon * epsilon)) * Math.log((1.0 - gamma) / 2.0);
		double w = 0;		
		if(numratingBA >= Nmin){
			w = 1.0;
			trustAdvisor = priTr_BA;
		}else{
			w = ((double) (numratingBA)) / (Nmin);
			double pubTr_BA = publicTrustAdvisor(aid,criteriaid);
			trustAdvisor = w * priTr_BA + (1 - w) * pubTr_BA;
		}		

		return trustAdvisor;
	}

	//computes the reputation of the seller based on the approach taken by the defense model.
	@Override
	public double calculateTrust(Seller seller, Buyer honestBuyer, int criteriaid) {
		readInstances(criteriaid);
		int bid = honestBuyer.getId();

		double pos_BAforS = 0.0;
		double neg_BAforS = 0.0;	
		int numBuyers = ecommerce.getNumOfBuyers();
		int sid = seller.getId();
		Vector<Integer> stroedAdvisors = honestBuyer.getAdvisors();
		stroedAdvisors.clear(); 
		for(int i = 0; i < numBuyers; i++){
			int aid = i;
			if(aid == bid){
				trustOfAdvisors.set(aid, 1.0);
			}
			String key = aid+"_"+sid;
			if (bsr.get(key).get(0+"_"+criteriaid) ==0 && bsr.get(key).get(1+"_"+criteriaid)==0) {
				//no transaction with seller
				trustOfAdvisors.set(aid, 0.5);
				continue; 
			}
			else{
				double trustAdvisor = trustAdvisor( bid, aid, criteriaid);
				trustOfAdvisors.set(aid, trustAdvisor);
				neg_BAforS += trustAdvisor * bsr.get(key).get(0+"_"+criteriaid);
				pos_BAforS += trustAdvisor * bsr.get(key).get(1+"_"+criteriaid);
				//set the trust for advisors;
				stroedAdvisors.add(aid);
				honestBuyer.setTrustAdvisor(aid, trustAdvisor);
			}
		}
		honestBuyer.calculateAverageTrusts(sid);  //get the average trust of advisors based on seller		
		double rep_BAforS = (pos_BAforS + 1.0 * Parameter.m_laplace) / (pos_BAforS + neg_BAforS + 2.0 * Parameter.m_laplace);		

		return rep_BAforS;
	}

    //seller selected by the defense model based on the reputation predicted by the model.
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

	//method to get the transaction instances from the environment class, Used to initialize the 
	//buyer-seller rating (bsr) and seller-rating (sr) hashmaps. ArrayList for advisors is also 
	//initialized as per the buyer/advisor present.
	public void readInstances(int criteriaid){
		Instances transactions = ecommerce.getInstTransactions();
		totalBuyers = transactions.attribute(Parameter.m_bidIdx).numValues();
		totalSellers = transactions.attribute(Parameter.m_sidIdx).numValues();	
		m_NumInstances = transactions.numInstances();
		trustOfAdvisors = new ArrayList<Double>();
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}

		rtimes = new ArrayList<Double>();
		for(int i=0; i<Parameter.NO_OF_DAYS; i++){
			rtimes.add(0.0);
		}
		//read the instances
		bsr = ecommerce.getBsr();
		

		sr = ecommerce.getSr();
	
	}
}
