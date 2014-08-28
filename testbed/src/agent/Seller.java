/**
 * class Seller: inherits the attributes and methods from the Agent class. And contains additional 
 * variable and methods performed on/by the seller in the testbed environment.
 */

package agent;

import java.util.ArrayList;
import java.util.HashMap;

import environment.Environment;

import main.Parameter;
import main.Product;
import main.Rating;



public class Seller extends Agent{

	private ArrayList<Rating> ratingsToBuyers;
	private ArrayList<Rating> ratingsFromBuyers;
	private ArrayList<Product> productsOnSale = new ArrayList<Product>();
	private ArrayList<Buyer> buyersRated;
	private ArrayList<Buyer> buyersRatedMe;
	private HashMap<Integer, Integer> dailySales;
	private HashMap<Integer, Integer> pos = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> neg = new HashMap<Integer, Integer>();
	public double alpha[] = new double[Parameter.NO_OF_CRITERIA];//for reece defense model

	private double totalRating = 0;


	public Seller(Environment ec){
		ecommerce = ec;
		dailySales = new HashMap<Integer, Integer> ();
		for(int i=0; i<=Parameter.NO_OF_DAYS;i++){
			dailySales.put(i, 0);
		}
		for(int i=0; i<ecommerce.getNumOfBuyers(); i++){
			pos.put(i, 0);
			neg.put(i,0);
		}
	}

	public void addProductToList(Product p){
		productsOnSale.add(p);
	}
	public ArrayList<Product> getProductsOnSale() {
		return productsOnSale;
	}

	public void setProductsOnSale(ArrayList<Product> productsOnSale) {
		this.productsOnSale = productsOnSale;
	}

	public ArrayList<Rating> getRatingsToBuyers() {
		return ratingsToBuyers;
	}

	public void setRatingsToBuyers(ArrayList<Rating> ratingsToBuyers) {
		this.ratingsToBuyers = ratingsToBuyers;
	}

	public ArrayList<Rating> getRatingsFromBuyers() {
		return ratingsFromBuyers;
	}

	public void setRatingsFromBuyers(ArrayList<Rating> ratingsFromBuyers) {
		this.ratingsFromBuyers = ratingsFromBuyers;
	}

	public ArrayList<Buyer> getBuyersRatedMe() {
		return buyersRatedMe;
	}

	public void setBuyersRatedMe(ArrayList<Buyer> buyersRatedMe) {
		this.buyersRatedMe = buyersRatedMe;
	}

	public ArrayList<Buyer> getBuyersRated() {
		return buyersRated;
	}

	public void setBuyersRated(ArrayList<Buyer> buyersRated) {
		this.buyersRated = buyersRated;
	}

	public void addSales (int day){
		dailySales.put(day,  dailySales.get(day) +1);
	}

	public HashMap<Integer, Integer> getDailySales() {
		return dailySales;
	}
	public void setDailySales(HashMap<Integer, Integer> dailySales) {
		this.dailySales = dailySales;
	}
	public HashMap<Integer, Integer> getPos() {
		return pos;
	}
	public void setPos(HashMap<Integer, Integer> pos) {

		this.pos = pos;
	}
	public HashMap<Integer, Integer> getNeg() {
		return neg;
	}
	public void setNeg(HashMap<Integer, Integer> neg) {
		this.neg = neg;
	}

	//positive or negative ratings
	public void addPos(int bid){
		pos.put(bid, pos.get(bid)+1);
	}

	public void addneg(int bid){
		neg.put(bid,  neg.get(bid)+1);
	}

}
