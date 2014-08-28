/**
 * Integrated Clustering-Based (iCLUB) model
 * Employs clustering methods:Density-Based Clustering routine (DBSCAN) 
 * Utilizes two components:Local and global knowledge to remove unfair ratings
 */
package defenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import main.Parameter;
import distributions.PseudoRandom;
import agent.Buyer;
import agent.Seller;
import environment.Environment;

public class IClub extends Defense{
	// parameter setting for DBSCAN
	private int minPts = 1; 
	private double eps = 0.3; 
	// parameter for iCLUB
	private int epsilon = 1; 
	
	/**Unlike BRS which only handles binary ratings, iCLUB is a model put forward to tackle multi-nominal ratings. 
	 * It employs clustering methods such as the Density-Based Clustering routine (DBSCAN) and 
	 * utilizes two components, Local and global knowledge to remove unfair ratings. 
	 * The local knowledge is derived by comparing the buyer’s ratings to a particular seller with 
	 * the ratings from advisors to that particular seller. If a particular advisor’s ratings are 
	 * found not to be in the same cluster as the buyer’s ratings, 
	 * they will be regarded as unfair ratings and will be filtered out. 
	 * In other words, such advisors’ ratings are inconsistent with the buyer’s ratings. 
	 * In the event if the buyer’s knowledge of a particular seller is limited (Transactions between buyer and that seller is minimal), 
	 * the buyer will need to rely on global knowledge. The global knowledge is derived by comparing the buyer’s ratings 
	 * with the set of advisors’ ratings to all sellers excluding the ratings to the seller that is 
	 * being evaluated. A set of advisors whose ratings are in the same cluster as 
	 * the buyer’s ratings will be used as a benchmark to filter out unfair ratings in the future.
	 * 
	 */
	@Override
	public double calculateTrust(Seller seller, Buyer honestBuyer, int criteriaid) {
		int bid = honestBuyer.getId();
		int sid = seller.getId();
		double numOfRatings = 0;
		String key = bid+"_"+sid;
		trustOfAdvisors = new ArrayList<Double>(); 
		bsr = ecommerce.getBsr();
		if (trustOfAdvisors.size()==0){
			for(int h=0; h<totalBuyers; h++){
				trustOfAdvisors.add(0.0);
			}
		}
		if(Parameter.RATING_TYPE.compareToIgnoreCase("binary") == 0){

			numOfRatings = bsr.get(key).get(0+"_"+criteriaid) + bsr.get(key).get(1+"_"+criteriaid);
		}
		else {
			int mnrLen = Parameter.RATING_MULTINOMINAL.length;
			for(int j=0; j<mnrLen; j++){
				if(bsr.get(key)==null) continue;
				numOfRatings += bsr.get(key).get(j+"_"+criteriaid);
			}
		}
		Vector advisor = null;
		if (numOfRatings >= epsilon) {
			advisor = local(bid, sid, criteriaid);
		} else {
			advisor = global(bid, sid, criteriaid);
		}

		// calculate the reputation from advisor
		if (advisor == null || advisor.size() == 0)
			return 0.5;

		double rep_aBS = 0.5;
		if (Parameter.RATING_TYPE.compareTo("binary") == 0) {
			rep_aBS = trustBasedAdvisorsB(bid, sid, advisor, criteriaid);
		} else {
			rep_aBS = trustBasedAdvisorsM(bid, sid, advisor, criteriaid);
		}


		// get the trust for advisors
		Vector<Integer> storedAdvisors = honestBuyer.getAdvisors();
		storedAdvisors.clear();
		for (int i = 0; i < advisor.size(); i++) {
			int aid = Integer.parseInt(advisor.get(i).toString());
			if (aid == bid)
				continue;
			if (aid < Parameter.NO_OF_DISHONEST_BUYERS+Parameter.NO_OF_HONEST_BUYERS) {
				trustOfAdvisors.set(aid,1.0);
			}		
			storedAdvisors.add(aid);
			honestBuyer.setTrustAdvisor(aid, 1.0);
		}
		honestBuyer.calculateAverageTrusts(sid); // get the average trust of
		return rep_aBS;
	}

	// for multinominal ratings;
	private double trustBasedAdvisorsM(int bid, int sid, Vector advisor, int criteriaid) {

		double rep_aBS = 0.5;
		HashMap<String, HashMap<String, Double>> bsr = ecommerce.getBsr();
		int mnrLen = Parameter.RATING_MULTINOMINAL.length;
		int halfPos = mnrLen / 2;
		ArrayList<Double> BAforS = new ArrayList<Double>();
		for(int i=0; i<2; i++){
			BAforS.add(0.0);
		}

		for (int i = 0; i < advisor.size(); i++) {

			int aid = Integer.parseInt(advisor.get(i).toString());
			if (aid == bid)
				continue;
			String key = aid+"_"+sid;
			for(int j=0; j<mnrLen; j++){
				if(bsr.get(key) ==null) continue;
				if (j < halfPos) {
					BAforS.set(0, (bsr.get(key).get(j+"_"+criteriaid) * Math.abs(halfPos-j)) + BAforS.get(0));
				} else if (j > halfPos) {
					BAforS.set(1, (bsr.get(key).get(j+"_"+criteriaid) * Math.abs(halfPos-j)) + BAforS.get(1));
				}
			}
		}
		rep_aBS = (BAforS.get(1) + 1.0 * Parameter.m_laplace)
				/ (BAforS.get(0) + BAforS.get(1) + 2.0 * Parameter.m_laplace);

		return rep_aBS;
	}

	// for binary ratings;
	private double trustBasedAdvisorsB(int bid, int sid, Vector advisor, int criteriaid) {

		double rep_aBS = 0.5;
		ArrayList<Double> BAforS = new ArrayList<Double>();
		for(int i=0; i<2; i++){
			BAforS.add(0.0);
		}
		HashMap<String, HashMap<String, Double>> bsr = ecommerce.getBsr();

		for (int i = 0; i < advisor.size(); i++) {
			int aid = Integer.parseInt(advisor.get(i).toString());
			if (aid == bid)
				continue;
			String key = aid+"_"+sid;
			if(bsr.get(key)==null) continue;

			BAforS.set(0, BAforS.get(0) + bsr.get(key).get(0+"_"+criteriaid));
			BAforS.set(1, BAforS.get(1) + bsr.get(key).get(1+"_"+criteriaid));
		}
		rep_aBS = (BAforS.get(1) + 1.0 * Parameter.m_laplace)
				/ (BAforS.get(0) + BAforS.get(1) + 2.0 * Parameter.m_laplace);

		return rep_aBS;
	}

	private Vector global(int bid, int sid, int criteriaid) {

		// step 1: find the local advisor for seller which have transaction with
		// buyer <bid>, and merge the local advisor
		int numSellers = ecommerce.getNumOfSellers();
		ArrayList<Vector> localadvisor_BSref = new ArrayList<Vector>();
		for(int i=0; i<numSellers; i++){
			localadvisor_BSref.add(null);
		}
		for (int i = 0; i < numSellers; i++) {
			int Sref = i;
			if (Sref == sid)
				continue;
			localadvisor_BSref.set(i, local(bid, Sref, criteriaid));
		}
		// merge such local advisor for reference sellers based on buyer
		Vector localadvisor_BSref_merge = new Vector(); // W_F in paper
		for (int i = 0; i < numSellers; i++) {
			int Sref = i;
			if (Sref == sid || localadvisor_BSref.get(i) == null)
				continue;
			localadvisor_BSref_merge = localadvisor_BSref.get(i);
		}
		for (int i = 0; i < numSellers; i++) {
			int Sref = i;
			if (Sref == sid || localadvisor_BSref.get(i) == null)
				continue;
			localadvisor_BSref_merge = interSection(localadvisor_BSref_merge,localadvisor_BSref.get(i));
		}

		// step 2: find the local advisor for (bid, sid), merge the from local
		// advisor and local reference advisor
		ArrayList<Vector> globaladvisor = DBSCAN(bid, sid, criteriaid); // W_F_j in paper
		if (globaladvisor == null)
			return null;
		for (int i = 0; i < globaladvisor.size(); i++) {
			Vector localadvisor_BS_cluster = globaladvisor.get(i); // W_c in paper
			// ignore the local information;
			if (localadvisor_BS_cluster.contains(bid + ""))
				localadvisor_BS_cluster.remove(bid + "");
			if (localadvisor_BSref_merge.size() > 0) {
				globaladvisor.set(i,interSection(localadvisor_BSref_merge,localadvisor_BS_cluster));
			} else {
				globaladvisor.set(i,localadvisor_BS_cluster);
			}
		}

		// step 3: find the maximum intersection
		ArrayList<Integer> gaSize = new ArrayList<Integer>();
		for(int i=0; i<globaladvisor.size(); i++){
			gaSize.add(0);
		}
		int maxSize = -Integer.MAX_VALUE;
		for (int i = 0; i < globaladvisor.size(); i++) {
			gaSize.set(i, globaladvisor.get(i).size());
			if (gaSize.get(i) > maxSize) {
				maxSize = gaSize.get(i);
			}
		}
		ArrayList<Integer> gaMaxsize = new ArrayList<Integer>();
		for(int i=0; i<globaladvisor.size(); i++){
			gaMaxsize.add(0);
		}
		int gaMaxsizeIdx = 0;
		for (int i = 0; i < globaladvisor.size(); i++) {
			if (gaSize.get(i)== maxSize) {
				gaMaxsize.set(gaMaxsizeIdx++, i);
			}
		}
		int q = gaMaxsize.get(PseudoRandom.randInt(0, gaMaxsizeIdx - 1));

		Vector globaladvisor_BS = globaladvisor.get(q);
		return globaladvisor_BS;
	}

	private Vector interSection(Vector av, Vector bv) {

		Vector isv = new Vector();
		for (int i = 0; i < av.size(); i++) {
			Object ab_element = av.get(i);
			if (bv.contains(ab_element) && isv.contains(ab_element) == false) {
				isv.add(ab_element);
			}
		}

		return isv;
	}

	private Vector local(int bid, int sid, int criteriaid) {
		// get the cluster assignment by DBSCAN
		ArrayList<Vector> buyersclubs = DBSCAN(bid, sid, criteriaid);
		// return the clusters, not
		// include the noise
		// instances

		// find the bid, sid belong to which cluster
		if (buyersclubs == null) {
			return null;
		}

		int cIdx_BS = -1; // cluster index of (bid, sid), -1 means the noise
		for (int i = 0; i < buyersclubs.size(); i++) {
			if (buyersclubs.get(i).contains(bid + "")) {
				cIdx_BS = i;
				break;
			}
		}
		// find the advisor for cluster_BS
		if (cIdx_BS == -1) { // cIdx_BS == -1, because there is not such advisor
			// cluster (sid), include the (bid, sid).
			return null;
		}
		Vector localadvisor_BS = buyersclubs.get(cIdx_BS);
		// ignore the buyer itself
		localadvisor_BS.remove(bid + "");
		if (localadvisor_BS.size() == 0) {
			localadvisor_BS = null;
		}
		return localadvisor_BS;
	}

	private ArrayList<Vector> DBSCAN(int bid, int sid, int criteriaid) {
		// return the clusters of bid = the advisor buyers (be similar with the
		// bid) for sid

		int rLen = 0;
		HashMap<String, HashMap<String, Double>> bsr = ecommerce.getBsr();
		if (Parameter.RATING_TYPE.compareTo("binary") == 0) {
			rLen = Parameter.RATING_BINARY.length - 1;
		} else {
			rLen = Parameter.RATING_MULTINOMINAL.length;
		}
		int numBuyers = ecommerce.getNumOfBuyers();

		// create a new instances;
		FastVector attInfo = new FastVector();
		// attribute include: [neg, pos] or [1, 2, 3, 4, 5], 3 = null
		for (int i = 0; i < rLen; i++) {
			attInfo.addElement(new Attribute("rating" + Integer.toString(i + 1)));
		}
		Instances header = new Instances("ratings.arff", attInfo, numBuyers);
		Instances ratings = new Instances(header);

		ArrayList<Boolean> validatebid = new ArrayList<Boolean>();
		for(int i=0; i<numBuyers; i++){
			validatebid.add(false);
		}

		for (int i = 0; i < numBuyers; i++) {
			int Bref = i;
			double sum = 0.0;
			for(int j=0; j<rLen; j++){
				String key=Bref+"_"+sid;
				if(bsr.get(key)==null) continue;
				sum+= bsr.get(key).get(j+"_"+criteriaid);
			}
			if (sum == 0) {
				validatebid.set(i,false);
			} else {
				Instance inst = new Instance(ratings.numAttributes());
				inst.setDataset(ratings);
				for (int j = 0; j < rLen; j++) {
					String key=Bref+"_"+sid;
					if(bsr.get(key)==null) continue;
					inst.setValue(j, bsr.get(key).get(j+"_"+criteriaid)/ sum);
				}
				validatebid.set(i,true);
				ratings.add(inst);
			}
		}
		if (ratings.numInstances() == 0) {
			return null;
		}

		try {
			// initialize the dbscan parameters
			Clusterer dbscan = new weka.clusterers.DBScan();
			((weka.clusterers.DBScan) dbscan).setMinPoints(minPts);
			((weka.clusterers.DBScan) dbscan).setEpsilon(eps);
			// build cluster
			dbscan.buildClusterer(ratings);

			// find the cluster has difference buyers;
			ArrayList<Vector> clubs = new ArrayList<Vector>();

			for (int i = 0; i < dbscan.numberOfClusters(); i++) {
				clubs.add(i,new Vector());
			}
			int inst_idx = -1;
			for (int i = 0; i < numBuyers; i++) {
				if (validatebid.get(i) == false)
					continue;
				inst_idx++;
				Instance inst = ratings.instance(inst_idx);
				int cnum = -1;
				try {
					cnum = dbscan.clusterInstance(inst);
				} catch (Exception e) {
					cnum = -1; // -1 means noise
				}
				if (cnum != -1) {
					clubs.get(cnum).add(i + "");
				}
			}

			return clubs;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

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
		ecommerce.getMcc().updateDailyTPR(TPRValues,ecommerce.getDay());

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
		ecommerce.updateDailyReputationDiff(trustValues, sid,criteriaid);
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
