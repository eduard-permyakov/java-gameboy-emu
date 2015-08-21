package emulator;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
	
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
	}
	
	@Override
	public void keyPressed(KeyEvent event) {
		updateJoypadRegister(event);
		gameBoy.resumeCPUExecution();
	}

	@Override
	public void keyReleased(KeyEvent event) {
		updateJoypadRegister(event);
	}

	@Override
	public void keyTyped(KeyEvent event) {/* NOT NEEDED */}
	
	private boolean isP14Low() {
		char joypadRegVal = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		return ((joypadRegVal >> 4) & 0b1) == 0b1;
	}
	
	private boolean isP15Low() {
		char joypadRegVal = gameBoy.memory.readByte(InputHandler.JOYPAD_ADDR);
		return ((joypadRegVal >> 5) & 0b1) == 0b1;
	}
	
	private void updateJoypadRegister(KeyEvent event){
		if(isP14Low()){
			switch(event.getKeyCode()){
			case A_KEY:			break;
			case B_KEY:			break;
			case UP_KEY:		
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x04, isKeyLow(event), HardwareType.Joypad);	
				break;
			case DOWN_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x08, isKeyLow(event), HardwareType.Joypad);	
				break;
			case LEFT_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x02, isKeyLow(event), HardwareType.Joypad);	
				break;
			case RIGHT_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x01, isKeyLow(event), HardwareType.Joypad);	
				break;
			case START_KEY:		break;
			case SELECT_KEY: 	break;
			default:			return;
			}
		}else if(isP15Low()){
			switch(event.getKeyCode()){
			case A_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x01, isKeyLow(event), HardwareType.Joypad);	
				break;
			case B_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x02, isKeyLow(event), HardwareType.Joypad);	
				break;
			case UP_KEY:		break;
			case DOWN_KEY:		break;
			case LEFT_KEY: 		break;
			case RIGHT_KEY:		break;
			case START_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x08, isKeyLow(event), HardwareType.Joypad);	
				break;
			case SELECT_KEY:
				gameBoy.memory.setMask(InputHandler.JOYPAD_ADDR, (char)0x04, isKeyLow(event), HardwareType.Joypad);	
				break;
			default:			return;
			}
		}
		raiseJoypadInterrupt();
	}
	
	private boolean isKeyLow(KeyEvent event){
		return event.getID() == KeyEvent.KEY_RELEASED;
	}
	
	private void raiseJoypadInterrupt(){
		gameBoy.interruptCPU(Interrupt.InterruptJoypad);
	}

}
