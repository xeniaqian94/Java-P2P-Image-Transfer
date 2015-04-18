import java.io.Serializable;
import java.util.ArrayList;


public class PeerList implements Serializable{
	ArrayList<PeerInfo> peerList=new ArrayList<PeerInfo>();
	
	synchronized public void updatePeerList(PeerList newPeerList){
//		System.out.println("new # "+newPeerList.getSize()+" old # "+getSize());
		peerList=newPeerList.getPeerList();
//		printPeerList();
	}
	
	synchronized public ArrayList<PeerInfo> getPeerList(){
		return peerList;
		
	}
	synchronized public int getSize(){
		return peerList.size();
	}
	synchronized public void printPeerList(){
		System.out.println("There are "+peerList.size()+" elements in the peer list");
        for (PeerInfo p:peerList){
     	   p.print();
        }
	}
	
	synchronized public void addPeer(PeerInfo peer){
		peerList.add(peer);
		System.out.println("Add peer succeed. Current List:");
		printPeerList();
	}
	
	synchronized public String getiIP(int i){
		return peerList.get(i).getIP();
	}
	
	synchronized public int getiPort(int i){
		return peerList.get(i).getPort();
	}

}
