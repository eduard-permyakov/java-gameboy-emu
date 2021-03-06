package emulator;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class InputHandler implements KeyListener {
	
	private ArrayList<Integer> pressedKeyCodes;
	
	private GameBoy gameBoy;
	
	public final static int JOYPAD_ADDR = 0xFF00; 
	
	private final static int A_KEY = KeyEvent.VK_A;
	private final static int B_KEY = KeyEvent.VK_S;
	private final static int UP_KEY = KeyEvent.VK_UP;
	private final static int DOWN_KEY = KeyEvent.VK_DOWN;
	private final static int LEFT_KEY = KeyEvent.VK_LEFT;
	private final static int RIGHT_KEY = KeyEvent.VK_RIGHT;
	private final static int START_KEY = KeyEvent.VK_SPACE;
	private final static int SELECT_KEY = KeyEvent.VK_ENTER;

	public InputHandler(GameBoy gameBoy){
		this.gameBoy = gameBoy;
		this.pressedKeyCodes = new ArrayList<Integer>();
	}
	
	@Override
	public synchronized void keyPressed(KeyEvent event) {
//		System.out.println("P14 low: " + isP14Low());
//		System.out.println("P15 low: " + isP15Low());
		this.pressedKeyCodes.add(event.getKeyCode());
		
		updateJoypadRegisterForPress(event);
		gameBoy.resumeCPUExecution();
	}

	@Override
	public synchronized void keyReleased(KeyEvent event) {
		this.pressedKeyCodes.remove((Object)event.getKeyCode());
		updateJoypadRegisterForRelease(event);
	}

	@Override
	public void keyTyped(KeyEvent event) {/* NOT NEEDED */}
	
	private boolean isP14Low() {
		char joypadRegVal = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		return (joypadRegVal & 0x10) == 0;
	}
	
	private boolean isP15Low() {
		char joypadRegVal = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		return (joypadRegVal & 0x20) == 0;
	}
	
	private void updateJoypadRegisterForRelease(KeyEvent event){
		
		char joypadReg = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		
		switch(event.getKeyCode()){
		case A_KEY:
		case RIGHT_KEY: 
			joypadReg |= 0x01;	break;
		case B_KEY:
		case LEFT_KEY:
			joypadReg |= 0x02;	break;
		case SELECT_KEY:
		case UP_KEY:
			joypadReg |= 0x04;	break;
		case START_KEY:
		case DOWN_KEY:
			joypadReg |= 0x08;	break;
		}
		
		gameBoy.memory.writeByte(JOYPAD_ADDR, joypadReg, HardwareType.Joypad);
		
	}
	
	private void updateJoypadRegisterForPress(KeyEvent event){
		
		char joypadReg = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		
		if(isP14Low()){
			switch(event.getKeyCode()){
			case UP_KEY:		joypadReg &= ~0x04;	break;
			case DOWN_KEY:		joypadReg &= ~0x08;	break;
			case LEFT_KEY:		joypadReg &= ~0x02;	break;
			case RIGHT_KEY:		joypadReg &= ~0x01;	break;
			default:			return;
			}
		}else if(isP15Low()){
			switch(event.getKeyCode()){
			case A_KEY:			joypadReg &= ~0x01;	break;
			case B_KEY:			joypadReg &= ~0x02;	break;
			case START_KEY:		joypadReg &= ~0x08;	break;
			case SELECT_KEY:	joypadReg &= ~0x04;	break;
			}
		}
		gameBoy.memory.writeByte(JOYPAD_ADDR, joypadReg, HardwareType.Joypad);
		raiseJoypadInterrupt();
	}
	
	private void raiseJoypadInterrupt(){
		gameBoy.requestInterrupt(Interrupt.InterruptJoypad);
	}

	public synchronized void updateJoypadRegForInputLineChange(){
		
		char joypadReg = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		
		joypadReg |= 0xF;
		
		for(Integer pressedKeyCode : this.pressedKeyCodes){
			if(isP14Low()){
				switch(pressedKeyCode){
				case UP_KEY:		joypadReg &= ~0x04;	break;
				case DOWN_KEY:		joypadReg &= ~0x08;	break;
				case LEFT_KEY:		joypadReg &= ~0x02;	break;
				case RIGHT_KEY:		joypadReg &= ~0x01;	break;
				}
			}else if(isP15Low()){
				switch(pressedKeyCode){
				case A_KEY:			joypadReg &= ~0x01;	break;
				case B_KEY:			joypadReg &= ~0x02;	break;
				case START_KEY:		joypadReg &= ~0x08;	break;
				case SELECT_KEY:	joypadReg &= ~0x04;	break;
				}
			}
		}
		
		gameBoy.memory.writeByte(JOYPAD_ADDR, joypadReg, HardwareType.Joypad);
	}
}
