package emulator;

import java.awt.Color;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import emulator.LCDController;

public class GameBoy extends Thread{
	
	int old_y;
	boolean reset = false;
	
	public boolean lcdControllerIsIdle;
	
	private int machineCycles;
	private int clockCycles;
	
	private CPU cpu;
	private LCDController lcd;
	private ScreenFrame screenFrame;
	public Memory memory;
		
	public GameBoy() {
		init();
	}
	
	private void init() {
		
	    final CyclicBarrier barrier = new CyclicBarrier(1);
		
		memory = new Memory(this);
		cpu = new CPU(this, barrier);
		lcd = new LCDController(this, barrier);
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
	
	public synchronized void LCDControllerDidNotifyOfStateCompletion(){
		lcdControllerIsIdle = true;
		notifyAll();
		cpu.setState(CPUState.CPU_STATE_EXECUTING);
	}
	
	public void projectRow(int row, char[] pixelsArray){
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
			
			newState = LCDControllerState.LCD_STATE_VBLANK;
			lcd.setLCDState(newState);

			if((clockCycles%LCDController.TOTAL_REFRESH_CYCLES - LCDController.TOTAL_PRE_VBLANK_CYCLES)/456 > old_y){
				//System.out.println("old_y: " + old_y);
				if(lcdControllerIsIdle){
					lcdControllerIsIdle = false;
					lcd.run();
				}else{
					cpu.setState(CPUState.CPU_STATE_WAITING);

				}
			}
			old_y = ((clockCycles%LCDController.TOTAL_REFRESH_CYCLES - LCDController.TOTAL_PRE_VBLANK_CYCLES))/456;
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
		
		old_y = 0;
		
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
