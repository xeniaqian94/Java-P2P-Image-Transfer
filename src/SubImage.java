

import java.io.Serializable;

public class SubImage implements Serializable{
	int x;
	int y;
	int[] pixels;

	public SubImage(int x, int y, int[] pixels){
		this.x=x;
		this.y=y;
		this.pixels=pixels;
	}
}
