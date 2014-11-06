
public class Range {
	private double min;
	private double max;
	private Customer depot;
	
	public Range(Customer depot, double min2, double max2){
		this.min = min2;
		this.max = max2;
		this.depot = depot;
	}
	
	public Boolean contains(Customer c){
		if(depot.distance(c) >= min && depot.distance(c) <= max) return true;
		return false;
	}
	
	public double getMin() {
		return this.min;
	}
	
	public void setMin(int min) {
		this.min = min;
	}
	
	public double getMax() {
		return this.max;
	}
	
	public void setMax(int max) {
		this.max = max;
	}
}
