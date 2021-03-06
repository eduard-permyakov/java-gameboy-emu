package emulator;

import java.util.concurrent.CyclicBarrier;

enum LCDControllerState{
	LCD_STATE_HBLANK,
	LCD_STATE_VBLANK,
	LCD_STATE_READING_OAM_ONLY,
	LCD_STATE_READING_OAM_AND_VRAM
}

enum PaletteType{
	PaletteTypeBackground(0),
	PaletteTypeObject0(1),
	PaletteTypeObject1(2);
	
	private PaletteType(int n) {value = n;}
	private final int value;
}

class PixelData{
	public char color;
	public PaletteType type;
	
	public PixelData(char color, PaletteType type){
		this.color = color;
		this.type = type;
	}
}

public class LCDController extends Thread{
	
	private boolean lcdEnabled = true;
		
	private char y;
	
	private char winPosX;
	private char winPosY;
	
	private char scrollPosX;
	private char scrollPosY;
	
	private char[][] spriteAttsArray;
	private char[][] spritesArray;
	private char[] bgDataArray;
	private PixelData[] linePixelArray;
//	private PaletteType[] linePixelTypeArray;

	private GameBoy gameBoy;
	private CyclicBarrier barrier;
	
	private LCDControllerState state;
	
	public final static int HBLANK_CYCLES					= 204;
	public final static int VBLANK_CYCLES 					= 4560;
	public final static int READING_OAM_ONLY_CYCLES			= 80;
	public final static int READING_OAM_AND_VRAM_CYCLES		= 172;
	public final static int HORIZONTAL_LINE_CYCLES			= 456;
	public final static int TOTAL_REFRESH_CYCLES			= 70224;
	public final static int TOTAL_PRE_VBLANK_CYCLES			= 65664;
	
	public final static char LCDC_REGISTER_ADDR 			= 0xFF40;//LCD Control Register address
	
	public final static char BG_DISPLAY_BIT 				= 0x01;	//0=Off, 1=On
	public final static char OBJ_DISPLAY_ENABLE_BIT 		= 0x02;	//0=Off, 1=On
	public final static char OBJ_SIZE_BIT 					= 0x04;	//0=8x8, 1=8x16
	public final static char BG_TILE_MAP_DISPLAY_SEL_BIT 	= 0x08; //0=9800-9BFF, 1=9C00-9FFF
	public final static char BG_WIN_TILE_DATA_SEL_BIT		= 0x10;	//0=8800-97ff, 1=8000-8FFF
	public final static char WINDOW_DISPLAY_ENABLE_BIT 		= 0x20;	//0=Off, 1=On
	public final static char WIN_TILE_MAP_DISPLAY_SEL_BIT	= 0x40; //0=9800-9BFF, 1=9C00-9FFF
	public final static char LCD_DISPLAY_ENABLE				= 0x80; //0=Off, 1=On (When bit 0 is cleared, the background becomes white)
	
	public final static char LCD_REGISTER_ADDR 				= 0xFF41;//LCD status register address
	
	public final static char MODE_BITS 						= 0x03; //Mode flag(modes 0-3) 	[READ ONLY]
	public final static char COINCIDENCE_FLAG_BIT			= 0X04; //0=LYC<>LY, 1=LYC=LY  	[READ ONLY]
	public final static char MODE_0_H_BLANK_INTERRUPT_BIT	= 0X08; //1=Enable				[R/W]
	public final static char MODE_1_V_BLANK_INTERRUPT_BIT	= 0x10; //1=Enable				[R/W]
	public final static char MODE_2_OAM_INTERRUPT			= 0x20; //1=Enable				[R/W]
	public final static char LYC_EQ_LY_COINCIDEN_INTERRUPT 	= 0X40;	//1=Enable				[R/W]
	
	public final static char SCROLL_Y_REGISTER_ADDR			= 0xFF42;
	public final static char SCROLL_X_REGISTER_ADDR			= 0xFF43;
	
	public final static char LY_REGISTER_ADDR				= 0XFF44;
	public final static char LYC_REGISTER_ADDR				= 0xFF45;
	
	public final static char WY_REGISTER_ADDR				= 0xFF4A;
	public final static char WX_REGISTER_ADDR				= 0XFF4B;
	
	//background palette data
	public final static char BGP_REGISTER_ADDR				= 0xFF47;
	public final static char OBJ0P_REGISTER_ADDR 			= 0xFF48;
	public final static char OBJ1P_REGISTER_ADDR 			= 0xFF49;
	
	public final static char COLOR_NUM_0_BITS				= 0x03;
	public final static char COLOR_NUM_1_BITS				= 0x0C;
	public final static char COLOR_NUM_2_BITS				= 0x30;
	public final static char COLOR_NUM_3_BITS				= 0xC0;
	
	//object palette 0
	public final static char OBP0_REGISTER_ADDR				= 0xFF48;
	//object palette 1
	public final static char OBP1_REGISTER_ADDR				= 0xFF49;
	
	//  Source:      XX00-XX9F   ;XX in range from 00-F1h
	//	Destination: FE00-FE9F
	//  The written value specifies the transfer source address divided by 100h
	public final static char DMA_REGISTER_ADDR				= 0xFF46;
	
	public final static char VRAM_TILE_DATA_START_ADDR		= 0x8000;
	public final static char VRAM_BG_MAPS_ONE_ADDR			= 0x9800;
	public final static char VRAM_BG_MAPS_TWO_ADDR			= 0x9C00;
	
	public final static char SPRITE_ATTR_FLAG_PALETTE_NUM_BIT	= 0x10;
	public final static char SPRITE_ATTR_X_FLIP_BIT				= 0x20;	//0=Normal, 1=Horizontally mirrored
	public final static char SPRITE_ATTR_Y_FLIP_BIT				= 0x40; //0=Normal, 1=Vertically mirrored
	public final static char SPRITE_ATTR_OBJ_TO_BG_PRIO_BIT		= 0x80; //0=OBJ above BG, 1=OBJ behind BG color 1-3
	
	//when sprites with different x coordinate values overlap, the one with the smaller x coordinate (closer to
	
	public LCDController(GameBoy gameBoy, CyclicBarrier barrier){
		this.gameBoy = gameBoy;
		this.barrier = barrier;
		this.state = LCDControllerState.LCD_STATE_READING_OAM_ONLY;
	
		y = 0;
		winPosX = 0;
		winPosY = 0;
		
		scrollPosX = 0;
		scrollPosY = 0;
		
		spriteAttsArray 	= new char[40][4];
		spritesArray 		= new char[384][16];
		bgDataArray 		= new char[1024];
		linePixelArray		= new PixelData[256];
//		linePixelTypeArray 	= new PaletteType[256];
	}
	//TODO:
	/*
	 * Specifies the upper/left positions of the Window area. (The window is 
	 *  an alternate background area which can be displayed above of the normal background.
	 *  OBJs (sprites) may be still displayed above or behinf the window, just as for normal BG.)
	 *  The window becomes visible (if enabled) when positions are set in range WX=0..166,
 	 *  WY=0..143. A postion of WX=7, WY=0 locates the window at upper left, it is then completly 
 	 *  covering normal background.
 	 *  
	 * */
	
	
	public void run(){
		
		if(!lcdEnabled){
			gameBoy.LCDControllerDidNotifyOfStateCompletion();
			return;
		}
		
//		System.out.println("LY: " + Integer.toString(y));
		
//		if(this.state == LCDControllerState.LCD_STATE_VBLANK)
//			System.out.println("---> VBLANK" +"(y: "+(int)y+")"+"(clk: "+gameBoy.getClockCycles()+")");
//		else
//			System.out.println("LCD Controller State: " + this.state +"(y: "+(int)y+")"+"(clk: "+gameBoy.getClockCycles()+")");

		updateScrollValues();
		winPosX = gameBoy.memory.readByte(WX_REGISTER_ADDR);
		
		char statReg = gameBoy.memory.readByte(LCDController.LCD_REGISTER_ADDR);

		switch(this.state){
		case LCD_STATE_HBLANK:
			
//			long endTime = System.nanoTime();
//			long stallTimeNano = Math.max(((CPU.NANOSECONDS_IN_SECOND/CPU.PROCESSOR_DAMPED_FREQUENCY_HZ) 
//					- (endTime - prevTime)), 0);
//			long stallTimeMillis = TimeUnit.MILLISECONDS.convert(stallTimeNano, TimeUnit.NANOSECONDS);
//			
//			try {
//				//Thread.sleep(0);
//				Thread.sleep(stallTimeMillis, (int)(stallTimeNano%1000000));
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			if(y > 143)
//				y = 0;
			
			if(((statReg >> 3) & 0x1) > 0)
				generateStatInterrupt();
			
			makeLinePixelArray();
			gameBoy.projectRow(y, linePixelArray);
			
			y++;
				
			break;
		case LCD_STATE_VBLANK:
			
			if(y == 144)
				gameBoy.requestInterrupt(Interrupt.InterruptVBlank);
			else
				gameBoy.stopRequestingInterrupt(Interrupt.InterruptVBlank);


			if(((statReg >> 4) & 0x1) > 0)
				generateStatInterrupt();
			
//			System.out.println("LY at start of VBLANK: " + (int)y);
			winPosY = gameBoy.memory.readByte(WY_REGISTER_ADDR);
			
			y++;
			
//			if(y > 152)
//				y = 0;
				
			break;
		case LCD_STATE_READING_OAM_ONLY:
			
			if(((statReg >> 5) & 0x1) > 0)
				generateStatInterrupt();
			
			readOAM();
				
			break;
		case LCD_STATE_READING_OAM_AND_VRAM:
			readOAMandVRAM();
				
			break;
			default:

		}
			
		updateLYRegister();
		updateStatusRegister();
		gameBoy.LCDControllerDidNotifyOfStateCompletion();

	}
	
	public void resetY(){
		y = 0;
	}
	
	private void updateScrollValues(){
		scrollPosX = gameBoy.memory.readByte(SCROLL_X_REGISTER_ADDR);
		scrollPosY = gameBoy.memory.readByte(SCROLL_Y_REGISTER_ADDR);
	}
	
	private void updateStatusRegister(){
		//LCD status register
//		gameBoy.memory.setMask(LCD_REGISTER_ADDR, (char)0x80, true, HardwareType.LCDController);
		
//		if(gameBoy.memory.readByte(LCD_REGISTER_ADDR) == y){
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, LYC_EQ_LY_COINCIDEN_INTERRUPT, true, HardwareType.LCDController);
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, COINCIDENCE_FLAG_BIT, true, HardwareType.LCDController);
//		}else{
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, LYC_EQ_LY_COINCIDEN_INTERRUPT, false, HardwareType.LCDController);
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, COINCIDENCE_FLAG_BIT, false, HardwareType.LCDController);
//		}
//		
//		//hblank
//		if(this.state == LCDControllerState.LCD_STATE_HBLANK){
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_0_H_BLANK_INTERRUPT_BIT, true, HardwareType.LCDController);
//		}else{
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_0_H_BLANK_INTERRUPT_BIT, false, HardwareType.LCDController);
//		}
//		
//		//vblank
//		if(this.state == LCDControllerState.LCD_STATE_VBLANK){
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_1_V_BLANK_INTERRUPT_BIT, true, HardwareType.LCDController);
//		}else{
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_1_V_BLANK_INTERRUPT_BIT, false, HardwareType.LCDController);
//		}
//		
//		//oam
//		if(this.state == LCDControllerState.LCD_STATE_READING_OAM_ONLY){
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_2_OAM_INTERRUPT, true, HardwareType.LCDController);
//		}else{
//			gameBoy.memory.setMask(LCD_REGISTER_ADDR, MODE_2_OAM_INTERRUPT, false, HardwareType.LCDController);//if i-enabled
//		}
		
		//mode flag
		char modeFlag = 0;
		switch(this.state){
		case LCD_STATE_HBLANK:
			modeFlag = 0b00;
			break;
		case LCD_STATE_VBLANK:
			modeFlag = 0b01;
			break;
		case LCD_STATE_READING_OAM_ONLY:
			modeFlag = 0b10;
			break;
		case LCD_STATE_READING_OAM_AND_VRAM:
			modeFlag = 0b11;
			break;
		}
		if(!lcdEnabled)
			modeFlag = 0b01;
		gameBoy.memory.setMask(LCD_REGISTER_ADDR, (char)0b11, false, HardwareType.LCDController);
		gameBoy.memory.setMask(LCD_REGISTER_ADDR, modeFlag, true, HardwareType.LCDController);
	}
	
	private void updateLYRegister() {
//		System.out.println(Integer.toString(y));
//		System.out.println("update ly: " + (int)y);
		gameBoy.memory.writeByte(LY_REGISTER_ADDR, y, HardwareType.LCDController);
	}
	
	private void generateStatInterrupt(){
		gameBoy.requestInterrupt(Interrupt.InterruptLCDC);
	}
	
	private void readOAM(){
		final char baseAddressOAM = Memory.SPRITE_ATTRIB_MEMORY_ADDR;
		for(int i = 0; i < 40; i++){
			for(int j = 0; j < 4; j++){
				spriteAttsArray[i][j] = gameBoy.memory.readByte(baseAddressOAM + 4*i + j);
			}
		}
		
	}
	
	private void readOAMandVRAM(){
		
		final char baseAddressVRAM = Memory.EIGHT_KB_VIDEO_RAM_ADDR;
		for(int i = 0; i < 384; i++){
			for(int j = 0; j < 16; j++){
				spritesArray[i][j] = gameBoy.memory.readByte(baseAddressVRAM + 16*i + j);
			}
		}
		
		//TODO: here it is possible to select one of two maps based on register values
		for(int i = 0; i < 1024; i++){
//			if(gameBoy.memory[0x9800 + i] != 0){
//				System.out.println("sending: " + gameBoy.memory[0x9800 + i] +" to index: " + i);
//			}
			bgDataArray[i] = gameBoy.memory.readByte(0x9800 + i);
		}
		
	}
	
	/*
	 * 	As it was said before, there are two Tile Pattern
 		Tables at $8000-8FFF and at $8800-97FF
 		(use this fact for determining the PixelType)
	 * */
	
	//bugged
	private void makeLinePixelArray(){
		//TODO: diff. tile addressing if it's BG #2
		final int bgTileBaseIndex = ((int)((scrollPosY+y)/8))*32;
		final int yCoordinate = (scrollPosY+y)%8;
		
		//first "draw" the background tiles
		for(int i = 0; i < 32; i++){

			char[] sprite = spritesArray[bgDataArray[(bgTileBaseIndex+i)]]; //....
			int rowBitSequence = ((sprite[2*yCoordinate] << 8) | sprite[2*yCoordinate+1]);

//			System.out.println(String.format("%16s", Integer.toBinaryString(rowBitSequence)).replace(' ', '0'));
			for(int j = 0; j < 8; j++){
				final char color = (char)((rowBitSequence >> (7-j)) & 0b11);
//				System.out.println("color " +j + ": " + String.format("%2s", Integer.toBinaryString(color)).replace(' ', '0'));
				linePixelArray[8*i+j] = new PixelData(color, PaletteType.PaletteTypeBackground);
			}
			
//			if(rowBitSequence > 0)
//				System.out.println("break");
		}
		
		//now go through all the objects
		for(int i = 0; i < 40; i++){
			char[] spriteAtts = spriteAttsArray[i];

			char y = (char) (spriteAtts[0] - 16);
			char x = (char) (spriteAtts[1] - 8);
			char index = spriteAtts[2];
			char flags = spriteAtts[3];//TODO
			int lineIndex = this.y - y;
			
			if(lineIndex < 0 || lineIndex >= 8)
				continue; 
			
			//this hides the sprite
			if(y == 0 || x == 0)
				continue;
			
			//gameBoy.setDebugFlag();
			
			char[] sprite = spritesArray[index];
			int rowBitSequence = ((sprite[2*lineIndex] << 8) | sprite[2*lineIndex+1]);

			for(int j = 0; j < 8; j++){
				final char color = (char)((rowBitSequence >> (7-j)) & 0b11);
				//TODO: resolve < 256
				if(color != 0 && (x+j) < 256) {//color 0 is transparent for sprites
					linePixelArray[x+j].color = color;
					linePixelArray[x+j].type = PaletteType.PaletteTypeObject0; //TODO: also might be obj1
				}
			}
		}
		
	}
	
	private void performVBlank(){
		//w/e happens during vblank
	}
	
	public void setLCDState(LCDControllerState state){
		this.state = state;
	}
	
	public LCDControllerState getLCDState(){
		return this.state;
	}
	
	public void enableLCD(){
		System.out.println("enable lcd");
		this.lcdEnabled = true;
	}
	
	public void disableLCD(){
		System.out.println("disable lcd");
		this.lcdEnabled = false;
		y = 0;
		updateLYRegister();
	}

}
