package src.ero.tool;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;



public class Application extends Canvas implements Runnable {
	public ImageHandler iHan;
	
	public int selX,selY;
	public int r,g,b,sr,sg,sb;	
	public boolean capCol = false;
	
	public int winX=200,winY=250,scale=2;
	public int vWinX=200,vWinY=200,vWinScale=2;	
	
	public boolean isRunning = true;
	public Input input;
	
	public BufferedImage image;
	private int noiseRange = 1;
	private int staticStage=0;
	int[] pixels,layer2,picture,helpscr;
	public boolean numberLock = false;
	public int pixelSize=1;
	public Preview prev;
	private JFrame frame;
	private int frames=0;
	private int ticks=0;
	private int KeyCooldown=2;
	private boolean capLoc;
	public ArrayList<UndoObj> undoList = new ArrayList<>(1);

	private boolean addUndo = false;

	private boolean helpOpen;

	private boolean previewOn;
	
	
	public void Init() throws IOException {
		iHan = new ImageHandler();
		System.out.println("Main thread started");
		prev = new Preview(vWinX,vWinY,this);
		prev.setVis(previewOn);
		// Init all images
		for (int i=0;i<ImageDB.imagePath.length;i++) { 	
			BufferedImage tBI = ImageIO.read((Meta.class.getResourceAsStream("/res/" + ImageDB.imagePath[i])));
			Image ni = new Image();
			ni.imageID = i;
			ni.name = ImageDB.imagePath[i].substring(0, ImageDB.imagePath[i].length()-4);			
			ni.pixels = tBI.getRGB(0, 0, tBI.getWidth(), tBI.getHeight(), null,0,tBI.getWidth());
			ni.imgHeight = tBI.getHeight();
			ni.imgWidth = tBI.getWidth();
			Random random = new Random();			
			ni.imageUID = random.nextLong();			
			iHan.newImage(ni);	
			
		}
					
		
				
		image = new BufferedImage(winX, winY, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();		
		layer2 = new int[pixels.length];
		picture = new int[pixels.length];
		helpscr = new int[pixels.length];
		setMinimumSize(new Dimension((int)(winX * scale), (int)(winY * scale)));
		setMaximumSize(new Dimension((int)(winX * scale), (int)(winY * scale)));
		setPreferredSize(new Dimension((int)(winX * scale), (int)(winY * scale)));

		frame = new JFrame("EroDraw");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		input = new Input();
		this.addKeyListener(input);
		this.addMouseListener(input);
		int li=5;
		for (int i=0;i<helpscr.length;i++) {
			helpscr[i] = 0x222222;
		}
		draw("H - Open/Close Help",5,li+=10,0x888888,helpscr);
		draw("Numpad 789 - RGB+",5,li+=10,0x888888,helpscr);
		draw("Numpad 456 - RGB-",5,li+=10,0x888888,helpscr);
		draw("Numpad /+Star - Draw Main/Alt",5,li+=10,0x888888,helpscr);
		draw("E - Pick Color",5,li+=10,0x888888,helpscr);
		draw("S - Flip Main/Alt",5,li+=10,0x888888,helpscr);
		draw("N - Lock RGB changes",5,li+=10,0x888888,helpscr);
		draw("F - Replace same with Main",5,li+=10,0x888888,helpscr);
		draw("R - Create randoms",5,li+=10,0x888888,helpscr);
		draw("L - Locl RGB Limit",5,li+=10,0x888888,helpscr);
		draw("M - Normalize Main/Alt",5,li+=10,0x888888,helpscr);
		draw("P - Preview real size",5,li+=10,0x888888,helpscr);
		draw("Caps Loc - RGB Change + 10",5,li+=10,0x888888,helpscr);
		draw("Ctrl - Change RGB by 5",5,li+=10,0x888888,helpscr);
		draw("Ctrl S/L - Save/Load Image",5,li+=10,0x888888,helpscr);
		draw("Ctrl Z - Undo",5,li+=10,0x888888,helpscr);
		
	}
	
	public void logic() throws Exception {
		capLoc = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		if (KeyCooldown <=0) {
			if (input.keyPressed[KeyEvent.VK_H]) {
				System.out.println("Help: "+helpOpen);
				helpOpen=!helpOpen;
				
				KeyCooldown=120;				
			}
			if (helpOpen) return;
			
			if (input.keyPressed[KeyEvent.VK_P]) {
				previewOn =! previewOn;
				System.out.println("Preview Window: "+previewOn);
				prev.setVis(previewOn);
				KeyCooldown=120;
				input.keyPressed[KeyEvent.VK_P] = false;
			}
			int pMo=0;
			if (input.keyPressed[111]) {
				addUndo = true;
				paintPixel(picture,selX, selY, rgb2col(r,g,b));// -		}
				addUndo = false;
				KeyCooldown=20;
			}
			if (input.keyPressed[106]) {
				addUndo = true;
				paintPixel(picture,selX,selY,rgb2col(sr,sg,sb));
				addUndo = false;
				KeyCooldown=20;
			}
			/*
			 * 		if (staticStage==1) RandString = "Static/Noise?";
		if (staticStage==2) RandString = "Static";
		if (staticStage==3) RandString = "Noise - #?";
		if (staticStage==4) RandString = "Noise - " + noiseRange;
			 */
			if (staticStage==0 && input.keyPressed[KeyEvent.VK_R]) {
				staticStage=1;
				KeyCooldown=10;
			}
			else if (staticStage==1) {
				if (input.keyPressed[KeyEvent.VK_S]) {
					staticStage=2;
					KeyCooldown=10;
					return;
				}
				else if (input.keyPressed[KeyEvent.VK_N]) {
					staticStage=3;
					KeyCooldown=10;
					return;
				}
				
			}
			else if (staticStage==2 && input.keyPressed[KeyEvent.VK_R]) {
				staticStage=0;
				KeyCooldown=10;
			}
			else if (staticStage==3) {
				System.out.println("A:"+input.lastCharPress+":");
				if (input.isNumeric(input.lastCharPress)) {
					System.out.println("?");
					noiseRange = Integer.parseInt(input.lastCharPress);				
					staticStage=4;
					KeyCooldown=10;					
				}					
			}
			else if (staticStage==4 && input.keyPressed[KeyEvent.VK_R]) {
				staticStage=0;
				KeyCooldown=10;
			}
			
			if (input.keyPressed[KeyEvent.VK_UP]) {
				pMo = selY-pixelSize;
				if (pMo > 0) selY=pMo;
				else selY=0;
			}
			if (input.keyPressed[KeyEvent.VK_DOWN]) {
				pMo = selY+pixelSize;
				if (pMo < vWinY) selY = pMo;
				else selY = vWinY-pixelSize; //TODO
			}
			if (input.keyPressed[KeyEvent.VK_LEFT]) {
				pMo = selX-=pixelSize;
				if (pMo > 0) selX=pMo;
				else selX=0;
			}
			if (input.keyPressed[KeyEvent.VK_RIGHT]) {
				pMo = selX + pixelSize;
				//System.out.println("A:"+ pMo + " B:" + vWinX);
				if (pMo<vWinX) selX = pMo;
				else selX=vWinX - pixelSize; //TODO 		
			}

			if (input.keyPressed[KeyEvent.VK_S] && !Input.keyPressed[KeyEvent.VK_CONTROL]) {
				System.out.println("Flipped Primary and Secondary colors: " + r + "-" + g + "-" + b + "  -  " + sr + "-" + sg + "-" + sb); 
				flipSelectedCols();
				KeyCooldown=10;
				return;
			}
			if (input.keyPressed[KeyEvent.VK_I]) System.out.println("X:" + selX/pixelSize + " Y:" + selY/pixelSize + " C:" + Integer.toHexString(new Color(r,g,b).getRGB()) + " Cap:" + capLoc);			
			if (input.keyPressed[KeyEvent.VK_L] && !Input.keyPressed[KeyEvent.VK_CONTROL]) {
				capCol = !capCol;
				System.out.println("Color cap: " + capCol);
			}
			if (Input.keyPressed[KeyEvent.VK_N]) {
				numberLock = !numberLock;
				System.out.println("Color Lock: " + numberLock);
			}
			if (Input.keyPressed[KeyEvent.VK_M]) {				
				System.out.println("Finding medium color between primary (#"+Integer.toHexString(new Color(r,g,b).getRGB()) + ""
						+ "), and secondary (#"+Integer.toHexString(new Color(sr,sg,sb).getRGB())+")...");
				findMedium();		
				System.out.println("..Which is (#"+Integer.toHexString(new Color(r,g,b).getRGB())+")");
				
			}
			
			
			if (input.keyPressed[KeyEvent.VK_CONTROL]) {
				if (input.keyPressed[KeyEvent.VK_S]) {
					saveImage();
					System.out.println("Saved image");
					input.keyPressed[KeyEvent.VK_S] = false;
					KeyCooldown=60;
				}				
				if (input.keyPressed[KeyEvent.VK_L]) {
					loadImage();
					System.out.println("Loaded image");
					input.keyPressed[KeyEvent.VK_L] = false;
					KeyCooldown=60;
				}
				if (input.keyPressed[KeyEvent.VK_Z]) {
					Undo();
					System.out.println("Undone");
					KeyCooldown=30;
				}
				
				if (input.keyPressed[KeyEvent.VK_NUMPAD7]) {			
					if (r+5<255) r+=5;
					else if (!capCol) r=0;	
					else r=255;										
				}
				else if (input.keyPressed[KeyEvent.VK_NUMPAD4]) {
					if (r-5>0) r-=5;
					else if (!capCol) r=255;
					else r=0;
				}
				
				if (input.keyPressed[KeyEvent.VK_NUMPAD8]) {
					if (g+5<255) g+=5;
					else if (!capCol) g=0;
					else g=255;
				}
				
				else if (input.keyPressed[KeyEvent.VK_NUMPAD5]) {
					if (g-5>0) g-=5;
					else if (!capCol) g=255;
					else g=0;
				}
				
				if (input.keyPressed[KeyEvent.VK_NUMPAD9]) {
					if (b+5<255) b+=5;
					else if (!capCol) b=0;
					else b=255;
				}
				else if (input.keyPressed[KeyEvent.VK_NUMPAD6]) {
					if (b-5>0) b-=5;
					else if (!capCol) b=255;
					else b=0;
				}
			} else {
				if (!capLoc) {
						if (input.keyPressed[KeyEvent.VK_NUMPAD7]) {
							if (numberLock) {
								if (r<255) r++;
								if (g<255) g++;
								if (b<255) b++;
							}
							else if (r<255) r++;
							else if (!capCol) r=0;			
						}
						else if (input.keyPressed[KeyEvent.VK_NUMPAD4]) {
							if (numberLock) {
								if (r>0) r--;
								if (g>0) g--;
								if (b>0) b--;
							}							
							else if (r>0) r--;
							else if (!capCol) r=255;
						}
						
						if (input.keyPressed[KeyEvent.VK_NUMPAD8]) {
							if (numberLock) {
								if (r<255) r++;
								if (g<255) g++;
								if (b<255) b++;
							}
							else if (g<255) g++;
							else if (!capCol) g=0;
						}
						
						else if (input.keyPressed[KeyEvent.VK_NUMPAD5]) {
							if (numberLock) {
								if (r>0) r--;
								if (g>0) g--;
								if (b>0) b--;
							}
							else if (g>0) g--;
							else if (!capCol) g=255;
						}
						
						if (input.keyPressed[KeyEvent.VK_NUMPAD9]) {
							if (numberLock) {
								if (r<255) r++;
								if (g<255) g++;
								if (b<255) b++;
							}
							else if (b<255) b++;
							else if (!capCol) b=0;
						}
						else if (input.keyPressed[KeyEvent.VK_NUMPAD6]) {
							if (numberLock) {
								if (r>0) r--;
								if (g>0) g--;
								if (b>0) b--;
							}
							else if (b>0) b--;
							else if (!capCol) b=255;
						}
				} else {
					if (input.keyPressed[KeyEvent.VK_NUMPAD7]) {			
						if (r+5<255) r+=5;
						else if (!capCol) r=0;
						else r=255;
					}
					else if (input.keyPressed[KeyEvent.VK_NUMPAD4]) {
						if (r-5>0) r-=5;
						else if (!capCol) r=255;
						else r=0;
					}
					
					if (input.keyPressed[KeyEvent.VK_NUMPAD8]) {
						if (g+5<255) g+=5;
						else if (!capCol) g=0;
						else g=255;
					}
					
					else if (input.keyPressed[KeyEvent.VK_NUMPAD5]) {
						if (g-5>0) g-=5;
						else if (!capCol) g=255;
						else g=0;
					}
					
					if (input.keyPressed[KeyEvent.VK_NUMPAD9]) {
						if (b+5<255) b+=5;
						else if (!capCol) b=0;
						else b=255;
					}
					else if (input.keyPressed[KeyEvent.VK_NUMPAD6]) {
						if (b-5>0) b-=5;
						else if (!capCol) b=255;
						else b=0;
					}				
				}
			}
			
			
			
			
			
			
			KeyCooldown = 3;
		}
		if (KeyCooldown>0)KeyCooldown--;
		
				
		
		if (input.keyPressed[KeyEvent.VK_1]) pixelSize=1;
		if (input.keyPressed[KeyEvent.VK_2]) pixelSize=2;
		if (input.keyPressed[KeyEvent.VK_3]) pixelSize=3;
		if (input.keyPressed[KeyEvent.VK_4]) pixelSize=4;
		if (input.keyPressed[KeyEvent.VK_5]) pixelSize=5;
		if (input.keyPressed[KeyEvent.VK_6]) pixelSize=6;
		if (input.keyPressed[KeyEvent.VK_7]) pixelSize=7;
		if (input.keyPressed[KeyEvent.VK_8]) pixelSize=8;
		
		
		
		
		if (input.keyPressed[KeyEvent.VK_E]) eyedropCurrent();
		if (input.keyPressed[KeyEvent.VK_F]) fill();
		
		
	}
	
	public void render() {
		if (helpOpen) {
			for (int i=0;i<helpscr.length;i++) {
				pixels[i] = helpscr[i];
			}
			BufferStrategy bs = getBufferStrategy();
			if (bs == null) {
				createBufferStrategy(3);
				return;
			}
			
			Graphics g = bs.getDrawGraphics();
			g.fillRect(0, 0, getWidth(), getHeight());
			

			int ww = winX * scale;
			int hh = winY * scale ;
			int xo = (getWidth() - ww) / 2;
			int yo = (getHeight() - hh) / 2;
			
			g.drawImage(image, xo, yo, ww, hh, null);
			g.dispose();
			bs.show();
			return;
		}
		
		for (int i=0;i<layer2.length;i++) { //Clear
			layer2[i] = 0xffff00ff;
			pixels[i] = 0x111111;
		}				
		
		
		//iHan.drawImage(this, 1, 0, 0);		
		
		//UI Elements..
		String RandString = "";
		if (staticStage==0) RandString = "";
		if (staticStage==1) RandString = "Static/Noise?";
		if (staticStage==2) RandString = "Static";
		if (staticStage==3) RandString = "Noise - #?";
		if (staticStage==4) RandString = "Noise - " + noiseRange;
		draw("R: " + r,5,170+50,new Color(r,0,0).getRGB(),layer2);
		draw("G: " + g, 45,170+50,new Color(0,g,0).getRGB(),layer2);
		draw("B: " + b, 85,170+50,new Color(0,0,b).getRGB(),layer2);		
		if (staticStage>0) draw(RandString, 5, 242, 0xff00f0,layer2);
		draw("x"+selX/pixelSize+"/"+(vWinX/pixelSize-1), 150,190+50,0x444444,layer2);
		draw("y"+selY/pixelSize+"/"+(vWinY/pixelSize-1), 150,180+50,0x444444,layer2);
		if (capLoc) draw("+-5",108,158+50,0x222222,layer2);
		if (numberLock) {
			layer2[106+206*vWinX] = 0xff00f0;
			layer2[106+211*vWinX] = 0xff00f0;
			layer2[106+216*vWinX] = 0xff00f0;
			drawRect(layer2,107,206,108,217,0xff00f0);
		}
		paintPixel(layer2,selX,selY,new Color(r,g,b).getRGB());
		
		
		
		int bcol;		
		if (!capCol) bcol = 0xff000000;
		else bcol = 0xff550000;
		
		iHan.drawImage(this, 0, 0, 50);
		
		drawRect(picture,5,155+50,6+toPercentage(r),158+50,0xff0000);
		drawRect(picture,6+toPercentage(r),155+50,106,158+50,bcol);
		
		drawRect(picture,5,160+50,6+toPercentage(g),163+50,0x00ff00);
		drawRect(picture,6+toPercentage(g),160+50,106,163+50,bcol);
		
		drawRect(picture,5,165+50,6+toPercentage(b),168+50,0x0000ff);
		drawRect(picture,6+toPercentage(b),165+50,106,168+50,bcol);
		
		drawRect(picture, 5,180+50,15,190+50,rgb2col(r,g,b));
		drawRect(picture, 20,180+50,30,190+50,rgb2col(sr,sg,sb));
		
		
		//....End
		
		
		for (int i=0;i<picture.length;i++) {
			//if (picture[i] == 0xff111111) {
				pixels[i] = picture[i];
			//}
		}
		for (int i=0;i<layer2.length;i++) {
			if (layer2[i]!=0xffff00ff) {
				pixels[i] = layer2[i];
			}
		}
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		

		
		
		
		
		
		
		Graphics g = bs.getDrawGraphics();
		g.fillRect(0, 0, getWidth(), getHeight());
		

		int ww = winX * scale;
		int hh = winY * scale ;
		int xo = (getWidth() - ww) / 2;
		int yo = (getHeight() - hh) / 2;
		
		g.drawImage(image, xo, yo, ww, hh, null);
		g.dispose();
		bs.show();
	}
	
	@Override
	public void run() {	
		try {
			
			
			double unprocessed=0;
			double nsPerTick = 1000000000.0 / 60;
		    long lastTime=0;		
		    int lastTimer1=0;
			Init();
	
			
			while (isRunning) {
				long now = System.nanoTime();
				unprocessed += (now - lastTime) / nsPerTick;
				lastTime = now;
				boolean shouldRender = true;
				while (unprocessed >= 1) {
					ticks++;
					logic();
					unprocessed -= 1;				
					shouldRender = true;
				}			
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (shouldRender) {
					
					frames++;
					render();	
					if (previewOn) 	{
						prev.setWinSize(vWinX/pixelSize, vWinX/pixelSize);
						prev.renderr();
					}

				}
	
				if (System.currentTimeMillis() - lastTimer1 > 1000) {
					lastTimer1 += 1000;
					//System.out.println(Statics.ticks + " ticks, " + Statics.frames + " fps");
					frames = 0;
					ticks = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void drawRect(int[] scr, int sx, int sy, int ex, int ey, int col) {
		/*
		 * 		if (staticStage==1) RandString = "Static/Noise?";
	if (staticStage==2) RandString = "Static";
	if (staticStage==3) RandString = "Noise - #?";
	if (staticStage==4) RandString = "Noise - " + noiseRange;
		 */
		UndoObj uobj = new UndoObj();		
		if (addUndo) {
			uobj.xLoc=sx;
			uobj.yLoc=sy;
			uobj.xLocE=ex;
			uobj.yLocE=ey;
			uobj.pixels = new int[(ex-sx)+(ey+sy)];
		}
									
		Random random = new Random();
		Color c = new Color(col);
		for (int cx=0;cx<(ex-sx);cx++) {			
			for (int cy=0;cy<(ey-sy);cy++) {
				if (staticStage==2) {
					try {
						int tc = random.nextInt(15);
						int tr=r+tc;
						int tg=g+tc;
						int tb=b+tc;
						c = new Color(tr,tg,tb);						
					} catch (IllegalArgumentException e) {
						System.out.println("Colors out of range for static: " + r+"-"+g+"-"+b);
						e.printStackTrace();
					}
				}
				if (addUndo) uobj.pixels[cx+cy] = scr[(sx+cx) + (sy+cy) * vWinX];
				scr[(sx+cx) + (sy+cy) * vWinX] = c.getRGB();
				
			}
			
		}
		
		if (addUndo) {
			undoList.add(uobj);				
		}
	}
	
	
	
	public void drawRect(int[] scr, int sx, int sy, int ex, int ey, int[] src) {	
		for (int cx=0;cx<(ex-sx);cx++) {			
			for (int cy=0;cy<(ey-sy);cy++) {	
				System.out.println(Integer.toHexString(picture[(sx+cx) + (sy+cy) * vWinX])+"|"+Integer.toHexString(src[cx+cy]));
				picture[(sx+cx) + (sy+cy) * vWinX] = src[cx+cy];
			}			
		}					
	}
	
	
	/**
	 * Automatically assumes number range 0-255
	 * @param Number
	 * @return 
	 */
	public int toPercentage(int Nbr) { //TODO terribly done..
		double t = (double) (Nbr - 0) / (255 - 0) * 100;
		return Integer.valueOf((int) Math.round(t));			
	}
	
	public int rgb2col(int r, int g, int b) {
		Color col = new Color(r,g,b);		
		return col.getRGB();
	}
	
	public void paintPixel(int[] scr, int x, int y, int rgb) {
				
		int srgb=rgb;
		if (staticStage==4) {
			int hm = noiseRange;
			if (hm==0) hm = 10;
			Color cc = new Color(rgb);
			int r = cc.getRed();
			int g = cc.getGreen();
			int b = cc.getBlue();
			Random random = new Random();
			int ran = random.nextInt(hm);
			r+=ran;
			g+=ran;
			b+=ran;
			srgb = new Color(r,g,b).getRGB();
		}									
		drawRect(scr, x,y,x+pixelSize,y+pixelSize,srgb);
	}

	public void flipSelectedCols() {
		int[] t = {r,g,b};
		r = sr;
		g = sg;
		b = sb;
		
		sr = t[0];
		sg = t[1];
		sb = t[2];
	}
	
	public void eyedropCurrent() {
		Color col = new Color(picture[selX+selY * winX]);
		r = col.getRed();
		g = col.getGreen();
		b = col.getBlue();
	}
	private static final String chars = "" + //
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!?\"'/\\<>()[]{}" + //
			"abcdefghijklmnopqrstuvwxyz_               " + //
			"0123456789+-=*:;÷≈ƒÂ                      " + //
			"";

	public void draw(String string, int x, int y, int col, int[] pix) {
		for (int i = 0; i < string.length(); i++) {
			int ch = chars.indexOf(string.charAt(i));			
			//System.out.println(ch);
			if (ch < 0) continue;
			
			int xx = ch % 42;
			int yy = ch / 42;
			draw(iHan.imageDB.get(1), x + i * 6, y, xx * 6, yy * 8, 5, 8, col,pix);
		}
	}
	
	public void draw(Image image, int xOffs, int yOffs, int xo, int yo, int w, int h,int col, int[] pix) {		
		for (int y = 0; y < h; y++) {
			int yPix = y + yOffs;
			if (yPix < 0 || yPix >= winY) continue;
			for (int x = 0; x < w; x++) {
				int xPix = x + xOffs;
				if (xPix < 0 || xPix >= winX) continue;

				int src = image.pixels[(x + xo) + (y + yo) * image.imgWidth];
				if (src != 0xffff00ff) {
					pix[xPix + yPix * winX] = col;
				}
			}
		}
	}
	public void fill() {
		int cc = picture[selX+selY*vWinX];
		for (int i=0;i<picture.length;i++) {
			if (picture[i]==cc) picture[i] = new Color(r,g,b).getRGB();
		}
	}
	
	public void saveImage() throws IOException {		
		System.out.println("Attempting file save");
		File file;
		JFileChooser fc = new JFileChooser();
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();			
			
			BufferedImage bi = new BufferedImage(vWinX, vWinY, BufferedImage.TYPE_INT_ARGB);
			for (int i=0;i<bi.getHeight();i++) {
				for (int i2=0;i2<bi.getWidth();i2++) {
					bi.setRGB(i, i2, picture[i+i2*vWinX]);
				}
			}
			
			ImageIO.write(bi, "png", file);
			System.out.println("Image out!");
				
		}									
	}

	public void saveImageNow() throws IOException {		
		System.out.println("Attempting emergency save");		
			BufferedImage bi = new BufferedImage(vWinX, vWinY, BufferedImage.TYPE_INT_ARGB);
			for (int i=0;i<bi.getHeight();i++) {
				for (int i2=0;i2<bi.getWidth();i2++) {
					bi.setRGB(i, i2, picture[i+i2*vWinX]);
				}						
			ImageIO.write(bi, "png", new File(Paths.get(".").toAbsolutePath().normalize().toString() + "/EmergencyOut.png"));
			System.out.println("Emergency image out!");
				
		}									
	}
	
	
	public void findMedium() {
		r = (r+sr)/2;
		g = (g+sg)/2;
		b = (b+sb)/2;					
	}	
	
	public void loadImage() throws IOException {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			BufferedImage bi = new BufferedImage(vWinX,vWinY, BufferedImage.TYPE_INT_ARGB);
			bi = ImageIO.read(file);
			System.out.println(vWinX+"|"+vWinY+"-"+bi.getHeight()+"|"+bi.getWidth());
			for (int i=0;i<bi.getHeight();i++) {
				for (int i2=0;i2<bi.getWidth();i2++) {									
					picture[i+i2*vWinX] = bi.getRGB(i, i2);;
				}
			}
		}
	}
	
	public void Undo() {
		System.out.println("CurSize: "+undoList.size());
		if (undoList.size()==0) {
			System.out.println("No more undos left!");
			return;
		}
		UndoObj uobj = undoList.get(undoList.size()-1); //-1?		
		drawRect(picture,uobj.xLoc,uobj.yLoc,uobj.xLocE,uobj.yLocE,uobj.pixels);
		//	public void drawRect(int[] scr, int sx, int sy, int ex, int ey, int col) {
		undoList.remove(uobj); //???
	}


}
