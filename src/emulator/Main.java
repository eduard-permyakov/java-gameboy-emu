package emulator;

import java.util.concurrent.TimeUnit;

public class Main extends Thread{
	
	final static int PROCESSOR_FREQUENCY_HZ = 4194304;
	final static int NANOSECONDS_IN_SECOND = 1000000000;
	
	final static int PROCESSOR_DAMPING_FACTOR = 20;
	final static int PROCESSOR_DAMPED_FREQUENCY_HZ = 
			(int)(PROCESSOR_FREQUENCY_HZ/PROCESSOR_DAMPING_FACTOR);
	
	private GameBoy gameBoy;
	private RomLoader romLoader;
	
	//private ScreenFrame screenFrame;
	
	public Main() {
		gameBoy = new GameBoy();
		
		romLoader = new RomLoader(gameBoy);
		romLoader.loadROM("./TestROMs/Tetris.gb");
	}
	
	public void run() {
		while(true){
			long startTime = System.nanoTime();
			
			gameBoy.run();
			
			long endTime = System.nanoTime();
			long stallTimeNano = Math.max(((NANOSECONDS_IN_SECOND/PROCESSOR_DAMPED_FREQUENCY_HZ) 
					- (endTime - startTime)), 0);
			long stallTimeMillis = TimeUnit.MILLISECONDS.convert(stallTimeNano, TimeUnit.NANOSECONDS);
			
			try {
				Thread.sleep(stallTimeMillis, (int)(stallTimeMillis%1000000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

}
