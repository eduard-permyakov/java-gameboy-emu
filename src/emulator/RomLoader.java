package emulator;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class RomLoader {
	
	private GameBoy gameBoy;
	
	public RomLoader(GameBoy gameBoy){
		this.gameBoy = gameBoy;
	}
	
	public void loadROM(String filepath){
		
		DataInputStream input = null;
		try{
			input = new DataInputStream(new FileInputStream(new File(filepath)));
			
			int offset = 0;
			while(input.available() > 0) {
				
				gameBoy.memory[0x0000 + offset] = (char)(input.readByte() & 0xFF);
				offset++;
			}
			
		}catch(IOException e){
			e.printStackTrace();
			System.exit(0);
		}finally{
			if(input != null){
				try{ input.close(); }catch(IOException ex){}
			}
			try {
				writeROMInfo();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void writeROMInfo() throws UnsupportedEncodingException {
				
		char[] title = Arrays.copyOfRange(gameBoy.memory, 0x0134, 0x0143);
		byte[] byteTitle = new byte[title.length];
		for(int i = 0; i < title.length; i++){
			byteTitle[i] = (byte)(title[i]);
		}
		System.out.println("Title: " + new String(byteTitle, "UTF-8"));
		
		char[] publisher = Arrays.copyOfRange(gameBoy.memory, 0x0144, 0x0145);
		byte[] bytePublisher = new byte[title.length];
		for(int i = 0; i < publisher.length; i++){
			bytePublisher[i] = (byte)(publisher[i]);
		}
		System.out.println("Publisher(new): " + new String(bytePublisher, "UTF-8"));
		
		System.out.println("Super GameBoy: "+ ((gameBoy.memory[0x146]) != 0 ? gameBoy.memory[0x146]: "0"));
		
		System.out.println("Cartridge type: "+ (int)(gameBoy.memory[0x147]));
		
		System.out.println("ROM size: " + (0x8000 << gameBoy.memory[0x148]));
		
		System.out.println("Destination: " + ((gameBoy.memory[0x14A]) != 0 ? gameBoy.memory[0x14A]: "0"));
		
		System.out.println("Publisher(old)" + (int)gameBoy.memory[0x014B]);
		
		System.out.println("Version: " + ((gameBoy.memory[0x14C]) != 0 ? gameBoy.memory[0x14C]: "0"));

	}
	
}
