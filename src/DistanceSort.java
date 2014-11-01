import java.util.Comparator;


public class DistanceSort implements Comparator<Customer> {
	private Customer c;
	
	public DistanceSort(Customer c){
		this.c = c;
	}
	
	@Override
	public int compare(Customer a, Customer b) {
		if(c.distance(a) > c.distance(b)) return 1;
		if(c.distance(a) < c.distance(b)) return -1;
		return 0;
	}

}
