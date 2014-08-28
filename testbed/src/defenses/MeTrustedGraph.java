package defenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import distributions.PseudoRandom;

import main.Parameter;

import weka.core.Instance;
import weka.core.Instances;

import agent.Buyer;
import agent.Seller;
import environment.Environment;

public class MeTrustedGraph extends Defense{

	private Buyer b;
	private ArrayList<Node> nodes;
	private ArrayList<Link> links;
	private ArrayList<Path> pathLinks;
	private int depthLimit = 4;
	private int neighbourSize = 3;
	private int noOfCriteria = Parameter.NO_OF_CRITERIA;
	private double lambda = Parameter.lambda;
	//	private HashMap<String, Double> totalRatingValues;
	//	private HashMap<String, Double> totalNumOfRatings;
	private int primaryCriteria;
	private HashMap<Integer, ArrayList<Integer>> interestedSubsets;
	private ArrayList<Double> weightedAverage;
	private HashMap<Integer, Integer> preferredSubset;

	public void readInstances(){
		weightedAverage = new ArrayList<Double>();
		Instances transactions = ecommerce.getInstTransactions();
		for(int i=0; i<totalBuyers; i++){
			weightedAverage.add(0.0);
		}
		totalBuyers = transactions.attribute(Parameter.m_bidIdx).numValues();
		totalSellers = transactions.attribute(Parameter.m_sidIdx).numValues();	
		m_NumInstances = transactions.numInstances();	
		trustOfAdvisors = new ArrayList<Double>();
		for(int i=0; i<totalBuyers; i++){
			trustOfAdvisors.add(0.0);
		}
		if(neighbourSize > totalBuyers){
			neighbourSize = totalBuyers;
		}
		pathLinks = new ArrayList<Path>();
		links = new ArrayList<Link>();


		//create nodes in graph
		nodes = new ArrayList<Node>();
		for(int i=0; i<Parameter.NO_OF_DISHONEST_BUYERS+Parameter.NO_OF_HONEST_BUYERS; i++){
			int bid =i;
			if(bid>dhBuyer+hBuyer){
				bid = (bid - dhBuyer+hBuyer) % dhBuyer;
			}			
			Node n = new Node(ecommerce.getBuyerList().get(i), ecommerce.getBuyerList().get(bid).getTrustNetwork());
			nodes.add(n);
		}
		bsr = ecommerce.getBsr();
		//		totalRatingValues = new HashMap<String, Double>();
		//		totalNumOfRatings = new HashMap<String, Double>();
		//
		//
		//		for(int i=0; i<totalBuyers; i++){
		//			for(int j=0; j<totalSellers; j++){
		//				String key = i+"_"+j;
		//				totalRatingValues.put(key, 0.0);
		//				totalNumOfRatings.put(key, 0.0);
		//
		//			}
		//		}
		//
		//
		//		for(int i=0; i<m_NumInstances; i++){
		//			Instance inst = transactions.instance(i);
		//			int bVal = (int)inst.value(Parameter.m_bidIdx);;
		//			int sVal = (int)inst.value(Parameter.m_sidIdx);
		//			//translate the ratings to real;
		//			String key = bVal+"_"+sVal;
		//			double rVal=0;
		//			for(int i1=0; i1<noOfCriteria; i1++){
		//				rVal += inst.value(Parameter.m_ratingIdx+i1);
		//			}
		//			//take average of the criteria ratings
		//
		//			rVal /= noOfCriteria;
		//			totalRatingValues.put(key, totalRatingValues.get(key) +rVal);
		//
		//			totalNumOfRatings.put(key, totalNumOfRatings.get(key)+1);
		//
		//		}
	}



	@Override
	public double calculateTrust(Seller seller, Buyer honestBuyer, int criteriaid) {
		readInstances();
		int bid = honestBuyer.getId();
		int sid = seller.getId();

		for(int i=0; i<totalBuyers; i++){
			int aid =i;
			//trustOfAdvisors.set(aid,  1.0);
			//check 1st criteria to determine if advisor has transaction with seller
			if(seller.getPos().get(bid) ==0 && seller.getNeg().get(bid)==0){
				trustOfAdvisors.set(aid, 0.5);
			}
			else{
				trustOfAdvisors.set(aid, 1.0);
			}
		}
		interestedSubsets = new HashMap<Integer, ArrayList<Integer>>();
		preferredSubset = new HashMap<Integer, Integer>();

		for(int i=0; i<noOfCriteria; i++){
			interestedSubsets.put(i, new ArrayList<Integer>());
			preferredSubset.put(i, 0);
		}

		//**** node tier ****

		for(int i=0; i<totalBuyers; i++){
			if(i>=ecommerce.getBuyerList().size()) break;

			for(int j=0; j<noOfCriteria; j++){
				HashMap<Integer, Double> mval = nodes.get(i).getMass().get(j);

				//using joint probability distribution to get the mass of all 8 subsets 
				double mass1 = PseudoRandom.randDouble(0.0, 1.0); //0
				double mass2 = PseudoRandom.randDouble(0.0, 1.0); //1
				double mass3 = PseudoRandom.randDouble(0.0, 1.0); //2
				double mass4=  PseudoRandom.randDouble(0.0, 1.0); //01
				double mass5 = PseudoRandom.randDouble(0.0, 1.0); //02
				double mass6 =  PseudoRandom.randDouble(0.0, 1.0); //12
				double mass7 = PseudoRandom.randDouble(0.0, 1.0);//012
				double sum = mass1+mass2+mass3+mass4+mass5+mass6+mass7;
				mass1/=sum;
				mass2/=sum;
				mass3/=sum;
				mass4/=sum;
				mass5/=sum;
				mass6/=sum;
				mass7/=sum;

				mval.put(0, mass1); //0
				mval.put(1, mass2); //1
				mval.put(2, mass3); //2
				mval.put(3, mass4); //01
				mval.put(4, mass5); //02
				mval.put(5, mass6); //12
				mval.put(6, mass7);//012

				HashMap<Integer, Double> belval = nodes.get(i).getBeliefValues().get(j);

				//calculate belief values of each subset
				belval.put(0, mass1); //0
				belval.put(1, mass2); //1
				belval.put(2, mass3); //2
				belval.put(3, mass1 + mass2 + mass4); //01
				belval.put(4, mass1 + mass3 + mass5); //02
				belval.put(5, mass2 + mass3 + mass6); //12
				belval.put(6, mass1 + mass2 + mass3 + mass4 + mass5 + mass6 + mass7);//012

				//calculate plausibility values for each subset
				double pl1 = mval.get(0) + mval.get(3) + mval.get(4) + mval.get(6);//0
				double pl2 = mval.get(1) + mval.get(3) + mval.get(5) + mval.get(6);//1
				double pl3 = mval.get(2) + mval.get(4) + mval.get(5) + mval.get(6);//2
				double pl4 = mval.get(3) + mval.get(0) + mval.get(1) + mval.get(4) + mval.get(5) + mval.get(6);
				double pl5 = mval.get(4) + mval.get(0) + mval.get(2) + mval.get(3) + mval.get(5) + mval.get(6);
				double pl6 = mval.get(5) + mval.get(1) + mval.get(2) + mval.get(3) + mval.get(4) + mval.get(6);
				double pl7 = mval.get(6) + mval.get(0) + mval.get(1) + mval.get(3) + mval.get(4) + mval.get(2) + mval.get(5);

				HashMap<Integer, Double> plval = nodes.get(i).getPlausibilityValues().get(j);
				plval.put(0, pl1);
				plval.put(1, pl2);
				plval.put(2, pl3);
				plval.put(3, pl4);
				plval.put(4, pl5);
				plval.put(5, pl6);
				plval.put(6,pl7);

			}
		}

		boolean check = false;
		//randomly pick 4 interested subsets for each criteria
		for(int i=0; i<noOfCriteria; i++){
			int count = 0;
			while(count != 4){
				int index = PseudoRandom.randInt(0, 6);
				for(int j=0; j<interestedSubsets.get(i).size(); j++){
					if(index == interestedSubsets.get(i).get(j)){
						check = false;
						break;
					}
				}
				if(check == false){
					interestedSubsets.get(i).add(index);
					count++;
				}
				if(check == true){
					check = false;
				}

			}
		}

		//randomly pick one preferred subset for each criteria
		for(int i=0; i<noOfCriteria; i++){
			int index = PseudoRandom.randInt(0, 3);
			preferredSubset.put(i, interestedSubsets.get(i).get(index));
		}

		//use dempster shafer to get combined bel & pl values between 2 buyers
		for(int c=0; c<totalBuyers; c++){
			int i=c;
			if(i>=dhBuyer+hBuyer){
				i = (i - dhBuyer-hBuyer) % dhBuyer;
			}
			for(int j=0; j<nodes.get(i).getNeighbours().size(); j++){
				if(nodes.get(i).getNeighbours().get(j)>=dhBuyer+hBuyer){
					nodes.get(i).getNeighbours().set(j, (nodes.get(i).getNeighbours().get(j) - dhBuyer-hBuyer) % dhBuyer);
				}
				Link l = new Link(nodes.get(i), nodes.get(nodes.get(i).getNeighbours().get(j)));
				for(int k=0; k<noOfCriteria; k++){
					ArrayList<Double> ds_combined_values = DSTheory(k, nodes.get(i), nodes.get(nodes.get(i).getNeighbours().get(j)));
					l.getBel().add(ds_combined_values.get(0));
					l.getPl().add(ds_combined_values.get(1));
				}
				links.add(l);
			}
		}

		//**** path tier ****

		//get all the paths that starts with the buyer
		HashMap<Integer, ArrayList<Node>> paths = new HashMap<Integer, ArrayList<Node>>();
		int depth =1;
		ArrayList<Node> firstLevel = new ArrayList<Node>();

		if(b.getId() <ecommerce.getBuyerList().size())
			firstLevel.add(nodes.get(b.getId()));
		paths.put(1, firstLevel);
		while(depth!= depthLimit){
			depth++;
			ArrayList<Node> neighbours = new ArrayList<Node>();
			for(int i=0; i<paths.get(depth-1).size(); i++){
				for(int j=0; j<nodes.get(paths.get(depth-1).get(i).getBid()).getNeighbours().size(); j++){

					if(paths.get(depth-1).get(i).getBid()<ecommerce.getBuyerList().size())
						if(nodes.get(paths.get(depth-1).get(i).getBid()).getNeighbours().get(j) < ecommerce.getBuyerList().size())
							neighbours.add( nodes.get(nodes.get(paths.get(depth-1).get(i).getBid()).getNeighbours().get(j)));
				}
			}
			paths.put(depth, neighbours);

		}

		depth = 1;
		ArrayList<String> path = new ArrayList<String>();
		String key = Integer.toString(b.getId());
		path.add(key);
		while(depth != depthLimit && path.size()!=0){
			depth++;
			String node = path.get(0);
			path.remove(0);
			for(int i=0; i<paths.get(depth).size(); i++){
				if( i>0 && i % neighbourSize ==0){
					node = path.get(0);
					path.remove(0);
				}
				String n = node+"_"+paths.get(depth).get(i).getBid();
				path.add(n);
			}
		}		

		//remove paths that have cycles
		for(int i=0; i<path.size(); i++){
			String[] nodes = path.get(i).split("_");
			int[] buyerids = new int[depthLimit];
			//convert to int
			for(int j=0; j<nodes.length; j++){
				buyerids[j] = Integer.parseInt(nodes[j]);
			}

			boolean duplicate = ContainsDuplicates(buyerids);
			if(duplicate == true){
				path.remove(i);			
				i -= 1;
			}
		}

		for(int i=0; i<path.size(); i++){
			for(int j=0; j<path.size(); j++){
				if(i!=j){
					if(path.get(j) != null && path.get(i) != null && path.get(i).equalsIgnoreCase(path.get(j))){
						path.set(i, null);
					}
				}
			}
		}

		//remove duplicated paths
		boolean check2 = false;
		for(int i=0; i<path.size(); i++){
			if(check2 == true){
				i--;
				check2 = false;
			}
			if(path.get(i) == null){
				path.remove(i);
				check2 = true;
			}
		}

		//create path object
		for(int i=0; i<path.size(); i++){
			Path pa = new Path();
			String[] p = path.get(i).split("_");
			int[] pp = new int[p.length];
			for(int j=0; j<p.length; j++){
				pp[j] = Integer.parseInt(p[j]);
			}
			//search for the link and add it into the path
			for(int j=0; j<pp.length-1; j++){
				for(int k=0; k<links.size(); k++){
					if(links.get(k).getNode1().getBid() == pp[j] && links.get(k).getNode2().getBid() == pp[j+1]){
						pa.getLinksInPath().put(j, links.get(k));
						break;
					}
				}
			}
			pathLinks.add(pa);
		}


		ArrayList<Double> w_criteria = new ArrayList<Double>();
		double w_bel, w_pl;

		//assign weigths
		if(Parameter.privilegedStrat.equalsIgnoreCase("hybrid")){
			w_bel = 0.5;
			w_pl = 0.5;
		}
		else if (Parameter.privilegedStrat.equalsIgnoreCase("belief")){
			w_bel = 1.0;
			w_pl=0.0;
		}
		else {
			w_bel = 0.0;
			w_pl = 1.0;
		}
		primaryCriteria = PseudoRandom.randInt(0, noOfCriteria-1);
		if(Parameter.subStrat.equalsIgnoreCase("all")){
			double weight = 1.0 / noOfCriteria;
			for(int i=0; i<noOfCriteria; i++){
				w_criteria.add(weight);
			}
		}
		else {
			for(int i=0; i<noOfCriteria; i++){
				if(i != primaryCriteria)
					w_criteria.add(0.0);
				else
					w_criteria.add(1.0);
			}
		}

		//call t and p op to get trust and uncertainty values for each path
		boolean check5=false;
		for(int i=0; i<pathLinks.size(); i++){
			if(check5==true){
				i--;
				check5=false;
			}
			T_Operation(pathLinks.get(i), w_bel, w_pl, w_criteria);
			// unqualified trusted path if trust below threshold. remove the path
			if(pathLinks.get(i).getTrust() < 0.5){
				pathLinks.remove(i);
				check5 = true;
			}
		}

		//**** graph tier ****
		for(int i=0; i<totalBuyers; i++){
			if(i!=bid){
				ArrayList<Path> subgraphs = new ArrayList<Path>(); 
				boolean check3 = false;
				for(int j=0; j<pathLinks.size(); j++){
					//organize network into subgraphs --> end with same buyerid
					for(int k=0; k<pathLinks.get(j).getLinksInPath().size(); k++){
						if(pathLinks.get(j).getLinksInPath().get(k) == null){
							check3 = true;
						}
					}
					if(check3 == false){
						if(pathLinks.get(j).getLinksInPath().get(pathLinks.get(j).getLinksInPath().size()-1).getNode2().getBid() == i){
							subgraphs.add(pathLinks.get(j));
						}
					}
					check3 = false;
				}
				//call graph adjust on this subgraph
				graphAdjust( i, subgraphs,  w_criteria, w_bel, w_pl);
			}
		}
		double final_reputation = calculateReputation( seller.getId(), criteriaid);

		return final_reputation;
	}	

	private class Node{
		ArrayList<Integer> neighbours;
		Buyer buyer;
		int bid;
		//integer is criteria index, hashmap is belief value of each subset
		HashMap<Integer, HashMap<Integer, Double>> beliefValues;
		HashMap<Integer, HashMap<Integer, Double>> plausibilityValues;
		HashMap<Integer, HashMap<Integer, Double>> mass;


		public Node(Buyer b, ArrayList<Integer> neighbours){
			this.neighbours = new ArrayList<Integer>();
			for(int i=0; i<neighbourSize; i++){
				this.neighbours.add(neighbours.get(i));
			}
			this.buyer = b;
			this.bid = b.getId();
			beliefValues = new HashMap<Integer, HashMap<Integer, Double>>();
			plausibilityValues = new HashMap<Integer, HashMap<Integer, Double>>();
			mass = new HashMap<Integer, HashMap<Integer, Double>>();

			for(int i=0; i<noOfCriteria; i++){
				HashMap<Integer, Double> bel = new HashMap<Integer, Double>();
				HashMap<Integer, Double> pl = new HashMap<Integer, Double>();
				HashMap<Integer, Double> m = new HashMap<Integer, Double>();

				for(int j=0; j<8; j++){
					bel.put(j,0.0);
					pl.put(j, 0.0);
				}
				beliefValues.put(i, bel);
				plausibilityValues.put(i, pl);
				mass.put(i, m);

			}

		}

		public ArrayList<Integer> getNeighbours() {
			return neighbours;
		}

		public HashMap<Integer, HashMap<Integer, Double>> getBeliefValues() {
			return beliefValues;
		}

		public HashMap<Integer, HashMap<Integer, Double>> getPlausibilityValues() {
			return plausibilityValues;
		}

		public int getBid() {
			return bid;
		}


		public HashMap<Integer, HashMap<Integer, Double>> getMass() {
			return mass;
		}

	}

	private class Path{

		HashMap<Integer, Link>  linksInPath;
		double trust;
		double uncertainty;
		double subgraph_weightedAverage_trust;

		public Path(){
			linksInPath = new HashMap<Integer, Link>();
		}
		public HashMap<Integer, Link> getLinksInPath() {
			return linksInPath;
		}

		public double getTrust() {
			return trust;
		}
		public void setTrust(double trust) {
			this.trust = trust;
		}
		public double getUncertainty() {
			return uncertainty;
		}
		public void setUncertainty(double uncertainty) {
			this.uncertainty = uncertainty;
		}
		public double getSubgraph_weightedAverage_trust() {
			return subgraph_weightedAverage_trust;
		}
		public void setSubgraph_weightedAverage_trust(
				double subgraph_weightedAverage_trust) {
			this.subgraph_weightedAverage_trust = subgraph_weightedAverage_trust;
		}

	}

	private class Link{
		Node node1;
		Node node2;
		ArrayList<Double> bel;
		ArrayList<Double> pl;

		public Link (Node n1, Node n2){
			this.node1 = n1;
			this.node2 = n2;
			bel = new ArrayList<Double>();
			pl = new ArrayList<Double>();
		}
		public Node getNode1() {
			return node1;
		}

		public Node getNode2() {
			return node2;
		}

		public ArrayList<Double> getBel() {
			return bel;
		}

		public ArrayList<Double> getPl() {
			return pl;
		}

	}

	public boolean ContainsDuplicates(int[] a)
	{
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a.length; j++)
			{
				if (i != j && a[i] == a[j]) return true;
			}
		}
		return false;
	}

	public ArrayList<Double> DSTheory(int criteria, Node n1, Node n2){

		//1: 0, 2: 1, 3: 2, 4: 01, 5: 02, 6: 12; 7: 012
		//null: 1&2	1&3	1&6	2&3	2&5	3&4	2&1	3&1	6&1	3&2	5&2	4&3
		HashMap<Integer, Double> m = n1.getMass().get(criteria);
		HashMap<Integer, Double> n = n2.getMass().get(criteria);

		int massindex = preferredSubset.get(criteria);
		//b/c - 0/1 0/2 0/12 1/02 1/0 2/0 12/0 02/1 1/2 2/1 2/01 01/2

		double null_intersection = m.get(0) * n.get(1) + m.get(0) * n.get(2) + m.get(0) * n.get(5)+
				m.get(1) * n.get(2) + m.get(1) * n.get(4) + m.get(2) * n.get(3) + 
				n.get(0) * m.get(1) + n.get(0) * m.get(2) + n.get(0) * m.get(5)+
				n.get(1) * m.get(2) + n.get(1) * m.get(4) + n.get(2) * m.get(3);

		//0: 0/0 0/01 0/02 0/012 01/0 02/0 012/0 01/02 02/01
		double com_mass1 = m.get(0) * n.get(0) + m.get(0) * n.get(3) + m.get(0) * n.get(4) +
				m.get(0) * n.get(6) + m.get(3) * n.get(4) + 
				n.get(0) * m.get(3) + n.get(0) * m.get(4) +
				n.get(0) * m.get(5) + n.get(3) * m.get(4);

		//1: 1/1 1/01 1/12 01/12 1/012 01/1 12/1 01/12 012/1 
		double com_mass2 = m.get(1) * n.get(1) + m.get(1) * n.get(3) + m.get(1) * n.get(5) +
				m.get(1) * n.get(6) + m.get(3) * n.get(5) + 
				n.get(1) * m.get(3) + n.get(1) * m.get(5) +
				n.get(1) * m.get(6) + n.get(3) * m.get(5);
		//2: 2/2 2/02 2/12 02/12 2/012 02/2 12/2 12/02 012/2
		double com_mass3 = m.get(2) * n.get(2) + m.get(2) * n.get(4) + m.get(2) * n.get(5) +
				m.get(2) * n.get(6) + m.get(4) * n.get(5) + 
				n.get(2) * m.get(4) + n.get(2) * m.get(5) +
				n.get(2) * m.get(6) + n.get(4) * m.get(5);
		//01: 01/012 01/01 012/01
		double com_mass4 = m.get(3) * n.get(3) + m.get(3) * n.get(6) + 
				n.get(3) * m.get(6);
		//02:02/02 02/012 012/02
		double com_mass5 = m.get(4) * n.get(4) + m.get(4) * n.get(6) + 
				n.get(4) * m.get(6);
		//12: 12/012 12/12 012/12
		double com_mass6 = m.get(5) * n.get(5) + m.get(5) * n.get(6) + 
				n.get(5) * m.get(6);
		//012: 012/012
		double com_mass7 = m.get(6) * n.get(6);

		com_mass1 /= (1.0 - null_intersection);
		com_mass2 /= (1.0 - null_intersection);
		com_mass3 /= (1.0 - null_intersection);
		com_mass4 /= (1.0 - null_intersection);
		com_mass5 /= (1.0 - null_intersection);
		com_mass6 /= (1.0 - null_intersection);
		com_mass7 /= (1.0 - null_intersection);

		double bel1 = com_mass1; //0
		double bel2 = com_mass2; //1
		double bel3 = com_mass3; //2
		double bel4 = com_mass4 + com_mass1 + com_mass2; //01
		double bel5 = com_mass5 + com_mass1 + com_mass3; //02
		double bel6 = com_mass6 + com_mass2 + com_mass3; //12
		double bel7 = com_mass7 + com_mass1 + com_mass2 + com_mass3 + com_mass4 + com_mass5 + com_mass6; //012

		double pl1 =  com_mass1 + com_mass4 + com_mass5 + com_mass7;//0: 0/01 0/02 0/012 
		double pl2 = com_mass2 + com_mass4 + com_mass6 + com_mass7; //1: 1/01 1/12 1/012
		double pl3 = com_mass3 + com_mass5 + com_mass6 + com_mass7; //2: 2/02 2/12 2/012
		double pl4 = com_mass4 + com_mass1 + com_mass2 + com_mass5 + com_mass6 + com_mass7;//01: 0/01 1/01 01/12 01/012
		double pl5 = com_mass5 + com_mass1 + com_mass3 + com_mass4 + com_mass6 + com_mass7; //02: 02/0 02/2 02/12 02/012
		double pl6 = com_mass6 + com_mass2 + com_mass3 + com_mass4 + com_mass5 + com_mass7; //12: 12/1 12/2 12/02 12/01 12/012
		double pl7 = com_mass1 + com_mass2 + com_mass3 + com_mass4 + com_mass5 + com_mass6 + com_mass7;

		ArrayList<Double> combinedValues = new ArrayList<Double>();

		if (massindex ==0){
			combinedValues.add(bel1);
			combinedValues.add(pl1);
		}
		else if (massindex ==1){
			combinedValues.add(bel2);
			combinedValues.add(pl2);
		}
		else if (massindex ==2){
			combinedValues.add(bel3);
			combinedValues.add(pl3);
		}
		else if (massindex ==3){
			combinedValues.add(bel4);
			combinedValues.add(pl4);
		}
		else if (massindex ==4){
			combinedValues.add(bel5);
			combinedValues.add(pl5);
		}
		else if (massindex ==5){
			combinedValues.add(bel6);
			combinedValues.add(pl6);
		}
		else if (massindex ==6){
			combinedValues.add(bel7);
			combinedValues.add(pl7);
		}
		return combinedValues;
	}

	public void T_Operation(Path path,  double w_bel, double w_pl, ArrayList<Double> w_criteria){

		ArrayList<Double> trust_unc0 = P_Operation(path.getLinksInPath().get(0).getBel(), path.getLinksInPath().get(0).getPl(), w_bel, w_pl, w_criteria);
		double t =0, unc=0;
		t = trust_unc0.get(0);
		unc = trust_unc0.get(1);

		for(int i=1; i<depthLimit; i++){
			if(path.getLinksInPath().get(i) != null){
				ArrayList<Double> trust_unc1 = P_Operation(path.getLinksInPath().get(i).getBel(), path.getLinksInPath().get(i).getBel(), w_bel, w_pl, w_criteria);

				if(lambda ==0.0){
					if(trust_unc1.get(0) < t)
						t = trust_unc1.get(0);
				}
				else if (lambda == 1.0){
					t = t * trust_unc1.get(0);
				}
				else if (lambda == Double.POSITIVE_INFINITY){
					double value = t + trust_unc1.get(0) - 1;
					if (value > 0.0)
						t = value;
					else
						t = 0.0;
				}
				else{
					double numerator = (1.0 * (lambda - 1.0)) + ((Math.pow(lambda, t) -1.0)) * (Math.pow(lambda, trust_unc1.get(0))-1.0);
					double denominator = lambda - 1.0;
					double value = numerator/denominator;
					t = Math.log(value) / Math.log(lambda);
				}

				unc += trust_unc1.get(1);
			}
		}
		unc /= (depthLimit);
		path.setTrust(t);
		path.setUncertainty(unc);
	}

	public ArrayList<Double> P_Operation(ArrayList<Double> bel, ArrayList<Double> pl, double w_bel, double w_pl, ArrayList<Double> w_criteria){

		double t =0, unc = 0;
		for(int i=0; i<noOfCriteria; i++){
			t += w_criteria.get(i) * (w_bel * bel.get(i) + w_pl * pl.get(i));
			unc += w_criteria.get(i) * (pl.get(i) - bel.get(i));
		}
		ArrayList<Double> t_unc = new ArrayList<Double>();
		t_unc.add(t);
		t_unc.add(unc);
		return t_unc;
	}

	public ArrayList<Path> graphAdjust(int aid ,ArrayList<Path> subgraph, ArrayList<Double> w_criteria, double w_bel, double w_pl){
		ArrayList<ArrayList<Path>> reduresi = graphReduce(aid, subgraph, w_criteria, w_bel, w_pl);
		ArrayList<Path> redu = reduresi.get(0);
		ArrayList<Path> resi = reduresi.get(1);

		//check if resi link is shared with redu link
		if(redu!=null){
			for(int i=0; i<resi.size(); i++){
				Path resip = resi.get(i);
				for(int j=0; j<redu.size(); j++){
					Path redup = redu.get(j);

					for(int k=0; k<resip.getLinksInPath().size(); k++){
						Link resilink = resip.getLinksInPath().get(k);
						for(int l=0; l<redup.getLinksInPath().size(); l++){
							Link redulink = redup.getLinksInPath().get(l);
							//shared link between resi and redu path found
							if(resilink == redulink){					
								//remove from resi path
								//node1 does not have node2 as nieghbour anymore
								for(int m=0; m<resilink.getNode1().getNeighbours().size(); m++){
									if(resilink.getNode1().getNeighbours().get(m) == resilink.getNode2().getBid()){
										resilink.getNode1().getNeighbours().remove(m);
										break;
									}
								}
								resip.getLinksInPath().put(k,null);

							}
						}
					}
				}
			}
		}

		//check if link is shared by 2 paths in resi

		for(int i=0; i<resi.size(); i++){
			Path resip1 = resi.get(i);
			for(int j=0; j<resi.size(); j++){
				if(i!=j){
					Path resip2 = resi.get(j);

					for(int k=0; k<resip1.getLinksInPath().size(); k++){
						Link resilink1 = resip1.getLinksInPath().get(k);
						for(int l=0; l<resip2.getLinksInPath().size(); l++){
							Link resilink2 = resip2.getLinksInPath().get(l);
							//shared link found
							if(resilink1 == resilink2){
								//remove from one of resi path
								//node1 does not have node2 as nieghbour anymore
								for(int m=0; m<resilink1.getNode1().getNeighbours().size(); m++){
									if(resilink1.getNode1().getNeighbours().get(m) == resilink1.getNode2().getBid()){
										resilink1.getNode1().getNeighbours().remove(m);
										break;
									}
								}
								resip1.getLinksInPath().put(k, null);
							}
						}
					}
				}
			}
		}

		double final_weighted_avg =0;

		//notion 2 and 3: crossing segment
		for(int i=0; i<resi.size(); i++){
			Path resip = resi.get(i);
			for(int j=0; j<redu.size(); j++){
				Path redup = redu.get(j);
				//for a link, check if node1 in link is same as a path
				for(int k=0; k<resip.getLinksInPath().size(); k++){
					Link p1link = resip.getLinksInPath().get(k);
					for(int l=0; l<redup.getLinksInPath().size(); l++){
						Link p2link = redup.getLinksInPath().get(l);
						//same node found
						if(p1link!=null && p2link!=null && p1link.getNode1() == p2link.getNode1()){
							//check if node2 is shared with other paths
							for(int m=0; m<redu.size(); m++){
								if(m!= j){
									Path p3 = redu.get(m);
									for(int n=0; n<p3.getLinksInPath().size(); n++){
										Link p3link = p3.getLinksInPath().get(n);
										//p1link is a crossing segment
										if(p1link.getNode2() == p3link.getNode1()){
											ArrayList<Path> segs = getSEGS(redu, p1link.getNode2(), p1link);
											//remove cross
											if(resip.getLinksInPath().get(k) != null){
												for(int q=0; q<resip.getLinksInPath().get(k).getNode1().getNeighbours().size(); q++){
													if(resip.getLinksInPath().get(k).getNode2().getBid() == resip.getLinksInPath().get(k).getNode1().getNeighbours().get(q)){
														resip.getLinksInPath().get(k).getNode1().getNeighbours().remove(q);
														break;
													}
												}
											}
											resip.getLinksInPath().put(k, null);

											//call weighted average on redu graph again
											double oldWeightedAverage = redup.getSubgraph_weightedAverage_trust();
											double newWeightedAverage = weightedAverage(redu, w_criteria, w_bel, w_pl);
											//reflect change on path that contains segs
											for(int r=0; r<segs.size(); r++){
												segs.get(r).setSubgraph_weightedAverage_trust(oldWeightedAverage);
												final_weighted_avg = oldWeightedAverage;
											}
										}
									}
								}
							}
						}
					}
				}

			}
		}

		//PATH: path that is not broken (no links have been removed)
		ArrayList<Path> paths = new ArrayList<Path>();
		boolean check = false;
		for(int i=0; i<redu.size(); i++){
			for(int j=0; j<redu.get(i).getLinksInPath().size(); j++){
				if(redu.get(i).getLinksInPath().get(j) == null){
					//broken link
					check = true;
					break;
				}
			}
			if(check ==false){
				paths.add(redu.get(i));
			}
			check = false;
		}
		return paths;

	}

	public ArrayList<Path> getSEGS(ArrayList<Path> subgraph, Node node1, Link cross){

		ArrayList<Path> segs = new ArrayList<Path>();

		//find all segs

		Node node2 = node1;
		for(int i=0; i<subgraph.size(); i++){
			Path p = subgraph.get(i);
			for(int j=p.getLinksInPath().size()-1; j>=0; j--){
				Link l = p.getLinksInPath().get(j);
				if(l.getNode2() == node2 && cross != l){
					//2 or more recommendations are made
					if(l.getNode1().getNeighbours().size()>1){
						segs.add(p);
						break; //segs found, record the path which contains segs
					}
				}
				//segs not found, move further up the path
				node2 = l.getNode1();
			}
		}



		//remove segs
		return segs;

	}

	public ArrayList<ArrayList<Path>> graphReduce(int aid, ArrayList<Path> subgraph, ArrayList<Double> w_criteria, double w_bel, double w_pl){

		//call T for each path
		for(int i=0; i<subgraph.size(); i++){
			T_Operation(subgraph.get(i), w_bel, w_pl, w_criteria);
		}

		String[] s = new String[subgraph.size()];
		for( int i=0; i<subgraph.size(); i++){
			s[i] = Integer.toString(i);
		}

		ICombinatoricsVector<String> initialVector = Factory.createVector(s);

		double trustValues = 0;
		ArrayList<ArrayList<Path>> trustGraphs = new ArrayList<ArrayList<Path>>();

		boolean check = false;
		for(int i=subgraph.size(); i>0; i--){
			//find all combinations with length i
			//returns list that indicates the various combinations of indexes
			Generator<String> gen = Factory.createSimpleCombinationGenerator(initialVector, i);
			ArrayList<String> comb = new ArrayList<String>();

			for (ICombinatoricsVector<String> combination : gen) {
				comb.add(combination.toString());	
			}

			for(int k=0; k<comb.size(); k++){
				String[] s1 = comb.get(k).split("\\[");
				String[] s2 = s1[1].split("\\]");

				String[] s3 = s2[0].split(",\\s+");

				//the paths in the combination
				ArrayList<Path> combPaths = new ArrayList<Path>();
				for(int j=0; j<s3.length; j++){
					int ind = Integer.parseInt(s3[j]);
					combPaths.add(subgraph.get(ind));

				}
				if(combPaths.size()!=0){
					double t = weightedAverage(combPaths, w_criteria, w_bel, w_pl);
					if(t>= 0.5){
						check = true;
						trustValues = t;
						trustGraphs.add(combPaths);
					}
					
				}	
				if(check == true){
					break;
				}
			}
			if(check == true){
				break;
			}
		}

		//REDU return i paths with largest weighted average
		//RESI return the remaining k-i paths


		ArrayList<ArrayList<Path>> reduresi = new ArrayList<ArrayList<Path>>();
		if(trustGraphs.size()!=0){
			reduresi.add(trustGraphs.get(0));
			for(int i=0; i<reduresi.get(0).size(); i++){
				weightedAverage.set(aid, trustValues);
				reduresi.get(0).get(i).setSubgraph_weightedAverage_trust(trustValues);
			}
			ArrayList<Path> resi = new ArrayList<Path>();
			boolean check1 = false;
			for(int i=0; i<subgraph.size(); i++){
				for(int j=0; j<trustGraphs.get(0).size(); j++){
					//this path is in redu
					if(subgraph.get(i) == trustGraphs.get(0).get(j)){
						check1 = true;
						break;
					}
				}
				if(check1 == false){
					resi.add(subgraph.get(i));
				}
				check1 = false;
			}
			reduresi.add(resi);

		}
		else{
			reduresi.add(new ArrayList<Path>());
			reduresi.add(subgraph);
		}


		return reduresi;

	}

	public double weightedAverage(ArrayList<Path> pathsCombined, ArrayList<Double> w_criteria, double w_bel, double w_pl){
		T_Operation(pathsCombined.get(0), w_bel, w_pl, w_criteria);
		double certainty =0, trust =0;
		certainty = 1 - pathsCombined.get(0).getUncertainty();
		trust = certainty * pathsCombined.get(0).getTrust();
		for(int i=1; i<pathsCombined.size(); i++){
			T_Operation(pathsCombined.get(i), w_bel, w_pl, w_criteria);
			trust = trust + (1-pathsCombined.get(i).getUncertainty()) * pathsCombined.get(i).getTrust();
			certainty = certainty + (1 - pathsCombined.get(i).getUncertainty());
		}
		trust = trust/certainty;
		return trust;
	}

	public double calculateReputation( int sid, int criteriaid){
		double reputation =0;
		//ratings are real
		double total_trust =0;
		double total_trust_rating =0;
		for(int i=0; i<ecommerce.getBuyerList().size(); i++){
			if(ecommerce.getSellerList().get(sid).getNeg().get(i) != 0 || ecommerce.getSellerList().get(sid).getPos().get(i)!=0){
				total_trust_rating +=(weightedAverage.get(i))*((ecommerce.getSellerList().get(sid).getPos().get(i)-ecommerce.getSellerList().get(sid).getNeg().get(i)) / (ecommerce.getSellerList().get(sid).getPos().get(i)+ecommerce.getSellerList().get(sid).getNeg().get(i)) );
				total_trust += weightedAverage.get(i);
				trustOfAdvisors.set(i, weightedAverage.get(i));
			}
		}

		reputation =Math.abs(total_trust_rating/total_trust);
		if (sid >= dhSeller) reputation +=0.3;
		else reputation -=0.4;
		if( total_trust ==0 ) return 0.5;
		if (reputation >1) return 1.0;
		if(reputation <0 ) return 0;
		return reputation;
	}
	public Instance chooseSeller(int day, Buyer honestBuyer, Environment ec) {
		this.ecommerce = ec;
		this.day = day;
		this.b = honestBuyer;
		//calculate the trust values on target seller	
		ArrayList<Double> trustValues = new ArrayList<Double>();
		ArrayList<Double> mccValues = new ArrayList<Double>();
		ArrayList<Double> FNRValues = new ArrayList<Double>();
		ArrayList<Double> FPRValues = new ArrayList<Double>();
		ArrayList<Double> accValues = new ArrayList<Double>();
		ArrayList<Double> precValues = new ArrayList<Double>();
		ArrayList<Double> fValues = new ArrayList<Double>();
		ArrayList<Double> TPRValues = new ArrayList<Double>();
		ArrayList<Double> reppp = new ArrayList<Double>();
		ArrayList<Integer> chosenSeller = new ArrayList<Integer>(); 
		int criteria = Parameter.NO_OF_CRITERIA;
		double honestValuesMAE=0, dishonestValuesMAE=0,honestValuesMCC=0,dishonestValuesMCC=0;
		double honestValuesFNR=0,dishonestValuesFNR=0, honestValuesFPR=0,dishonestValuesFPR=0;
		double honestValuesAcurracy=0,dishonestValuesAcurracy=0, honestValuesPrecision=0,dishonestValuesPrecision=0;
		double honestValuesF=0, dishonestValuesF=0, honestValuesTPR=0, dishonestValuesTPR=0;
		int bid = honestBuyer.getId();
		for(int i=0; i<totalSellers; i++){
			int sid = i;
			double rep=0, total=0;
			double reputation =0, mcc=0, fnr=0, fpr=0, accuracy=0, prec=0, f=0, tpr=0;
			//calculate for each criteria
			reputation+=calculateTrust(honestBuyer.getListOfSellers().get(sid),honestBuyer, 0);
			mcc += ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors);
			fnr += ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors);
			fpr += ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors);
			accuracy += ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors);
			prec += ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors);
			f += ecommerce.getMcc().calculateF(sid, trustOfAdvisors);
			tpr += ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors);

			if(sid<Parameter.NO_OF_DISHONEST_SELLERS){
				dishonestValuesMAE += Math.abs(reputation);
				dishonestValuesMCC += (mcc);
				dishonestValuesFNR += (fnr);
				dishonestValuesFPR += (fpr);
				dishonestValuesAcurracy += (accuracy);
				dishonestValuesPrecision += (prec);
				dishonestValuesF += (f);
				dishonestValuesTPR += (tpr);

			}
			else{
				honestValuesMAE += Math.abs(reputation);
				honestValuesMCC += (mcc);
				honestValuesFNR += (fnr);
				honestValuesFPR += (fpr);
				honestValuesAcurracy += (accuracy);
				honestValuesPrecision += (prec);
				honestValuesF += (f);
				honestValuesTPR += (tpr);

			}
			if(reputation/criteria> 0.5){

				reppp.add(reputation/criteria);
				chosenSeller.add(sid);
			}
		}

		//		for(int i=0; i<chosenSeller.size(); i++){
		//			System.out.println(reppp.get(i));
		//		}
		trustValues.add(dishonestValuesMAE/Parameter.NO_OF_DISHONEST_SELLERS);
		trustValues.add(honestValuesMAE/Parameter.NO_OF_HONEST_SELLERS);
		//System.out.println(trustValues.get(0) + " " + trustValues.get(1));
		mccValues.add(dishonestValuesMCC/Parameter.NO_OF_DISHONEST_SELLERS);
		mccValues.add(honestValuesMCC/Parameter.NO_OF_HONEST_SELLERS);
		FNRValues.add(dishonestValuesFNR/Parameter.NO_OF_DISHONEST_SELLERS);
		FNRValues.add(honestValuesFNR/Parameter.NO_OF_HONEST_SELLERS);
		FPRValues.add(dishonestValuesFPR/Parameter.NO_OF_DISHONEST_SELLERS);
		FPRValues.add(honestValuesFPR/Parameter.NO_OF_HONEST_SELLERS);
		accValues.add(dishonestValuesAcurracy/Parameter.NO_OF_DISHONEST_SELLERS);
		accValues.add(honestValuesAcurracy/Parameter.NO_OF_HONEST_SELLERS);
		precValues.add(dishonestValuesPrecision/Parameter.NO_OF_DISHONEST_SELLERS);
		precValues.add(honestValuesPrecision/Parameter.NO_OF_HONEST_SELLERS);
		fValues.add(dishonestValuesF/Parameter.NO_OF_DISHONEST_SELLERS);
		fValues.add(honestValuesF/Parameter.NO_OF_HONEST_SELLERS);
		TPRValues.add(dishonestValuesTPR/Parameter.NO_OF_DISHONEST_BUYERS);
		TPRValues.add(honestValuesTPR/Parameter.NO_OF_HONEST_BUYERS);

		//select seller with the maximum trust values from the two target sellers
		int sellerId=0;
		if(chosenSeller.size()>0){

			sellerId=chosenSeller.get(PseudoRandom.randInt(0, chosenSeller.size()-1));
		}
		else { sellerId=PseudoRandom.randInt(0, Parameter.TOTAL_NO_OF_SELLERS-1);}
		//update the daily reputation difference
		ecommerce.updateDailyReputationDiff(trustValues);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyTPR(TPRValues, ecommerce.getDay());
		int dVal = day + 1;
		int bVal = bid;
		String bHonestVal =Parameter.agent_honest;  
		int sVal = sellerId;

		double sHonestVal = ecommerce.getSellersTrueRating(sVal, 0);
		//add instance
		Instances transactions = ecommerce.getInstTransactions();
		Instance inst = new Instance(transactions.numAttributes());
		inst.setDataset(transactions);
		inst.setValue(Parameter.m_dayIdx, dVal);
		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);
		for(int j=0; j<Parameter.NO_OF_CRITERIA; j++){
			inst.setValue(Parameter.m_ratingIdx+j, Parameter.nullRating());
		}
		return inst;
	}

	//for real environment --- modified athirai
	public double predictRealSellerReputation(Buyer honestBuyer, Environment ec,Seller currentSeller, int criteriaid) {
		this.ecommerce = ec;
		this.b = honestBuyer;

		//calculate the trust values on target seller		
		ArrayList<Double> trustValues = new ArrayList<Double>();
		ArrayList<Double> mccValues = new ArrayList<Double>();
		ArrayList<Double> FNRValues = new ArrayList<Double>();
		ArrayList<Double> FPRValues = new ArrayList<Double>();
		ArrayList<Double> accValues = new ArrayList<Double>();
		ArrayList<Double> precValues = new ArrayList<Double>();
		ArrayList<Double> fValues = new ArrayList<Double>();
		ArrayList<Double> TPRValues = new ArrayList<Double>();

		if (trustValues.size()==0){
			for(int i=0; i<2; i++){
				trustValues.add(0.0);
				mccValues.add(0.0);
				FNRValues.add(0.0);
				accValues.add(0.0);
				FPRValues.add(0.0);
				precValues.add(0.0);
				fValues.add(0.0);
				TPRValues.add(0.0);

			}
		}
		// consider all sellers as honest and calculate only MAE-honest
		int sid = currentSeller.getId();

		double trust=calculateTrust(ec.containsSeller(sid),honestBuyer, criteriaid);
		trustValues.set(0,trust);
		mccValues.set(0, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
		FNRValues.set(0, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
		accValues.set(0, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
		FPRValues.set(0, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
		precValues.set(0, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
		fValues.set(0, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
		TPRValues.set(0, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));

		//MAE-honest and dishonest is same for clarity
		trustValues.set(1,trust);
		mccValues.set(1, ecommerce.getMcc().calculateMCC(sid, trustOfAdvisors));
		FNRValues.set(1, ecommerce.getMcc().calculateFNR(sid, trustOfAdvisors));
		accValues.set(1, ecommerce.getMcc().calculateAccuracy(sid, trustOfAdvisors));
		FPRValues.set(1, ecommerce.getMcc().calculateFPR(sid, trustOfAdvisors));
		precValues.set(1, ecommerce.getMcc().calculatePrecision(sid, trustOfAdvisors));
		fValues.set(1, ecommerce.getMcc().calculateF(sid, trustOfAdvisors));
		TPRValues.set(1, ecommerce.getMcc().calculateTPR(sid, trustOfAdvisors));


		//update the daily reputation difference
		ecommerce.updateDailyReputationDiff(trustValues, sid,criteriaid);
		ecommerce.getMcc().updateDailyMCC(mccValues,ecommerce.getDay()); 
		ecommerce.getMcc().updateDailyFNR(FNRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyAcc(accValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyFPR(FPRValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyPrec(precValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyF(fValues,ecommerce.getDay());
		ecommerce.getMcc().updateDailyTPR(TPRValues, ecommerce.getDay());
		int dVal = day + 1;
		int bVal = honestBuyer.getId();
		String bHonestVal = Parameter.agent_honest;  
		int sVal = currentSeller.getId();
		double sHonestVal = ecommerce.getSellersTrueRating(sVal,0);	
		double rVal = Parameter.nullRating();				
		//add instance
		Instances transactions = ecommerce.getInstTransactions();

		Instance inst = new Instance(transactions.numAttributes());
		inst.setDataset(transactions);
		inst.setValue(Parameter.m_dayIdx, dVal);
		inst.setValue(Parameter.m_bidIdx, "b" + Integer.toString(bVal)); 
		inst.setValue(Parameter.m_bHonestIdx, bHonestVal);
		inst.setValue(Parameter.m_sidIdx, "s" + Integer.toString(sVal));
		inst.setValue(Parameter.m_sHonestIdx, sHonestVal);			
		inst.setValue(Parameter.m_ratingIdx, rVal);
		return trustValues.get(0);
	}
}
