import java.io.Serializable;
import java.util.ArrayList;


public class ImageAndList implements Serializable{
	PeerList peerList;
	SubImage imageBlock;
	public ImageAndList(SubImage imageBlock,PeerList peerList){
		this.imageBlock=imageBlock;
		this.peerList=peerList;
	}
	
	synchronized public PeerList getPeerList(){
		return peerList;
		
	}
	synchronized public SubImage getImageBlock(){
		return imageBlock;
		
	}

}
