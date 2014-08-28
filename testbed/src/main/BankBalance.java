/**class BankBalance: Stores the balance details of the agents involved in the ecommerce environment.
 * 
 */
package main;

import java.util.ArrayList;
import java.util.HashMap;

import agent.Buyer;

public class BankBalance{
	
	HashMap<Integer, ArrayList<Double> > bankBalance;
	
	public BankBalance(){
		bankBalance = new HashMap<Integer, ArrayList<Double>>();
	}

	public HashMap<Integer, ArrayList<Double>> getBankBalance() {
		return bankBalance;
	}


	public void setBankBalance(HashMap<Integer, ArrayList<Double>> bankBalance) {
		this.bankBalance = bankBalance;
	}


	public void updateDailyBankBalance(int day, ArrayList<Buyer> buyerList){
		ArrayList<Double> bankBal = new ArrayList<Double>();
		for(int i=0; i<buyerList.size(); i++){
			bankBal.add(buyerList.get(i).getAccount().getBalance());
		}
		bankBalance.put(day, bankBal);
	}

	
	
}
