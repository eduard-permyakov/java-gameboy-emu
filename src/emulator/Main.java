package emulator;

import java.util.concurrent.TimeUnit;

public class Main{
	
	private GameBoy gameBoy;
	private RomLoader romLoader;
	
	//private ScreenFrame screenFrame;
	
	public Main() {
		gameBoy = new GameBoy();
		
		romLoader = new RomLoader(gameBoy);
		romLoader.loadROM("./TestROMs/Tetris.gb");
//		romLoader.loadROM("./TestROMs/BOOTSTRAP.bin");
		
		gameBoy.start();
		
	}
	
	public static void main(String[] args) {
		Main main = new Main();
	}

}
