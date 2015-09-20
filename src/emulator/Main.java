package emulator;

public class Main{
	
	private GameBoy gameBoy;
	private RomLoader romLoader;
	
	//private ScreenFrame screenFrame;
	
	public Main() {
		gameBoy = new GameBoy();
		
		romLoader = new RomLoader(gameBoy);
//		romLoader.loadROM("./TestROMs/Super Mario Land (World).gb");
//		romLoader.loadROM("./TestROMs/cpu_instrs.gb");
//		romLoader.loadROM("./TestROMs/GBTICTAC.GB");
//		romLoader.loadROM("./TestROMs/GuessTheNumber_1.gb");
		romLoader.loadROM("./TestROMs/TCFOS.GB");
//		romLoader.loadROM("./TestROMs/Tetris.gb");
//		romLoader.loadROM("./TestROMs/BOOTSTRAP.bin");
		
		//Groups of instuctions testing
//		romLoader.loadROM("./TestROMs/individual/01-special.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/02-interrupts.gb");
//		romLoader.loadROM("./TestROMs/individual/03-op sp,hl.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/04-op r,imm.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/05-op rp.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/06-ld r,r.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/07-jr,jp,call,ret,rst.gb");// <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/08-misc instrs.gb"); //err
//		romLoader.loadROM("./TestROMs/individual/09-op r,r.gb"); // <-- PASSED
//		romLoader.loadROM("./TestROMs/individual/10-bit ops.gb");
//		romLoader.loadROM("./TestROMs/individual/11-op a,(hl).gb"); //err
		
		gameBoy.start();
		
	}
	
	public static void main(String[] args) {
		new Main();
	}

}
