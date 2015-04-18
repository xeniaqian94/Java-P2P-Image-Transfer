import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class ImageServer {
	
	BufferedImage bufferedImage=new BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
	PeerList peerList=new PeerList();
	ArrayList<ObjectOutputStream> OutputStreams=new ArrayList<ObjectOutputStream>();
	ArrayList<PrintWriter> PrintWriters=new ArrayList<PrintWriter>();
	int[] subPixel=new int[400];
	 
	public static void main(String[] args) {
	       ImageServer imageServer = new ImageServer();
	       imageServer.go();
	       
	    }

	public void go(){

		Thread NewPeerHandler = new Thread(new NewPeerHandler());
        NewPeerHandler.start();
        
        Thread guiThread = new Thread(new serverGUIThread());
        guiThread.start();
        
        Thread serverUploadServer=new Thread(new UploadServer());
        serverUploadServer.start();
        
//        while (true){tellEveryone();}
        
	}

	public class NewPeerHandler implements Runnable{
		public void run() {
			
		       try {
		       ServerSocket serverSocket = new ServerSocket(8000);
		
		       while(true) {
		          Socket socketFromNewPeer = serverSocket.accept(); //accept connection
		                   //send port information
		                   //add new peer into the peer list
		          ObjectInputStream peerInfoIn=new ObjectInputStream(socketFromNewPeer.getInputStream());
		          ObjectOutputStream peerInfoOut=new ObjectOutputStream(socketFromNewPeer.getOutputStream());
		          
		          
		          int thisPort=(int)peerInfoIn.readObject();
				  PeerInfo thisPeer=new PeerInfo(socketFromNewPeer.getInetAddress().getHostAddress().toString(),thisPort);
				  thisPeer.print();
				  peerList.addPeer(thisPeer);
				  
//				  tellEveryone();
		          
		          peerInfoOut.flush();
		          peerInfoOut.close();
		          peerInfoIn.close();
		          
		          socketFromNewPeer.close();
		          
		          
		        }
		      }catch(Exception ex) {
//		         ex.printStackTrace();
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
			while(true) {
				try {
					Socket peerInSocket = ss.accept();
					
					Thread t = new Thread(new Uploader(peerInSocket));
			        t.start();
				} catch (IOException e) {
//					e.printStackTrace();
				}
		     }
			
		}
		
		public class Uploader implements Runnable{   
			Socket peerInSocket;
			ObjectOutputStream peerOut;
			public Uploader(Socket peerInSocket) throws IOException{
				this.peerInSocket=peerInSocket;
				peerOut=new ObjectOutputStream(peerInSocket.getOutputStream());
				OutputStreams.add(peerOut);
				
			}
			public void run(){
				try{
					int randi=new Random().nextInt(20);
					int randj=new Random().nextInt(20);
					while (true){
						Thread.sleep(150);
						peerOut.reset();
				        int[] subPixel=new int[400];
				        if (randi<19) randi++; 
				        else if (randj<19) {randi=0; randj++;}
				        else {randi=0;randj=0;}
				        
//						System.out.println("TOLD ONCE "+randi+randj);
					    subPixel=bufferedImage.getRGB(randi*20,randj*20,20,20,subPixel,0,20);
						peerOut.writeObject(new ImageAndList(new SubImage(randi,randj,subPixel),peerList));   //读和写并不流畅，如何连贯起来。不要一边readObject()时出现null Exception
				        peerOut.flush();
					}

				}	
				catch (Exception ex){
//					ex.printStackTrace();
				}
		
			}
		}

	}
	
	public void tellEveryone() {
	      Iterator it = OutputStreams.iterator();
	      
	      while(it.hasNext()) {
	        try {
	        	
	           ObjectOutputStream thisOut=(ObjectOutputStream)it.next();
               thisOut.reset();
	           int[] subPixel=new int[400];
			   int randi=new Random().nextInt(20);
			   int randj=new Random().nextInt(20);
//			   System.out.println("TOLD ONCE "+randi+randj);
			   subPixel=bufferedImage.getRGB(randi*20,randj*20,20,20,subPixel,0,20);
			   thisOut.writeObject(new ImageAndList(new SubImage(randi,randj,subPixel),peerList));   //读和写并不流畅，如何连贯起来。不要一边readObject()时出现null Exception
	           thisOut.flush();
//	           thisOut.close();
	         } catch(Exception ex) {
//	              ex.printStackTrace();
	         }
	      
	       }// end while
	      
	       
	   } // close tellEveryone
	
	public class serverGUIThread implements Runnable{
		private JFrame jf;
		private JButton jb;
		private ImageCanvas jp;
		JFileChooser jfc;
		Image currentImage;
		BufferedImage originImage=null;
		int[] pixels=new int[400*400];
		
		public void run() {
			
			try {
			    originImage = ImageIO.read(new File("Flower.png"));
			} catch (IOException e) {
			}
			Graphics g=bufferedImage.getGraphics();
			g.drawImage(originImage, 0,0,400,400,null);
			g.dispose();
			setPixel();
			
	    	jfc=new JFileChooser();
			try{
				if (jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					try {
					    originImage = ImageIO.read(new File(jfc.getSelectedFile().getPath()));
					} catch (IOException ex) {
					}
					g=bufferedImage.getGraphics();
					g.drawImage(originImage, 0,0,400,400,null);
					g.dispose();
				}
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null, "", "Picture Load failed", JOptionPane.ERROR_MESSAGE);
			}
			setPixel();
			
			jf=new JFrame();
			jb=new JButton("Load another image");
			jb.addActionListener(new fileChooserListener());
			jp=new ImageCanvas();
			jf.getContentPane().add(BorderLayout.CENTER,jp);
			jf.getContentPane().add(BorderLayout.SOUTH,jb);
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.setSize(410,450);
			jf.setLocationRelativeTo(null);
			jf.setVisible(true);
		}
		
		public void setPixel(){
			pixels=bufferedImage.getRGB(0,0,400,400,pixels,0,400);
		}
		
		public class ImageCanvas extends Canvas{
			public void paint(Graphics g){
				g.drawImage(bufferedImage,5,5,this);

			}
		}
		
		public class fileChooserListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				jfc=new JFileChooser();
				try{
					if (jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
						try {
						    originImage = ImageIO.read(new File(jfc.getSelectedFile().getPath()));
						} catch (IOException ex) {
						}
						Graphics g=bufferedImage.getGraphics();
						g.drawImage(originImage, 0,0,400,400,null);
						g.dispose();
					}
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "", "Picture Load failed", JOptionPane.ERROR_MESSAGE);
				}
		        setPixel();
		        jp.repaint();
		        System.out.println("Image pixel changed! Repaint once!");
//		        tellEveryone();
			}
		}
		

	}


}
