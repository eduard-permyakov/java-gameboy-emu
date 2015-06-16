package emulator;

import java.awt.Color;
import java.util.Arrays;
import emulator.LCDController;

public class Memory {
	
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
		memory = new char[65536];
		this.gameBoy = gameBoy;
	}
	
	public void writeByte(int address, char data){
		
		memory[address] = data;
		
		//echo the 8kb internal RAM
		if(address >= 0xC000 && address <  0xE000){
			char echoAddress = (char)(address + 0x2000);
			memory[echoAddress] = data; 
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
	
	public void setMask(int address,char mask, boolean bit){
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
		return (char)(memory[address] & 0xFF);
	}
	
	public void DMATransfer() {
		char sourceAddress = (char)(((memory[LCDController.DMA_REGISTER_ADDR] / 0x100) << 8) | 0x0);
		char destinationAddress = 0xFE00;
		for(int i = 0; i <= 0x9F; i++){
			memory[destinationAddress + i] = memory[sourceAddress + i];
		}
	}
	
}
