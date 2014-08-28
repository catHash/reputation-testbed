/** class Camouflage: inherits the attributes and methods of the Attack class. 
 * Contains additional variables and methods performed by the attack model specifically.
 *  
 * Purpose: Under the camouflage attacks, dishonest advisors provide fair ratings initially to disguise
 * their honesty and create an honest reputation with the buyers during the initial stage. After
 * which the advisors starts providing unfair ratings in the later stages. 
 * This model is designed for multiple criteria.
 */

package attacks;

import main.Parameter;
import weka.core.Instance;
import weka.core.Instances;
import agent.Buyer;
import distributions.PseudoRandom;
import environment.Environment;

public class CamouflageSelective extends Attack{

	/**inverse ratings done for multi-criteria attack models
	 * The scheme chosen is such that the first half of the criteria will be rated unfairly
	 * and the remaining criteria will be given fair ratings.
	*/
	public double[] giveUnfairRatingMultiCriteria(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);
		// find the dishonest buyer in <day>, give unfair rating
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double rVal = 0.0;
		
		double[] TrueRatings = ecommerce.getSellersTrueRating().get(sVal);
		
		int numDishonest=0;
		for(int i=0;i<ecommerce.getBuyerList().size();i++){
			if(ecommerce.getBuyerList().get(i).isIshonest()==false)
		     numDishonest++;
		}
		
		int falseRatings = Parameter.NO_OF_CRITERIA/2;
		double [] criteriaRatings = new double[Parameter.NO_OF_CRITERIA];

        int bId = (int)inst.value(Parameter.m_bidIdx);		
		
        if(bId<numDishonest/2){        
        for(int i=0;i<falseRatings;i++){
		
        if(sVal == Parameter.TARGET_DISHONEST_SELLER ||sVal == Parameter.TARGET_HONEST_SELLER){
			//unfair ratings for target sellers
		    criteriaRatings[i] = complementRating(TrueRatings[i]);
        }
		else if(sVal > Parameter.TARGET_DISHONEST_SELLER && sVal < Parameter.TARGET_HONEST_SELLER){
			//fair rating for common sellers
			criteriaRatings[i] = TrueRatings[i];
		} else{
			System.err.println("Not such rating");
		}
        }//for
        for(int i=falseRatings;i<Parameter.NO_OF_CRITERIA;i++){
        	
        	criteriaRatings[i] = TrueRatings[i];
        	
        }//for
        }//if
        else{
        	for(int i=falseRatings;i<Parameter.NO_OF_CRITERIA;i++){
        		
                if(sVal == Parameter.TARGET_DISHONEST_SELLER ||sVal == Parameter.TARGET_HONEST_SELLER){
        			//unfair ratings for target sellers
        		    criteriaRatings[i] = complementRating(TrueRatings[i]);
                }
        		else if(sVal > Parameter.TARGET_DISHONEST_SELLER && sVal < Parameter.TARGET_HONEST_SELLER){
        			//fair rating for common sellers
        			criteriaRatings[i] = TrueRatings[i];
        		} else{
        			System.err.println("Not such rating");
        		}        		
        		
        	}//for
        	for(int i=0;i<falseRatings;i++){
        		criteriaRatings[i] = TrueRatings[i];
        	}
        }
		// add the rating to instances
		
        for(int i=0;i<Parameter.NO_OF_CRITERIA;i++){
			inst.setValue(Parameter.m_ratingIdx+i, criteriaRatings[i]);
		}
        
		//update the eCommerce information
		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return criteriaRatings;
	}



	public String getParameterInfo(){
		String str= "camouflage: ";
		return str;
	}
	//randomly choose seller and stores the transaction in the form of an instance which is to be returned
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
				sellerid = (PseudoRandom.randDouble() < 0.5)? Parameter.TARGET_DISHONEST_SELLER:Parameter.TARGET_HONEST_SELLER;
			}else{
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
		double[] rVal = new double[Parameter.NO_OF_CRITERIA];
		for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
			inst.setValue(Parameter.m_ratingIdx+j, Parameter.nullRating());	
		}


		return inst;
	}

	
	public double complementRating(double trueRating){
		//double trueRating= ecommerce.getSellersTrueRating(sid,0);
		double cRating = 1.0;
		if(Parameter.RATING_TYPE.equalsIgnoreCase("binary")){
			cRating = -trueRating;
		} else if(Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")){
			cRating = Parameter.RATING_MULTINOMINAL.length + 1 - trueRating;
		} else if(Parameter.RATING_TYPE.equalsIgnoreCase("real")){
			cRating = 1.0 - trueRating;
		} else{
			System.out.println("not such type of rating");
		}

		return cRating;
	}


	@Override
	public double giveUnfairRating(Instance inst) {
		// TODO Auto-generated method stub
		return 0;
	}	
	
}
