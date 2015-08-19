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
//		romLoader.loadROM("./TestROMs/cpu_instrs.gb");
//		romLoader.loadROM("./TestROMs/GBTICTAC.GB");
//		romLoader.loadROM("./TestROMs/Tetris.gb");
//		romLoader.loadROM("./TestROMs/BOOTSTRAP.bin");
		
		//Groups of instuctions testing
		romLoader.loadROM("./TestROMs/individual/01-special.gb");
//		romLoader.loadROM("./TestROMs/individual/02-interrupts.gb");
//		romLoader.loadROM("./TestROMs/individual/03-op sp,hl.gb");
//		romLoader.loadROM("./TestROMs/individual/04-op r,imm.gb");
//		romLoader.loadROM("./TestROMs/individual/05-op rp.gb");
//		romLoader.loadROM("./TestROMs/individual/06-ld r,r.gb");
//		romLoader.loadROM("./TestROMs/individual/07-jr,jp,call,ret,rst.gb");
//		romLoader.loadROM("./TestROMs/individual/08-misc instrs.gb");
//		romLoader.loadROM("./TestROMs/individual/09-op r,r.gb");
//		romLoader.loadROM("./TestROMs/individual/10-bit ops.gb");
//		romLoader.loadROM("./TestROMs/individual/11-op a,(hl).gb");

		
		gameBoy.start();
		
	}
	
	public static void main(String[] args) {
		Main main = new Main();
	}

}
