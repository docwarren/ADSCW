import java.util.*;
import java.io.*;

public class VRSolution {
	public VRProblem problem;
	public List<Route> solution;
	public TreeMap<Double, List<Route>> savings = new TreeMap<Double, List<Route>>();
	
	//Students should implement another solution
	public void clarkWright() throws Exception{
		oneRoutePerCustomerSolution();
		
		findAllPairs();
		
		while(savings.size() > 0){
			Double s = savings.lastKey();
			Route a = savings.get(s).get(0);
			Route b = savings.get(s).get(1);
			if(verifyJoin(a, b)){
				a.addRoute(b);
				solution.remove(b);
			}
			savings.remove(s);
			findAllPairs();						//  O(n^2/2)
		}
	}
	
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

	public double calculatePairSaving(Route a, Route b){
		Customer cus1 = a.getEnd();
		Customer cus2 = b.getStart();
		double bridge = cus1.distance(cus2);	// O(1)
		double sav1 = cus1.distance(this.problem.depot);	// O(1)
		double sav2 = cus2.distance(this.problem.depot);	// O(1)
		return sav1 + sav2 - bridge;	// O(1)
	}

 	public void findAllPairs(){
 		savings.clear();
 		for(int j = 0; j < this.solution.size(); j++){
 			for( int i = j + 1; i < this.solution.size(); i++ ){
 				Route a = this.solution.get(j);
 				Route b = this.solution.get(i);
 				calculateSavings(a, b);
 			}
 		}
	}
 	
 	public void calculateSavings(Route a, Route b){
		double sav = calculatePairSaving(a, b);
		double sav2 = calculatePairSaving(b, a);
		ArrayList<Route> pair = new ArrayList<Route>();
		if(sav2 > sav) {
			pair.add(b);
			pair.add(a);
			if(sav2 > 0 && verifyJoin(b, a)) savings.put( sav2, pair );
		}
		else {
			pair.add(a);
			pair.add(b);
			if(sav > 0 && verifyJoin(a, b)) savings.put( sav, pair );
		}
 	}

 	//=================================================================================================================
	public VRSolution(VRProblem problem){
		this.problem = problem;
	}

	//The dumb solver adds one route per customer
	public void oneRoutePerCustomerSolution(){
		this.solution = new ArrayList<Route>();
		for(Customer c:problem.customers){
			Route route = new Route(c, problem.depot);
			this.solution.add(route);
		}
	}
 	
 	//Calculate the total journey
	public double solutionCost(){
		double cost = 0;
		for(Route route:solution){
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
