/**Class to create transaction entity. The information contained in each transaction entity gets
 * stored in a detailed .arff file to document the transactions created by the simulated environment. 
 */
package main;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import agent.Buyer;
import agent.Seller;


public class Transaction {

	private Buyer buyer;
	private Seller seller;
	private Product product;
	private String time;
	private double amountPaid;
	private Rating rating;
	private int day;
	private int quantity;
	private double price;
	private String remarks;

	public String getRemarks(){
		return remarks;
	}

	public void setRemarks(String remarks){
		this.remarks = remarks;
	}

	public double getPrice(){
		return price;
	}

	public Product getProduct(){
		return product;
	}

	public void setPrice(double price){
		this.price = price;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setQuantity(int quantity){
		this.quantity=quantity;
	}

	public int getDay(){
		return day;
	}

	public void setDay(int day){
		this.day = day;
	}

	public Transaction(){

	}

	public void create(Buyer buyer, Seller seller, Product product, int quantity, double price, int day, double amountPaid){
		this.buyer = buyer;
		this.seller = seller;
		this.product = product;
		this.amountPaid = amountPaid;
		this.price = price;
		this.day = day;
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		time = sdf.format(cal.getTime());
		this.rating = new Rating();
	}
	
	public void updateTransRatings( double[] value){
		rating.create(seller, buyer, value);
	}

	public void edit(){

	}

	public Transaction view(){
		Transaction t = null;
		return t;
	}

	public Buyer getBuyer() {
		return buyer;
	}

	public Seller getSeller() {
		return seller;
	}

	public String getTime() {
		return time;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

	public Rating getRating() {
		return rating;
	}



	public Transaction getTransaction(){
		Transaction t = null;
		return t;
	}

}
