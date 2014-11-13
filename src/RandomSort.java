import java.util.Comparator;
import java.util.Random;


public class RandomSort implements Comparator<Customer> {

	@Override
	public int compare(Customer a, Customer b) {
		Random r = new Random();
		int x = r.nextInt(10);
		if(x < 5) return 1;
		else if(x >= 5) return -1;
		return 0;
	}

}
