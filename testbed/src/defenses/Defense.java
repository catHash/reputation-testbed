/**class Defense: Serves as an abstract class to define methods and attributes to be inherited by
 * the different defense models.   
 */
package defenses;

import java.util.*;

import weka.core.Instance;
import environment.*;
import agent.Buyer;
import agent.Seller;
import main.Parameter;


public abstract class Defense {
	protected Environment ecommerce = null;
	protected int day;
	protected int dhBuyer = Parameter.NO_OF_DISHONEST_BUYERS;
	protected int hBuyer = Parameter.NO_OF_HONEST_BUYERS;
	protected int dhSeller = Parameter.NO_OF_DISHONEST_SELLERS;
	protected int hSeller = Parameter.NO_OF_HONEST_SELLERS;
	protected String defenseName = null;
	protected int totalBuyers = dhBuyer + hBuyer;
	protected int totalSellers = dhSeller + hSeller;
	protected int m_NumInstances;	

	protected HashMap<String, HashMap<String, Double>> bsr;
	protected ArrayList<Double> trustOfAdvisors;
	protected ArrayList<Double> rtimes;


	public abstract double calculateTrust(Seller seller, Buyer honestBuyer, int criteriaid);
	public abstract Instance chooseSeller(int day, Buyer b, Environment ec);
	public abstract double predictRealSellerReputation(Buyer b, Environment ec,Seller s, int criteriaid);

	
	public void setEcommerce(Environment ec){
		ecommerce = ec; 
	}

	//perform the defense model
	public double giveFairRating(Instance inst){
		// step 1: insert rating from honest buyer		
		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		// find the dishonest buyer in <day>, give unfair rating
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

		return fairRating;
	}

	//Used by the multi-criteria defense models
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

		return fairRating;

	}

}