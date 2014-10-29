import java.util.Comparator;
import java.util.TreeMap;


public class savingSort implements Comparator<String> {
	private TreeMap<String, Double> map;
	
	public savingSort(TreeMap<String, Double> map){
		this.map = map;
	}
	
	@Override
	public int compare(String a, String b) {
		// TODO Auto-generated method stub
		if(map.get(a) > map.get(b)) return -1;
		else if(map.get(b) > map.get(a)) return 1;
		else return 0;
	}
	
}
