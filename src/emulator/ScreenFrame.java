package emulator;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class ScreenFrame extends JFrame{
	
	ScreenPanel screenPanel;

	public ScreenFrame(){
		super();
		init();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Eduard's GameBoy Emulator");
		setVisible(true);
	}
	
	private void init() {
		screenPanel = new ScreenPanel();
		
		setLayout(new BorderLayout());
		add(screenPanel, BorderLayout.CENTER);
		
		pack();
	}
}
