import java.util.*;
public class Experiment {
	private static ArrayList<Long> timing;
	HashMap<Integer, Double> times = new HashMap<Integer, Double>();
	
	public static void main(String[] args)throws Exception{
		String [] probs = {
				"rand00010",
				"rand00020",
				"rand00030",
				"rand00040",
				"rand00050",
				"rand00060"
				};
		
		// Times how long it takes to solve each problem.
		System.out.printf("File\tSolution size\tSolution cost\tTime taken\tAverage Time\n");
		
		for (String f:probs){
			long totTime = 0;
			int iters = 20;
			timing = new ArrayList<Long>();
			VRProblem vrp = new VRProblem(f+"prob.csv");
			VRSolution vrs = new VRSolution(vrp);
			
			for(int i = 0; i < iters; i++){
				long start = System.nanoTime();
				// vrs.oneRoutePerCustomerSolution();
				vrs.nearestPoint();
				long t = System.nanoTime()-start;
				timing.add(t);
				totTime += t/1000;
			}
			vrs.writeOut(f+"NP2.csv");
			System.out.printf("%s , \t%d , \t%f , \t%d,\t%d \t%s\n",
					f,vrp.size(), vrs.solutionCost(),totTime, totTime / iters, timing);
		}
	}
}
