package APIs;

import java.io.File;
import javax.sound.sampled.*;
public class prueba {
	String[]palabras=new String [100];
	AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
	AudioFormat aF = new AudioFormat(18000, 16, 1, true, false);
	TargetDataLine tD;
	File f = new File("jdes.flac");
	
	public prueba() {
		try {
			DataLine.Info dLI = new DataLine.Info(TargetDataLine.class,aF);
			tD = (TargetDataLine) AudioSystem.getLine(dLI);
			new CapThread().start();
			System.out.println("Grabando durante 10s...");
			Thread.sleep(10000);
			tD.close();
		}catch (Exception e) {}
	}
	
	class CapThread extends Thread {
		public void run() {
			try {
				tD.open(aF);
				tD.start();
				AudioSystem.write(new AudioInputStream(tD), aFF_T, f);
			}
			catch (Exception e){}
		}
	}
	public static void main(String[] args) {
		new prueba(); 
	}
}