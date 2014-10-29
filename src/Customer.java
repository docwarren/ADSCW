import java.awt.geom.Point2D;


public class Customer extends Point2D.Double{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Requirements of the customer (number to be delivered)
	public int req;
	public Customer(int x, int y, int requirement){
		this.x = x;
		this.y = y;
		this.req = requirement;
	}
}
