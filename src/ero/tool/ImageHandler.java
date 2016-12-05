package src.ero.tool;

import java.util.ArrayList;
import java.util.Random;



public class ImageHandler {
	public ArrayList<Image> imageDB = new ArrayList<>(50);
	public enum drawMode {
		SPRITE_CENTER, SPRITE_TOPLEFT
	};
	public drawMode curDMode = drawMode.SPRITE_TOPLEFT;
	
	public void drawImage(Application display, int img, int x, int y) {
		//System.out.println(Integer.toHexString(imageDB.get(img).pixels[2]));
		for (int iy=0;iy<imageDB.get(img).imgHeight;iy++) {
			//if (ix<display.getX() && ix > 0) continue;
					
			for (int ix=0;ix<imageDB.get(img).imgWidth;ix++) {
				//if (iy<display.getY() && iy > 0) continue;
				
				int col = imageDB.get(img).pixels[ix+iy * imageDB.get(img).imgWidth];
				if (col != 0xffff00ff) {
				//System.out.println("IW: "+imageDB.get(img).imgWidth + " IH: " + imageDB.get(img).imgHeight);
					if (curDMode == drawMode.SPRITE_TOPLEFT) display.picture[(x+ix) + (y+iy) * display.winX] = col;
					else {
						//TODO implement centered Drawing
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param display Screen Display
	 * @param img Image in Index
	 * @param sx Starting X of Image
	 * @param sy Starting Y of Image
	 * @param ex Ending X of Image
	 * @param ex Ending Y of Image
	 * @param scrX Where to render on screen X
	 * @param scrY ...Y
	 */
	public void drawPartalImage(Application display, int img, int sx, int sy, int ex, int ey, int scrX, int scrY) {
		for (int cx=0;cx<(ex-sx);cx++) {
			//if (sx+cx > display.winX && sx+cx < 0) continue;
			for (int cy=0;cy<(ey-sy);cy++) {
				//if (sy+cy > display.winY && sy+cy < 0) continue;
			//	System.out.println(sy);
				int col = imageDB.get(img).pixels[(sx+cx)+(sy+cy) * imageDB.get(img).imgWidth];
				if (col != 0xffff00ff) {
					//System.out.println("IW: "+imageDB.get(img).imgWidth + " IH: " + imageDB.get(img).imgHeight);
						if (curDMode == drawMode.SPRITE_TOPLEFT) display.pixels[(scrX+cx) + (scrY+cy) * display.winX] = col;
						else {
							//TODO implement centered Drawing
						}
					}
			}
		}
	}
	
	

		
	/**
	 * @param already prepped Image
	 * @return index number of where new image is in imageDB
	 */
	public int newImage(Image img) {
		int index = findEmptyImage();
		imageDB.set(index, img);
		//System.out.println("New Image: Name-"+img.name + " Image array size-"+imageDB.size());
		return index;
	}
		

	
	
	/**	  
	 * @param imageID
	 * @return returns index of requested image in imageDB, if not found, returns -1
	 */
	public int findImage(int imageID) {
		for (int i=0;i<imageDB.size();i++) {
			if (imageDB.get(i).imageID==imageID) return i;
		}
		return -1;
	}
	
	private long generateImageUID() {
		Random random = new Random();
		boolean found = false;
		boolean badID = false;
		Long uID = random.nextLong();
		
		while (!found) {
			if (badID) {
				uID = random.nextLong();
				badID = false;
			}
			
			for (int i=0;i<imageDB.size();i++) {
				if (imageDB.get(i).imageUID == uID) badID = true;				
			}			
			if (badID) found = false;
			else found = true;
		}
		return uID;
	}
	
	private int findEmptyImage() {
		for (int i=0;i<imageDB.size();i++) {
			if (imageDB.get(i).imageUID==0) return i;
		}
		imageDB.add(new Image());		
		return imageDB.size()-1; //Uhh...imageDB.size() - 1?
	}
	
	/**	 
	 * @param name, not case sensitive
	 * @return imageDB index of image requested. -1 if not found.
	 */
	public int findImagefromName(String name) {
		for (int i=0;i<imageDB.size();i++) {
			if (imageDB.get(i).name.toLowerCase().equals(name.toLowerCase())) return i;
		}
		return -1;
	}
	
}
