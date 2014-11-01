import java.util.Comparator;


public class DistanceSort implements Comparator<Customer> {

	private Customer centre;
	
	public DistanceSort(Customer c){
		this.centre = c;
	}
	
	@Override
	public int compare(Customer c1, Customer c2) {
		if(this.centre.distance(c1) > this.centre.distance(c2)) return 1;
		else if(this.centre.distance(c1) < this.centre.distance(c2)) return -1;
		return 0;
	}
}
