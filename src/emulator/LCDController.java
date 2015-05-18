package emulator;

public class LCDController {
	
	private GameBoy gameBoy;
	
	public final static char LCDC_ADDR = 0xFF40; 					//LCD Control Register address
	
	public final static char BG_DISPLAY_BIT 				= 0x01;	//0=Off, 1=On
	public final static char OBJ_DISPLAY_ENABLE_BIT 		= 0x02;	//0=Off, 1=On
	public final static char OBJ_SIZE_BIT 					= 0x04;	//0=8x8, 1=8x16
	public final static char BG_TILE_MAP_DISPLAY_SEL_BIT 	= 0x08; //0=9800-9BFF, 1=9C00-9FFF
	public final static char BG_WIN_TILE_DATA_SEL_BIT		= 0x10;	//0=8800-97ff, 1=8000-8FFF
	public final static char WINDOW_DISPLAY_ENABLE_BIT 		= 0x20;	//0=Off, 1=On
	public final static char WIN_TILE_MAP_DISPLAY_SEL_BIT	= 0x40; //0=9800-9BFF, 1=9C00-9FFF
	public final static char LCD_DISPLAY_ENABLE				= 0x80; //0=Off, 1=On (When bit 0 is cleared, the background becomes white)
	
	public final static char LCD_ADDR = 0xFF41; 					//LCD status register address
	
	public final static char MODE_BITS 						= 0x03; //Mode flag(modes 0-3) 	[READ ONLY]
	public final static char COINCIDENCE_FLAG_BIT			= 0X04; //0=LYC<>LY, 1=LYC=LY  	[READ ONLY]
	public final static char MODE_0_H_BLANK_INTERRUPT_BIT	= 0X08; //1=Enable				[R/W]
	public final static char MODE_1_V_BLANK_INTERRUPT_BIT	= 0x10; //1=Enable				[R/W]
	public final static char MODE_2_OAM_INTERRUPT			= 0x20; //1=Enable				[R/W]
	public final static char LYC_EQ_LY_COINCIDEN_INTERRUPT 	= 0X40;	//1=Enable				[R/W]
	
	public final static char SCROLL_X_REGISTER_ADDR			= 0xFF42;
	public final static char SCROLL_Y_REGISTER_ADDR			= 0xFF43;
	
	public final static char LY_REGISTER_ADDR				= 0XFF44;
	public final static char LYC_REGISTER_ADDR				= 0xFF45;
	
	public final static char WY_REGISTER_ADDR				= 0xFF4A;
	public final static char WX_REGISTER_ADDR				= 0XFF4B;
	
	//background palette data
	public final static char BGP_REGISTER_ADDR				= 0xFF47;
	
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
	
	public LCDController(GameBoy gameBoy){
		this.gameBoy = gameBoy;
	}

}
