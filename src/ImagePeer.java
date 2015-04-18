import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class ImagePeer {
	BufferedImage bufferedImage=new BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
	private ImageCanvas jp=new ImageCanvas();
	int[] pixels=new int[400*400];
	int[] subPixel=new int[400];
	ArrayList<ObjectOutputStream> OutputStreams=new ArrayList<ObjectOutputStream>();
	
	PeerList peerList=new PeerList();
	int peer1Port=8001;
	int myPort;
	String myIP;
	String serverAddress;
	
	boolean imAdd=false;
	
	public static void main(String[] args){
		ImagePeer imagePeer=new ImagePeer();
		imagePeer.go();

	}
	
	synchronized public void setMyPort(int port){
		System.out.println("I'm Port "+port);
		myPort=port;
	}
	
	synchronized public void setMyIP(String IP){
		myIP=IP;
	}
	
	synchronized public int returnMyPort(){
		return myPort;
	}
	
	synchronized public String returnMyIP(){
		return myIP;
	}
	public void go(){
		
		//Later, pop up a window to ask for the Server
//		serverAddress="localhost";
		String serverAddress = JOptionPane.showInputDialog(null,
				  "Connect to server:",
				  "Input IP Address",
				  JOptionPane.QUESTION_MESSAGE);
		
		Thread guiThread = new Thread(new GUIThread());
        guiThread.start();
		
		Thread peerUploadServer=new Thread(new UploadServer());
		peerUploadServer.start();
		
		Thread connectToServer=new Thread(new ConnectToServer());
		connectToServer.start();
		
		
		
		Thread downloaderPeer1=new Thread(new Downloader(serverAddress,8001));
		downloaderPeer1.start();
		
		
	}
	
	
	public class ConnectToServer implements Runnable{  //弹出 连接的输入窗口？？？？
		
		synchronized public void run(){
			try {
		           Socket sock = new Socket(serverAddress, 8000);
		           ObjectOutputStream peerInfoOut=new ObjectOutputStream(sock.getOutputStream());
		           ObjectInputStream peerInfoIn=new ObjectInputStream(sock.getInputStream());

		           setMyIP(sock.getLocalAddress().getHostAddress());
		           peerInfoOut.writeObject(returnMyPort());
		           
		           peerInfoIn.close();
		           peerInfoOut.close();
		           
		           sock.close();
		        } catch(Exception ex) {
//		           ex.printStackTrace();
		        }
		}
	}
	
	public class UploadServer implements Runnable{
		
		int port;
		public void run(){
			port=8001;
			ServerSocket ss=null;
			while(ss==null&&port<65536){
				try{
					ss=new ServerSocket(port);
				}catch(IOException e) {
					port++;
				}
			}
			setMyPort(port);
			while(true) {
		        Socket peerInSocket;
				try {
					peerInSocket = ss.accept();
//					ObjectOutputStream peerOut=new ObjectOutputStream(peerInSocket.getOutputStream());
//					OutputStreams.add(peerOut);
					Thread t = new Thread(new Uploader(peerInSocket));
			        t.start();
				} catch (IOException e) {
//					e.printStackTrace();
				}
		     }
			
		}
		public int returnPort(){
			System.out.println("return port invoked"+port);
			return port;
		}
		
		public class Uploader implements Runnable{
			Socket peerInSocket;
			ObjectOutputStream peerOut;
			
			public Uploader(Socket peerInSocket) throws IOException{
				this.peerInSocket=peerInSocket;
				peerOut=new ObjectOutputStream(peerInSocket.getOutputStream());
				
			}
			public void run(){
				try{
					System.out.println("P2P Uploader set to send data to port:"+peerInSocket.getPort());

					int randi=new Random().nextInt(20);
					int randj=new Random().nextInt(20);
					while (true){
						Thread.sleep(350);
						peerOut.reset();
//						System.out.println("randi= "+randi+" randj= "+randj);
				        int[] subPixel=new int[400];
				        if (randi<19) randi++; 
				        else if (randj<19) {randi=0; randj++;}
				        else {randi=0;randj=0;}
					    subPixel=bufferedImage.getRGB(randi*20,randj*20,20,20,subPixel,0,20);
						peerOut.writeObject(new ImageAndList(new SubImage(randi,randj,subPixel),peerList));   //读和写并不流畅，如何连贯起来。不要一边readObject()时出现null Exception
				        peerOut.flush();
					}
				}catch (Exception ex){
//					ex.printStackTrace();
				}
				
			}
		}

	}
	
	public class Downloader implements Runnable{
		String serverIP;
		int serverPort;
		
		public Downloader(String serverIP,int serverPort){
			this.serverIP=serverIP;
			this.serverPort=serverPort;
		}
		public void run(){
			try {
		           Socket sock = new Socket(serverIP, serverPort);
//		           System.out.println("P2P Downloader set to download from "+sock.getLocalAddress()+": "+sock.getLocalPort()+" to "+serverIP+": "+serverPort);
		           System.out.println("P2P Downloader set to download from "+serverIP+": "+serverPort);
		           
		           ObjectInputStream peerInfoIn=new ObjectInputStream(sock.getInputStream());
		           ObjectOutputStream peerInfoOut=new ObjectOutputStream(sock.getOutputStream());
		           
		           Object thisObject;
		           
                   while (true){
	        		   ImageAndList newBlock=(ImageAndList)peerInfoIn.readObject();
//	        		   System.out.print("Block receieved! ");
	        		   PeerList newPeerList=newBlock.getPeerList();
	        		   if (newPeerList!=null&&peerList.getSize()<newPeerList.getSize())
	        				   addDownloader(newBlock.getPeerList());
	        		   SubImage subImage=newBlock.getImageBlock();
	        		   bufferedImage.setRGB(subImage.x*20, subImage.y*20, 20,20,subImage.pixels,0,20);
//	        		   System.out.println("Receieved! Gonna repaint!");
      				   jp.repaint();
//      				tellEveryone(subImage.x,subImage.y);
//      				   System.out.println("Repaint finished!");
        		   }
                   
		        } catch(Exception ex) {
//		        	System.out.println(ex.getMessage());
//		           ex.printStackTrace();
		        }
		}
	}
	synchronized public void addDownloader(PeerList newPeerList){
//		System.out.println("start a downloader for new peer in the list myIP "+myIP+" myUploadServerPort "+myPort);
		
		for(int i=peerList.getSize();i<newPeerList.getSize();i++){
			if (newPeerList.getiIP(i)!=myIP&&newPeerList.getiPort(i)!=myPort){
//				System.out.println("I'm "+myPort+" I'm starting Downloader "+newPeerList.getiIP(i)+" "+newPeerList.getiPort(i));
			    try{
					Thread newDownloader=new Thread(new Downloader(newPeerList.getiIP(i),newPeerList.getiPort(i)));
					newDownloader.start();
			    }catch (Exception ex){ }
			}
			
		}
		System.out.println("_________________________");
		System.out.println(myPort+"'s List updated!!!");
		peerList.updatePeerList(newPeerList);
		
	}
	
	public class GUIThread implements Runnable{
		private JFrame jf;
		BufferedImage originImage=null;
		BufferedImage grid[][]=new BufferedImage[20][20];
        
		public void run(){
			
			for (int i=0;i<20;i++)
				for(int j=0;j<20;j++)
					grid[i][j]=new BufferedImage(20,20,BufferedImage.TYPE_INT_RGB);
			try {
			    originImage = ImageIO.read(new File("Default.png"));
			} catch (IOException e) {
			}
			Graphics g=bufferedImage.getGraphics();
			g.drawImage(originImage, 0,0,400,400,null);
			g.dispose();
			
			jf=new JFrame();
    		jf.getContentPane().add(BorderLayout.CENTER,jp);
    		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		jf.setSize(410,440);
    		jf.setLocationRelativeTo(null);
    		jf.setVisible(true);
		}

		public void setPixel(){
			pixels=bufferedImage.getRGB(0,0,400,400,pixels,0,400);
		}

	}
	
	public class ImageCanvas extends Canvas{
		synchronized public void paint(Graphics g){
//			System.out.println("Within Repaint");
			g.drawImage(bufferedImage,5,5,this);
			setPixel();
//			System.out.println("After Repaint");
		}
	}
	
	public void setPixel(){
		pixels=bufferedImage.getRGB(0,0,400,400,pixels,0,400);
	}
	
	public void tellEveryone(int randi, int randj) {
	      Iterator it = OutputStreams.iterator();
	      System.out.println("OutputStream now has:"+OutputStreams.size()+" elements");
	      while(it.hasNext()) {
	        try {
	           ObjectOutputStream thisOut=(ObjectOutputStream)it.next();
	           thisOut.reset();
 
	           int[] subPixel=new int[400];
			   
			   System.out.println("TOLD ONCE "+randi+randj);
			   subPixel=bufferedImage.getRGB(randi*20,randj*20,20,20,subPixel,0,20);
			   thisOut.writeObject(new ImageAndList(new SubImage(randi,randj,subPixel),peerList));   //读和写并不流畅，如何连贯起来。不要一边readObject()时出现null Exception
	           thisOut.flush();
	         } catch(Exception ex) {
//	              ex.printStackTrace();
	         }
	      
	       } // end while
	      

	       
	   } // close tellEveryone


}
