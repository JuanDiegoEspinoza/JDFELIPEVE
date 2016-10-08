package APIs;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javaFlacEncoder.FLAC_FileEncoder;


public class grabarAudio{

	public static TargetDataLine tD;
	public static AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
	AudioFormat aF = new AudioFormat(16000, 16, 1, true, false);
	public DataLine.Info dLI = new DataLine.Info(TargetDataLine.class,aF);
	public static FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
	public TargetDataLine tD1;
	public Thread th;
	static File audioGrabado = new File("audioGrabado.flac");
	

	public void inStreaming() throws LineUnavailableException{
		AudioFormat format = new AudioFormat(18000, 16 , 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetLine.open(format);
        targetLine.start();
	}
	
	public void detenerStreamming() throws InterruptedException{
		tD1.close();
		System.out.println("Finalizando");
	}
	
	public File getAudio(){
		return this.audioGrabado;
	}
	
	
	public void inStreaming2() throws InterruptedException, LineUnavailableException{
		System.out.println("Iniciando grabación...");
		tD1 = (TargetDataLine)AudioSystem.getLine(dLI);
		Thread th = new CapThread();
		th.start();
		System.out.println("Grabando...");
		th.sleep(10000);
		detenerStreamming();
	}
	
	class CapThread extends Thread {
		public void run() {
		try {
			tD.open(aF);
			tD.start();
			AudioSystem.write(new AudioInputStream(tD), aFF_T, audioGrabado);
		}
			catch (Exception e){}
			}
	}
	/*
	 public static void main(String[] args) throws LineUnavailableException, InterruptedException, IOException, UnsupportedAudioFileException{
		   grabarAudio ss = new grabarAudio();
		   ss.inStreaming2();
	 }
		 */
}