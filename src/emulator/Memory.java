package emulator;

import java.awt.Color;
import java.util.Arrays;
import emulator.LCDController;

enum HardwareType {
	CPU,
	LCDController,
	Joypad,
	ROMLoader,
	Memory
}
//TODO: control read access based on type...

enum MBC1MaxMemMode {
    SixteenEightMode, 
    FourThirtyTwoMode;
}


public class Memory {
	
	private int memoryBankingMode;	//TODO: make this cleaner with enum & stuff
	
	//mbc1 - improve this...
	private MBC1MaxMemMode 	mbc1Mode;
	private boolean 		mbcRAM1enabled;
	private char[][] 		mbc1Banks;
	private char 			currentRomBankAddr;
	private final char		mbc1Offset = 0x4000;
	
	private char[] memory;
	private GameBoy gameBoy;
	
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
	

	public Memory(GameBoy gameBoy){
		this.memory = new char[65536];
		this.gameBoy = gameBoy;
		
		//set all lines of the joypad register high initially
		memory[InputHandler.JOYPAD_ADDR] = (char)0xFF;

		//mbc1
		mbc1Banks = new char[32][0x4000];
		mbc1Mode = MBC1MaxMemMode.SixteenEightMode; //default
		mbcRAM1enabled = false; //default
	}
	
	public void setMemoryBankingMode(int mode){
		this.memoryBankingMode = mode;
	}
	
	public void writeROMByte(int address, char data){
		switch(this.memoryBankingMode){
			case 0:	this.memory[address] = data; break;
			case 1: {
				if(address < 0x4000){
					this.memory[address] = data;
				}else{
					int bankIndex = (address / 0x4000) - 1;
					mbc1Banks[bankIndex][address - (bankIndex + 1) * 0x4000] = data;
				}
				break;
			}				
		}
	}
	
	public void writeByte(int address, char data, HardwareType type){
		 
		//This is where we can select the rom bank mode for MBC1
		switch(this.memoryBankingMode){
		case 0: break;
		case 1:{
			if(address >= 0x0000 && address <= 0x7FFF){
				if(address >= 0x0000 && address <= 0x1FFF){
					
					if(this.mbc1Mode == MBC1MaxMemMode.FourThirtyTwoMode){
						switch(data & 0xF){	//TODO: figure out ram in mbc1
						case 0b1010:	this.mbcRAM1enabled = true;		break;
						default: 		this.mbcRAM1enabled = false;	break;
						}
					}
					return;

				}else if(address >= 0x2000 && address <= 0x3FFF){
					
					System.out.println("will select an appropriate ROM bank at 4000-7FFF");
					// will select an appropriate ROM bank at 4000-7FFF
					char bankAddr = (char) (data & 0x1F);
					if(bankAddr == 0x00)	bankAddr = 0x01;
					if(bankAddr == 0x20)	bankAddr = 0x21;
					if(bankAddr == 0x40)	bankAddr = 0x41;
					if(bankAddr == 0x60)	bankAddr = 0x61;
					this.currentRomBankAddr = bankAddr;
					
				}else if(address >= 0x4000 && address <= 0x5FFF){
					
					// will select an appropriate RAM bank at A000-C000
					if(this.mbc1Mode == MBC1MaxMemMode.FourThirtyTwoMode){
						this.currentRomBankAddr = (char)(data & 0b11);
					}
					
					// set the two most significant ROM address lines
					if(this.mbc1Mode == MBC1MaxMemMode.SixteenEightMode){
						char twoBits = (char)(data & 0b11);
						this.currentRomBankAddr &= ~0b110000;		//double check this
						this.currentRomBankAddr |= (twoBits << 4);
					}
					
				}else if(address >= 0x6000 && address <= 0x7FFF){
					switch(data & 0x1){
					case 0: this.mbc1Mode = MBC1MaxMemMode.SixteenEightMode;System.out.println("16-8 mode");	break;
					case 1: this.mbc1Mode = MBC1MaxMemMode.FourThirtyTwoMode;System.out.println("4-32 mode");	break;
					}
				}
				return;
			}
	
		}
		default: break;//add the rest of the mem. banks at some point
		}
		
		//restrict which bits can be written to by diff. hardware for the joypad register
		if(address == InputHandler.JOYPAD_ADDR){
			switch(type){
			
			case CPU:
				memory[address] = (char) ((char)(~0x30 & memory[address]));
				memory[address] = (char) ((char)(data & 0x30) | memory[address]);
			case LCDController:	break;
			case Joypad:
				memory[address] = (char) ((char)(~0xCF & memory[address]));
				memory[address] = (char) ((char)(data & 0xCF) | memory[address]);
			case ROMLoader:		break;
			default:			break;
			}
			return;
		}
		
		memory[address] = data;			
		
		//echo the 8kb internal RAM
		if(address >= 0xC000 && address <  0xE000){
			int echoAddress = (address + 0x2000);
//			memory[echoAddress] = data; 
			writeByte(echoAddress, data, HardwareType.Memory);
		}
		
		//actions based on specific register addresses
		if(address == LCDController.DMA_REGISTER_ADDR){
			DMATransfer();
		}
		
		//set background palette
		if(address == LCDController.BGP_REGISTER_ADDR){
			char color1val = (char)(memory[address] & 0b11);
			char color2val = (char)((memory[address] >> 2) & 0b11);
			char color3val = (char)((memory[address] >> 4) & 0b11);
			char color4val = (char)((memory[address] >> 6) & 0b11);
			char[] colorValsArray = {color1val, color2val, color3val, color4val};
			Color[] colorsArray = new Color[4];
			
			for(int i = 0; i < 4; i++){
				char colorVal = colorValsArray[i];
				Color color = null;
				switch(colorVal){
				case 0b00:
					color = Color.white;
					break;
				case 0b01:
					color = Color.lightGray;
					break;
				case 0b10:
					color = Color.darkGray;
					break;
				case 0b11:
					color = Color.black;
					break;
					default:
						break;
				
				}
				colorsArray[i] = color;
			}
			gameBoy.setColorPalette(PaletteType.PaletteTypeBackground, colorsArray);
			
		}
		
		//object palette 0; values of 0 transparent
		if(address == LCDController.OBJ0P_REGISTER_ADDR){
			
			char color1val = memory[address];
			char color2val = (char)(memory[address] >> 2);
			char color3val = (char)(memory[address] >> 4);
			char color4val = (char)(memory[address] >> 6);
			char[] colorValsArray = {color1val, color2val, color3val, color4val};
			Color[] colorsArray = new Color[4];
			
			for(int i = 0; i < 4; i++){
				char colorVal = colorValsArray[i];
				Color color = null;
				switch(colorVal){
				case 0b00:
					color = Color.white;
					break;
				case 0b01:
					color = Color.lightGray;
					break;
				case 0b10:
					color = Color.darkGray;
					break;
				case 0b11:
					color = Color.black;
					break;
					default:
						break;
				
				}
				colorsArray[i] = color;
			}
			gameBoy.setColorPalette(PaletteType.PaletteTypeObject0, colorsArray);
		}
		
		//object palette 1; valeus of 0 transparent
		if(address == LCDController.OBJ1P_REGISTER_ADDR){
			
			char color1val = memory[address];
			char color2val = (char)(memory[address] >> 2);
			char color3val = (char)(memory[address] >> 4);
			char color4val = (char)(memory[address] >> 6);
			char[] colorValsArray = {color1val, color2val, color3val, color4val};
			Color[] colorsArray = new Color[4];
			
			for(int i = 0; i < 4; i++){
				char colorVal = colorValsArray[i];
				Color color = null;
				switch(colorVal){
				case 0b00:
					color = Color.white;
					break;
				case 0b01:
					color = Color.lightGray;
					break;
				case 0b10:
					color = Color.darkGray;
					break;
				case 0b11:
					color = Color.black;
					break;
					default:
						break;
				
				}
				colorsArray[i] = color;
			}
			gameBoy.setColorPalette(PaletteType.PaletteTypeObject1, colorsArray);
		}
	}
	
	public void setMask(int address,char mask, boolean bit, HardwareType type){
		if(address == InputHandler.JOYPAD_ADDR){
			System.out.println("writing!!!!");
		}
		if(bit == true){
			memory[address] |= (mask & 0xFF);
		}else{
			memory[address] &= ~(mask & 0xFF);
		}
	}
	
	public char[] readContiguousBlock(int startAddress, int endAddress){
		return Arrays.copyOfRange(memory, startAddress, endAddress);
	}

	public char readByte(int address){
		switch(this.memoryBankingMode){
		case 0: break;
		case 1: {
			if(mbc1Mode == MBC1MaxMemMode.FourThirtyTwoMode){
				return (char)(mbc1Banks[currentRomBankAddr][address - mbc1Offset] & 0xFF);
			}
			break;
		}
		default: break;
		}
			
		return (char)(memory[address] & 0xFF);
	}
	
	public void DMATransfer() {
		char sourceAddress = (char)(((memory[LCDController.DMA_REGISTER_ADDR] / 0x100) << 8) | 0x0);
		char destinationAddress = 0xFE00;
		for(int i = 0; i <= 0x9F; i++){
			this.writeByte(destinationAddress + i, memory[sourceAddress + i], HardwareType.Memory);
//			memory[destinationAddress + i] = memory[sourceAddress + i];
		}
	}
	
}
