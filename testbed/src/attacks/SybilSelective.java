/**class SybilSelective: Inherits the properties from the Attack class and contains additional attributes and methods.
 * 
 * Purpose: Dishonest advisors can attempt to control the e-market place by becoming the majority. These
 * dishonest advisors can mount large-scale continuous unfair ratings on sellers; defense models
 * working on the notion that the majority of buyers are honest will fail to detect such an occurrence.
 * Applicable for multiple criteria and must have a majority of dishonest buyers.
 */

package attacks;

import weka.core.Instance;
import weka.core.Instances;
import agent.Buyer;
import distributions.PseudoRandom;
import environment.Environment;
import main.Parameter;

public class SybilSelective extends Attack{
	
	private double dishonest_buyer_percentage;

	public SybilSelective(){

		dishonest_buyer_percentage = (double)Parameter.NO_OF_DISHONEST_BUYERS/((double)Parameter.NO_OF_HONEST_BUYERS + (double)Parameter.NO_OF_DISHONEST_BUYERS);
		if (dishonest_buyer_percentage < 0.5){
			System.out.println("No a Sybil attack! Please ensure disnohest buyers are majority.");
			System.exit(0);
		}
	}

	/**inverse ratings done for multi-criteria attack models
	 * The scheme chosen is such that half of the criteria will be rated unfairly
	 * and the remaining criteria will be given fair ratings.
	*/	
	public double[] giveUnfairRatingMultiCriteria(Instance inst){

		String bHonestVal = inst.stringValue(Parameter.m_bHonestIdx);				
		// find the dishonest buyer in <day>, give unfair rating
		if (bHonestVal == Parameter.agent_honest){
			System.out.println("error, must be dishonest buyer");
		}
		int sVal = (int)(inst.value(Parameter.m_sidIdx));
		
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
        
        	criteriaRatings[i] = complementRating(TrueRatings[i]);
        	
        }//for
        for(int i=falseRatings;i<Parameter.NO_OF_CRITERIA;i++)
        	criteriaRatings[i] = TrueRatings[i];
        }
        else{
        	for(int i=falseRatings;i<Parameter.NO_OF_CRITERIA;i++){
        		criteriaRatings[i] = complementRating(TrueRatings[i]);
        	}
        	for(int i=0;i<falseRatings;i++)
        		criteriaRatings[i] = TrueRatings[i];
        }//else
		
		for(int i=0;i<Parameter.NO_OF_CRITERIA;i++){
			inst.setValue(Parameter.m_ratingIdx+i, criteriaRatings[i]);
		}
        // add the rating to instances

		ecommerce.getInstTransactions().add(new Instance(inst));
		ecommerce.updateArray(inst);

		return criteriaRatings;
	}
	
    // randomly chooses a seller and returns an instance of the details of the transaction the seller takes part in.
	public Instance chooseSeller(int day, Buyer b, Environment ec){
		int sellerid;
		this.day = day;
		this.ecommerce = ec;
		Instances transactions = ecommerce.getInstTransactions();

		if(PseudoRandom.randDouble() < Parameter.m_dishonestBuyerOntargetSellerRatio){ //Para.m_targetDomination
			sellerid = (PseudoRandom.randDouble() < 0.5)? Parameter.TARGET_DISHONEST_SELLER:Parameter.TARGET_HONEST_SELLER;
		}else{
			//1 + [0, 18) = [1, 19) = [1, 18]
			sellerid = 1 + (int) (PseudoRandom.randDouble() * (Parameter.NO_OF_DISHONEST_SELLERS + Parameter.NO_OF_HONEST_SELLERS - 2));
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
