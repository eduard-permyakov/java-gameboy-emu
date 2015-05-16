package emulator;

public class CPU {
	
	private GameBoy gameBoy;
	
	private char M; //machine cycles
	private char T;	//clock cycles
	
	private char[] registers;
	
	private final static int INDEX_A = 0;
	private final static int INDEX_B = 1;
	private final static int INDEX_C = 2;
	private final static int INDEX_D = 3;
	private final static int INDEX_E = 4;
	private final static int INDEX_H = 5;
	private final static int INDEX_L = 6;
	private final static int INDEX_F = 7;
	
	private char pc;	//Program Counter
	private char sp;	//Stack Pointer
	
	private final static char ZERO_BIT = 0x80;
	private final static char OP_BIT = 0x40;
	private final static char HALF_CARRY_BIT = 0x20;
	private final static char CARRY_BIT = 0x10;
		
	private char currentOpcode;
	
	public CPU(GameBoy gameBoy) {
		this.gameBoy = gameBoy;
		init();
	}
	
	public void fetchNextOpcode(){
		currentOpcode = gameBoy.memory[pc];
		pc += 2;
	}
	
	public void decodeAndExecuteOpcode(){
		switch(currentOpcode & 0xFF){
		
		/****************************/
		/******* 8 BIT LOADS ********/
		/****************************/
		
		case 0x06:{
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_B] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x0E:{
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_C] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x16: {
			
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_D] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1E: {
			
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_E] = immediate;
			
			M += 2;
			T += 8;
			break;
			
		}
		
		case 0x26: {
			
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_H] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2E: {
			
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			
			registers[INDEX_L] = immediate;
			
			M += 2;
			T += 8;
			
		}
		
		case 0x7F: {
			registers[INDEX_A] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x78: {
			registers[INDEX_A] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x79: {
			registers[INDEX_A] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x7A: {
			registers[INDEX_A] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x7B: {
			registers[INDEX_A] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x7C: {
			registers[INDEX_A] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x7D: {
			registers[INDEX_A] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x7E: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x40: {
			registers[INDEX_B] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x41: {
			registers[INDEX_B] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x42: {
			registers[INDEX_B] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x43: {
			registers[INDEX_B] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x45: {
			registers[INDEX_B] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x46: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_B] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x48: {
			registers[INDEX_C] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x49: {
			registers[INDEX_C] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4A: {
			registers[INDEX_C] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4B: {
			registers[INDEX_C] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4C: {
			registers[INDEX_C] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4D: {
			registers[INDEX_C] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4E: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_C] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x50: {
			registers[INDEX_D] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x51: {
			registers[INDEX_D] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x52: {
			registers[INDEX_D] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x53: {
			registers[INDEX_D] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x54: {
			registers[INDEX_D] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x55: {
			registers[INDEX_D] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x56: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_D] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x58: {
			registers[INDEX_E] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x59: {
			registers[INDEX_E] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5A: {
			registers[INDEX_E] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5B: {
			registers[INDEX_E] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5C: {
			registers[INDEX_E] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5D: {
			registers[INDEX_E] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5E: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_E] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x60: {
			registers[INDEX_H] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x61: {
			registers[INDEX_H] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x62: {
			registers[INDEX_H] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x63: {
			registers[INDEX_H] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x64: {
			registers[INDEX_H] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x65: {
			registers[INDEX_H] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x66: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_H] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x68: {
			registers[INDEX_L] = registers[INDEX_B];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x69: {
			registers[INDEX_L] = registers[INDEX_C];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x6A: {
			registers[INDEX_L] = registers[INDEX_D];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x6B: {
			registers[INDEX_L] = registers[INDEX_E];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x6C: {
			registers[INDEX_L] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x6D: {
			registers[INDEX_L] = registers[INDEX_L];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		
		case 0x6E: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_L] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x70: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_B];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x71: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_C];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x72: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_D];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x73: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_E];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x74: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_H];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x75: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_L];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x36: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = gameBoy.memory[pc];
			pc += 2;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x0A: {
			char address = (char)((registers[INDEX_B] << 8) & registers[INDEX_C]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1A: {
			char address = (char)((registers[INDEX_D] << 8) & registers[INDEX_E]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xFA: {
			char address = (char)(gameBoy.memory[pc] & (gameBoy.memory[pc+2] << 8));
			pc += 4;
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0x3E: {
			char immediate = (char)(gameBoy.memory[pc] & 0xFF);
			pc += 2;
			registers[INDEX_A] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x47: {
			registers[INDEX_A] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x4F: {
			registers[INDEX_B] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x57: {
			registers[INDEX_D] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x5F: {
			registers[INDEX_E] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x67: {
			registers[INDEX_H] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x6F: {
			registers[INDEX_L] = registers[INDEX_A];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x02: {
			char address = (char)((registers[INDEX_B] << 8) & registers[INDEX_C]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x12: {
			char address = (char)((registers[INDEX_E] << 8) & registers[INDEX_E]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x77: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xEA: {
			char address = (char)(gameBoy.memory[pc] & (gameBoy.memory[pc+2] << 8));
			pc += 4;
			gameBoy.memory[address] = registers[INDEX_A];
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xF2: {
			char address = (gameBoy.memory[0xFF00 + registers[INDEX_C]]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE2: {
			char address = (gameBoy.memory[0xFF00 + registers[INDEX_C]]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x3A: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			address--;
			registers[INDEX_H] = (char)((address >> 8) & 0xFF);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x32: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			address--;
			registers[INDEX_H] = (char)((address >> 8) & 0xFF);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2A: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			address++;
			registers[INDEX_H] = (char)((address >> 8) & 0xFF);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x22: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			gameBoy.memory[address] = registers[INDEX_A];
			
			address++;
			registers[INDEX_H] = (char)((address >> 8) & 0xFF);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE0: {
			char immediate = gameBoy.memory[pc];
			pc += 2;
			
			gameBoy.memory[0xFF00 + immediate] = registers[INDEX_A];
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xF0: {
			char immediate = gameBoy.memory[pc];
			pc += 2;
			
			registers[INDEX_A] = gameBoy.memory[0xFF00 + immediate];
			
			M += 3;
			T += 12;
			
			break;
		}
		
		/****************************/
		/******* 16 BIT LOADS *******/
		/****************************/
		
		case 0x01: {
			char immediateLS = gameBoy.memory[pc];
			pc += 2;
			char immediateMS = gameBoy.memory[pc];
			pc +=2;
			
			registers[INDEX_B] = immediateMS;
			registers[INDEX_C] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x11: {
			char immediateLS = gameBoy.memory[pc];
			pc += 2;
			char immediateMS = gameBoy.memory[pc];
			pc +=2;
			
			registers[INDEX_D] = immediateMS;
			registers[INDEX_E] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x21: {
			char immediateLS = gameBoy.memory[pc];
			pc += 2;
			char immediateMS = gameBoy.memory[pc];
			pc +=2;
			
			registers[INDEX_H] = immediateMS;
			registers[INDEX_L] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x31: {
			char immediateLS = gameBoy.memory[pc];
			pc += 2;
			char immediateMS = gameBoy.memory[pc];
			pc +=2;
			
			sp = (char)((immediateMS << 8) & immediateLS);
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xF9: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			sp = address;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xF8: {
			char immediate = gameBoy.memory[pc];
			pc += 2;
			char address = (char)(sp + immediate);
			
			registers[INDEX_H] = (char)(address << 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			//reset Z flag
			registers[INDEX_F] = (char) (registers[INDEX_F] & ~ZERO_BIT);
			//reset N flag
			registers[INDEX_F] = (char) (registers[INDEX_F] & ~OP_BIT);
			//set H
			if((immediate + (sp & 0xFF)) > 0xFF)
				registers[INDEX_H] = (char)(registers[INDEX_F] & HALF_CARRY_BIT);
			else
				registers[INDEX_H] = (char)(registers[INDEX_F] & ~HALF_CARRY_BIT);
			//set C
			if((int)(immediate + (sp)) > 0xFFFF)
				registers[INDEX_H] = (char)(registers[INDEX_F] & CARRY_BIT);
			else
				registers[INDEX_H] = (char)(registers[INDEX_F] & ~CARRY_BIT);

			
			M += 3;
			T += 12;

			
			break;
		}
		
		case 0x08: {
			char immediateLS = gameBoy.memory[pc];
			pc += 2;
			char immediateMS = gameBoy.memory[pc];
			pc +=2;
			
			char address = (char)((immediateMS << 8) & immediateLS);
			
			gameBoy.memory[address] = (char)(sp & 0xFF);
			gameBoy.memory[address+1] = (char)(sp >> 8);
			
			M += 5;
			T += 20;
			
			break;
		}
		
			default:
				System.err.println("Unsupported Opcode!");
				System.exit(0);
		}
	}
	
	private void init() {
		
		registers = new char[8];
		
		//The entry point of the program
		pc = 0x0100;
		sp = 0xFFFE;
	}
	
	
	private void reset(){
		for(char c : registers){
			c = 0x0000;
		}
		pc = 0;
		sp = 0;
		
		M = 0;
		T = 0;
	}
	
}
