/**The purpose of the rating class is to allow to create entities with buyer ad seller
 * and store the rating between them for the different criteria. 
 */
package main;
import java.util.ArrayList;

import agent.Buyer;
import agent.Seller;


public class Rating {

	private ArrayList<Criteria> criteriaRatings;
	private Buyer rater;
	private Seller ratee;
	
	public Rating(){
	criteriaRatings = new ArrayList<Criteria>();
	}
	
	public ArrayList<Criteria> getCriteriaRatings() {
		return criteriaRatings;
	}

	public void setCriteriaRatings(ArrayList<Criteria> criteriaRatings) {
		this.criteriaRatings = criteriaRatings;
	}

	//creates a Ratings object. An array type has been used to store multiple ratings for more
	//than one criteria
	public void create(Seller sid, Buyer bid, double[] ratings){
		this.rater = bid;
		this.ratee = sid;
		for(int i=0; i<ratings.length; i++){
			Criteria c = new Criteria();
			c.setCriteriaId(i);
			c.setCriteriaRatingValue(ratings[i]);
			criteriaRatings.add(c);
		}
	}
}
