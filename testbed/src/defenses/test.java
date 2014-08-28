//package defenses;
//import java.util.ArrayList;
//import java.util.Vector;
//
//import weka.core.Instance;
//import weka.core.Instances;
//
//import distributions.BetaDistribution;
//import distributions.PseudoRandom;
//import environment.Environment;
//import main.Parameter;
//import agent.Buyer;
//import agent.Seller;
//
//public class BRS extends Defense{
//	// ** if a buyer gives a reputation to a seller that falls 
//	//    in the rejection area of 'q' or '1-q', then the buyer's
//	//    rating will be ignored **
//	private double quantile = 0.01;
//
//
//	public double calculateTrust( Seller s, Buyer b, int criteriaid){
//		trustOfAdvisors = new ArrayList<Double>();
//		for(int i=0; i<totalBuyers; i++){
//			trustOfAdvisors.add(0.0);
//		}
//		int bid = b.getId();
//		int sid = s.getId();
//		double repbs = 0.5;
//		bsr = ecommerce.getBsr();
//		int numBuyers = ecommerce.getNumOfBuyers();
//		ArrayList<Boolean> trustAdvisors = new ArrayList<Boolean>();
//		for(int i=0; i<numBuyers; i++){
//			trustAdvisors.add(false);
//		}
//		for(int j=0; j<numBuyers; j++){
//			int aid =j;
//			if(aid == (Parameter.NO_OF_DISHONEST_BUYERS+Parameter.NO_OF_HONEST_BUYERS)) break;
//			trustAdvisors.set(aid, true);
//			String key = aid+"_"+sid;
//			if(bsr.get(key).get(0+"_"+criteriaid) == 0 && bsr.get(key).get(1+"_"+criteriaid) ==0){
//				trustOfAdvisors.set(aid, 0.5);
//				trustAdvisors.set(aid, false);
//			}
//		}
//
//		boolean iterative = true;
//		do{
//			iterative = false;
//			ArrayList<Double> bsnpsum = new ArrayList<Double>();
//			for(int i=0; i<2; i++){
//				bsnpsum.add(0.0);
//			}
//			for(int j=0; j<numBuyers; j++){
//				int aid = j;
//				if(aid==bid) continue;
//				if(trustAdvisors.get(aid) == false) continue;
//				String key = aid+"_"+sid;
//				bsnpsum.set(0, bsnpsum.get(0) + bsr.get(key).get(0+"_"+criteriaid));
//				bsnpsum.set(1, bsnpsum.get(1) + bsr.get(key).get(1+"_"+criteriaid));
//			}
//			repbs = (bsnpsum.get(1) + 1.0) / (bsnpsum.get(0) + bsnpsum.get(1) + 2.0);
//			for(int j=0; j<numBuyers; j++){
//				int aid = j;
//				if(trustAdvisors.get(aid) == false) continue;
//				String key = aid+"_"+sid;
//				BetaDistribution BDist_BrefS = new BetaDistribution((double) (bsr.get(key).get(1+"_"+criteriaid)+ 1), (double) (bsr.get(key).get(0+"_"+criteriaid) + 1));
//				double l = BDist_BrefS.getProbabilityOfQuantile(quantile);
//				double u = BDist_BrefS.getProbabilityOfQuantile(1 - quantile);
//				if (repbs < l || repbs > u) {
//					// remove this buyer from the honest list
//					trustAdvisors.set(aid, false);
//					//since a buyer is removed from the list, reputation is calculated again (do while loop)
//					iterative = true;
//				}
//			}
//		} while(iterative);
//
//		Vector<Integer> stroedAdvisors = b.getAdvisors();
//		stroedAdvisors.clear();
//		ArrayList<Double> npbas = new ArrayList<Double>();
//		for(int i=0; i<2; i++){
//			npbas.add(0.0);
//		}
//		for(int j=0; j<numBuyers; j++){
//			int aid =j;
//			if(aid==bid) continue;
//			if(trustAdvisors.get(aid) == false) continue;
//			trustOfAdvisors.set(aid,  1.0);
//			String key = aid+"_"+sid;
//			npbas.set(0, npbas.get(0)+bsr.get(key).get(0+"_"+criteriaid));
//			npbas.set(1,  npbas.get(1) + bsr.get(key).get(1+"_"+criteriaid));
//			stroedAdvisors.add(aid);
//			b.setTrustAdvisor(aid, 1.0);
//		}
//		b.calculateAverageTrusts(sid);  //get the average trust of advisors based on seller
//		repbs = (npbas.get(1) + 1.0 * Parameter.m_laplace) / (npbas.get(0) + npbas.get(1) + 2.0 * Parameter.m_laplace);
//		return repbs;
//	}
//	public Instance chooseSeller(int day, Buyer honestBuyer, Environment ec) {
//		this.ecommerce = ec;
//		this.day = day;
//		//calculate the trust values on target seller	
//		ArrayList<Double> trustValues = new ArrayList<Double>();
//		ArrayList<Double> mccValues = new ArrayList<Double>();
//		ArrayList<Double> FNRValues = new ArrayList<Double>();
//		ArrayList<Double> FPRValues = new ArrayList<Double>();
//		ArrayList<Double> accValues = new ArrayList<Double>();
//		ArrayList<Double> precValues = new ArrayList<Double>();
//		ArrayList<Double> fValues = new ArrayList<Double>();
//		ArrayList<Double> TPRValues = new ArrayList<Double>();
//
//		ArrayList<Integer> chosenSeller = new ArrayList<Integer>(); 
//		int criteria = Parameter.NO_OF_CRITERIA;
//		double honestValuesMAE=0, dishonestValuesMAE=0,honestValuesMCC=0,dishonestValuesMCC=0;
//		double honestValuesFNR=0,dishonestValuesFNR=0, honestValuesFPR=0,dishonestValuesFPR=0;
//		double honestValuesAcurracy=0,dishonestValuesAcurracy=0, honestValuesPrecision=0,dishonestValuesPrecision=0;
//		double honestValuesF=0, dishonestValuesF=0, honestValuesTPR=0, dishonestValuesTPR=0;
//		int bid = honestBuyer.getId();
//
//		for(int i=0; i<totalSellers; i++){
//			int sid = i;
//			double reputation =0, mcc=0, fnr=0, fpr=0, accuracy=0, prec=0, f=0, tpr=0;
//			//calculate for each criteria
//			for(int j=0; j<criteria; j++){
//				reputation+=calculateTrust(honestBuyer.getListOfSellers().get(sid),honestBuyer, j);
//				mcc += ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors);
//				fnr += ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors);
//				fpr += ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors);
//				accuracy += ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors);
//				prec += ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors);
//				f += ecommerce.getMcc().calculateF(sid, trustOfAdvisors);
//				tpr += ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors);
//			}
//			if(sid<Parameter.NO_OF_DISHONEST_SELLERS){
//				dishonestValuesMAE += (reputation/criteria);
//				dishonestValuesMCC += (mcc/criteria);
//				dishonestValuesFNR += (fnr/criteria);
//				dishonestValuesFPR += (fpr/criteria);
//				dishonestValuesAcurracy += (accuracy/criteria);
//				dishonestValuesPrecision += (prec/criteria);
//				dishonestValuesF += (f/criteria);
//				dishonestValuesTPR += (tpr/criteria);
//			}
//			else{
//				honestValuesMAE += (reputation/criteria);
//				honestValuesMCC += (mcc/criteria);
//				honestValuesFNR += (fnr/criteria);
//				honestValuesFPR += (fpr/criteria);
//				honestValuesAcurracy += (accuracy/criteria);
//				honestValuesPrecision += (prec/criteria);
//				honestValuesF += (f/criteria);
//				honestValuesTPR += (tpr/criteria);
//			}
//			if(reputation/criteria > 0.5){
//				chosenSeller.add(sid);
//			}
//		}
//
//		trustValues.add(dishonestValuesMAE/Parameter.NO_OF_DISHONEST_SELLERS);
//		trustValues.add(honestValuesMAE/Parameter.NO_OF_HONEST_SELLERS);
//		mccValues.add(dishonestValuesMCC/Parameter.NO_OF_DISHONEST_SELLERS);
//		mccValues.add(honestValuesMCC/Parameter.NO_OF_HONEST_SELLERS);
//		FNRValues.add(dishonestValuesFNR/Parameter.NO_OF_DISHONEST_SELLERS);
//		FNRValues.add(honestValuesFNR/Parameter.NO_OF_HONEST_SELLERS);
//		FPRValues.add(dishonestValuesFPR/Parameter.NO_OF_DISHONEST_SELLERS);
//		FPRValues.add(honestValuesFPR/Parameter.NO_OF_HONEST_SELLERS);
//		accValues.add(dishonestValuesAcurracy/Parameter.NO_OF_DISHONEST_SELLERS);
//		accValues.add(honestValuesAcurracy/Parameter.NO_OF_HONEST_SELLERS);
//		precValues.add(dishonestValuesPrecision/Parameter.NO_OF_DISHONEST_SELLERS);
//		precValues.add(honestValuesPrecision/Parameter.NO_OF_HONEST_SELLERS);
//		fValues.add(dishonestValuesF/Parameter.NO_OF_DISHONEST_SELLERS);
//		fValues.add(honestValuesF/Parameter.NO_OF_HONEST_SELLERS);
//		TPRValues.add(dishonestValuesTPR/Parameter.NO_OF_DISHONEST_BUYERS);
//		TPRValues.add(honestValuesTPR/Parameter.NO_OF_HONEST_BUYERS);
//
//		//select seller with the maximum trust values from the two target sellers
//		int sellerId=0;
//		if(chosenSeller.size()>0){
//			sellerId=chosenSeller.get(PseudoRandom.randInt(0, chosenSeller.size()-1));
//		}
//		else sellerId=PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);
//
//		//update the daily reputation difference
//		ecommerce.updateDailyReputationDiff(trustValues);
//		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyTPR(TPRValues,ecommerce.getDay());
//
//		int dVal = day + 1;
//		int bVal = bid;
//		String bHonestVal =Parameter.agent_honest;  
//		int sVal = sellerId;
//
//		double sHonestVal = ecommerce.getSellersTrueRating(sVal, 0);
//		//add instance
//		Instances transactions = ecommerce.getInstTransactions();
//		Instance inst = new Instance(transactions.numAttributes());
//		inst.setDataset(transactions);
//		inst.setValue(Parameter.m_dayIdx, dVal);
//		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
//		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
//		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
//		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);
//		for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
//			inst.setValue(Parameter.m_ratingIdx+j, Parameter.nullRating());
//		}
//		return inst;
//	}
//
//	//for real environment --- modified athirai
//	public double predictRealSellerReputation(Buyer honestBuyer, Environment ec,Seller currentSeller, int criteriaid) {
//		this.ecommerce = ec;
//		//calculate the trust values on target seller		
//		ArrayList<Double> trustValues = new ArrayList<Double>();
//		ArrayList<Double> mccValues = new ArrayList<Double>();
//		ArrayList<Double> FNRValues = new ArrayList<Double>();
//		ArrayList<Double> FPRValues = new ArrayList<Double>();
//		ArrayList<Double> accValues = new ArrayList<Double>();
//		ArrayList<Double> precValues = new ArrayList<Double>();
//		ArrayList<Double> fValues = new ArrayList<Double>();
//		ArrayList<Double> TPRValues = new ArrayList<Double>();
//
//		if (trustValues.size()==0){
//			for(int i=0; i<2; i++){
//				trustValues.add(0.0);
//				mccValues.add(0.0);
//				FNRValues.add(0.0);
//				accValues.add(0.0);
//				FPRValues.add(0.0);
//				precValues.add(0.0);
//				fValues.add(0.0);
//				TPRValues.add(0.0);
//
//			}
//		}
//		// consider all sellers as honest and calculate only MAE-honest
//		int sid = currentSeller.getId();
//
//		double trust=calculateTrust(ec.containsSeller(sid),honestBuyer, criteriaid);
//		trustValues.set(0,trust);
//		mccValues.set(0, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
//		FNRValues.set(0, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
//		accValues.set(0, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
//		FPRValues.set(0, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
//		precValues.set(0, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
//		fValues.set(0, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
//		TPRValues.set(0, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));
//
//		//MAE-honest and dishonest is same for clarity
//		trustValues.set(1,trust);
//		mccValues.set(1, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
//		FNRValues.set(1, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
//		accValues.set(1, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
//		FPRValues.set(1, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
//		precValues.set(1, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
//		fValues.set(1, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
//		TPRValues.set(1, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));
//
//
//		//update the daily reputation difference
//		ecommerce.updateDailyReputationDiff(trustValues);
//		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay()); 
//		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
//		ecommerce.getMcc().updateDailyTPR(TPRValues,ecommerce.getDay());
//
//		int dVal = day + 1;
//		int bVal = honestBuyer.getId();
//		String bHonestVal = Parameter.agent_honest;  
//		int sVal = currentSeller.getId();
//		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);	
//		double rVal = Parameter.nullRating();				
//		//add instance
//		Instances transactions = ecommerce.getInstTransactions();
//
//		Instance inst = new Instance(transactions.numAttributes());
//		inst.setDataset(transactions);
//		inst.setValue(Parameter.m_dayIdx, dVal);
//		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
//		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
//		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
//		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);			
//		inst.setValue(Parameter.m_ratingIdx, rVal);
//		return trustValues.get(0);
//	}
//}
