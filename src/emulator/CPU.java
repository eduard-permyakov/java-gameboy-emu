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
		pc += 1;
	}
	
	public void decodeAndExecuteOpcode(){
		System.out.println("opcode: " + Integer.toHexString(currentOpcode).toUpperCase());
		
		switch(currentOpcode){
		
		/****************************/
		/******* 8 BIT LOADS ********/
		/****************************/
		
		case 0x00: {
			M += 1;
			T += 4;
			break;
		}
		
		case 0x06:{
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			registers[INDEX_B] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x0E:{
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			registers[INDEX_C] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x16: {
			
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			registers[INDEX_D] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1E: {
			
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			registers[INDEX_E] = immediate;
			
			M += 2;
			T += 8;
			break;
			
		}
		
		case 0x26: {
			
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			registers[INDEX_H] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2E: {
			
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			registers[INDEX_L] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x70: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_B]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x71: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_C]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x72: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_D]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x73: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_E]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x74: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_H]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x75: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_L]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x36: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, gameBoy.memory[pc]);
			pc += 1;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x0A: {
			char address = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1A: {
			char address = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xFA: {
			char address = (char)(gameBoy.memory[pc] | (gameBoy.memory[pc+1] << 8));
			pc += 2;
			registers[INDEX_A] = gameBoy.memory[address];
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0x3E: {
			char immediate = gameBoy.memory[pc];
			pc += 1;
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
			char address = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			writeByteToMemory(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x12: {
			char address = (char)((registers[INDEX_E] << 8) | registers[INDEX_E]);
			writeByteToMemory(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x77: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xEA: {
			char address = (char)(gameBoy.memory[pc] | (gameBoy.memory[pc+1] << 8));
			pc += 2;
			writeByteToMemory(address, registers[INDEX_A]);
			
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
			writeByteToMemory(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x3A: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			address--;
			registers[INDEX_H] = (char)((address >> 8) & 0xF);
			registers[INDEX_L] = (char)(address & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x32: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_A]);
			
			address--;
			registers[INDEX_H] = (char)((address >> 8) & 0xF);
			registers[INDEX_L] = (char)(address & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2A: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory[address];
			
			address++;
			registers[INDEX_H] = (char)((address >> 8) & 0xF);
			registers[INDEX_L] = (char)(address & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x22: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			writeByteToMemory(address, registers[INDEX_A]);
			
			address++;
			registers[INDEX_H] = (char)((address >> 8) & 0xF);
			registers[INDEX_L] = (char)(address & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE0: {
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
			gameBoy.memory[0xFF00 + immediate] = registers[INDEX_A];
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xF0: {
			char immediate = gameBoy.memory[pc];
			pc += 1;
			
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
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			registers[INDEX_B] = immediateMS;
			registers[INDEX_C] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x11: {
			char immediateLS = gameBoy.memory[pc];
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			registers[INDEX_D] = immediateMS;
			registers[INDEX_E] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x21: {
			char immediateLS = gameBoy.memory[pc];
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			registers[INDEX_H] = immediateMS;
			registers[INDEX_L] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x31: {
			char immediateLS = gameBoy.memory[pc];
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			sp = (char)((immediateMS << 8) | immediateLS);
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xF9: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			sp = address;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xF8: {
			char immediate = gameBoy.memory[pc];
			pc += 1;
			char address = (char)(sp + immediate);
			
			registers[INDEX_H] = (char)(address << 8);
			registers[INDEX_L] = (char)(address & 0xF);
			
			//reset Z flag
			registers[INDEX_F] &= ~ZERO_BIT;
			//reset N flag
			registers[INDEX_F] &= ~OP_BIT;
			//set H
			if((immediate + (sp & 0xF)) > 0xF)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			//set C
			if((int)(immediate + (sp)) > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;

			
			M += 3;
			T += 12;

			
			break;
		}
		
		case 0x08: {
			char immediateLS = gameBoy.memory[pc];
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			char address = (char)((immediateMS << 8) | immediateLS);
			
			gameBoy.memory[address] = (char)(sp & 0xF);
			gameBoy.memory[address+1] = (char)(sp >> 8);
			
			M += 5;
			T += 20;
			
			break;
		}
		
		case 0xF5: {
			
			gameBoy.memory[sp] = registers[INDEX_F];
			sp --;
			gameBoy.memory[sp] = registers[INDEX_A];
			sp --;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC5: {
			
			gameBoy.memory[sp] = registers[INDEX_C];
			sp --;
			gameBoy.memory[sp] = registers[INDEX_B];
			sp --;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xD5: {
			
			gameBoy.memory[sp] = registers[INDEX_E];
			sp --;
			gameBoy.memory[sp] = registers[INDEX_D];
			sp --;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xE5: {
			
			gameBoy.memory[sp] = registers[INDEX_L];
			sp --;
			gameBoy.memory[sp] = registers[INDEX_H];
			sp --;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xF1: {
			
			registers[INDEX_F] = gameBoy.memory[sp];
			sp ++;
			registers[INDEX_A] = gameBoy.memory[sp];
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC1: {
			
			registers[INDEX_C] = gameBoy.memory[sp];
			sp ++;
			registers[INDEX_B] = gameBoy.memory[sp];
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xD1: {
			
			registers[INDEX_E] = gameBoy.memory[sp];
			sp ++;
			registers[INDEX_D] = gameBoy.memory[sp];
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xE1: {
			
			registers[INDEX_L] = gameBoy.memory[sp];
			sp ++;
			registers[INDEX_H] = gameBoy.memory[sp];
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		/****************************/
		/******** 8 BIT ALU *********/
		/****************************/
		
		case 0x87: {
			int result = ((registers[INDEX_A] + registers[INDEX_A]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_A] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x80: {
			int result = ((registers[INDEX_A] + registers[INDEX_B]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_B] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x81: {
			int result = ((registers[INDEX_A] + registers[INDEX_C]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_C] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x82: {
			int result = ((registers[INDEX_A] + registers[INDEX_D]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_D] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x83: {
			int result = ((registers[INDEX_A] + registers[INDEX_E]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_E] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x84: {
			int result = ((registers[INDEX_A] + registers[INDEX_H]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_H] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x85: {
			int result = ((registers[INDEX_A] + registers[INDEX_L]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (registers[INDEX_L] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x86: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int result = ((registers[INDEX_A] + gameBoy.memory[address]));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (gameBoy.memory[address] &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xC6: {
			char immediate = gameBoy.memory[pc];
			pc += 1;
			int result = ((registers[INDEX_A] + immediate));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0x7) + (immediate &0x7) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x8F: {
			int result = (registers[INDEX_A] + registers[INDEX_A] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_A] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x88: {
			int result = (registers[INDEX_A] + registers[INDEX_B] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_B] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x89: {
			int result = (registers[INDEX_A] + registers[INDEX_C] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_C] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8A: {
			int result = (registers[INDEX_A] + registers[INDEX_D] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_D] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8B: {
			int result = (registers[INDEX_A] + registers[INDEX_E] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_E] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8C: {
			int result = (registers[INDEX_A] + registers[INDEX_H] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_H] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8D: {
			int result = (registers[INDEX_A] + registers[INDEX_L] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (registers[INDEX_L] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8E: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]); 
			int result = (registers[INDEX_A] + gameBoy.memory[address] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (gameBoy.memory[address] &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0xCE: {
			char immediate = gameBoy.memory[pc]; 
			pc += 1;
			int result = (registers[INDEX_A] + immediate + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0x7) + (immediate &0x7) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0x7)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] &= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		//TODO:
		//LEFT OFF AT SUBS
		
		//just implement what I need atm
		case 0xC3: {
			char immediateLS = gameBoy.memory[pc];
			pc += 1;
			char immediateMS = gameBoy.memory[pc];
			pc +=1;
			
			char address = (char)((immediateMS << 8) | immediateLS);
			pc = address;
			
			M += 4;
			T += 16;
						
			break;
 		}
		
		case 0xAF: {
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_A]) & 0xF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= OP_BIT;
			registers[INDEX_F] &= HALF_CARRY_BIT;
			registers[INDEX_F] &= CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x05: {
			char result = (char) (registers[INDEX_B]-1);
			
			if(registers[INDEX_B] == 0)
				registers[INDEX_F] &= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			if((registers[INDEX_B] & 0x7) == 0)
				registers[INDEX_F] &= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			return;
		}
		
		case 0x20: {
			byte signedImmediate = (byte)(gameBoy.memory[pc]);
			pc += 1;
			
			//check not zero flag
			if(((registers[INDEX_F] & ZERO_BIT) >> 4) == 0){
				pc = (char)(pc + signedImmediate);
			}
			
			break;
		}
		
			default:
				System.err.println("Unsupported Opcode!");
				System.exit(0);
		}
	}
	
	void writeByteToMemory(char address, char data){
		gameBoy.memory[address] = data;
		
		//echo the 8kb internal RAM
		if(address >= 0xC000 && address <  0xE000){
			char echoAddress = (char)(address + 0x2000);
			gameBoy.memory[echoAddress] = data; 
		}
		
	}
	
	private void init() {
		
		registers = new char[8];
		
		//The entry point of the program
		pc = 0x0100;
		sp = 0xFFFE;
		
		//initial data loaded into RAM/registers
		registers[INDEX_A] = 0x01;	registers[INDEX_F] = 0xB0;
		registers[INDEX_B] = 0x00;	registers[INDEX_C] = 0x13;
		registers[INDEX_D] = 0x00;	registers[INDEX_E] = 0xD8;
		registers[INDEX_L] = 0x01;	registers[INDEX_L] = 0x4D;
		
		gameBoy.memory[0xFF05] = 0x00;	//TIMA
		gameBoy.memory[0xFF06] = 0x00;	//TMA
		gameBoy.memory[0xFF07] = 0x00;	//TAC
		gameBoy.memory[0xFF10] = 0x80;	//NR10
		gameBoy.memory[0xFF11] = 0xBF;	//NR11
		gameBoy.memory[0xFF12] = 0xF3;	//NR12
		gameBoy.memory[0xFF14] = 0xBF;	//NR14
		gameBoy.memory[0xFF16] = 0x3F;	//NR21
		gameBoy.memory[0xFF17] = 0x00;	//NR22
		gameBoy.memory[0xFF19] = 0xBF;	//NR24
		gameBoy.memory[0xFF1A] = 0x7F;	//NR30
		gameBoy.memory[0xFF1B] = 0xFF;	//NR31
		gameBoy.memory[0xFF1C] = 0x9F;	//NR32
		gameBoy.memory[0xFF1E] = 0xBF;	//NR33
		gameBoy.memory[0xFF20] = 0xFF;	//NR41
		gameBoy.memory[0xFF21] = 0x00;	//NR42
		gameBoy.memory[0xFF22] = 0x00;	//NR43
		gameBoy.memory[0xFF23] = 0xBF;	//NR30
		gameBoy.memory[0xFF24] = 0x77;	//NR50
		gameBoy.memory[0xFF25] = 0xF3;	//NR51
		gameBoy.memory[0xFF26] = 0xF1; 	//NR52, $F0 for super gameboy
		gameBoy.memory[0xFF40] = 0x91;	//LCDC
		gameBoy.memory[0xFF42] = 0x00;	//SCY
		gameBoy.memory[0xFF43] = 0x00;	//SCX
		gameBoy.memory[0xFF45] = 0x00;	//LYC
		gameBoy.memory[0xFF47] = 0xFC;	//BGP
		gameBoy.memory[0xFF48] = 0xFF;	//OBPO
		gameBoy.memory[0xFF49] = 0xFF;	//OBP1
		gameBoy.memory[0xFF4A] = 0x00;	//WY
		gameBoy.memory[0xFF4B] = 0x00;	//WX
		gameBoy.memory[0xFFFF] = 0x00;	//IE
	}
	
	
	private void reset(){
		init();
	}
	
}
