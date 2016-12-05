package src.ero.tool;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Input implements KeyListener, MouseListener {

public static boolean[] keyPressed = new boolean[25565];
public String lastCharPress="";
	@Override
	public void keyPressed(KeyEvent arg0) {
		keyPressed[arg0.getKeyCode()] = true;
		lastCharPress = arg0.getKeyChar() + "";		
	}
	
	@Override
	public void keyReleased(KeyEvent arg0) {
		keyPressed[arg0.getKeyCode()] = false;
		//System.out.println(arg0.getKeyCode() + " " + arg0.getKeyChar());
		
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
}