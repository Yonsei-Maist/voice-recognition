package kr.ac.yonsei.global.module.microsoft;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import kr.ac.yonsei.global.module.aws.AwsPolly;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

@Component
public class AzureTextToSpeech {

    @Value("${microsoft.azure.key}")
    private String key;

    @Value("${microsoft.azure.region}")
    private String region;

    private static SpeechConfig speechConfig;

    @PostConstruct
    void init() {
        speechConfig = SpeechConfig.fromSubscription(key, region);
    }

    public String TextToSpeech(String text, String language) throws Exception {

        String speakFile = "speakFile"+RandomString.make()+".wav";
        AudioConfig audioConfig = AudioConfig.fromWavFileOutput(speakFile);

        AutoDetectSourceLanguageConfig autoDetectSourceLanguageConfig =
                AutoDetectSourceLanguageConfig.fromLanguages(Arrays.asList(language));

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
        synthesizer.SpeakText(text);

        AwsPolly polly = new AwsPolly();

        File file = new File(speakFile);
        String speakFileUrl = polly.uploadFileToAwsBucket(file, speakFile);

        return speakFileUrl;
    }

    public String TextToSpeechSSML(String text, String language, String voiceName) throws Exception {

        String speakFile = "speakFile"+RandomString.make()+".wav";
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);

        Document doc = null;
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.newDocument();
        if (doc != null){
            Element speak = doc.createElement("speak");
            speak.setAttribute("version", "1.0");
            speak.setAttribute("xmlns", "https://www.w3.org/2001/10/synthesis");
            speak.setAttribute("xml:lang", language);

            Element voice = doc.createElement("voice");
            voice.setAttribute("name", voiceName);
            voice.appendChild(doc.createTextNode(text));

            speak.appendChild(voice);
            doc.appendChild(speak);
        }

        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        String ssml = writer.getBuffer().toString();
        SpeechSynthesisResult result = synthesizer.SpeakSsml(ssml);
        byte[] audioData = result.getAudioData();
        InputStream inputStream = new ByteArrayInputStream(audioData);

        AwsPolly polly = new AwsPolly();
        String speakFileUrl = polly.uploadStreamToAwsBucket(inputStream, speakFile);

        return speakFileUrl;
    }

    private static String xmlToString(String filePath) {
        File file = new File(filePath);
        StringBuilder fileContents = new StringBuilder((int)file.length());

        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString().trim();
        } catch (FileNotFoundException ex) {
            return "File not found.";
        }
    }
}
