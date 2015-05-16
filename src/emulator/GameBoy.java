package emulator;

public class GameBoy {
	
	CPU cpu;
	
	public char[] memory;
	
	final static int SIXTEEN_KB_ROM_BANK_0_ADDR 			= 0x0000;
	final static int SIXTEEN_KB_SWITCHABLE_ROM_BANK_ADDR 	= 0x4000;
	final static int EIGHT_KB_VIDEO_RAM_ADDR 				= 0x8000;
	final static int EIGHT_KB_SWITCHABLE_RAM_BANK_ADDR 		= 0xA000;
	final static int EIGHT_KB_INTERNAL_RAM_ADDR 			= 0xC000;
	final static int ECHO_OF_EIGHT_KB_INTERNAL_RAM_ADDR		= 0xE000;
	final static int SPRITE_ATTRIB_MEMORY_ADDR 				= 0xFEA0;
	final static int IO_PORTS_ADDR 							= 0xFF00;
	final static int INTERNAL_RAM_ADDR 						= 0xFF80;
	final static int INTERRUPT_TABLE_REGISTER_ADDR 			= 0xFFFF;
		
	public GameBoy() {
		init();
	}
	
	private void init() {
		memory = new char[65535];
		cpu = new CPU(this);
	}
	
	public void executeClockCycle() {
		//System.out.println("next clock cycle");
		cpu.fetchNextOpcode();
		cpu.decodeAndExecuteOpcode();
	}

}
