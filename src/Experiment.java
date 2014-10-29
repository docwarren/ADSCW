import java.io.PrintStream;
import java.util.*;
public class Experiment {
	private static ArrayList<Long> timing;
	private String fileName = "~/Documents/AlgsCW/";
	HashMap<Integer, Double> times = new HashMap<Integer, Double>();
	
	public static void main(String[] args)throws Exception{
		String [] probs = {
				"rand00100",
				"rand00200",
				"rand00300",
				"rand00400",
//				"rand00500",
//				"rand00600"
				};
		
		// Times how long it takes to solve each problem.
		System.out.printf("File\tSolution size\tSolution cost\tTime taken\tAverage Time\n");
		
		for (String f:probs){
			long totTime = 0;
			timing = new ArrayList<Long>();
			VRProblem vrp = new VRProblem(f+"prob.csv");
			VRSolution vrs = new VRSolution(vrp);
			for(int i=0;i<1;i++){
				long start = System.nanoTime();
				// vrs.oneRoutePerCustomerSolution();
				vrs.clarkWright();
				long t = System.nanoTime()-start;
				timing.add(t);
				totTime += t/1000000;
			}
			vrs.writeOut(f+"MINE.csv");
			System.out.printf("%s , \t%d , \t%f , \t%d,\t%d \t%s\n",
					f,vrp.size(), vrs.solutionCost(),totTime, totTime / 50, timing);
		}
	}
}
