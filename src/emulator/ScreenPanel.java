package emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import emulator.PixelData;

import javax.swing.JPanel;

public class ScreenPanel extends JPanel{
	
	private Color[] backgroundColors;
	private Color[] obj0Colors;	
	private Color[] obj1Colors;

	
	private PixelData[] pixelsArray;
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
		
		pixelsArray = new PixelData[160];
		backgroundColors = new Color[4];
		obj0Colors = new Color[4];
		obj1Colors = new Color[4];
		
		setPreferredSize(screenDimension);
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		
		for(int i = 0; i < 160; i++){
			if(pixelsArray[i] == null)	continue;	//when it's not set initially
			
			switch(pixelsArray[i].type){
			case PaletteTypeBackground:{
				g.setColor(backgroundColors[pixelsArray[i].color]);
				break;
			}
			case PaletteTypeObject0:{
				g.setColor(obj0Colors[pixelsArray[i].color]);
				break;
			}
			case PaletteTypeObject1:{
				g.setColor(obj1Colors[pixelsArray[i].color]);
				break;
			}
			default:
				g.setColor(Color.red); //error

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
//		for(int i = 0; i < 144; i++){
//			g.setColor(Color.green);
//			if(i%8 == 0)
//				g.setColor(Color.red);
//			g.drawLine(0, i * PIXEL_SCALE_FACTOR, this.getSize().width, i * PIXEL_SCALE_FACTOR);
//		}
	}
	
	public Dimension getScreenDimension() {
		return screenDimension;
	}
	
	public void paintRow(int row, PixelData[] pixelsArray){
		this.currentRow = row;
		this.pixelsArray = pixelsArray;
		
		this.paintImmediately(0, row*PIXEL_SCALE_FACTOR, SCREEN_WIDTH, PIXEL_SCALE_FACTOR);
	}
	
	public void setBackgroundAndWindowColors(Color c1, Color c2, Color c3, Color c4){
		
//		System.out.println("Setting bg colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		backgroundColors[0] = c1;
		backgroundColors[1] = c2;
		backgroundColors[2] = c3;
		backgroundColors[3] = c4;

	}
	
	public void setObject0Colors(Color c1, Color c2, Color c3, Color c4){
		
//		System.out.println("Setting obj0 colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		obj0Colors[0] = c1;
		obj0Colors[1] = c2;
		obj0Colors[2] = c3;
		obj0Colors[3] = c4;
		
	}
	
	public void setObject1Colors(Color c1, Color c2, Color c3, Color c4){
		
//		System.out.println("Setting obj1 colors: " +c1 +", " +c2+", "+c3+", "+c4);
		
		obj1Colors[0] = c1;
		obj1Colors[1] = c2;
		obj1Colors[2] = c3;
		obj1Colors[3] = c4;
		
	}
	
}
