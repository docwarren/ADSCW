import java.util.ArrayList;
import java.util.Comparator;

public class RangeSort implements Comparator<Customer> {
	
	private ArrayList<Range> ranges = new ArrayList<Range>();
	
	public RangeSort(ArrayList<Range> ranges){
		this.ranges = ranges;
	}
	
	@Override
	public int compare(Customer a, Customer b) {
		int aIndex = 0;
		int bIndex = 0;
		for(int x = 0; x < ranges.size(); x++){
			if(ranges.get(x).contains(a)) aIndex = x;
			if(ranges.get(x).contains(b)) bIndex = x;
		}
		if(aIndex > bIndex) return 1;
		else if(bIndex > aIndex) return -1;
		else if(a.angle > b.angle) return -1;
		else if(b.angle > a.angle) return 1;
		return 0;
	}

	public ArrayList<Range> getRanges() {
		return this.ranges;
	}

	public void setRanges(ArrayList<Range> ranges) {
		this.ranges = ranges;
	}
	
	
}