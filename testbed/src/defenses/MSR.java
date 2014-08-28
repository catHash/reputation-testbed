/**
 * Multicriteria Subjective Reputation(MSR) model
 * Subjective expected utility calculated for each criteria
 * The subjective expected utilities are then further aggregated 
 * by the weighted ordered weighted average(WOWA) operator
 * in order to model various preference structures
 */
package defenses;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import distributions.BetaDistribution;
import distributions.PseudoRandom;
import environment.Environment;
import main.Parameter;
import main.Transaction;
import agent.Buyer;
import agent.Seller;

public class MSR extends Defense{
	
	public Instance chooseSeller(int day,Buyer b, Environment ec) {
		int bid = b.getId();
		this.ecommerce = ec;
		this.day = day;
		
		//calculate the trust values on target seller		
		HashMap<Integer,double[]> trustPerCriteria = new HashMap();
		ArrayList<Double> trustValues = new ArrayList<Double>();
		ArrayList<Double> mccValues = new ArrayList<Double>();
		ArrayList<Double> FNRValues = new ArrayList<Double>();
		ArrayList<Double> FPRValues = new ArrayList<Double>();
		ArrayList<Double> accValues = new ArrayList<Double>();
		ArrayList<Double> precValues = new ArrayList<Double>();
		ArrayList<Double> fValues = new ArrayList<Double>();
		ArrayList<Double> TPRValues = new ArrayList<Double>();

		ArrayList<Integer> chosenSeller = new ArrayList<Integer>(); 

		double dishonestValuesMAE=0.0;double honestValuesMAE=0.0;
		double dishonestValuesMCC=0.0;double honestValuesMCC=0.0;
		double dishonestValuesFNR=0.0;double honestValuesFNR=0.0;
		double dishonestValuesFPR=0.0;double honestValuesFPR=0.0;
		double dishonestValuesAcurracy=0.0;double honestValuesAcurracy=0.0;
		double dishonestValuesPrecision=0.0;double honestValuesPrecision=0.0;
		double dishonestValuesF=0.0;double honestValuesF=0.0;
		double dishonestValuesTPR=0.0, honestValuesTPR=0.0;
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
		
		for(int i=0;i<Parameter.TOTAL_NO_OF_SELLERS;i++){
			int sid=i;
			double aveReputation=0.0;
			aveReputation = calculateMultiCriteriaTrust(b,b.getSeller(sid));
			calculateTrustOfAdvisors(b,b.getSeller(sid));
			if(aveReputation >0.5){
				chosenSeller.add(sid);			
			}
			if(ecommerce.getSellerList().get(sid).isIshonest()==false){

				dishonestValuesMAE+=aveReputation;
				dishonestValuesMCC+=ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors);
				dishonestValuesFNR+=ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors);
				dishonestValuesFPR+=ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors);
				dishonestValuesAcurracy+=ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors);
				dishonestValuesPrecision+=ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors);
				dishonestValuesF+=ecommerce.getMcc().calculateF(sid, trustOfAdvisors);
				dishonestValuesTPR+= ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors);
			}

			else{
				honestValuesMAE += aveReputation;
				honestValuesMCC+=ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors);
				honestValuesFNR+=ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors);
				honestValuesFPR+=ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors);
				honestValuesAcurracy+=ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors);
				honestValuesPrecision+=ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors);
				honestValuesF+=ecommerce.getMcc().calculateF(sid, trustOfAdvisors);	
				honestValuesTPR+= ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors);
			}
		}//for

		dishonestValuesMAE/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesMCC/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesFNR/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesFPR/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesAcurracy/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesPrecision/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesF/=Parameter.NO_OF_DISHONEST_SELLERS;
		dishonestValuesTPR/=Parameter.NO_OF_DISHONEST_SELLERS;
		honestValuesMAE/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesMCC/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesFNR/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesFPR/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesAcurracy/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesPrecision/=Parameter.NO_OF_HONEST_SELLERS;
		honestValuesF/=Parameter.NO_OF_HONEST_SELLERS;			
		honestValuesTPR/=Parameter.NO_OF_HONEST_SELLERS;
		trustValues.set(0,dishonestValuesMAE);
		mccValues.set(0,dishonestValuesMCC);
		FNRValues.set(0, dishonestValuesFNR);
		accValues.set(0, dishonestValuesAcurracy);
		FPRValues.set(0, dishonestValuesFPR);
		precValues.set(0, dishonestValuesPrecision);
		fValues.set(0, dishonestValuesF);
		TPRValues.set(0, dishonestValuesTPR);
		trustValues.set(1,honestValuesMAE);
		mccValues.set(1,honestValuesMCC);
		FNRValues.set(1, honestValuesFNR);
		accValues.set(1, honestValuesAcurracy);
		FPRValues.set(1, honestValuesFPR);
		precValues.set(1, honestValuesPrecision);
		fValues.set(1, honestValuesF);
		TPRValues.set(1, honestValuesTPR);
		//update the daily reputation difference
		ecommerce.updateDailyReputationDiff(trustValues);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());			
		ecommerce.getMcc().updateDailyTPR(TPRValues, ecommerce.getDay());

		//select seller with the maximum trust values from the two target sellers
		int sellerId=0;
		if(chosenSeller.size()>0){

			sellerId=chosenSeller.get(PseudoRandom.randInt(0, chosenSeller.size()-1));
		}
		else sellerId=PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);
		int dVal = day + 1;
		int bVal = bid;
		String bHonestVal = Parameter.agent_honest;  
		int sVal = sellerId;
		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);	
		//add instance
		Instances transactions = ecommerce.getInstTransactions();

		Instance inst = new Instance(transactions.numAttributes());
		inst.setDataset(transactions);
		inst.setValue(Parameter.m_dayIdx, dVal);
		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);
		double rVal[] = new double[Parameter.NO_OF_CRITERIA];
		for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
			inst.setValue(Parameter.m_ratingIdx+j, Parameter.nullRating());
			rVal[j] = Parameter.nullRating();
		}
		return inst;
	}

	public double calculateTrust(Seller seller,Buyer buyer,Environment env){return 0.0;}
	
	public double calculateMultiCriteriaTrust(Buyer b, Seller sid){
		
		trustOfAdvisors  = new ArrayList<Double>();
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}
		
		ArrayList<Double> sub_exp_u = new ArrayList<Double>();
		double WOWA = 0.0;
		int aid;
		ArrayList <Double> utilitiesSum = new ArrayList<Double>();
		for (int p=0; p<Parameter.NO_OF_CRITERIA; p++){
			utilitiesSum.add(0.0);
		}
	
		int transCount = 0;
		//read transactions
		for(int q=0;q<Parameter.NO_OF_CRITERIA;q++){
			transCount = 0;
			double temp = 0;
			for (int m = 0; m < totalBuyers; m++) {	
				aid = m;
				if (aid == b.getId())continue;  //ignore its own rating
				trustOfAdvisors.set(aid, 1.0);
				if (b.getListOfBuyers().get(m).getTrans().size()!=0){
					for(int i=0; i<b.getListOfBuyers().get(m).getTrans().size(); i++){
					if(b.getBuyer(m).getTrans().get(i).getSeller().getId()==sid.getId()){
						if(b.getBuyer(m).getTrans().get(i).getRating().getCriteriaRatings().size()!=0){
							temp+=b.getBuyer(m).getTrans().get(i).getRating().getCriteriaRatings().get(q).getCriteriaRatingValue();
							transCount++;
						}
					}
					}
				}
				trustOfAdvisors.set(aid, 1.0);
			}
			//for each criteria, calculate sum of outcome values
			utilitiesSum.set(q, temp);
		}
		
		//if no transaction with the seller, return 0.5
		if (transCount==0) {		
			return 0.5;
		}
		else {	
			for (int p=0; p<Parameter.NO_OF_CRITERIA; p++){
				//for each criteria, calculate subjective expected utility value
				//sub_exp_u= (sum of outcome values/transCount)/15
				//utility range [1,2,3,4,5], transform by dividing 15
				//max utility is 1/3, mapping to 1 by *3
				sub_exp_u.add((utilitiesSum.get(p)*3/(transCount*15)));
			}
		}
			
		ArrayList<Double> unsorted_sub_u = new ArrayList<Double>();
		unsorted_sub_u.addAll(sub_exp_u);
		
		//sort the sub_exp_u in descending order
		for(int n=0;n<sub_exp_u.size()-1;n++)
		{
		   for(int m=n+1;m<sub_exp_u.size();m++)
		   {
		      if(sub_exp_u.get(n)<sub_exp_u.get(m))
		      {
		        double temp=sub_exp_u.get(n);
		        sub_exp_u.set(n,sub_exp_u.get(m));
		        sub_exp_u.set(m,temp);
		      }   
		   }
		}
		
		//get original index of sorted sub_exp_u elements
		ArrayList<Integer> sortedOrder = new ArrayList<Integer>();
		for(int s=0;s<sub_exp_u.size();s++){
			for(int u=0;u<sub_exp_u.size();u++){
				if (sub_exp_u.get(s).equals(unsorted_sub_u.get(u))){				
						sortedOrder.add(u);
						unsorted_sub_u.set(u, -1.0);
				}
			}
		}
				
		//get sum of importance weights with sortedOrder of sub_exp_u
		ArrayList <Double> imp_weight_sum = new ArrayList<Double>();
		double temp_sum = 0;		
		for (int r=0; r<=Parameter.NO_OF_CRITERIA; r++){			
			if (r ==0){
				imp_weight_sum.add(0.0);
			}
			else {
				temp_sum=temp_sum+Parameter.MSR_imp_weight.get(sortedOrder.get(r-1));				
				imp_weight_sum.add(temp_sum);
			}
		}
		
		//calculate WOWA
		ArrayList <Double> weight = new ArrayList<Double>();
		for (int m=0; m<Parameter.NO_OF_CRITERIA; m++){
			weight.add(calculateWeight(imp_weight_sum.get(m+1))-calculateWeight(imp_weight_sum.get(m)));
		}	
		
		for (int q=0; q<Parameter.NO_OF_CRITERIA; q++){
			WOWA = WOWA + sub_exp_u.get(q)*weight.get(q);
		}
		
		return WOWA;		
	}
	
	public double calculateWeight(double n){
		
		/*weights w are constructed by cumulation of the preferential weights
		 *and their decumulation according to the corresponding distribution 
		 *of importance weights*/
		
		double temp_sum_p= 0;
		int cur_index = 0;
		
		if (n==0){
			return 0.0;
		}
		else if (n==1){
			return 1.0;
		}
		else{
			for (double i=0.0;i<Parameter.NO_OF_CRITERIA; i=i+1.0){
				if((n>(i/Parameter.NO_OF_CRITERIA)) & (n<((i+1)/Parameter.NO_OF_CRITERIA))){
					cur_index = (int)i;
				}
			}
			if (cur_index==0){
				return n*Parameter.MSR_pre_weight.get(cur_index)/(1.0/Parameter.NO_OF_CRITERIA);
			}
			else{
				for (int m=0; m<cur_index; m++){
					temp_sum_p=temp_sum_p+Parameter.MSR_imp_weight.get(m);
				}
				double cur_index_value = (double)cur_index;
				return temp_sum_p+Parameter.MSR_pre_weight.get(cur_index)*(n-(cur_index_value/Parameter.NO_OF_CRITERIA))/(1.0/Parameter.NO_OF_CRITERIA);
			}
		}		
	}
	
	public void calculateTrustOfAdvisors(Buyer b, Seller sid){
		int aid;
		for (int m = 0; m < totalBuyers; m++) {
			aid = m;
			if (aid == b.getId())continue;  //ignore its own rating
			for(int i=0; i<b.getListOfBuyers().get(aid).getTrans().size(); i++){

				if(b.getBuyer(aid).getTrans().get(i).getSeller().getId()==sid.getId())

					trustOfAdvisors.set(aid, 1.0);
				else{
					trustOfAdvisors.set(aid, 0.0);
				}	

			}
		}

	}	 

	public void calculateReputation1(Buyer buyer,Seller seller,ArrayList<Boolean> trustAdvisors){}
	public ArrayList<Boolean> calculateReputation2(Buyer buyer, Seller seller,ArrayList<Boolean> trustAdvisors){return new ArrayList<Boolean>(); }
		
	@Override
	public double calculateTrust(Seller seller, Buyer honestBuyer,
			int criteriaid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double predictRealSellerReputation(Buyer honestBuyer, Environment ec,Seller currentSeller, int criteriaid) {
		// TODO Auto-generated method stub
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
        double trust=0.0;
		
		trust=calculateMultiCriteriaTrust(honestBuyer,ec.containsSeller(sid));
		
		calculateTrustOfAdvisors(honestBuyer,currentSeller);
		
		if(trust < 0.1) trust =0;
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
		ecommerce.updateDailyReputationDiff(trustValues, sid, criteriaid);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay()); 
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyTPR(TPRValues,ecommerce.getDay());

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


	


}
