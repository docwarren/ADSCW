import java.util.Comparator;

public class AngleSort implements Comparator<Customer> {

	@Override
	public int compare(Customer a, Customer b) {
		if(a.angle > b.angle) return -1;
		else if(a.angle < b.angle) return 1;
		return 0;
	}
}