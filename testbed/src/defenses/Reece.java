package defenses;
/**This defense model has been implemented to test how utilizing the experience of
 * advisors on transactions with common sellers from different attack patterns on a
 * multi-criteria rating system performs as per the different evaluation metrics. 
 * 
 */

import umontreal.iro.lecuyer.probdistmulti.DirichletDist;

import java.util.ArrayList;
import java.util.HashMap;

import main.Parameter;
import main.Transaction;
import agent.Buyer;
import agent.Seller;
import distributions.PseudoRandom;
import environment.Environment;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;
public class Reece extends Defense{

	@Override
	//method to choose seller based on the reputation predicted by the defense model
	public Instance chooseSeller(int day, Buyer b, Environment ec) {
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
			double[] reputation=calculateMultiCriteriaTrust(b,b.getSeller(sid));

			double S=0.0; double aveReputation=0.0;
			for(int j=0;j<reputation.length;j++)
				S+=reputation[j];
			aveReputation = S/(reputation.length);

			calculateTrustOfAdvisors(b,b.getSeller(sid));

			if(aveReputation >0.5){ chosenSeller.add(sid); System.out.println("chosen seller "+sid+" day "+ecommerce.getDay());}
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
		int sellerId=0;
		if(chosenSeller.size()>0){

			sellerId=PseudoRandom.randInt(0, chosenSeller.size()-1);
		}
		else sellerId=PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);

		int dVal = day + 1;
		int bVal = b.getId();
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

	//method to calculate the trust/reputation on multiple criteria
	public double[] calculateMultiCriteriaTrust(Buyer b, Seller sid){
		trustOfAdvisors = new ArrayList<Double>();
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}

		double alpha2[] = new double[Parameter.NO_OF_CRITERIA];
		double bsr0 =0; double bsr1=0;

		int q;int aid;
		for( q=0; q <Parameter.NO_OF_CRITERIA; q++){
			bsr0 =0; bsr1 = 0;
			for (int m = 0; m < totalBuyers; m++) {
				aid = m;
				if (aid == b.getId())continue;  //ignore its own rating

				trustOfAdvisors.set(aid, 1.0);

				for(int i=0; i<b.getListOfBuyers().get(aid).getTrans().size(); i++){

					if(b.getBuyer(aid).getTrans().get(i).getSeller().getId()==sid.getId()){

						if (b.getBuyer(aid).getTrans().get(i).getRating().getCriteriaRatings().get(q).getCriteriaRatingValue()<=0){
							bsr0 ++;
						}
						if (b.getBuyer(aid).getTrans().get(i).getRating().getCriteriaRatings().get(q).getCriteriaRatingValue()>=1){
							bsr1 ++;
						}
						
					}
				}
				trustOfAdvisors.set(aid, 1.0);
			}

			sid.alpha[q] = (bsr1+1.0)/(bsr1 + bsr0 + 2.0);

		}

		return sid.alpha;
		
	}

	//this method returns the list of advisors who have been involved in a transaction
	//with the seller whose reputation is being calculated
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
	//method to use the dirichlet formula to get the predicted reputation of the seller
	public void displayMeanDirichlet(Environment env){


		double alpha[] = new double[Parameter.NO_OF_CRITERIA];double alpha2[] = new double[Parameter.NO_OF_CRITERIA];	
		for(int i=0;i<env.getSellerList().size();i++){

			for(int q=0;q<Parameter.NO_OF_CRITERIA;q++){
				int bsr0=0,bsr1=0;
				for(int m=0;m<totalBuyers;m++){

					for(int t=0;t<env.getBuyerList().get(m).getTrans().size();t++){


						if(env.getBuyerList().get(m).getTrans().get(t).getSeller().getId() == i){
							if(env.getBuyerList().get(m).getTrans().get(t).getRating().getCriteriaRatings().get(q).getCriteriaRatingValue()<=0)
								bsr0++;
							if(env.getBuyerList().get(m).getTrans().get(t).getRating().getCriteriaRatings().get(q).getCriteriaRatingValue()>0)
								bsr1++;	

						}
					}
				}
				alpha2[q] = bsr1+1.0;
				alpha[q] = (bsr1+1.0)/(bsr0+bsr1+2.0);
			}
		}
		DirichletDist obj = new DirichletDist(alpha2);
		double[][] CoVar = obj.getCovariance();
		double[] mean = obj.getMean();

		System.out.println("COVARIANCE");
		for(int i=0;i<CoVar.length;i++){
			for(int j=0;j<CoVar[0].length;j++)
				System.out.print(" "+CoVar[i][j]);
			System.out.println();
		}
	}
	public void calculateReputation1(Buyer b, Seller sid, ArrayList<Boolean> trustAdvisors){}
	public ArrayList<Boolean> calculateReputation2(Buyer b, Seller sid, ArrayList<Boolean> trustAdvisors){return new ArrayList<Boolean>();}

	@Override
	public double calculateTrust(Seller seller, Buyer honestBuyer,
			int criteriaid) {
		// TODO Auto-generated method stub
		return 0;
	}

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
        double trust=0.0;
		
		double[] trustArray=calculateMultiCriteriaTrust(honestBuyer,ec.containsSeller(sid));
		for(int i=0;i<trustArray.length;i++)
			trust+=trustArray[i];
		trust=trust/trustArray.length;
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
