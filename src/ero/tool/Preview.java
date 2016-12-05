package src.ero.tool;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.atomic.AtomicIntegerArray;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Preview extends Canvas {

	/**
	 * Launch the application.
	 */
	private int[] pixelz,bg;

	public int x,y;
	private JFrame frame;
	private BufferedImage image,tib;
	private boolean isRunning = true;
	private Application app;
	private int ticks;
	private int frames;

	/**
	 * Create the frame.
	 */
	public Preview(int x, int y, Application app) {
		this.app = app;
		 this.x = x;
		 this.y = y;
			System.out.println("Preview thread started with X"+x+ " Y"+y);

			setBounds(100, 100, 450, 300);
			
			setMinimumSize(new Dimension(x,y));
			setMaximumSize(new Dimension(x,y));
			setPreferredSize(new Dimension(x,y));	
			frame = new JFrame("ED Preview");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			frame.add(this, BorderLayout.CENTER);
			frame.pack();
			
			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			frame.setVisible(false);			
			image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
			pixelz = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			bg = new int[pixelz.length];
			
			

		}					
		
		public void renderr() {			
			for (int i=0;i<pixelz.length;i++) {
				pixelz[i]=bg[i];
			}
			bg = app.picture;
			BufferStrategy bs = getBufferStrategy();
			if (bs == null) {
				createBufferStrategy(3);
				return;
			}
			
			Graphics g = bs.getDrawGraphics();
			g.fillRect(0, 0, x,y);
			


			g.drawImage(getScaled(image,x,y), 0, 0, x, y, null);
			g.dispose();
			bs.show();
			return;
		}
		public void newImg(int[] i) {
			pixelz = i;
		}
		public void setVis(boolean b) {
			this.setVisible(b);
			frame.setVisible(b);
		}
		
		public void setWinSize(int x, int y) {
			this.x = x;
			this.y = y;
			this.setSize(x, y);
			frame.setSize(x+3, y+25);
		}
		/**
		* Resizes an image using a Graphics2D object backed by a BufferedImage.
		* @param srcImg - source image to scale
		* @param w - desired width
		* @param h - desired height
		* @return - the new resized image
		*/
		public static BufferedImage getScaled(final BufferedImage img, final int w, final int h) {
		    final java.awt.Image image = img.getScaledInstance(w, h, java.awt.Image.SCALE_FAST);
		    final BufferedImage bufferedImage = new BufferedImage(w, h, img.getType());
		    bufferedImage.getGraphics().drawImage(image, 0, 0, null);
		    return bufferedImage;
		}
}
