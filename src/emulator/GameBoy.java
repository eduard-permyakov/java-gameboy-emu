package emulator;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class GameBoy extends Thread{
	
	public boolean lcdControllerIsIdle;
	
	private int machineCycles;
	private int clockCycles;
	
	private CPU cpu;
	
	private LCDController lcd;
	
	private ScreenFrame screenFrame;
	
	public volatile char[] memory;
	
	final static int SIXTEEN_KB_ROM_BANK_0_ADDR 			= 0x0000;
	final static int SIXTEEN_KB_SWITCHABLE_ROM_BANK_ADDR 	= 0x4000;
	final static int EIGHT_KB_VIDEO_RAM_ADDR 				= 0x8000;
	final static int EIGHT_KB_SWITCHABLE_RAM_BANK_ADDR 		= 0xA000;
	final static int EIGHT_KB_INTERNAL_RAM_ADDR 			= 0xC000;
	final static int ECHO_OF_EIGHT_KB_INTERNAL_RAM_ADDR		= 0xE000;
	final static int SPRITE_ATTRIB_MEMORY_ADDR 				= 0xFE00;
	final static int IO_PORTS_ADDR 							= 0xFF00;
	final static int INTERNAL_RAM_ADDR 						= 0xFF80;
	final static int INTERRUPT_TABLE_REGISTER_ADDR 			= 0xFFFF;
		
	public GameBoy() {
		init();
	}
	
	private void init() {
		
	    final CyclicBarrier barrier = new CyclicBarrier(1);
		
		memory = new char[65536];
		cpu = new CPU(this, barrier);
		lcd = new LCDController(this, barrier);
		screenFrame = new ScreenFrame();
		
		lcdControllerIsIdle = false;

	}
	
	public void start() {
		
	    Thread cpuThread = new Thread(cpu);
	    Thread lcdControllerThread = new Thread(lcd);

	    cpuThread.start();
	    lcdControllerThread.start();
	    
	}
	
	public void DMATransfer() {
		char sourceAddress = (char)(((memory[LCDController.DMA_REGISTER_ADDR] / 0x100) << 8) | 0x0);
		char destinationAddress = 0xFE00;
		for(int i = 0; i <= 0x9F; i++){
			memory[destinationAddress + i] = memory[sourceAddress + i];
		}
	}
	
	public synchronized void LCDControllerDidNotifyOfStateCompletion(){
		lcdControllerIsIdle = true;
		notifyAll();
		cpu.setState(CPUState.CPU_STATE_EXECUTING);
	}
	
	public void projectRow(int row, char[] pixelsArray){
		screenFrame.screenPanel.paintRow(row, pixelsArray);
	}

	//GET/SET
	
	public int getMachineCycles() {
		return machineCycles;
	}
	
	public void setMachineCycles(int machineCycles){
		this.machineCycles = machineCycles;
	}
	
	public int getClockCycles() {
		return clockCycles;
	}
	
	public void setClockCycles(int clockCycles){
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

		}

	}

}
