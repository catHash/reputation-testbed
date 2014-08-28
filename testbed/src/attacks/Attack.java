/**
 * class Attack: Abstract class to declare common variables and methods used by the attack models
 * implemented in the testbed.
 */

package attacks;

import environment.*;
import agent.Buyer;
import weka.core.Instance;
import main.Parameter;


public abstract class Attack {
	protected int day;
	protected Environment ecommerce = null;
	public abstract double giveUnfairRating(Instance inst);
	public abstract Instance chooseSeller(int day, Buyer b, Environment ec);


	public Environment getEcommerce() {
		return ecommerce;
	}
	public void setEcommerce(Environment ecommerce) {
		this.ecommerce = ecommerce;
	}

	//inverse the rating to make it unfair rating
	public double complementRating(int sid){
		double trueRating= ecommerce.getSellersTrueRating(sid,0);
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
	
	/**inverse ratings done for multi-criteria attack models
	 * The scheme chosen is such that the first half of the criteria will be rated unfairly
	 * and the remaining criteria will be given fair ratings.
	*/
	public double[] giveUnfairRatingMultiCriteria(Instance inst){
		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);				
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		double[] TrueRatings = ecommerce.getSellersTrueRating().get(sVal);
		int falseRatings = Parameter.NO_OF_CRITERIA/2;
		double [] criteriaRatings = new double[Parameter.NO_OF_CRITERIA];

		for(int i=0;i<falseRatings;i++){
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
		for(int i = falseRatings;i<Parameter.NO_OF_CRITERIA;i++){

			if(Parameter.RATING_TYPE.equalsIgnoreCase("binary")){
				criteriaRatings[i] = TrueRatings[i];

			}
			else if(Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")){
				criteriaRatings[i] = TrueRatings[i];
			}
			else if(Parameter.RATING_TYPE.equalsIgnoreCase("real")){
				criteriaRatings[i] = TrueRatings[i];
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
