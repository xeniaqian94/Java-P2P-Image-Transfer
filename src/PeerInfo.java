import java.io.Serializable;


public class PeerInfo implements Serializable{
	private String IP;
	private int port;
	
	public PeerInfo(String IP,int port){
		this.IP=IP;
		this.port=port;
	}
	
	public void print(){
		System.out.println("This peer's Info: "+IP+": "+port);
	}
	
	public String getIP(){
		return IP;
	}
	public int getPort(){
		return port;
	}

}
