package emulator;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class ScreenFrame extends JFrame{
	
	public ScreenPanel screenPanel;

	public ScreenFrame(GameBoy gameBoy){
		super();
		init();
		
		this.addKeyListener(gameBoy.inputHandler);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle("Eduard's GameBoy Emulator");
		setVisible(true);
	}
	
	private void init() {
		
		new RepeatingReleasedEventsFixer().install();
		
		screenPanel = new ScreenPanel();
		
		setLayout(new BorderLayout());
		add(screenPanel, BorderLayout.CENTER);
		
		pack();
	}
	
}
