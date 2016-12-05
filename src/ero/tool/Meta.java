package src.ero.tool;

import java.io.IOException;

public class Meta {
	
	public static void main(String[] args) {
		Application app = null;
		System.out.println("EroDraw started with args: " + args);
		try {
			app = new Application();
			app.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("EroDraw fatal error detected. Attempting to back up current image..");
			try {
				app.saveImageNow();
				System.out.println("Emergency save was a success. Crashing now, bye bye.");
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Even the emergency output failed. Oops.");
			}
		}
	}
}
