/**
 * class Agent: Abstract class to declare and contain the variables and methods to be used by Buyers and Sellers.
 */


package agent;

import java.util.ArrayList;

import weka.core.Instance;
import weka.core.Instances;
import main.Account;
import main.Transaction;
import environment.*;


public abstract class Agent {


	//added - athirai
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (id != other.id)
			return false;
		return true;
	}

	protected int id;
	protected boolean ishonest; //only true/false means honest/dishonest
	protected int day=0;
	protected Instances history;
	
	//points to global information
	protected Environment ecommerce = null;
	protected ArrayList<Seller> listOfSellers = null;
	protected ArrayList<Buyer> listOfBuyers = null;
	protected ArrayList<Transaction> trans = null;
	protected Account account;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public Instances getHistory() {
		return history;
	}

	public void setHistory(Instances history) {
		this.history = history;
	}

	public Environment getEcommerce() {
		return ecommerce;
	}

	public void setEcommerce(Environment ecommerce, boolean buyer) {
		this.ecommerce = ecommerce;
		history = new Instances(ecommerce.getInstTransactions());
		if(buyer==true)
			history.setRelationName("buyer" + id);
		else
			history.setRelationName("seller" + id);
		trans = new ArrayList<Transaction>();

	}


	public ArrayList<Seller> getListOfSellers() {
		return listOfSellers;
	}

	public void setListOfSellers(ArrayList<Seller> listOfSellers) {
		this.listOfSellers = listOfSellers;
	}

	public ArrayList<Buyer> getListOfBuyers() {
		return listOfBuyers;
	}

	public void setListOfBuyers(ArrayList<Buyer> listOfBuyers) {
		this.listOfBuyers = listOfBuyers;
	}

	public ArrayList<Transaction> getTrans() {
		return trans;
	}

	public void setTrans(ArrayList<Transaction> trans) {
		this.trans = trans;
	}

	public void addTrans(Transaction t){
		this.trans.add(t);
	}
	public boolean isIshonest() {
		return ishonest;
	}

	public void setIshonest(boolean ishonest) {
		this.ishonest = ishonest;
	}

	public void addInstance(Instance inst){
		history.add(inst);
	}

	public Seller getSeller(int sid){
		return listOfSellers.get(sid);
	}

	public Buyer getBuyer(int bid){
		return listOfBuyers.get(bid);
	}

}
