package emulator;

import java.util.concurrent.TimeUnit;

public class Main{
	
	private GameBoy gameBoy;
	private RomLoader romLoader;
	
	//private ScreenFrame screenFrame;
	
	public Main() {
		gameBoy = new GameBoy();
		
//		char a = 0xdf;
//		char b = 0xff;
//		char c = (char)(a << 8 | b);
//		
//		System.out.println(Integer.toHexString(c));
		
		romLoader = new RomLoader(gameBoy);
		romLoader.loadROM("./TestROMs/cpu_instrs.gb");
//		romLoader.loadROM("./TestROMs/GBTICTAC.GB");
		//romLoader.loadROM("./TestROMs/Tetris.gb");
//		romLoader.loadROM("./TestROMs/BOOTSTRAP.bin");
		
		gameBoy.start();
		
	}
	
	public static void main(String[] args) {
		Main main = new Main();
	}

}
