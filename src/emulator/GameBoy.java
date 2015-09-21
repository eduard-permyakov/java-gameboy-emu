package emulator;

import java.awt.Color;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import emulator.LCDController;

public class GameBoy extends Thread{
	
	public final static int INTERRUPT_FLAG_REGISTER_ADDR = 0xFF0F;
	
	private int old_y = 9; //shouldn't be 0 at start
	boolean reset = false;
	
	public boolean lcdControllerIsIdle;
	
	private int machineCycles;
	private int clockCycles;
	
	private CPU cpu;
	private LCDController lcd;
	private ScreenFrame screenFrame;
	
	public InputHandler inputHandler;
	public Memory memory;
		
	public GameBoy() {
		init();
	}
	
	private void init() {
		
	    final CyclicBarrier barrier = new CyclicBarrier(1);
		
		memory = new Memory(this);
		cpu = new CPU(this, barrier);
		lcd = new LCDController(this, barrier);
		inputHandler = new InputHandler(this);
		screenFrame = new ScreenFrame(this);
		
		lcdControllerIsIdle = false;

	}
	
	public void start() {
		
		cpu.init();
		
	    Thread cpuThread = new Thread(cpu);
	    Thread lcdControllerThread = new Thread(lcd);

	    cpuThread.start();
	    lcdControllerThread.start();
	    
	}
	
//	public void DMATransfer() {
//		char sourceAddress = (char)(((memory[LCDController.DMA_REGISTER_ADDR] / 0x100) << 8) | 0x0);
//		char destinationAddress = 0xFE00;
//		for(int i = 0; i <= 0x9F; i++){
//			memory[destinationAddress + i] = memory[sourceAddress + i];
//		}
//	}
	
	public void requestInterrupt(Interrupt type){
		
		switch(type){
		case InterruptVBlank:					
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x01, true, HardwareType.Interrupt); break;
		case InterruptLCDC:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x02, true, HardwareType.Interrupt); break;
		case InterruptTimerOverflow:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x04, true, HardwareType.Interrupt); break;
		case InterruptSerialIOTransferComplete:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x08, true, HardwareType.Interrupt); break;
		case InterruptJoypad:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x10, true, HardwareType.Interrupt); break;
		}
		
//		if(cpu.interruptsEnabled())
//			cpu.interrupt(type);
	}
	
	public void stopRequestingInterrupt(Interrupt type){
		
		switch(type){
		case InterruptVBlank:					
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x01, false, HardwareType.Interrupt); break;
		case InterruptLCDC:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x02, false, HardwareType.Interrupt); break;
		case InterruptTimerOverflow:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x04, false, HardwareType.Interrupt); break;
		case InterruptSerialIOTransferComplete:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x08, false, HardwareType.Interrupt); break;
		case InterruptJoypad:
			memory.setMask(INTERRUPT_FLAG_REGISTER_ADDR, (char)0x10, false, HardwareType.Interrupt); break;
		}
		
	}
	
	public void resumeCPUExecution(){
		cpu.resumeExecution();
	}
	
	public synchronized void LCDControllerDidNotifyOfStateCompletion(){
		lcdControllerIsIdle = true;
		notifyAll();
		cpu.setState(CPUState.CPU_STATE_EXECUTING);
	}
	
	public void projectRow(int row, PixelData[] pixelsArray){
		screenFrame.screenPanel.paintRow(row, pixelsArray);
	}
	

	public void setColorPalette(PaletteType type, Color[] colors){
		switch(type){
		case PaletteTypeBackground:
			screenFrame.screenPanel.setBackgroundAndWindowColors(colors[0], colors[1], colors[2], colors[3]);
			break;
		case PaletteTypeObject0:
			screenFrame.screenPanel.setObject0Colors(colors[0], colors[1], colors[2], colors[3]);
			break;
		case PaletteTypeObject1:
			screenFrame.screenPanel.setObject1Colors(colors[0], colors[1], colors[2], colors[3]);
			break;
			default:
				break;
		}
	}
	
	public void setDebugFlag(){
		cpu.debugFlag = true;
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
//		System.out.println("clock cycles: " + this.clockCycles);
		LCDControllerState prevState = lcd.getLCDState();
		LCDControllerState newState = null;
		
		if(clockCycles%LCDController.TOTAL_REFRESH_CYCLES >= LCDController.TOTAL_PRE_VBLANK_CYCLES){
			
			//System.out.println("vblank cycles: " + (clockCycles%LCDController.TOTAL_REFRESH_CYCLES));
			newState = LCDControllerState.LCD_STATE_VBLANK;
			lcd.setLCDState(newState);

			int currentCycles = clockCycles%LCDController.TOTAL_REFRESH_CYCLES;
			if((currentCycles - LCDController.TOTAL_PRE_VBLANK_CYCLES)/LCDController.HORIZONTAL_LINE_CYCLES != old_y){

				if(lcdControllerIsIdle){
					lcdControllerIsIdle = false;
					lcd.run();
				}else{
					cpu.setState(CPUState.CPU_STATE_WAITING);
				}
			}
			
			old_y = ((currentCycles - LCDController.TOTAL_PRE_VBLANK_CYCLES))/456;
			reset = false;
			
			return;
			
		}else{
			if(!reset){
				lcd.resetY();
				reset = true;
			}
			if(clockCycles%LCDController.HORIZONTAL_LINE_CYCLES < LCDController.READING_OAM_ONLY_CYCLES){
				
				newState = LCDControllerState.LCD_STATE_READING_OAM_ONLY;
				
			}else if(clockCycles%LCDController.HORIZONTAL_LINE_CYCLES < (LCDController.READING_OAM_AND_VRAM_CYCLES
					+ LCDController.READING_OAM_ONLY_CYCLES)){
				
				newState = LCDControllerState.LCD_STATE_READING_OAM_AND_VRAM;
				
			}else{
				
				newState = LCDControllerState.LCD_STATE_HBLANK;
				
			}
			
		}
				
		if(prevState != newState) {
			
			if(lcdControllerIsIdle){
				lcd.setLCDState(newState);
				lcdControllerIsIdle = false;
				lcd.run();
			}else{
				cpu.setState(CPUState.CPU_STATE_WAITING);

			}

		}

	}
	
	public void enableLCD(){
		lcd.enableLCD();
	}
	
	public void disableLCD(){
		lcd.disableLCD();
	}

}
