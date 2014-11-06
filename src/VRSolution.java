import java.util.*;
import java.io.*;

public class VRSolution {
	
	public VRProblem problem;
	public ArrayList<Range> circles;
	
	public List<List<Customer>> solution;
	public List<Customer> customers;
	
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
	
	// Doing it with angles
	public void radianAlg(){
		this.solution = new ArrayList<List<Customer>>();
		this.circles = new ArrayList<Range>();
		this.customers = new ArrayList<Customer>();
		
		// Get the angles of all of the points
		for(Customer c: this.problem.customers){
			c.makeAngle(this.problem.depot);
			customers.add(c);
		}		
		
		// Get the ranges of the circles you want
		getRanges();
		
		// Sort by range and Angle
		this.customers.sort(new RangeSort(this.circles));
		int count = 0;
		int load = 0;
		
		solution.add(new ArrayList<Customer>());
		
		while(customers.size() > 0){
			// Make a new route
			ArrayList<Customer> route = (ArrayList<Customer>) solution.get(count);
			
			// Add customers to it
			if(load + customers.get(0).req < this.problem.depot.req){
				route.add(customers.get(0));
				load += customers.get(0).req;
				customers.remove(0);
			}
			// until capacity is reached, then add it to the solution and make a new route
			else{
				count ++;
				load = 0;
				solution.add(new ArrayList<Customer>());
			}
		}
		
	}
	
	private void getRanges() {
		double max = this.customers.get(0).distance(this.problem.depot);
		double min  = this.customers.get(0).distance(this.problem.depot);;
		int size = this.customers.size();
		for(Customer c: this.customers){
			if(this.problem.depot.distance(c) > max) max = this.problem.depot.distance(c);
			if(this.problem.depot.distance(c) < min) min = this.problem.depot.distance(c);
		}
		double range = (max - min) / (size / 60);
		if(range == 0.0 || range > max) this.circles.add(new Range(this.problem.depot, min, max));
		else{
			for(double i = min; i < max; i += range){
				this.circles.add(new Range(this.problem.depot, i, i+range));
			}
		}
	}

	// New nearest point algorithm
	public void nearestPoint(){
		this.solution = new ArrayList<List<Customer>>();
		this.customers = new ArrayList<Customer>();
		
		for(Customer c: this.problem.customers){
			customers.add(c);
		}
		//customers.remove(0);
		int count = 0;
		int load = 0;
		solution.add(new ArrayList<Customer>());
		Customer start = customers.get(0);
		
		while(customers.size() > 0){
			ArrayList<Customer> route = (ArrayList<Customer>) solution.get(count);
			customers.sort(new DistanceSort(start));
			
			if(load + customers.get(0).req < this.problem.depot.req){
				route.add(customers.get(0));
				load += customers.get(0).req;
				start = customers.get(0);
				customers.remove(0);
			}
			else{
				count ++;
				load = 0;
				start = customers.get(0);
				solution.add(new ArrayList<Customer>());
			}
		}
	}
	
// Don't touch what is below this line=============================================================================================================
		
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
