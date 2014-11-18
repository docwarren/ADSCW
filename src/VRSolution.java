import java.util.*;
import java.io.*;

public class VRSolution {
	public VRProblem problem;
	public List<Route> solution;
	public ArrayList<Saving> savings = new ArrayList<Saving>();
	public ArrayList<Route> joined = new ArrayList<Route>();
	
	public VRSolution(VRProblem problem){
		this.problem = problem;
	}
	
	//Students should implement another solution
	public void clarkWright() throws Exception{
		// Assign each customer to a route
		oneRoutePerCustomerSolution();
		// Find all the possible route pairs and calculate savings
		findAllPairs();
		
		// While there are savings to be made
		while(savings.size() > 0){
			// Loop through all savings and join routes where appropriate
			for(int i = 0; i < savings.size();i++){
				// get the 2 routes associated with the saving
				Saving saving = savings.get(i);
				Route a = saving.getR1();
				Route b = saving.getR2();
				// If the routes have not been joined anywhere before, join them
				if(!joined.contains(a) && !joined.contains(b)){
					join(a, b);
					break;
				}
				// Otherwise If the list of joined routes does not contain the left route
				// then the right route must have been joined before
				else if(!joined.contains(a)){
					// Find a route that starts with the same customer as the right route
					for(Route r: solution){
						if(r.getStart() == b.getStart()) join(a, r);		// join it to the left route
						break;
					}
				}
				// Otherwise if the list of joined routes does not contains the right route
				// Then it must contain the left one
				else if(!joined.contains(b)){
					// Find a route that ends with the same route as the left route
					for(Route r: solution){
						if(r.getEnd() == a.getEnd()) join(r, b);	// join it to the right route
						break;
					}
				}
			}
			joined.clear();		// Clear the joined list
			savings.clear();	// Clear the savings list
			findAllPairs();		// Recheck for savings
		}
		randomise();		// Perform a simple search on each route
	}
	
	// Simple search algorithm to try to find the best route
	public void randomise(){
		for(Route r: this.solution){
			List<Customer> best = r.getRoute();
			// Create a temporary route with the same customers in it ( not a pointer to the original )
			List<Customer> mix = new ArrayList<Customer>();
			mix.addAll(best);
			Route ok = new Route(mix.get(0), this.problem.depot);
			// Mix up the route 10000 times and compare each solution to the best current solution
			for(int i = 0; i < 10000; i++){
				mix.sort(new RandomSort());		// randomly mix up the route
				ok.setRoute(mix);
				// if the jumbled route is better than our current route, use it instead
				if(ok.getCost() < r.getCost()){		
					List<Customer> t = new ArrayList<Customer>();
					for(Customer c: mix) t.add(c);	// Avoid pointing to mix ( which will be randomised in future )
					r.setRoute(t);
				}
			}
		}
	}

	// Join 2 routes in the solution, checking for exceeding capacity first
	private void join(Route a, Route b){
		if(verifyJoin(a, b)){		// check that a and b joining will not exceed capacity
			a.addRoute(b);
			joined.add(a);	// add them to the joined list
			joined.add(b);
			solution.remove(b);
		}
	}
	
	// Returns true if the capacity of the van will not be exceeded when the routes are joined
	private boolean verifyJoin(Route r1, Route r2) {
		Boolean result = true;
		int total = 0;
		for(Customer c: r1.getRoute()) total += c.req;
		for(Customer c: r2.getRoute()) total += c.req;
		if (total > problem.depot.req) {
			result = false;
		}
		return result;
	}
	
	// Calculate the savings that would occur if two routes were joined
	public double calculatePairSaving(Route a, Route b){
		Customer cus1 = a.getEnd();
		Customer cus2 = b.getStart();
		double bridge = cus1.distance(cus2);
		double sav1 = cus1.distance(this.problem.depot);
		double sav2 = cus2.distance(this.problem.depot);
		return sav1 + sav2 - bridge;
	}

	// Find all of the pairs of routes, calculate the savings, and add the savings to the savings list
 	public void findAllPairs(){
 		for(int j = 0; j < this.solution.size(); j++){
 			for( int i = j + 1; i < this.solution.size(); i++ ){
 				Route a = this.solution.get(j);
 				Route b = this.solution.get(i);
 				double sav = calculatePairSaving(a, b);
 				double sav2 = calculatePairSaving(b, a);
 				if(sav > sav2){
 					if(sav > 1 && verifyJoin(a, b)) savings.add( new Saving(sav, a, b) );
 				}
 				else if(sav2 > 1 && verifyJoin(b, a)) savings.add( new Saving(sav, b, a) );
 			}
 		}
 		savings.sort(new SavingSort());			// Order them from most savings to least
	}

	//The simple solver adds one route per customer
	public void oneRoutePerCustomerSolution(){
		this.solution = new ArrayList<Route>();
		this.solution = new ArrayList<Route>();
		for(Customer c:problem.customers){
			Route route = new Route(c, problem.depot);
			this.solution.add(route);
		}
	}
 	
 	//Calculate the total journey
	public double solutionCost(){
		double cost = 0;
		for(Route route: this.solution){
			cost += route.getCost();
		}
		return cost;
	}
		
	// Check that no routes exceed capacity
	// and that all customers are visited
	public Boolean verifySolution(){
		//Check that no route exceeds capacity
		Boolean okSoFar = true;
		for(Route route : solution){
			okSoFar = route.verify();
		}
		//Check that we keep the customer satisfied
		//Check that every customer is visited and the correct amount is picked up
		Map<String,Integer> reqd = new HashMap<String,Integer>();
		for(Customer c:this.problem.customers){
			String address = String.format("%fx%f", c.x,c.y);
			reqd.put(address, c.req);
		}
		for(Route route:this.solution){
			for(Customer c:route.getRoute()){
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
		this.solution = new ArrayList<Route>();
		Route route = null;
		while((s=br.readLine())!=null){
			String [] xycTriple = s.split(",");
			for(int i=0;i<xycTriple.length;i+=3){
				Customer c = new Customer((int)Double.parseDouble(xycTriple[i]),(int)Double.parseDouble(xycTriple[i+1]),(int)Double.parseDouble(xycTriple[i+2]));
				if(route == null || i == 0 ) route = new Route(c, this.problem.depot);
				else route.addCustomer(c);
			}
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
        for(Route route:this.solution){
        	ssb.append(String.format("<path d='M%s %s ",this.problem.depot.x,this.problem.depot.y));
        	for(Customer c:route.getRoute())
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
		for(Route route:this.solution){
			boolean firstOne = true;
			for(Customer c:route.getRoute()){
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
