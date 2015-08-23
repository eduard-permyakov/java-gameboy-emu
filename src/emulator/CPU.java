package emulator;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

enum CPUState{
	CPU_STATE_EXECUTING,
	CPU_STATE_WAITING,
}

enum Interrupt{
	InterruptVBlank,
	InterruptLCDC,
	InterruptTimerOverflow,
	InterruptSerialIOTransferComplete,
	InterruptJoypad
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
	
	private int pc;	//Program Counter
	private int sp;	//Stack Pointer
	
	private final static int ZERO_BIT = 0x80;
	private final static int OP_BIT = 0x40;
	private final static int HALF_CARRY_BIT = 0x20;
	private final static int CARRY_BIT = 0x10;
		
	private char currentOpcode;
	
	private boolean IME = true;
	private Interrupt currInterrupt = null;
	
	private int cntUntinEnableInterrupt = -1;
	private int cntUntilDisableInterrupt = -1;
	
	private boolean isStopped = false;
	
	public CPU(GameBoy gameBoy, CyclicBarrier barrier) {
		this.gameBoy = gameBoy;
		this.barrier = barrier;
		this.registers = new char[8];

//		init();
	}
	
	public synchronized boolean interruptsEnabled(){
		return IME;
	}
	
	
	public synchronized void interrupt(Interrupt type){
		System.out.println("--- interrupted ---");
		currInterrupt = type;
	}
	
	public void run(){
		while(true){
			
			
			long startTime = System.nanoTime();
			
			if(this.state == CPUState.CPU_STATE_EXECUTING){
				
				if(isStopped){
					execStoppedState();
				}else{
					
					updateInterruptStates();
					
					if(IME)
						serviceInterrrupts();
					fetchNextOpcode();
					decodeAndExecuteOpcode();
				}
				

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
	
	public void resumeExecution(){
		this.isStopped = false;
	}
	
	private void serviceInterrrupts(){
		System.out.println("service interrupts");
	}
	
	private void execStoppedState(){
		M += 1;
		T += 4;
		
		gameBoy.setMachineCycles(M);
		gameBoy.setClockCycles(T);
	}
	
	private void updateInterruptStates(){
		if(cntUntinEnableInterrupt >= 0){
			cntUntinEnableInterrupt--;
			if(cntUntinEnableInterrupt == -1)
				IME = true; System.out.println("enable interrupts");
		}
		
		if(cntUntilDisableInterrupt >= 0){
			cntUntilDisableInterrupt--;
			if(cntUntilDisableInterrupt == -1)
				IME = false; System.out.println("disable interrupts");
		}
	}
	
	private void fetchNextOpcode(){
		
		currentOpcode = gameBoy.memory.readByte(pc);
		
	}
	
	private void decodeAndExecuteOpcode(){
		
//		System.out.print("pc: " + Integer.toHexString(pc).toUpperCase());
//		System.out.println(" opcode: " + Integer.toHexString(currentOpcode).toUpperCase());
		
		if(pc == 0x3F4){
			System.out.print("");
		}
		
		if(pc == 0x2559){
			System.out.print("");
		}
		
		if(pc == 0x1E2D){
			System.out.print("");
		}
		
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
			
//			case 0x11: {
//				char carryFlag = (char)(((registers[INDEX_F]) >> 4) & 0x1);
//				
//				if(((registers[INDEX_C] >> 7) &0x1) > 0)
//					registers[INDEX_F] |= CARRY_BIT;
//				else
//					registers[INDEX_F] &= ~CARRY_BIT;
//				
//				registers[INDEX_F] &= ~OP_BIT;
//				registers[INDEX_F] &= ~HALF_CARRY_BIT;
//				
//				registers[INDEX_C] = (char)(((registers[INDEX_C] << 1) | carryFlag) & 0xFF);
//				
//				if(registers[INDEX_C] == 0)
//					registers[INDEX_F] |= ZERO_BIT;
//				else
//					registers[INDEX_F] &= ~ZERO_BIT;
//
//				
//				
//				M += 1;
//				T += 4;
//				
//				break;
//			}
			
//			case 0x37:{
//				
//				char upperNibble = (char) ((registers[INDEX_A] >> 4) & 0xF);
//				char lowerNibble = (char) (registers[INDEX_A] & 0xF);
//				registers[INDEX_A] = (char)((lowerNibble << 4) | upperNibble);
//				
//				if(registers[INDEX_A] == 0)
//					registers[INDEX_F] |= ZERO_BIT;
//				else
//					registers[INDEX_F] &= ~ZERO_BIT;
//				registers[INDEX_F] &= ~(OP_BIT | HALF_CARRY_BIT | CARRY_BIT);
//				
//				M += 2;
//				T += 8;
//				
//				break;
//			}
			
			case 0x87:{
				registers[INDEX_A] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x80:{
				registers[INDEX_B] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x81:{
				registers[INDEX_C] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x82:{
				registers[INDEX_D] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x83:{
				registers[INDEX_E] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x84:{
				registers[INDEX_H] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x85:{
				registers[INDEX_L] &= ~1;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			//SHIFT RIGHT INTO CARRY
			
			case 0x3F:{
				
				if((registers[INDEX_A] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_A] = (char)(registers[INDEX_A] >> 1);
				
				if(registers[INDEX_A] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x38:{
				if((registers[INDEX_B] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_B] = (char)(registers[INDEX_B] >> 1);
				
				if(registers[INDEX_B] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x39:{
				if((registers[INDEX_C] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_C] = (char)(registers[INDEX_C] >> 1);
				
				if(registers[INDEX_C] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x3A:{
				if((registers[INDEX_D] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_D] = (char)(registers[INDEX_D] >> 1);
				
				if(registers[INDEX_D] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x3B:{
				if((registers[INDEX_E] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_E] = (char)(registers[INDEX_E] >> 1);
				
				if(registers[INDEX_E] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x3C:{
				if((registers[INDEX_H] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_H] = (char)(registers[INDEX_H] >> 1);
				
				if(registers[INDEX_H] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x3D:{
				if((registers[INDEX_L] & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				registers[INDEX_L] = (char)(registers[INDEX_L] >> 1);
				
				if(registers[INDEX_L] == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x3E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				
				if((value & 1) == 0)
					registers[INDEX_F] &= ~CARRY_BIT;
				else
					registers[INDEX_F] |= CARRY_BIT;
				
				value = (char)(value >> 1);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				if(value == 0)
					registers[INDEX_F] |= ZERO_BIT;
				else
					registers[INDEX_F] &= ~ZERO_BIT;
				
				registers[INDEX_F] &= ~OP_BIT;
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
				
				M += 4;
				T += 16;
				
				break;
			}
			
			//Rotate right through carry flag
			
			case 0x1F:{
				rotateRegRightThroughCarry(INDEX_A);
				break;
			}
			
			case 0x18:{
				rotateRegRightThroughCarry(INDEX_B);
				break;
			}
			
			case 0x19:{
				rotateRegRightThroughCarry(INDEX_C);
				break;
			}
			
			case 0x1A:{
				rotateRegRightThroughCarry(INDEX_D);
				break;
			}
			
			case 0x1B:{
				rotateRegRightThroughCarry(INDEX_E);
				break;
			}
			
			case 0x1C:{
				rotateRegRightThroughCarry(INDEX_H);
				break;
			}
			
			case 0x1D:{
				rotateRegRightThroughCarry(INDEX_L);
				break;
			}
			
			case 0x1E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				rotateMemRightThroughCarry(address);
				break;
			}
			
			//rotate left through carry
			
			case 0x07:{
				rotateRegLeftThroughCarry(INDEX_A);
				break;
			}
			
			case 0x00:{
				rotateRegLeftThroughCarry(INDEX_B);
				break;
			}
			
			case 0x01:{
				rotateRegLeftThroughCarry(INDEX_C);
				break;
			}
			
			case 0x02:{
				rotateRegLeftThroughCarry(INDEX_D);
				break;
			}
			
			case 0x03:{
				rotateRegLeftThroughCarry(INDEX_E);
				break;
			}
			
			case 0x04:{
				rotateRegLeftThroughCarry(INDEX_H);
				break;
			}
			
			case 0x05:{
				rotateRegLeftThroughCarry(INDEX_L);
				break;
			}
			
			case 0x06:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				rotateMemLeftThroughCarry(address);
				break;
			}
			
			case 0x17:{
				rotateRegLeftIntoCarry(INDEX_A);
				break;
			}
			
			case 0x10:{
				rotateRegLeftIntoCarry(INDEX_B);
				break;
			}
			
			case 0x11:{
				rotateRegLeftIntoCarry(INDEX_C);
				break;
			}
			
			case 0x12:{
				rotateRegLeftIntoCarry(INDEX_D);
				break;
			}
			
			case 0x13:{
				rotateRegLeftIntoCarry(INDEX_E);
				break;
			}
			
			case 0x14:{
				rotateRegLeftIntoCarry(INDEX_H);
				break;
			}
			
			case 0x15:{
				rotateRegLeftIntoCarry(INDEX_L);
				break;
			}
			
			case 0x0F:{
				rotateRegRightIntoCarry(INDEX_A);
				break;
			}
			
			case 0x08:{
				rotateRegRightIntoCarry(INDEX_B);
				break;
			}
			
			case 0x09:{
				rotateRegRightIntoCarry(INDEX_C);
				break;
			}
			
			case 0x0A:{
				rotateRegRightIntoCarry(INDEX_D);
				break;
			}
			
			case 0x0B:{
				rotateRegRightIntoCarry(INDEX_E);
				break;
			}
			
			case 0x0C:{
				rotateRegRightIntoCarry(INDEX_H);
				break;
			}
			
			case 0x0D:{
				rotateRegRightIntoCarry(INDEX_L);
				break;
			}
			
			case 0x27:{
				shiftLeftIntoCarry(INDEX_A);
				break;
			}
			
			case 0x20:{
				shiftLeftIntoCarry(INDEX_B);
				break;
			}
			
			case 0x21:{
				shiftLeftIntoCarry(INDEX_C);
				break;
			}
			
			case 0x22:{
				shiftLeftIntoCarry(INDEX_D);
				break;
			}
			
			case 0x23:{
				shiftLeftIntoCarry(INDEX_E);
				break;
			}
			
			case 0x24:{
				shiftLeftIntoCarry(INDEX_H);
				break;
			}
			
			case 0x25:{
				shiftLeftIntoCarry(INDEX_L);
				break;
			}
			
			case 0x2F:{
				shiftRightIntoCarry(INDEX_A);
				break;
			}
			
			case 0x28:{
				shiftRightIntoCarry(INDEX_B);
				break;
			}
			
			case 0x29:{
				shiftRightIntoCarry(INDEX_C);
				break;
			}
			
			case 0x2A:{
				shiftRightIntoCarry(INDEX_D);
				break;
			}
			
			case 0x2B:{
				shiftRightIntoCarry(INDEX_E);
				break;
			}
			
			case 0x2C:{
				shiftRightIntoCarry(INDEX_H);
				break;
			}
			
			case 0x2D:{
				shiftRightIntoCarry(INDEX_L);
				break;
			}
			
			case 0x37:{
				swapNibbles(INDEX_A);
				break;
			}
			
			case 0x30:{
				swapNibbles(INDEX_B);
				break;
			}
			
			case 0x31:{
				swapNibbles(INDEX_C);
				break;
			}
			
			case 0x32:{
				swapNibbles(INDEX_D);
				break;
			}
			
			case 0x33:{
				swapNibbles(INDEX_E);
				break;
			}
			
			case 0x34:{
				swapNibbles(INDEX_H);
				break;
			}
			
			case 0x35:{
				swapNibbles(INDEX_L);
				break;
			}
			
			case 0x0E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				char rotatedBit = (char)(value &1);
				value = (char) ((rotatedBit << 7) | (value >> 1));
				
				if(rotatedBit == 0)				resetFlags(CARRY_BIT);
				else							setFlags(CARRY_BIT);
				
				if(value == 0)					setFlags(ZERO_BIT);
				else							resetFlags(ZERO_BIT);
				
				resetFlags(OP_BIT | HALF_CARRY_BIT);
				
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x16:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				char rotatedBit = (char)(value >> 7);
				char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
				value = (char)((oldCarry | (value << 1)) & 0xFF);
				
				if(rotatedBit == 0)				resetFlags(CARRY_BIT);
				else							setFlags(CARRY_BIT);
				
				if(value == 0)					setFlags(ZERO_BIT);
				else							resetFlags(ZERO_BIT);
				
				resetFlags(OP_BIT | HALF_CARRY_BIT);
				
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x26:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				char shiftedBit = (char)(value >> 7);
				value = (char)((value << 1) & 0xFF);
				
				if(shiftedBit == 0)				resetFlags(CARRY_BIT);
				else							setFlags(CARRY_BIT);
				
				if(value == 0)					setFlags(ZERO_BIT);
				else							resetFlags(ZERO_BIT);
				
				resetFlags(OP_BIT | HALF_CARRY_BIT);
				
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 2;
				T += 8;
				
				break;
			}
			
			case 0x2E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				char shiftedBit = (char)(value &1);
				char lastBit = (char)(value >> 7);
				value = (char)((lastBit << 7 | value >> 1) & 0xFF);
				
				if(shiftedBit == 0)				resetFlags(CARRY_BIT);
				else							setFlags(CARRY_BIT);
				
				if(value == 0)					setFlags(ZERO_BIT);
				else							resetFlags(ZERO_BIT);
				
				resetFlags(OP_BIT | HALF_CARRY_BIT);
				
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x36:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				char lowerNibble = (char)(value &0xFF);
				char upperNibble = (char)(value >> 8);
				
				value = (char) (lowerNibble << 8 | upperNibble);
				
				resetFlags(OP_BIT | HALF_CARRY_BIT | CARRY_BIT);
				if(value == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x46:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 0)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x4E:{
				
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 1)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
				
			}
			
			case 0x56:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 2)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x5E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 3)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x66:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 4)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x6E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 5)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x76:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 6)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x7E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				if((value & (1 << 7)) == 0)
					setFlags(ZERO_BIT);
				else
					resetFlags(ZERO_BIT);
				
				setFlags(HALF_CARRY_BIT);
				resetFlags(CARRY_BIT);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x86:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 0);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x8E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 1);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x96:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 2);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0x9E:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 3);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xA6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 4);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xAE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 5);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xB6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 6);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xBE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value &= ~(1 << 7);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xC6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 0);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xCE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 1);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xD6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 2);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xDE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 3);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xE6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 4);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xEE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 5);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xF6:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 6);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
				break;
			}
			
			case 0xFE:{
				char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
				char value = gameBoy.memory.readByte(address);
				value |= (1 << 7);
				gameBoy.memory.writeByte(address, value, HardwareType.CPU);
				
				M += 4;
				T += 16;
				
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
			registers[INDEX_B] = registers[INDEX_E];
			
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
			gameBoy.memory.writeByte(address, registers[INDEX_B], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x71: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_C], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x72: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_D], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x73: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_E], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x74: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_H], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x75: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_L], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x36: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, gameBoy.memory.readByte(++pc), HardwareType.CPU);
			
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
			registers[INDEX_B] = registers[INDEX_A];
			
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
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x12: {
			char address = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x77: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xEA: {
			char address = (char)(gameBoy.memory.readByte(++pc) | (gameBoy.memory.readByte(++pc) << 8));
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
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
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
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
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
			address--;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x2A: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
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
			gameBoy.memory.writeByte(address, registers[INDEX_A], HardwareType.CPU);
			
			address++;
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
						
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE0: {
			char immediate = gameBoy.memory.readByte(++pc);
			
			gameBoy.memory.writeByte(0xFF00 + immediate, registers[INDEX_A], HardwareType.CPU);
			
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
			
			sp = (immediateMS << 8 | immediateLS);
			
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
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			char address = (char)((sp + signedImmediate)%0x10000);
			
			registers[INDEX_H] = (char)(address >> 8);
			registers[INDEX_L] = (char)(address & 0xFF);
			
			//reset Z flag
			registers[INDEX_F] &= ~ZERO_BIT;
			//reset N flag
			registers[INDEX_F] &= ~OP_BIT;
			//set H
			if(((signedImmediate + sp)&0xF) < (sp&0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			//set C
			if(((signedImmediate + sp)&0xFF) < (sp&0xFF))
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
			
			gameBoy.memory.writeByte(address , (char)(sp & 0xFF), HardwareType.CPU);
			gameBoy.memory.writeByte(address+1, (char)(sp >> 8), HardwareType.CPU);
			
			M += 5;
			T += 20;
			
			break;
		}
		
		case 0xF5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_A], HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_F], HardwareType.CPU);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_B], HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_C], HardwareType.CPU);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xD5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_D], HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_E], HardwareType.CPU);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xE5: {
			
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_H], HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte(sp, registers[INDEX_L], HardwareType.CPU);
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xF1: {
			
			//Least significant nibble is always 0 in reg. F
			registers[INDEX_F] = (char)(gameBoy.memory.readByte(sp) & 0xF0);
			sp ++;
			registers[INDEX_A] = gameBoy.memory.readByte(sp);
			sp ++;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC1: {
			
			registers[INDEX_C] = gameBoy.memory.readByte(sp);
			sp ++;
			registers[INDEX_B] = gameBoy.memory.readByte(sp);
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
				registers[INDEX_F] &= ~ZERO_BIT;
			
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
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x80: {
			int result = ((registers[INDEX_A] + registers[INDEX_B]));
			
			//set z flag
			if((result%0x100) == 0)
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x81: {
			int result = ((registers[INDEX_A] + registers[INDEX_C]));
			
			//set z flag
			if((result%0x100) == 0)
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x82: {
			int result = ((registers[INDEX_A] + registers[INDEX_D]));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x83: {
			int result = ((registers[INDEX_A] + registers[INDEX_E]));
			
			//set z flag
			if((result%0x100) == 0)
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x84: {
			int result = ((registers[INDEX_A] + registers[INDEX_H]));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x85: {
			int result = ((registers[INDEX_A] + registers[INDEX_L]));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
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
			
			registers[INDEX_A] = (char)(result %0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x86: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char result = (char)((registers[INDEX_A] + gameBoy.memory.readByte(address)));
			
			//set z flag
			if(result%0x100 == 0)
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
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xC6: {
			char immediate = gameBoy.memory.readByte(++pc);
			int result = ((registers[INDEX_A] + immediate));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
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
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x8F: {
			int result = (registers[INDEX_A] + registers[INDEX_A] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_A] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x88: {
			int result = (registers[INDEX_A] + registers[INDEX_B] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_B] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x89: {
			int result = (registers[INDEX_A] + registers[INDEX_C] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_C] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8A: {
			int result = (registers[INDEX_A] + registers[INDEX_D] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_D] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8B: {
			int result = (registers[INDEX_A] + registers[INDEX_E] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_E] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8C: {
			int result = (registers[INDEX_A] + registers[INDEX_H] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_H] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8D: {
			int result = (registers[INDEX_A] + registers[INDEX_L] + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (registers[INDEX_L] &0xF) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x8E: {
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]); 
			int result = (registers[INDEX_A] + gameBoy.memory.readByte(address) + ((registers[INDEX_F] & CARRY_BIT) >> 4));
			
			//set z flag
			if((result%0x100) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//reset n flag
			registers[INDEX_F] &= ~OP_BIT;
			
			//set h flag
			if(((registers[INDEX_A] &0xF) + (gameBoy.memory.readByte(address) + ((registers[INDEX_F] & CARRY_BIT) >> 4)) > 0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(result > 0xFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0xCE: {
//			char immediate = gameBoy.memory.readByte(++pc); 
//			int result = (registers[INDEX_A] + immediate + ((registers[INDEX_F] & ZERO_BIT) >> 4));
//			
//			//set z flag
//			if((result%0x100) == 0)
//				registers[INDEX_F] |= ZERO_BIT;
//			else
//				registers[INDEX_F] &= ~ZERO_BIT;
//			
//			//reset n flag
//			registers[INDEX_F] &= ~OP_BIT;
//			
//			//set h flag
//			if(((registers[INDEX_A] &0xF) + (immediate &0xF) + ((registers[INDEX_F] & ZERO_BIT) >> 4)) > 0xF)
//				registers[INDEX_F] |= HALF_CARRY_BIT;
//			else
//				registers[INDEX_F] &= ~HALF_CARRY_BIT;
//			
//			//set c flag
//			if(result > 0xFF)
//				registers[INDEX_F] |= CARRY_BIT;
//			else
//				registers[INDEX_F] &= ~CARRY_BIT;
//			
//			registers[INDEX_A] = (char)(result % 0x100);
			
			char immediate = gameBoy.memory.readByte(++pc);
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			if((registers[INDEX_A] + immediate + carry) > 0xFF)
				setFlags(CARRY_BIT);
			else
				resetFlags(CARRY_BIT);
			
			if(((registers[INDEX_A]&0xF) + (immediate&0xF) + carry) > 0xF)
				setFlags(HALF_CARRY_BIT);
			else
				resetFlags(HALF_CARRY_BIT);
			
			resetFlags(OP_BIT);
			
			registers[INDEX_A] = (char)((registers[INDEX_A] + immediate + carry) % 0x100);
			
			if(registers[INDEX_A] == 0)
				setFlags(ZERO_BIT);
			else
				resetFlags(ZERO_BIT);
			
			
			M += 2;
			T += 8;
			
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
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
						
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
		
		case 0xA8:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_B]) & 0xFF);
			
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
		
		case 0xA9:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_C]) & 0xFF);
			
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
		
		case 0xAA:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_D]) & 0xFF);
			
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
		
		case 0xAB:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_E]) & 0xFF);
			
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
		
		case 0xAC:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_H]) & 0xFF);
			
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
		
		case 0xAD:{
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ registers[INDEX_L]) & 0xFF);
			
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
		
		case 0xAE:{
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char value = gameBoy.memory.readByte(address);
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ value) & 0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xEE:{
			char immediate = gameBoy.memory.readByte(++pc);
			registers[INDEX_A] = (char)((registers[INDEX_A] ^ immediate) & 0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
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
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}else{
				M += 2;
				T += 8;
			}
			
			break;
		}
		
		case 0x0C: {
			int result = registers[INDEX_C]+1;
			if(registers[INDEX_C] == 0xFF)
				result = 0;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_C] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_C] = (char)(result & 0xFF);
			
			break;
		}
		
		case 0xCD: {
			
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
				
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = (char)((immediateMS << 8) | immediateLS);

			
			M += 6;
			T += 24;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
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
			registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_A] = (char)(((registers[INDEX_A] << 1) | carryFlag) & 0xFF);
			
//			if(registers[INDEX_A] == 0)
//				registers[INDEX_F] |= ZERO_BIT;
//			else
//				registers[INDEX_F] &= ~ZERO_BIT;
			
			
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
			
			char retAddLS = gameBoy.memory.readByte(sp);
			sp ++;
			char retAddMS = gameBoy.memory.readByte(sp);
			sp ++;
			
			pc = (char)((retAddMS << 8) | retAddLS);
			
			M += 2;
			T += 8;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
						
			return;
		}
		
		case 0xF3: {
			cntUntilDisableInterrupt = 1;
			
			M += 1;
			T += 4;
			
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
		
		case 0x03:{
			
			char value = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			value++;
			
			registers[INDEX_B] = (char)(value >> 8);
			registers[INDEX_C] = (char)(value & 0xFF);
			
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
		
		case 0x33:{
			sp++;
			
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
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
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
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0x3C:{
			int result = registers[INDEX_A]+1;
			if(registers[INDEX_A] == 0xFF)
				result = 0;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_A] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_A] = (char)(result&0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x04: {
			int result = registers[INDEX_B]+1;
			if(registers[INDEX_B] == 0xFF)
				result = 0;
			
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
			
			M += 1;
			T += 4;
			
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
		
		case 0x91:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_C]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_C] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_C])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x97:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_A]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_A] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_A])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x92:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_D]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_D] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_D])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x93:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_E]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_E] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_E])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x94:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_H]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_H] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_H])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x95:{
			char result = (char)(registers[INDEX_A] - registers[INDEX_L]);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (registers[INDEX_L] &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < registers[INDEX_L])
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x96:{
			char value = gameBoy.memory.readByte((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char result = (char)(registers[INDEX_A] - value);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (value &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < value)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 2;
			T += 8;
			
			break;	
		}
		
		case 0xD6:{
			char value = gameBoy.memory.readByte(++pc);
			char result = (char)(registers[INDEX_A] - value);
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (value &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < value)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 2;
			T += 8;
			
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
			if(registers[INDEX_H] == 0xFF)
				result = 0;
			
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
			
			M += 1;
			T += 4;
			
			break;	
		}
		
		case 0x15: {
			
			char result = (char) (registers[INDEX_D]-1);
			if(registers[INDEX_D] == 0x00)
				result = 0xFF; //underflow
			
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
		
		case 0x0B:{
			
			char value = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			value--;
			
			registers[INDEX_B] = (char)(value >> 8);
			registers[INDEX_C] = (char)(value & 0xFF);
			
			M += 2;
			T += 8;

			
			break;
			
		}
		
		case 0xB7:{
			char valueReg = registers[INDEX_A];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB0:{
			char valueReg = registers[INDEX_B];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB1: {
			char valueReg = registers[INDEX_C];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
			
		}
		
		case 0xB2:{
			char valueReg = registers[INDEX_D];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB3:{
			char valueReg = registers[INDEX_E];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB4:{
			char valueReg = registers[INDEX_H];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		case 0xB5:{
			char valueReg = registers[INDEX_L];
			char valueA = registers[INDEX_A];
			
			if((valueA | valueReg) != 0){
				registers[INDEX_A] = (char)(valueA | valueReg);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB6:{
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char value = gameBoy.memory.readByte(address);
			char valueA = registers[INDEX_A];
			
			if((valueA | value) != 0){
				registers[INDEX_A] = (char)(valueA | value);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xF6:{
			char immediate = gameBoy.memory.readByte(++pc);
			char valueA = registers[INDEX_A];
			
			if((valueA | immediate) != 0){
				registers[INDEX_A] = (char)(valueA | immediate);
				registers[INDEX_F] &= ~ZERO_BIT;
			}else{
				registers[INDEX_A] = 0x00;
				registers[INDEX_F] |= ZERO_BIT;
			}
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] &= ~HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xFB:{
			cntUntinEnableInterrupt = 1;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x2F:{
			
			registers[INDEX_A] = (char) (~registers[INDEX_A] & 0xFF);
			
			registers[INDEX_F] |= OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xE6:{
			
			char immediate = gameBoy.memory.readByte(++pc);
			registers[INDEX_A] = (char) ((registers[INDEX_A] & immediate) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xA7:{
			
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_A]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xA0:{
			
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_B]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xA1:{
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_C]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xA2:{
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_D]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xA3:{
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_E]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xA4:{
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_H]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		case 0xA5:{
			registers[INDEX_A] = (char) ((registers[INDEX_A] & registers[INDEX_L]) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 1;
			T += 4;
			
			break;
		}
		case 0xA6:{
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char value = gameBoy.memory.readByte(address);
			
			registers[INDEX_A] = (char) ((registers[INDEX_A] & value) &0xFF);
			
			if(registers[INDEX_A] == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			registers[INDEX_F] |= HALF_CARRY_BIT;
			registers[INDEX_F] &= ~CARRY_BIT;
			
			M += 2;
			T += 8;
			
			break;
			
		}
		
		case 0xC7:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0000;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xCF:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0008;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xD7:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0010;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xDF:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0018;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xE7:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0020;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xEF:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0028;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xF7:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0030;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xFF:{
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
			sp --;
			gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
			
			pc = 0x0038;
			
			M += 8;
			T += 32;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0x09:{
			char value1 = (char)((registers[INDEX_B] << 8) | registers[INDEX_C]);
			char value2 = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int result = (value1 + value2);

			registers[INDEX_H] = (char)(result >> 8 & 0xFF);
			registers[INDEX_L] = (char)(result & 0xFF);
			
			if(result > 0xFFFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			if(((value1&0xFFF) + (value2&0xFFF)) > 0xFFF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x19:{
			
			char value1 = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			char value2 = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int result = (value1 + value2);

			registers[INDEX_H] = (char)(result >> 8 & 0xFF);
			registers[INDEX_L] = (char)(result & 0xFF);
			
			if(result > 0xFFFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			if(((value1&0xFFF) + (value2&0xFFF)) > 0xFFF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x29:{
			char value1 = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			char value2 = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int result = (value1 + value2);

			registers[INDEX_H] = (char)(result >> 8 & 0xFF);
			registers[INDEX_L] = (char)(result & 0xFF);
			
			if(result > 0xFFFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			if(((value1&0xFFF) + (value2&0xFFF)) > 0xFFF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x39:{
			char value1 = (char)sp;
			char value2 = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int result = (value1 + value2);

			registers[INDEX_H] = (char)(result >> 8 & 0xFF);
			registers[INDEX_L] = (char)(result & 0xFF);
			
			if(result > 0xFFFF)
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			if(((value1&0xFFF) + (value2&0xFFF)) > 0xFFF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xE9:{
			pc = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			
			M += 1;
			T += 4;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0xE8:{
			
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			
			char result = (char)(sp + signedImmediate);

			
			registers[INDEX_F] &= ~(ZERO_BIT | OP_BIT);
			
			if(((sp + signedImmediate)&0xFF) < (sp&0xFF))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			if(((sp + signedImmediate)&0xF) < (sp&0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			sp = result % 0x10000;
			
			M += 4;
			T += 16;
			
			break;
		}
		
		case 0xC4:{
			
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			char value = (char)(immediateMS << 8 | immediateLS);
			
			M += 3;
			T += 12;
			
			if((registers[INDEX_F] & ZERO_BIT) == 0){
				
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
				
				pc = value;
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xCC:{
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			char value = (char)(immediateMS << 8 | immediateLS);
			
			M += 3;
			T += 12;
			
			if((registers[INDEX_F] & ZERO_BIT) > 0){
				
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
				
				pc = value;
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xD4:{
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			char value = (char)(immediateMS << 8 | immediateLS);
			
			M += 3;
			T += 12;
			
			if((registers[INDEX_F] & CARRY_BIT) == 0){
				
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
				
				pc = value;
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xDC:{
			char immediateLS = gameBoy.memory.readByte(++pc);
			char immediateMS = gameBoy.memory.readByte(++pc);
			char value = (char)(immediateMS << 8 | immediateLS);
			
			M += 3;
			T += 12;
			
			if((registers[INDEX_F] & CARRY_BIT) > 0){
				
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 >> 8) ,HardwareType.CPU);
				sp --;
				gameBoy.memory.writeByte( sp, (char)(pc+1 &0xFF) ,HardwareType.CPU);
				
				pc = value;
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0x14:{
			int result = registers[INDEX_D]+1;
			if(registers[INDEX_D] == 0xFF)
				result = 0;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_D] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_D] = (char)(result&0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x1C:{
			int result = registers[INDEX_E]+1;
			if(registers[INDEX_E] == 0xFF)
				result = 0;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_E] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_E] = (char)(result&0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x2C:{
			int result = registers[INDEX_L]+1;
			if(registers[INDEX_L] == 0xFF)
				result = 0;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((registers[INDEX_L] &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_L] = (char)(result&0xFF);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x34:{
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int value = gameBoy.memory.readByte(address);
			int result = value+1;
			
			if((result&0xFF) == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] &= ~OP_BIT;
			
			if((value &0xF) + 1 > 0xF)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			gameBoy.memory.writeByte(address, (char)(result&0xFF), HardwareType.CPU);
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0xC0:{
			
			M += 2;
			T += 8;
			
			if((registers[INDEX_F] & ZERO_BIT) == 0){
				char retAddLS = gameBoy.memory.readByte(sp);
				sp ++;
				char retAddMS = gameBoy.memory.readByte(sp);
				sp ++;
				
				pc = (char)((retAddMS << 8) | retAddLS);
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;

		}
		
		case 0xC8:{
			M += 2;
			T += 8;
			
			if((registers[INDEX_F] & ZERO_BIT) > 0){
				char retAddLS = gameBoy.memory.readByte(sp);
				sp ++;
				char retAddMS = gameBoy.memory.readByte(sp);
				sp ++;
				
				pc = (char)((retAddMS << 8) | retAddLS);
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xD0:{
			M += 2;
			T += 8;
			
			if((registers[INDEX_F] & CARRY_BIT) == 0){
				char retAddLS = gameBoy.memory.readByte(sp);
				sp ++;
				char retAddMS = gameBoy.memory.readByte(sp);
				sp ++;
				
				pc = (char)((retAddMS << 8) | retAddLS);
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xD8:{
			M += 2;
			T += 8;
			
			if((registers[INDEX_F] & CARRY_BIT) > 0){
				
				char retAddLS = gameBoy.memory.readByte(sp);
				sp ++;
				char retAddMS = gameBoy.memory.readByte(sp);
				sp ++;
				
				pc = (char)((retAddMS << 8) | retAddLS);
				
				M += 3;
				T += 12;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0x9F:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_A] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_A]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_A] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x98:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_B] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_B]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_B] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x99:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_C] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_C]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_C] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x9A:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_D] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_D]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_D] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x9B:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_E] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_E]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_E] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x9C:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_H] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_H]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_H] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x9D:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (registers[INDEX_L] + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((registers[INDEX_L]&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (registers[INDEX_L] + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result%0x100);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x9E:{
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			int value = gameBoy.memory.readByte(address);
			
			char result = (char)(registers[INDEX_A] - (value + carry));
			
			//set z flag
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < (value + carry &0xF))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (value + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xBF:{
			char value = registers[INDEX_A];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB8:{
			char value = registers[INDEX_B];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xB9:{
			char value = registers[INDEX_C];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xBA:{
			char value = registers[INDEX_D];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xBB:{
			char value = registers[INDEX_E];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xBC:{
			char value = registers[INDEX_H];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xBD:{
			char value = registers[INDEX_L];
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
						
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xD9:{
			
			char retAddLS = gameBoy.memory.readByte(sp);
			sp ++;
			char retAddMS = gameBoy.memory.readByte(sp);
			sp ++;
			
			pc = (char)((retAddMS << 8) | retAddLS);
			
			IME = true;
			System.out.println("enable itnerrupts");
			
			M += 2;
			T += 8;
			
			gameBoy.setMachineCycles(M);
			gameBoy.setClockCycles(T);
			
			return;
		}
		
		case 0x3B:{
			sp --;
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x30:{
			
			M += 2;
			T += 8;
			
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			if((registers[INDEX_F] & CARRY_BIT) == 0){
				pc += 1 + signedImmediate;	//mb we need a sketchy + 1?
				
				M += 1;
				T += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0x38:{
			M += 2;
			T += 8;
			
			byte signedImmediate = (byte)(gameBoy.memory.readByte(++pc));
			if((registers[INDEX_F] & CARRY_BIT) > 0){
				pc += 1 + signedImmediate;	//mb we need a sketchy + 1?
				
				M += 1;
				T += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0x25:{
			char result = (char) (registers[INDEX_H]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_H] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_H] = result;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x2D:{
			char result = (char) (registers[INDEX_L]-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((registers[INDEX_L] & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			registers[INDEX_L] = result;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x35:{
			char address = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			
			char value = gameBoy.memory.readByte(address);
			char result = (char) (value-1);
			
			if(result == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			registers[INDEX_F] |= OP_BIT;
			
			if((value & 0xF) == 0)
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			gameBoy.memory.writeByte(address, result, HardwareType.CPU);
			
			M += 3;
			T += 12;
			
			break;
		}
		
		case 0x1F:{
			
			char rotatedBit = (char)(registers[INDEX_A] & 1);
			char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4); 
			registers[INDEX_A] = (char) (((oldCarry << 7) | (registers[INDEX_A] >> 1)) & 0xFF);
			
			if(rotatedBit == 0)				resetFlags(CARRY_BIT);
			else							setFlags(CARRY_BIT);
			
//			if(registers[INDEX_A] == 0)		setFlags(ZERO_BIT);
//			else							resetFlags(ZERO_BIT);
			
			resetFlags(OP_BIT | HALF_CARRY_BIT | ZERO_BIT);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0xC2:{
			M += 3;
			T += 12;
			
			char addLS = gameBoy.memory.readByte(++pc);
			char addMS = gameBoy.memory.readByte(++pc);
			
			if((registers[INDEX_F] & ZERO_BIT) == 0){
				
				pc = (char)((addMS << 8) | addLS);
				
				T += 1;
				M += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xCA:{
			M += 3;
			T += 12;
			
			char addLS = gameBoy.memory.readByte(++pc);
			char addMS = gameBoy.memory.readByte(++pc);
			
			if((registers[INDEX_F] & ZERO_BIT) > 0){
				
				pc = (char)((addMS << 8) | addLS);
				
				T += 1;
				M += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xD2:{
			M += 3;
			T += 12;
			
			char addLS = gameBoy.memory.readByte(++pc);
			char addMS = gameBoy.memory.readByte(++pc);
			
			if((registers[INDEX_F] & CARRY_BIT) == 0){
				
				pc = (char)((addMS << 8) | addLS);
				
				T += 1;
				M += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0xDA:{
			M += 3;
			T += 12;
			
			char addLS = gameBoy.memory.readByte(++pc);
			char addMS = gameBoy.memory.readByte(++pc);
			
			if((registers[INDEX_F] & CARRY_BIT) > 0){
				
				pc = (char)((addMS << 8) | addLS);
				
				T += 1;
				M += 4;
				
				gameBoy.setMachineCycles(M);
				gameBoy.setClockCycles(T);
				
				return;
			}
			
			break;
		}
		
		case 0x07:{
			char rotatedBit = (char) (((registers[INDEX_A] & 0x80) > 0) ? 1 : 0);
			registers[INDEX_A] = (char) ((rotatedBit | (registers[INDEX_A] << 1)) & 0xFF);
			
			if(rotatedBit == 0)				resetFlags(CARRY_BIT);
			else							setFlags(CARRY_BIT);
			
//			if(registers[INDEX_A] == 0)		setFlags(ZERO_BIT);
//			else							resetFlags(ZERO_BIT);
			
			resetFlags(OP_BIT | HALF_CARRY_BIT | ZERO_BIT);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x27:{
			
			if((registers[INDEX_F] & OP_BIT) == 0){
				if(flagsAreSet(HALF_CARRY_BIT) || (registers[INDEX_A]&0xF) > 0x9)
					registers[INDEX_A] += 0x06;
				
				if(flagsAreSet(CARRY_BIT) || registers[INDEX_A] > 0x9F)
					registers[INDEX_A] += 0x60;
			}else{
				if(flagsAreSet(HALF_CARRY_BIT))
					registers[INDEX_A] = (char)((registers[INDEX_A] - 0x06) & 0xFF);
				
				if(flagsAreSet(CARRY_BIT))
					registers[INDEX_A] -= 0x60;
			}
			
			resetFlags(HALF_CARRY_BIT | ZERO_BIT);
			
			if((registers[INDEX_A] & 0x100) > 0)
				setFlags(CARRY_BIT);
			
			registers[INDEX_A] &= 0xFF;
			
			if(registers[INDEX_A] == 0)
				setFlags(ZERO_BIT);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0xDE:{
			char immediate = gameBoy.memory.readByte(++pc);
			char carry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
			char result = (char)(registers[INDEX_A] - (immediate + carry));
			
			//set z flag
			if(result%0x100 == 0)
				registers[INDEX_F] |= ZERO_BIT;
			else
				registers[INDEX_F] &= ~ZERO_BIT;
			
			//set n flag
			registers[INDEX_F] |= OP_BIT;
			
			//set h flag
			if((registers[INDEX_A] &0xF) < ((immediate&0xF) + carry))
				registers[INDEX_F] |= HALF_CARRY_BIT;
			else
				registers[INDEX_F] &= ~HALF_CARRY_BIT;
			
			//set c flag
			if(registers[INDEX_A] < (immediate + carry))
				registers[INDEX_F] |= CARRY_BIT;
			else
				registers[INDEX_F] &= ~CARRY_BIT;
			
			registers[INDEX_A] = (char)(result % 0x100);
			
			M += 2;
			T += 8;
			
			break;	
		}
		
		case 0x1B:{
			
			char value = (char)((registers[INDEX_D] << 8) | registers[INDEX_E]);
			value--;
			
			registers[INDEX_D] = (char)(value >> 8);
			registers[INDEX_E] = (char)(value & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x37:{
			
			registers[INDEX_F] |= CARRY_BIT;
			registers[INDEX_F] &= ~(OP_BIT | HALF_CARRY_BIT);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x2B:{
			
			char value = (char)((registers[INDEX_H] << 8) | registers[INDEX_L]);
			value--;
			
			registers[INDEX_H] = (char)(value >> 8);
			registers[INDEX_L] = (char)(value & 0xFF);
			
			M += 2;
			T += 8;
			
			break;
		}
		
		case 0x10:{
			pc++;
			
			this.isStopped = true;
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x44:{
			
			registers[INDEX_B] = registers[INDEX_H];
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x0F:{
			char rotatedBit = (char)(registers[INDEX_A] & 1);
			registers[INDEX_A] = (char) (((rotatedBit << 7) | (registers[INDEX_A] >> 1)) & 0xFF);
			
			if(rotatedBit == 0)				resetFlags(CARRY_BIT);
			else							setFlags(CARRY_BIT);
			
//			if(registers[INDEX_A] == 0)		setFlags(ZERO_BIT);
//			else							resetFlags(ZERO_BIT);
			
			resetFlags(OP_BIT | HALF_CARRY_BIT | ZERO_BIT);
			
			M += 1;
			T += 4;
			
			break;
		}
		
		case 0x3F:{
			
			registers[INDEX_F] ^= CARRY_BIT;
			
			resetFlags(OP_BIT | HALF_CARRY_BIT);
			
			M += 1;
			T += 4;
			
			break;
		}
		
			default:{
				System.err.println("Unsupported Opcode: " +Integer.toHexString(currentOpcode).toUpperCase());
				System.exit(0);
			}

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
	
	public void init() {
		
		this.state = CPUState.CPU_STATE_EXECUTING;
		
		//The entry point of the program
		//TODO: temp test for bootstrap
		pc = 0x0100;//0x0100;
		sp = 0xFFFE;
		
		//initial data loaded into RAM/registers
		
		registers[INDEX_A] = 0x01;	registers[INDEX_F] = 0xB0;
		registers[INDEX_B] = 0x00;	registers[INDEX_C] = 0x13;
		registers[INDEX_D] = 0x00;	registers[INDEX_E] = 0xD8;
		registers[INDEX_H] = 0x01;	registers[INDEX_L] = 0x4D;
		
		gameBoy.memory.writeByte(0xFF05, (char)0x00, HardwareType.CPU);	//TIMA
		gameBoy.memory.writeByte(0xFF06, (char)0x00, HardwareType.CPU);	//TMA
		gameBoy.memory.writeByte(0xFF07, (char)0x00, HardwareType.CPU);	//TAC
		gameBoy.memory.writeByte(0xFF10, (char)0x80, HardwareType.CPU);	//NR10
		gameBoy.memory.writeByte(0xFF11, (char)0xBF, HardwareType.CPU);	//NR11
		gameBoy.memory.writeByte(0xFF12, (char)0xF3, HardwareType.CPU);	//NR12
		gameBoy.memory.writeByte(0xFF14, (char)0xBF, HardwareType.CPU);	//NR14
		gameBoy.memory.writeByte(0xFF16, (char)0x3F, HardwareType.CPU);	//NR21
		gameBoy.memory.writeByte(0xFF17, (char)0x00, HardwareType.CPU);	//NR22
		gameBoy.memory.writeByte(0xFF19, (char)0xBF, HardwareType.CPU);	//NR24
		gameBoy.memory.writeByte(0xFF1A, (char)0x7F, HardwareType.CPU);	//NR30
		gameBoy.memory.writeByte(0xFF1B, (char)0xFF, HardwareType.CPU);	//NR31
		gameBoy.memory.writeByte(0xFF1C, (char)0x9F, HardwareType.CPU);	//NR32
		gameBoy.memory.writeByte(0xFF1E, (char)0xBF, HardwareType.CPU);	//NR33
		gameBoy.memory.writeByte(0xFF20, (char)0xFF, HardwareType.CPU);	//NR41
		gameBoy.memory.writeByte(0xFF21, (char)0x00, HardwareType.CPU);	//NR42
		gameBoy.memory.writeByte(0xFF22, (char)0x00, HardwareType.CPU);	//NR43
		gameBoy.memory.writeByte(0xFF23, (char)0xBF, HardwareType.CPU);	//NR30
		gameBoy.memory.writeByte(0xFF24, (char)0x77, HardwareType.CPU);	//NR50
		gameBoy.memory.writeByte(0xFF25, (char)0xF3, HardwareType.CPU);	//NR51
		gameBoy.memory.writeByte(0xFF26, (char)0xF1, HardwareType.CPU); //NR52, $F0 for super gameboy
		gameBoy.memory.writeByte(0xFF40, (char)0x91, HardwareType.CPU);	//LCDC
		gameBoy.memory.writeByte(0xFF42, (char)0x00, HardwareType.CPU);	//SCY
		gameBoy.memory.writeByte(0xFF43, (char)0x00, HardwareType.CPU);	//SCX
		gameBoy.memory.writeByte(0xFF45, (char)0x00, HardwareType.CPU);	//LYC
		gameBoy.memory.writeByte(0xFF47, (char)0xFC, HardwareType.CPU);	//BGP
		gameBoy.memory.writeByte(0xFF48, (char)0xFF, HardwareType.CPU);	//OBPO
		gameBoy.memory.writeByte(0xFF49, (char)0xFF, HardwareType.CPU);	//OBP1
		gameBoy.memory.writeByte(0xFF4A, (char)0x00, HardwareType.CPU);	//WY
		gameBoy.memory.writeByte(0xFF4B, (char)0x00, HardwareType.CPU);	//WX
		gameBoy.memory.writeByte(0xFFFF, (char)0x00, HardwareType.CPU);	//IE
		
	}
	
	private final void rotateRegRightThroughCarry(int regIndex){
		
		char rotatedBit = (char)(registers[regIndex] & 1);
		char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4); 
		registers[regIndex] = (char) ((oldCarry << 7) | (registers[regIndex] >> 1));
		
		if(rotatedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
			
	}
	
	private final void rotateRegLeftThroughCarry(int regIndex){
		
		char rotatedBit = (char)(registers[regIndex] >> 7);
		registers[regIndex] = (char) ((rotatedBit | (registers[regIndex] << 1)) & 0xFF);
		
		if(rotatedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
			
	}
	
	private void rotateRegLeftIntoCarry(int regIndex){
		
		char rotatedBit = (char)(registers[regIndex] >> 7);
		char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4);
		registers[regIndex] = (char)((oldCarry | (registers[regIndex] << 1)) & 0xFF);
		
		if(rotatedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
	}
	
	private void rotateRegRightIntoCarry(int regIndex){
		char rotatedBit = (char)(registers[regIndex] &1);
		registers[regIndex] = (char) ((rotatedBit << 7) | (registers[regIndex] >> 1));
		
		if(rotatedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
	}
	
	private void shiftLeftIntoCarry(int regIndex){
		
		char shiftedBit = (char)(registers[regIndex] >> 7);
		registers[regIndex] = (char)((registers[regIndex] << 1) & 0xFF);
		
		if(shiftedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
	}
	
	private void shiftRightIntoCarry(int regIndex){
		
		char shiftedBit = (char)(registers[regIndex] &1);
		char lastBit = (char)(registers[regIndex] >> 7);
		registers[regIndex] = (char)((lastBit << 7 | registers[regIndex] >> 1) & 0xFF);
		
		if(shiftedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(registers[regIndex] == 0)	setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 2;
		T += 8;
	}
	
	private final void rotateMemLeftThroughCarry(int address){
		
		char value = gameBoy.memory.readByte(address);
		char rotatedBit = (char)(value >> 7);
		char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4); 
		value = (char) (oldCarry | (value << 1));
		
		if(rotatedBit == 0)				resetFlags(CARRY_BIT);
		else							setFlags(CARRY_BIT);
		
		if(value == 0)					setFlags(ZERO_BIT);
		else							resetFlags(ZERO_BIT);
		
		gameBoy.memory.writeByte(address, value, HardwareType.CPU);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 4;
		T += 16;
			
	}
	
	private final void rotateMemRightThroughCarry(int address){
		
		char value = gameBoy.memory.readByte(address);
		char rotatedBit = (char)(value & 1);
		char oldCarry = (char)((registers[INDEX_F] & CARRY_BIT) >> 4); 
		
		value = (char) ((oldCarry << 7) | (value >> 1));
		gameBoy.memory.writeByte(address, value, HardwareType.CPU);
		
		if(rotatedBit == 0)	resetFlags(CARRY_BIT);
		else				setFlags(CARRY_BIT);
		
		if(value == 0)		setFlags(ZERO_BIT);
		else				resetFlags(ZERO_BIT);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT);
		
		M += 4;
		T += 16;
		
	}
	
	private void swapNibbles(int regIndex){
		char lowerNibble = (char)(registers[regIndex] &0xFF);
		char upperNibble = (char)(registers[regIndex] >> 8);
		
		registers[regIndex] = (char) (lowerNibble << 8 | upperNibble);
		
		resetFlags(OP_BIT | HALF_CARRY_BIT | CARRY_BIT);
		if(registers[regIndex] == 0)
			setFlags(ZERO_BIT);
		else
			resetFlags(ZERO_BIT);
		
		M += 2;
		T += 8;
	}
	
	private final void setFlags(int mask){
		registers[INDEX_F] |= mask;
	}
	
	private final void resetFlags(int mask){
		registers[INDEX_F] &= ~mask;
	}
	
	private boolean flagsAreSet(int mask){
		return (registers[INDEX_F] & mask) != 0;
	}
	
	
	private void reset(){
		init();
	}
	
}
