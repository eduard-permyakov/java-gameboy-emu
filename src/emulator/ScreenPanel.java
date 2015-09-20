package emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ScreenPanel extends JPanel{
	
	private Color backgroundColor1;
	private Color backgroundColor2;
	private Color backgroundColor3;
	private Color backgroundColor4;
	
	private Color obj0Color1;
	private Color obj0Color2;
	private Color obj0Color3;
	private Color obj0Color4;
	
	private Color obj1Color1;
	private Color obj1Color2;
	private Color obj1Color3;
	private Color obj1Color4;
	
	private char[] pixelsArray;
	private int currentRow;
	
	private final static int X_PIXELS = 160;
	private final static int Y_PIXELS = 144;
	
	private final static int PIXEL_SCALE_FACTOR = 3;
	
	private final static int SCREEN_WIDTH = X_PIXELS * PIXEL_SCALE_FACTOR;
	private final static int SCREEN_HEIGHT = Y_PIXELS * PIXEL_SCALE_FACTOR;
	
	private Dimension screenDimension;

	public ScreenPanel() {
		super();
		screenDimension = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		setBackground(Color.black);
		
		pixelsArray = new char[160];
		
		setPreferredSize(screenDimension);
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		
		for(int i = 0; i < 160; i++){
			switch(pixelsArray[i]){
			case 0:
				g.setColor(backgroundColor1);
				break;
			case 1:
				g.setColor(backgroundColor2);
				break;
			case 2:
				g.setColor(backgroundColor3);
				break;
			case 3:
				g.setColor(backgroundColor4);
				break;
				default:
					g.setColor(Color.red);
			}
			g.fillRect(i*PIXEL_SCALE_FACTOR, currentRow*PIXEL_SCALE_FACTOR, 
					PIXEL_SCALE_FACTOR, PIXEL_SCALE_FACTOR);
		}
		
		//debug code
//		for(int i = 0; i < 160; i++){
//			g.setColor(Color.green);
//			if(i%8 == 0)
//				g.setColor(Color.red);
//			g.drawLine(i * PIXEL_SCALE_FACTOR, 0, i * PIXEL_SCALE_FACTOR, this.getSize().height);
//		}
	}
	
	public Dimension getScreenDimension() {
		return screenDimension;
	}
	
	public void paintRow(int row, char[] pixelsArray){
		this.currentRow = row;
		this.pixelsArray = pixelsArray;
		
		this.paintImmediately(0, row*PIXEL_SCALE_FACTOR, SCREEN_WIDTH, PIXEL_SCALE_FACTOR);
	}
	
	public void setBackgroundAndWindowColors(Color c1, Color c2, Color c3, Color c4){
		
		System.out.println("Setting bg colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		backgroundColor1 = c1;
		backgroundColor2 = c2;
		backgroundColor3 = c3;
		backgroundColor4 = c4;

	}
	
	public void setObject0Colors(Color c1, Color c2, Color c3, Color c4){
		
		System.out.println("Setting obj0 colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		obj0Color1 = c1;
		obj0Color2 = c2;
		obj0Color3 = c3;
		obj0Color4 = c4;
		
	}
	
	public void setObject1Colors(Color c1, Color c2, Color c3, Color c4){
		
		System.out.println("Setting obj1 colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		obj1Color1 = c1;
		obj1Color2 = c2;
		obj1Color3 = c3;
		obj1Color4 = c4;
		
	}
	
}
