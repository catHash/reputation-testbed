/**Criteria refers to the dimension/s on which a buyer/seller can be rated upon.
 * 
 */
package main;

public class Criteria {

	private double value=-999;
	private int id=-1;
	
	
	public double getCriteriaRatingValue() {
		return value;
	}
	public void setCriteriaRatingValue(double value) {
		this.value = value;
	}
	public int getCriteriaId() {
		return id;
	}
	public void setCriteriaId(int id) {
		this.id = id;
	}
	
	
}
