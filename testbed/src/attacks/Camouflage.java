/** class Camouflage: inherits the attributes and methods of the Attack class. 
 * Contains additional variables and methods performed by the attack model specifically.
 *  
 * Purpose: Under the camouflage attacks, dishonest advisors provide fair ratings initially to disguise
 * their honesty and create an honest reputation with the buyers during the initial stage. After
 * which the advisors starts providing unfair ratings in the later stages. 
 * This model is designed for single criteria.
 */
package attacks;

import agent.*;
import weka.core.Instance;
import weka.core.Instances;
import distributions.PseudoRandom;
import environment.Environment;
import main.Parameter;

public class Camouflage extends Attack{

	public Camouflage(){
	}

	// give unfair rating to seller.
	public double giveUnfairRating(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		// find the dishonest buyer in <day>, give unfair rating
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

	// method which randomly chooses seller and creates an instance containing details of the transaction
	public Instance chooseSeller(int day, Buyer b, Environment ec){
		//attack the target sellers with probability (Para.m_targetDomination), attack common sellers randomly with 1 - probability
		int sellerid;
		this.day = day;
		this.ecommerce = ec;
		Instances transactions = ecommerce.getInstTransactions();		

		if(day< 0.2 * Parameter.NO_OF_DAYS){ //|sellers| >= 20, 100, 500
			//for common seller, give fair rating
			sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
		}else{
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




