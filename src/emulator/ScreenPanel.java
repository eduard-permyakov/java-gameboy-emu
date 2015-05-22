package emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ScreenPanel extends JPanel {
	
	private Color color1;
	private Color color2;
	private Color color3;
	private Color color4;
	
	private char[] pixelsArray;
	private int currentRow;
	
	private final static int X_PIXELS = 160;
	private final static int Y_PIXELS = 144;
	
	private final static int PIXEL_SCALE_FACTOR = 2;
	
	private final static int SCREEN_WIDTH = X_PIXELS * PIXEL_SCALE_FACTOR;
	private final static int SCREEN_HEIGHT = Y_PIXELS * PIXEL_SCALE_FACTOR;
	
	private Dimension screenDimension;

	public ScreenPanel() {
		super();
		screenDimension = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		
		color1 = Color.white;
		color2 = Color.lightGray;
		color3 = Color.darkGray;
		color4 = Color.black;
		
		pixelsArray = new char[160];
		
		setPreferredSize(screenDimension);
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		
		for(int i = 0; i < 160; i++){
			System.out.print(pixelsArray[i]);
			switch(pixelsArray[i]){
			case 0x0:
				g.setColor(color1);
			case 0x1:
				g.setColor(color2);
			case 0x2:
				g.setColor(color3);
			case 0x3:
				g.setColor(color4);
				default:
					g.setColor(Color.red);
			}
			g.fillRect(i*PIXEL_SCALE_FACTOR, currentRow*PIXEL_SCALE_FACTOR, 
					PIXEL_SCALE_FACTOR, PIXEL_SCALE_FACTOR);
		}
		System.out.println();
	}
	
	public Dimension getScreenDimension() {
		return screenDimension;
	}
	
	public void paintRow(int row, char[] pixelsArray){
		this.currentRow = row;
		this.pixelsArray = pixelsArray;
		
		this.repaint(0, row*PIXEL_SCALE_FACTOR, SCREEN_WIDTH, PIXEL_SCALE_FACTOR);
	}
	
}
