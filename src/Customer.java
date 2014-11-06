import java.awt.geom.Point2D;


public class Customer extends Point2D.Double{
	public double angle;
	
	private static final long serialVersionUID = 1L;
	
	// Requirements of the customer (number to be delivered)
	public int req;
	public Customer(int x, int y, int requirement){
		this.x = x;
		this.y = y;
		this.req = requirement;
	}
	
	public void makeAngle(Customer c){
		this.angle = Math.toDegrees(Math.atan2(c.x - this.x, c.y - this.y));
		if(this.angle < 0) this.angle += 360;
	}
}
