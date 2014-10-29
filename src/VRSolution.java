import java.util.*;
import java.io.*;

public class VRSolution {
	public VRProblem problem;
	public List<List<Customer>> solution;
	public TreeMap<Double, int[]> savings = new TreeMap<Double, int[]>();
	
	public VRSolution(VRProblem problem){
		this.problem = problem;
	}

	//The dumb solver adds one route per customer
	public void oneRoutePerCustomerSolution(){
		this.solution = new ArrayList<List<Customer>>();
		for(Customer c:problem.customers){
			ArrayList<Customer> route = new ArrayList<Customer>();
			route.add(c);
			this.solution.add(route);
		}
	}
	
	//Students should implement another solution
	public void clarkWright(){
		// get the one route per customer solution
		oneRoutePerCustomerSolution();		//  O(n)
		// find all the pairs of routes
		findAllPairs();						//  O(n^2/2)
		joinPair();							//  O(n)
		
		while(!savings.isEmpty()){
			findAllPairs();					//  O(n^2/2) --- need to speed up this step
			// redoPairs();					// O(2n)
			joinPair();						//  O(n)
		}
	}
	
	public void joinPair(){
		HashMap<List<Customer>, Integer> joined = new HashMap<List<Customer>, Integer>();
		for(Double i: savings.descendingKeySet()){
			
			List<Customer> route1 = solution.get(savings.get(i)[0]);
			List<Customer> route2 = solution.get(savings.get(i)[1]);
			if(joined.containsKey(route1) || joined.containsKey(route2)) continue;
			joined.put(route1, 0);
			joined.put(route2, 0);
			route1.addAll(route2);		// ?Faster than looping through each one and adding them? O(n) vs O(1)x
			// Need to carry on making savings until I've exhausted every possible savings
			
			solution.remove(route2);
			break;
		}
	}
	
	private boolean verifyPair(int[] pair) {
		Boolean result = true;
		int total = 0;
		for(Customer c:solution.get(pair[0])) total += c.req;
		for(Customer c:solution.get(pair[1])) total += c.req;
		if (total > problem.depot.req) {
			result = false;
		}
		return result;
	}

	public double calculatePairSaving(int[] pair){
		// Originally had a brute force way
		// Get the length of the first route O(n)
		// Get the length of the second route O(n)
		// Get the length of the 2 routes joined O(n)
		// Do the math
		// Then realised I could substantially speed it up by doing it this way.
		List<Customer> route1 = solution.get(pair[0]); 	// O(1)
		List<Customer> route2 = solution.get(pair[1]);	// O(1)
		Customer cus1 = route1.get(route1.size() -1);	// O(1)
		Customer cus2 = route2.get(0);	// O(1)
		double bridge = cus1.distance(cus2);	// O(1)
		double sav1 = cus1.distance(this.problem.depot);	// O(1)
		double sav2 = cus2.distance(this.problem.depot);	// O(1)
		return sav1 + sav2 - bridge;	// O(1)
	}
	
	// Find the pairs of routes
 	public void findAllPairs(){
 		savings.clear();
		for(int i = 0; i < this.solution.size(); i++){
			for(int j = i+1; j < this.solution.size(); j++){
				int[] pair = {i, j};
				int[] pair2 = {j, i};
				calculateSavings(pair, pair2);
			}
		}
	}
 	
 	public void calculateSavings(int[] pair, int[] pair2){
 	// calculateSavings
		double sav = calculatePairSaving(pair);
		double sav2 = calculatePairSaving(pair2);
		if(sav2 > sav) sav = sav2;
		if(sav > 0){
			if(verifyPair(pair)) {
				savings.put( sav, pair );
			}
		}
 	}
	
	//Calculate the total journey
	public double solutionCost(){
		double cost = 0;
		for(List<Customer>route:solution){
			cost += routeCost(route);
		}
		return cost;
	}
	
	// Calculate the cost of a route
	public double routeCost(List<Customer> route){
		// System.out.print(route.size() + ": ");
		Customer prev = this.problem.depot;
		double cost = 0;
		for (Customer c:route){
			cost += prev.distance(c);
			prev = c;
		}
		//Add the cost of returning to the depot
		cost += prev.distance(this.problem.depot);
		return cost;
	}
	
	// Check that a route does not exceed capacity
	public Boolean verifyRoute(List<Customer> route){
		Boolean result = true;
		int total = 0;
		for(Customer c:route)
			total += c.req;
		if (total > problem.depot.req){
			System.out.printf("********FAIL Route starting %s is over capacity %d\n",
					route.get(0),
					total
					);
			result = false;
		}
		return result;
	}
	
	// Check that no routes exceed capacity
	// and that all customers are visited
	public Boolean verifySolution(){
		//Check that no route exceeds capacity
		Boolean okSoFar = true;
		for(List<Customer> route : solution){
			okSoFar = verifyRoute(route);
		}
		//Check that we keep the customer satisfied
		//Check that every customer is visited and the correct amount is picked up
		Map<String,Integer> reqd = new HashMap<String,Integer>();
		for(Customer c:this.problem.customers){
			String address = String.format("%fx%f", c.x,c.y);
			reqd.put(address, c.req);
		}
		for(List<Customer> route:this.solution){
			for(Customer c:route){
				String address = String.format("%fx%f", c.x,c.y);
				if (reqd.containsKey(address))
					reqd.put(address, reqd.get(address)-c.req);
				else
					System.out.printf("********FAIL no customer at %s\n",address);
			}
		}
		for(String address:reqd.keySet())
			if (reqd.get(address)!=0){
				System.out.printf("********FAIL Customer at %s has %d left over\n",address,reqd.get(address));
				okSoFar = false;
			}
		return okSoFar;
	}
	
	public void readIn(String filename) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String s;
		this.solution = new ArrayList<List<Customer>>();
		while((s=br.readLine())!=null){
			ArrayList<Customer> route = new ArrayList<Customer>();
			String [] xycTriple = s.split(",");
			for(int i=0;i<xycTriple.length;i+=3)
				route.add(new Customer(
						(int)Double.parseDouble(xycTriple[i]),
						(int)Double.parseDouble(xycTriple[i+1]),
						(int)Double.parseDouble(xycTriple[i+2])));
			solution.add(route);
		}
		br.close();
	}
	
	public void writeSVG(String probFilename,String solnFilename) throws Exception{
		String[] colors = "chocolate cornflowerblue crimson cyan darkblue darkcyan darkgoldenrod".split(" ");
		int colIndex = 0;
		String hdr = 
				"<?xml version='1.0'?>\n"+
				"<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' '../../svg11-flat.dtd'>\n"+
				"<svg width='8cm' height='8cm' viewBox='0 0 500 500' xmlns='http://www.w3.org/2000/svg' version='1.1'>\n";
		String ftr = "</svg>";
        StringBuffer psb = new StringBuffer();
        StringBuffer ssb = new StringBuffer();
        psb.append(hdr);
        ssb.append(hdr);
        for(List<Customer> route:this.solution){
        	ssb.append(String.format("<path d='M%s %s ",this.problem.depot.x,this.problem.depot.y));
        	for(Customer c:route)
        		ssb.append(String.format("L%s %s",c.x,c.y));
        	ssb.append(String.format("z' stroke='%s' fill='none' stroke-width='2'/>\n",
        			colors[colIndex++ % colors.length]));
        }
        for(Customer c:this.problem.customers){
        	String disk = String.format(
        			"<g transform='translate(%.0f,%.0f)'>"+
        	    	"<circle cx='0' cy='0' r='%d' fill='pink' stroke='black' stroke-width='1'/>" +
        	    	"<text text-anchor='middle' y='5'>%d</text>"+
        	    	"</g>\n", 
        			c.x,c.y,10,c.req);
        	psb.append(disk);
        	ssb.append(disk);
        }
        String disk = String.format("<g transform='translate(%.0f,%.0f)'>"+
    			"<circle cx='0' cy='0' r='%d' fill='pink' stroke='black' stroke-width='1'/>" +
    			"<text text-anchor='middle' y='5'>%s</text>"+
    			"</g>\n", this.problem.depot.x,this.problem.depot.y,20,"D");
    	psb.append(disk);
    	ssb.append(disk);
        psb.append(ftr);
        ssb.append(ftr);
        PrintStream ppw = new PrintStream(new FileOutputStream(probFilename));
        PrintStream spw = new PrintStream(new FileOutputStream(solnFilename));
        ppw.append(psb);
        spw.append(ssb);
    	ppw.close();
    	spw.close();
	}
	
	public void writeOut(String filename) throws Exception{
		PrintStream ps = new PrintStream(filename);
		for(List<Customer> route:this.solution){
			boolean firstOne = true;
			for(Customer c:route){
				if (!firstOne)
					ps.print(",");
				firstOne = false;
				ps.printf("%f,%f,%d",c.x,c.y,c.req);
			}
			ps.println();
		}
		ps.close();
	}
}
