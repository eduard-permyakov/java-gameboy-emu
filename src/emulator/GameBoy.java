package emulator;

import java.util.concurrent.TimeUnit;

public class GameBoy extends Thread{
	
	boolean lcdControllerIsIdle;
	
	private int machineCycles;
	private int clockCycles;
	
	private CPU cpu;
	
	private LCDController lcd;
	
	private ScreenFrame screenFrame;
	
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
	
	final static int PROCESSOR_FREQUENCY_HZ = 4194304;
	final static int NANOSECONDS_IN_SECOND = 1000000000;
	
	final static int PROCESSOR_DAMPING_FACTOR = 20;
	final static int PROCESSOR_DAMPED_FREQUENCY_HZ = 
			(int)(PROCESSOR_FREQUENCY_HZ/PROCESSOR_DAMPING_FACTOR);
		
	public GameBoy() {
		init();
	}
	
	private void init() {
		memory = new char[65536];
		cpu = new CPU(this);
		lcd = new LCDController(this);
		screenFrame = new ScreenFrame();
		
		lcdControllerIsIdle = false;

	}
	
	public void run() {
		
		cpu.start();
		lcd.start();
		
		while(true){
			long startTime = System.nanoTime();
			
			//gameBoy.run();
			//System.out.println("Clock cycles: " + clockCycles);
			
			long endTime = System.nanoTime();
			long stallTimeNano = Math.max(((NANOSECONDS_IN_SECOND/PROCESSOR_DAMPED_FREQUENCY_HZ) 
					- (endTime - startTime)), 0);
			long stallTimeMillis = TimeUnit.MILLISECONDS.convert(stallTimeNano, TimeUnit.NANOSECONDS);
			
			try {
				//Thread.sleep(0);
				Thread.sleep(stallTimeMillis, (int)(stallTimeMillis%1000000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void DMATransfer() {
		char sourceAddress = (char)(((memory[LCDController.DMA_REGISTER_ADDR] / 0x100) << 8) | 0x0);
		char destinationAddress = 0xFE00;
		for(int i = 0; i <= 0x9F; i++){
			memory[destinationAddress + i] = memory[sourceAddress + i];
		}
	}
	
//	public void run() {
//		cpu.fetchNextOpcode();
//		cpu.decodeAndExecuteOpcode();
//	}
	
	public synchronized void LCDControllerDidNotifyOfStateCompletion(){
		lcdControllerIsIdle = true;
		cpu.setState(CPUState.CPU_STATE_EXECUTING);
	}

	//GET/SET
	
	public synchronized int getMachineCycles() {
		return machineCycles;
	}
	
	public synchronized void setMachineCycles(int machineCycles){
		this.machineCycles = machineCycles;
	}
	
	public synchronized int getClockCycles() {
		return clockCycles;
	}
	
	public synchronized void setClockCycles(int clockCycles){
		this.clockCycles = clockCycles;
		LCDControllerState prevState = lcd.getLCDState();
		LCDControllerState newState = null;
		
		if(clockCycles%LCDController.TOTAL_REFRESH_CYCLES > LCDController.TOTAL_PRE_VBLANK_CYCLES){
			
			newState = LCDControllerState.LCD_STATE_VBLANK;
			
		}else{
			if(clockCycles%LCDController.HORIZONTAL_LINE_CYCLES < LCDController.READING_OAM_ONLY_CYCLES){
				
				newState = LCDControllerState.LCD_STATE_READING_OAM_ONLY;
				
			}else if(clockCycles%LCDController.HORIZONTAL_LINE_CYCLES < (LCDController.READING_OAM_AND_VRAM_CYCLES
					+ LCDController.READING_OAM_ONLY_CYCLES)){
				
				newState = LCDControllerState.LCD_STATE_READING_OAM_AND_VRAM;
				
			}else{
				
				newState = LCDControllerState.LCD_STATE_HBLANK;
				
			}
			
		}
		
		if(lcd.getLCDState() != newState) {
			
			if(lcdControllerIsIdle){
				lcd.setLCDState(newState);
				lcdControllerIsIdle = false;
				lcd.run();
			}else{
				cpu.setState(CPUState.CPU_STATE_WAITING);
			}
			//System.out.println("Old State: "+lcd.getLCDState());
			//System.out.println("Set LCD State: "+newState);

		}
	}

}
