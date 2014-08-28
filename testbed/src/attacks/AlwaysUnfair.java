/** class AlwaysUnfair: inherits the attributes and methods of the Attack class. 
 * Contains additional variables and methods performed by the attack model specifically.
 *  
 * Purpose: Acts on single criteria transactions. Objective is to set selected transactions with unfair rating.
 */
package attacks;

import agent.Buyer;
import weka.core.Instance;
import weka.core.Instances;
import main.Parameter;
import distributions.PseudoRandom;
import environment.Environment;


public class AlwaysUnfair extends Attack{

	// method which randomly chooses seller and creates an instance containing details of the transaction
	public Instance chooseSeller(int day, Buyer b, Environment ec){
		int sellerid;
		this.ecommerce = ec;
		this.day = day;

		Instances transactions = ecommerce.getInstTransactions();		
		if(PseudoRandom.randDouble() < Parameter.m_dishonestBuyerOntargetSellerRatio){ //Para.m_targetDomination
			sellerid = (PseudoRandom.randDouble() < 0.5)? PseudoRandom.randInt(0,Parameter.NO_OF_DISHONEST_SELLERS-1):PseudoRandom.randInt(Parameter.NO_OF_DISHONEST_SELLERS,Parameter.NO_OF_DISHONEST_SELLERS+Parameter.NO_OF_HONEST_SELLERS-1);
		} else{
			//1 + [0, 18) = [1, 19) = [1, 18]
			sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
		}
		int sVal = sellerid;
		double sHonestVal = ecommerce.getSellersTrueRating(sVal, 0);	

		//add instance, update the array in e-commerce
		int dVal = day + 1;
		int bVal = b.getId();
		String bHonestVal = Parameter.agent_dishonest;  
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

	//give unfair rating to seller
	@Override
	public double giveUnfairRating(Instance inst){
		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);				
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		//rating is always unfair
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double unfairRating = complementRating(sVal);
		double rVal = unfairRating;		

		// add the unfair rating to instances	
		inst.setValue(Parameter.m_ratingIdx, rVal);

		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return rVal;
	}
	
	@Override
	public double[] giveUnfairRatingMultiCriteria(Instance inst){
		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);				
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double[] TrueRatings = ecommerce.getSellersTrueRating().get(sVal);
		int falseRatings = Parameter.NO_OF_CRITERIA/2;
		double [] criteriaRatings = new double[Parameter.NO_OF_CRITERIA];

		for(int i=0;i<Parameter.NO_OF_CRITERIA;i++){
			if(Parameter.RATING_TYPE.equalsIgnoreCase("binary")){
				criteriaRatings[i] = - TrueRatings[i];
			}
			else if(Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")){
				criteriaRatings[i] = Parameter.RATING_MULTINOMINAL.length + 1 - TrueRatings[i];
			}
			else if(Parameter.RATING_TYPE.equalsIgnoreCase("real")){
				criteriaRatings[i] = 1.0 - TrueRatings[i];
			}
			else
				System.out.println("no such rating exists");
		}
	
			
		for(int i=0;i<Parameter.NO_OF_CRITERIA;i++){
			inst.setValue(Parameter.m_ratingIdx+i, criteriaRatings[i]);
		}
		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return criteriaRatings;
	}
}
