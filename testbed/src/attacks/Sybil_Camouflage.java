/**class Sybil_Camouflage: inherits properties from the Attack class and contains additional variables
 * and methods performed on/by it.
 * 
 * Purpose: This attack model inculcates a combination of the Sybil and Camouflage attack, where
 *  majority of the buyers are dishonest and carry out the camouflage attack.
 */
package attacks;


import agent.*;
import attacks.Attack;
import weka.core.Instance;
import weka.core.Instances;
import distributions.PseudoRandom;
import environment.Environment;
import main.Parameter;

public class Sybil_Camouflage  extends Attack{

	//After no_of_day_stay, the dishonest buyer just leaves and enters using a new account
	private double dishonest_buyer_percentage;

	public Sybil_Camouflage(){

		dishonest_buyer_percentage = (double)Parameter.NO_OF_DISHONEST_BUYERS/((double)Parameter.NO_OF_HONEST_BUYERS + (double)Parameter.NO_OF_DISHONEST_BUYERS);
		if (dishonest_buyer_percentage < 0.5){
			System.out.println("No a Sybil attack! Please ensure disnohest buyers are majority.");
			System.exit(0);
		}
	}

	// inverse rating for seller and returns the unfair rating to the caller.
	public double giveUnfairRating(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));

		double rVal = 0.0;
		if(sVal > Parameter.TARGET_DISHONEST_SELLER && sVal < Parameter.TARGET_HONEST_SELLER){
			//unfair ratings for target sellers
			double unfairRating = complementRating(sVal);
			rVal = unfairRating;
		}

		else if(sVal == Parameter.TARGET_DISHONEST_SELLER ||sVal == Parameter.TARGET_HONEST_SELLER){
			//fair rating for common sellers
			rVal = ecommerce.getSellersTrueRating(sVal,0);
		} else{
			System.err.println("Not such rating");
		}
		// add the rating to instances
		inst.setValue(Parameter.m_ratingIdx, rVal);

		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return rVal;
	}

    //chooses a seller randomly and creates an Instance containing the transaction details. 
	//The Instance is then returned. 
	public Instance chooseSeller(int day, Buyer b, Environment ec){
		this.day = day;
		this.ecommerce = ec;
		Instances transactions = ecommerce.getInstTransactions();		

		int sellerid;
		if(day< 0.2 * Parameter.NO_OF_DAYS){ 
			//for common seller, give fair rating
			sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
		} else{
			//attack the target sellers with probability (Para.m_targetDomination), attack common sellers randomly with 1 - probability
			if(PseudoRandom.randDouble() < Parameter.m_dishonestBuyerOntargetSellerRatio){ //Para.m_targetDomination
				sellerid = (PseudoRandom.randDouble() < 0.5)? PseudoRandom.randInt(0,Parameter.NO_OF_DISHONEST_SELLERS-1):PseudoRandom.randInt(Parameter.NO_OF_DISHONEST_SELLERS,Parameter.NO_OF_DISHONEST_SELLERS+Parameter.NO_OF_HONEST_SELLERS-1);
			} else{
				//1 + [0, 18) = [1, 19) = [1, 18]
				sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
			}
		}	
		int sVal = sellerid;
		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);		

		//add instance
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

}
