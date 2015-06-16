package emulator;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

enum CPUState{
	CPU_STATE_EXECUTING,
	CPU_STATE_WAITING
}

public class CPU extends Thread{
	
	public final static int PROCESSOR_FREQUENCY_HZ = 4194304;
	public final static int NANOSECONDS_IN_SECOND = 1000000000;
	
	public final static int PROCESSOR_DAMPING_FACTOR = 1;
	public final static int PROCESSOR_DAMPED_FREQUENCY_HZ = 
			(int)(PROCESSOR_FREQUENCY_HZ/PROCESSOR_DAMPING_FACTOR);
	
	private GameBoy gameBoy;
	public CyclicBarrier barrier;
	
	
	private CPUState state;
	
	private int M; //machine cycles
	private int T;	//clock cycles
	
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
	
	public CPU(GameBoy gameBoy, CyclicBarrier barrier) {
		this.gameBoy = gameBoy;
		this.barrier = barrier;
		init();
	}
	
	public void run(){
		while(true){
			
			long startTime = System.nanoTime();
			
			if(this.state == CPUState.CPU_STATE_EXECUTING){
				fetchNextOpcode();
				decodeAndExecuteOpcode();
			}else{	
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}

			}
//			System.out.println("CPU State: "+this.state);
			
			long endTime = System.nanoTime();
			long stallTimeNano = Math.max(((NANOSECONDS_IN_SECOND/PROCESSOR_DAMPED_FREQUENCY_HZ) 
					- (endTime - startTime)), 0);
			long stallTimeMillis = TimeUnit.MILLISECONDS.convert(stallTimeNano, TimeUnit.NANOSECONDS);
			
			try {
				TimeUnit.MILLISECONDS.sleep(stallTimeMillis);
				TimeUnit.NANOSECONDS.sleep(stallTimeNano%1000000);
				//Thread.sleep(stallTimeMillis, (int)(stallTimeMillis%1000000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public synchronized void setState(CPUState state){
		this.state = state;
	}
	
	public void fetchNextOpcode(){
		
		currentOpcode = gameBoy.memory.readByte(pc);
		
	}
	
	public void decodeAndExecuteOpcode(){
		
//		if(pc == 0x100){
//			for(int i = 0; i < gameBoy.memory.length; i++){
//				if(gameBoy.memory[i] != 0x0)
//					System.out.println("ADDR: "+Integer.toHexString(i).toUpperCase()+" value: "
//							+String.format("%16s", Integer.toBinaryString(gameBoy.memory[i])).replace(' ', '0'));
//			}
//		}
		
		//System.out.print("pc: " + Integer.toHexString(pc).toUpperCase());
		//System.out.println(" opcode: " + Integer.toHexString(currentOpcode).toUpperCase());
		
		switch(currentOpcode){
		
		/*longer opcodes with prefix cb*/
		case 0xCB: {
			pc++;
			fetchNextOpcode();
			
			switch(currentOpcode){
			
			case 0x7C: {
				char testBit = (char)((registers[INDEX_H] & 0x80) >> 7);
				
				if(testBit == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] |= HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x11: {
				char carryFlag = (char)(((registers[INDEX_F]) >> 4) & 0x1);
				
				if(((registers[INDEX_C] >> 7) &0x1) > 0)
					registers[INDEX_F] |= CARRY_BIT;
				else
					registers[INDEX_F] &= ~CARRY_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				registers[INDEX_C] = (char)(((registers[INDEX_C] << 1) | carryFlag) & 0xFF);
				
				if(registers[INDEX_C] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;

				
				
				M += 1;
				T += 4;
				
				break;
			}
			
				default:
					System.err.println("Unsupported opcode : CB "+Integer.toHexString(currentOpcode).toUpperCase());
					System.exit(0);
			}
			
			break;
		}
		
		case 0x00: {
			M += 1;
			T += 4;
			break;
		}
		
		/****************************/
		/******* 8 BIT LOADS ********/
		/****************************/
		
		case 0x06:{
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_B] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x0E:{
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_C] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x16: {
			
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_D] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1E: {
			
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_E] = immediate;
			
			M += 2;
			T += 8;
			break;
			
		}
		
		case 0x26: {
			
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_H] = immediate;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2E: {
			
			char immediate = gameBoy.memory.readByte(++pc);
			
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
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_B] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_C] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_D] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_E] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_H] = gameBoy.memory.readByte(address);
			
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
			registers[INDEX_L] = gameBoy.memory.readByte(address);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x70: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_B]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x71: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_C]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x72: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_D]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x73: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_E]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x74: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_H]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x75: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_L]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x36: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, gameBoy.memory.readByte(++pc));
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x0A: {
			char address = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x1A: {
			char address = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xFA: {
			char address = (char)(gameBoy.memory.readByte(++pc) | (gameBoy.memory.readByte(++pc) << 8));
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0x3E: {
			char immediate = gameBoy.memory.readByte(++pc);
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
			registers[INDEX_C] = registers[INDEX_A];
			
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
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x12: {
			char address = (char)((registers[INDEX_E] << 8) | registers[INDEX_E]);
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x77: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xEA: {
			char address = (char)(gameBoy.memory.readByte(++pc) | (gameBoy.memory.readByte(++pc) << 8));
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xF2: {
			char address = (gameBoy.memory.readByte(0xFF00 + registers[INDEX_C]));
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE2: {
			char address = (gameBoy.memory.readByte(0xFF00 + registers[INDEX_C]));
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x3A: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			address--;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x32: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			address--;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2A: {
			char address = (char)((registers[INDEX_H] << 8) & registers[INDEX_L]);
			registers[INDEX_A] = gameBoy.memory.readByte(address);
			
			address++;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x22: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_A]);
			
			address++;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE0: {
			char immediate = gameBoy.memory.readByte(++pc);
			
			gameBoy.memory.writeByte(0xFF00 + immediate, registers[INDEX_A]);
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xF0: {
			char immediate = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_A] = gameBoy.memory.readByte(0xFF00 + immediate);
						
			M += 3;
			T += 12;
			
			break;
		}
		
		/****************************/
		/******* 16 BIT LOADS *******/
		/****************************/
		
		case 0x01: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_B] = immediateMS;
			registers[INDEX_C] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x11: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_D] = immediateMS;
			registers[INDEX_E] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x21: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
			registers[INDEX_H] = immediateMS;
			registers[INDEX_L] = immediateLS;
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x31: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
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
			char immediate = gameBoy.memory.readByte(++pc);
			char address = (char)(sp + immediate);
			
			registers[INDEX_H] = (char)(address << 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			//reset Z flag
			registers[INDEX_F] &= ~ZERO_BIT;
			//reset N flag
			registers[INDEX_F] &= ~OP_BIT;
			//set H
			if((immediate + (sp & 0xF)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			//set C
			if((int)(immediate + (sp)) > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;

			
			M += 3;
			T += 12;

			
			break;
		}
		
		case 0x08: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
			char address = (char)((immediateMS << 8) | immediateLS);
			
			gameBoy.memory.writeByte(address , (char)(sp & 0xFF));
			gameBoy.memory.writeByte(address+1, (char)(sp >> 8));
			
			M += 5;
			T += 20;
			
			break;
		}
		
		case 0xF5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_F]);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_A]);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_C]);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_B]);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xD5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_E]);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_D]);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xE5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_L]);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_H]);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xF1: {
			
			registers[INDEX_F] = gameBoy.memory.readByte(sp);
			sp ++;
			registers[INDEX_A] = gameBoy.memory.readByte(sp);
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC1: {
			
			registers[INDEX_B] = gameBoy.memory.readByte(sp);
			sp ++;
			registers[INDEX_C] = gameBoy.memory.readByte(sp);
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xD1: {
			
			registers[INDEX_E] = gameBoy.memory.readByte(sp);
			sp ++;
			registers[INDEX_D] = gameBoy.memory.readByte(sp);
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xE1: {
			
			registers[INDEX_L] = gameBoy.memory.readByte(sp);
			sp ++;
			registers[INDEX_H] = gameBoy.memory.readByte(sp);
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] |= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_A] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_B] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_C] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] |= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_D] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_E] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] |= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_H] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] |= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (registers[INDEX_L] &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x86: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char result = (char)(((registers[INDEX_A] + gameBoy.memory.readByte(address))) &0xFF);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (gameBoy.memory.readByte(address)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xC6: {
			char immediate = gameBoy.memory.readByte(++pc);
			int result = ((registers[INDEX_A] + immediate));
			
			//set z flag
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] |= ~OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) + (immediate &0xF) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_A] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_B] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_C] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_D] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_E] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
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
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_H] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8D: {
			int result = (registers[INDEX_A] + registers[INDEX_L] + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_L] &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8E: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]); 
			int result = (registers[INDEX_A] + gameBoy.memory.readByte(address) + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (gameBoy.memory.readByte(address) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0xCE: {
			char immediate = gameBoy.memory.readByte(++pc); 
			int result = (registers[INDEX_A] + immediate + ((registers[INDEX_F] & ZERO_BIT) >> 4));
			
			//set z flag
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (immediate &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		//TODO:
		//LEFT OFF AT SUBS
		
		//just implement what I need atm
		case 0xC3: {
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			
			char address = (char)((immediateMS << 8) | immediateLS);
			pc = address;
			
			M += 4;
			T += 16;
						
			return;
 		}
		
		case 0xAF: {
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_A]) & 0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x05: {
			char result = (char) (registers[INDEX_B]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_B] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_B] = (char)(result&0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x20: {
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			
			//check not zero flag
			if(((registers[INDEX_F] & ZERO_BIT) >> 4) == 0){
				pc = (char)(pc +1 + signedImmediate); //TODO: hyper sketch +1
				
				M += 3;
				T += 12;
				return;
			}else{
				M += 2;
				T += 8;
			}
			
			break;
		}
		
		case 0x0C: {
			int result = registers[INDEX_C]+1;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_C] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_C] = (char)result;
			
			break;
		}
		
		case 0xCD: {
			
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
						
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) );
			sp--;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) );
			
			pc = (char)((immediateMS << 8) | immediateLS);

			
			M += 6;
			T += 24;
			
			return;
		}
		
		case 0x17: {
			
			char carryFlag = (char)(((registers[INDEX_F]) >> 4) & 0x1);
			
			if(((registers[INDEX_A] >> 7) &0x1) > 0)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_A] = (char)(((registers[INDEX_A] << 1) | carryFlag) & 0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x23: {
			
			char value = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			value++;
			
			registers[INDEX_H] = (char)(value >> 8);
			registers[INDEX_L] = (char)(value & 0xFF);
			
			M += 2;
			T += 8;

			
			break;
		}
		
		case 0xC9: {
			
			char retAddMS = gameBoy.memory.readByte(sp);
			sp ++;
			char retAddLS = gameBoy.memory.readByte(sp);
			pc ++;
			
			pc = (char)((retAddMS << 8) | retAddLS);
			
			M += 2;
			T += 8;
						
			return;
		}
		
		case 0xF3: {
			//TODO:
			System.out.println("disable interrupts after the instruction after this one is executed");
			
			break;
		}
		
		case 0xFE: {
			
			char immediate = gameBoy.memory.readByte(++pc);
			
			char result = (char)(registers[INDEX_A] - immediate);
			
			if(registers[INDEX_A] == immediate)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_A] &0xF) < (immediate &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			if(registers[INDEX_A] < immediate)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
						
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x13: {
			
			char value = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			value++;
			
			registers[INDEX_D] = (char)(value >> 8);
			registers[INDEX_E] = (char)(value & 0xFF);
			
			M += 2;
			T += 8;

			
			break;
		}
		
		case 0x3D: {
			char result = (char) (registers[INDEX_A]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_A] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_A] = result;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x28: {
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			
			//check not zero flag
			if(((registers[INDEX_F] & ZERO_BIT) >> 4) != 0){
				pc = (char)(pc +1 + signedImmediate); //TODO: hyper sketch +1
				
				M += 3;
				T += 12;
				return;
			}else{
				M += 2;
				T += 8;
			}
			
			break;
		}
		
		case 0x0D: {
			char result = (char) (registers[INDEX_C]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_C] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_C] = result;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x18: {
			byte signedImmediate = (byte)gameBoy.memory.readByte(++pc);
			pc = (char)(pc +1 + signedImmediate); //TODO: hyper sketch +1
			
			//these vals are not agreed upon?
			M += 2;
			T += 8;
			
			return;
		}
		
		case 0x04: {
			int result = registers[INDEX_B]+1;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_B] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_B] = (char)(result&0xFF);
			
			break;
		}
		
		case 0x90: {
			char result = (char)(registers[INDEX_A] - registers[INDEX_B]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_B] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_B])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
			
		}
		
		case 0x1D: {
			
			char result = (char) (registers[INDEX_E]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_E] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_E] = result;
			
			M += 1;
			T += 4;
			
			break;
			 
		}
		
		case 0x24:{
			
			int result = registers[INDEX_H]+1;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_H] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_H] = (char)result;
			
			break;	
		}
		
		case 0x15: {
			
			char result = (char) (registers[INDEX_D]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_D] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_D] = result;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xBE: {
			
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char value = gameBoy.memory.readByte(address);
			
			char result = (char)(registers[INDEX_A] - value);
			
			if(registers[INDEX_A] == value)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_A] &0xF) < (value &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			if(registers[INDEX_A] < value)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
						
			M += 2;
			T += 8;
			
			break;
		}
		
			default:
			
				System.err.println("Unsupported Opcode!");
				System.exit(0);
		}
		
		//increment program counter
		pc += 1;
		gameBoy.setMachineCycles(M);
		gameBoy.setClockCycles(T);

	}
	
//	void writeByteToMemory(char address, char data){
//		gameBoy.memory[address] = data;
//		
//		//echo the 8kb internal RAM
//		if(address >= 0xC000 && address <  0xE000){
//			char echoAddress = (char)(address + 0x2000);
//			gameBoy.memory[echoAddress] = data; 
//		}
//		
//		if(address == LCDController.DMA_REGISTER_ADDR){
//			gameBoy.DMATransfer();
//		}
//		
//	}
	
	private void init() {
		
		this.state = CPUState.CPU_STATE_EXECUTING;
		registers = new char[8];
		
		//The entry point of the program
		//TODO: temp test for bootstrap
		pc = 0x0;//0x0100;
		sp = 0xFFFE;
		
		//initial data loaded into RAM/registers
		/*
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
		*/
	}
	
	
	private void reset(){
		init();
	}
	
}
