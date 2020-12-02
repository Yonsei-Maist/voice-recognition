package kr.ac.yonsei.global.module.microsoft;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import net.bytebuddy.utility.RandomString;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.Future;

public class AuzreSpeechToText {

    public String speechToText(MultipartFile file, String language) throws Exception {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription("5cbb2b6a9e97434daa971cbd769e5175", "koreacentral");
        String text = fromFile(speechConfig, file, language);

        return text;
    }

    public String fromFile(SpeechConfig speechConfig, MultipartFile orifile, String language) throws Exception {

        File dir = new File("c:/Temp/auzre_speech_to_text");
        if(dir.exists() == false) {
            dir.mkdirs();
        }
        File[] fileList = dir.listFiles(); //파일리스트 얻어오기

        System.out.println(fileList.length);
        for (int j = 0; j < fileList.length; j++) {
            fileList[j].delete(); //파일 삭제
        }

        File file = new File(dir+"/auzre_speech_to_text"+ RandomString.make());
        orifile.transferTo(file);

        AudioConfig audioConfig = AudioConfig.fromWavFileInput(file.getPath());
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, language, audioConfig);

        Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
        SpeechRecognitionResult result = task.get();

        String text ="";
        switch (result.getReason()) {
            case RecognizedSpeech:
                text = result.getText();
                break;
            case NoMatch:
                text = "NOMATCH: Speech could not be recognized.";
                break;
            case Canceled: {
                CancellationDetails cancellation = CancellationDetails.fromResult(result);
                text = "CANCELED: Reason=" + cancellation.getReason();

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
            }
            break;
        }
        System.out.println(text);
        return result.getText();
    }
}
