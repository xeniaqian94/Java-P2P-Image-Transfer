import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class OldserverGUIThread implements Runnable{
	private JFrame jf;
	private JButton jb;
	private ImageCanvas jp;
	JFileChooser jfc;
	Image currentImage;
	BufferedImage originImage=null;
	BufferedImage bufferedImage=new BufferedImage(400,400,BufferedImage.TYPE_INT_RGB);
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
		for (int i=17;i<27;i++)System.out.print(pixels[i*30]);
		System.out.println();
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
		}
	}
	

}
