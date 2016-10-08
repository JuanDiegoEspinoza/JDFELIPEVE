package APIs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import org.apache.commons.io.FileUtils;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.WebSocketManager;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.util.CredentialUtils;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.RecognizedText;

public class VoiceToText extends HttpServlet {
	public SpeechToText  s2t = new SpeechToText();
	private static final long serialVersionUID = 1L;
	private String user = "8cbb3840-bc46-4e4b-919e-fbd7ba46e36e";
	private String password="HZLhwiHjqbYV";
	private static CountDownLatch lock = new CountDownLatch(1);
	public VoiceToText() throws ServletException{
		init();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
	    String vcap = System.getProperty("VCAP_SERVICES");
	    if (vcap == null){
	    	try {
	    		vcap = FileUtils.readFileToString(new File("vcap.txt"));
	    	}
	    	catch (IOException e) {
	    		e.printStackTrace();
	    	}
	   }
	   CredentialUtils.setServices(vcap);
	   s2t.setUsernameAndPassword(user, password);
	 }
	
	public void inStreaming() throws LineUnavailableException{
		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetLine.open(format);
        targetLine.start();
        AudioInputStream audioInputStream = new AudioInputStream(targetLine);
	}
	public TargetDataLine tD;
	public AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
	AudioFormat aF = new AudioFormat(16000, 16, 1, true, false);
	File f = new File("watson.flac");
	public DataLine.Info dLI = new DataLine.Info(TargetDataLine.class,aF);
	
	public void inStreaming2(){
		try {
			System.out.println("init try");
			tD = (TargetDataLine)AudioSystem.getLine(dLI);
			new CapThread().start();
			System.out.println("Grabando durante 10s...");
			Thread.sleep(10000);
			tD.close();
		}
		catch (Exception e) {System.out.println("e");}
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
	
	public String getRespuestas(File pAudio){
		SpeechResults results =s2t.recognize(pAudio).execute();
		String respuesta = results.getResults().get(0).toString();		
		return respuesta;
	}
	
	public String obtenerConversion_a_Texto(File pAudio){
		String verito = getRespuestas(pAudio);
		int posicion = verito.indexOf("transcript") + "transcript".length();
		String cadena = verito.substring(posicion+4, verito.length()-13);
		System.out.println (cadena);
		return cadena;
	}
	

		
	 public static void main(String[] args) throws ServletException, LineUnavailableException, InterruptedException{
		   VoiceToText ss = new VoiceToText();
		   System.out.println("Init");
		   grabarAudio audioGrabado = new grabarAudio();
		   audioGrabado.inStreaming2();
		   //System.out.println(audioGrabado.getAudio().getPath());
		   //System.out.println(audioGrabado.getAudio().getName());
		   ss.obtenerConversion_a_Texto(audioGrabado.getAudio());
		   System.out.println("Finalizo");
	 }
		 
}