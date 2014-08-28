/**Robustness is evaluated by comparing the transactional volume between the honest and
* dishonest sellers. The more robust (closer to 1) the trust model, the larger 
* the transactional volume difference between them. 
 */

package main;

import java.util.HashMap;

import environment.Environment;
import environment.EnvironmentS;


public class Robustness {

	private HashMap<Integer, Integer> noOfTrans_HS;
	private HashMap<Integer, Integer> noOfTrans_DS;
	private HashMap<Integer, Double> robustness;
	private Environment ec;
	public Robustness(Environment ec){
		this.ec = ec;
		noOfTrans_HS = new HashMap<Integer, Integer>();
		noOfTrans_DS = new HashMap<Integer, Integer>();
		robustness = new HashMap<Integer, Double>();
		for(int i=0; i<=Parameter.NO_OF_DAYS; i++){
			noOfTrans_DS.put(i, 0);
			noOfTrans_HS.put(i, 0);
		}
	}


	public double getRobustness(int day){
		int totalSalesDS =0, totalSalesHS=0;
		for(int i=0; i<ec.getNumOfSellers(); i++){
			if( i< Parameter.NO_OF_DISHONEST_SELLERS){
				totalSalesDS += ec.getSellerList().get(i).getDailySales().get(day);
			}
			else{

				totalSalesHS += ec.getSellerList().get(i).getDailySales().get(day);
			}
		}

		noOfTrans_DS.put(day, totalSalesDS);
		noOfTrans_HS.put(day, totalSalesHS);
		double theoricialBound2=0, theoricialBound1=0;
		if(ec instanceof EnvironmentS){
			 theoricialBound1 = ((day+1) * Parameter.NO_OF_HONEST_BUYERS);
			 theoricialBound2 = ((day+1) * Parameter.NO_OF_DISHONEST_BUYERS);
		}
		else{
			 theoricialBound1 = (ec.getNumOfRep());
			 theoricialBound2 = (ec.getNumOfRep());
		}
		double theoricialBound = theoricialBound1 > theoricialBound2? theoricialBound1: theoricialBound2;
		if(Parameter.includeSybil(ec.getAttackName())){
			theoricialBound = theoricialBound1 < theoricialBound2? theoricialBound1: theoricialBound2;
		}
		double robustness;
		robustness = (totalSalesDS - totalSalesHS) / (-theoricialBound);
		this.robustness.put(day, robustness);
		return robustness;
	}


	public HashMap<Integer, Integer> getNoOfTrans_HS() {
		return noOfTrans_HS;
	}


	public void setNoOfTrans_HS(HashMap<Integer, Integer> noOfTrans_HS) {
		this.noOfTrans_HS = noOfTrans_HS;
	}


	public HashMap<Integer, Integer> getNoOfTrans_DS() {
		return noOfTrans_DS;
	}


	public void setNoOfTrans_DS(HashMap<Integer, Integer> noOfTrans_DS) {
		this.noOfTrans_DS = noOfTrans_DS;
	}


	public HashMap<Integer, Double> getRobustness() {
		return robustness;
	}


	public void setRobustness(HashMap<Integer, Double> robustness) {
		this.robustness = robustness;
	}


}
