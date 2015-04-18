import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;


public class OldPeerGUIThread implements Runnable{
		private JFrame jf;
		private ImageCanvas jp;
		BufferedImage originImage=null;
		int[] pixels=new int[400*400];
		BufferedImage grid[][]=new BufferedImage[20][20];
        BufferedImage bufferedImage;
        
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
			Thread guiThread = new Thread(new clientGUIThread());
	        guiThread.start();

			try{
				Socket soc=new Socket("127.0.0.1",4343);
				ObjectOutputStream sOut=new ObjectOutputStream(soc.getOutputStream());
				ObjectInputStream sIn=new ObjectInputStream(soc.getInputStream());
				SubImage subImage;
				while (true){
					subImage=(SubImage)sIn.readObject();
					bufferedImage.setRGB(subImage.x*20, subImage.y*20, 20,20,subImage.pixels,0,20);
					jp.repaint();
				}
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

		public class clientGUIThread implements Runnable{
	        public void run() {
	    		jf=new JFrame();
	    		jp=new ImageCanvas();
	    		jf.getContentPane().add(BorderLayout.CENTER,jp);
	    		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    		jf.setSize(410,440);
	    		jf.setLocationRelativeTo(null);
	    		jf.setVisible(true);
			}
		}
		
		public class ImageCanvas extends Canvas{
			public void paint(Graphics g){
				g.drawImage(bufferedImage,5,5,this);
				setPixel();
			}
		}
		
		public void setPixel(){
			pixels=bufferedImage.getRGB(0,0,400,400,pixels,0,400);
		}

}

