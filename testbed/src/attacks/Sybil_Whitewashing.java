/**class Sybil_Whitewashing: inherits the properties of the Attack class.
 * 
 */

package attacks;

import agent.*;
import attacks.Attack;
import weka.core.Instance;
import weka.core.Instances;
import distributions.PseudoRandom;
import environment.Environment;
import main.Parameter;

public class Sybil_Whitewashing  extends Attack{	

	//After no_of_day_stay, the dishonest buyer just leaves and enters using a new account
	private double dishonest_buyer_percentage;
	//After no_of_day_stay, the dishonest buyer just leaves and enters using a new account
	private int no_of_day_stay = 1;
	
	public Sybil_Whitewashing(){

		dishonest_buyer_percentage = (double)Parameter.NO_OF_DISHONEST_BUYERS/((double)Parameter.NO_OF_HONEST_BUYERS + (double)Parameter.NO_OF_DISHONEST_BUYERS);
		if (dishonest_buyer_percentage < 0.5){
			System.out.println("No a Sybil attack! Please ensure disnohest buyers are majority.");
			System.exit(0);
		}
	}

	public double giveUnfairRating(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);				
		// find the dishonest buyer in <day>, give unfair rating
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double unfairRating = complementRating(sVal);
		double rVal = unfairRating;		
		// add the rating to instances
		inst.setValue(Parameter.m_ratingIdx, rVal);	

		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return rVal;
	}

	public Instance chooseSeller(int day, Buyer b, Environment ec){
		//attack the target sellers with probability (Para.m_targetDomination), attack common sellers randomly with 1 - probability
		int sellerid;
		this.day = day;
		this.ecommerce = ec;
		Instances transactions = ecommerce.getInstTransactions();				

		if(PseudoRandom.randDouble() < Parameter.m_dishonestBuyerOntargetSellerRatio){ //Para.m_targetDomination
			sellerid = (PseudoRandom.randDouble() < 0.5)? PseudoRandom.randInt(0, Parameter.NO_OF_DISHONEST_SELLERS-1):PseudoRandom.randInt(Parameter.NO_OF_DISHONEST_SELLERS, Parameter.NO_OF_DISHONEST_SELLERS+Parameter.NO_OF_HONEST_SELLERS-1);
		} else{
			//1 + [0, 18) = [1, 19) = [1, 18]
			sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
		}

		int sVal = sellerid;
		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);	

		//add instance
		int dVal = day + 1;			
		int bVal = b.getId();
		if (day > 0) {
			bVal = (Parameter.NO_OF_DISHONEST_BUYERS + Parameter.NO_OF_HONEST_BUYERS) + (day - 1) * Parameter.NO_OF_DISHONEST_BUYERS + bVal;			
		}

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


