package emulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ScreenPanel extends JPanel {
	
	private final static int X_PIXELS = 160;
	private final static int Y_PIXELS = 144;
	
	private final static int PIXEL_SCALE_FACTOR = 4;
	
	private final static int SCREEN_WIDTH = X_PIXELS * PIXEL_SCALE_FACTOR;
	private final static int SCREEN_HEIGHT = Y_PIXELS * PIXEL_SCALE_FACTOR;
	
	private Dimension screenDimension;

	public ScreenPanel() {
		super();
		screenDimension = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
		
		setPreferredSize(screenDimension);
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
	}
	
	public Dimension getScreenDimension() {
		return screenDimension;
	}
}
