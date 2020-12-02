package kr.ac.yonsei.controller;

import kr.ac.yonsei.dto.AuzreTextToSpeechRequestDto;
import kr.ac.yonsei.dto.AzureSpeechToTextRequestDto;
import kr.ac.yonsei.dto.PollyRequestDto;
import kr.ac.yonsei.dto.TranscribeRequestDto;
import kr.ac.yonsei.global.module.aws.AwsPolly;
import kr.ac.yonsei.global.module.aws.AwsTranscribe;
import kr.ac.yonsei.global.module.microsoft.AuzreSpeechToText;
import kr.ac.yonsei.global.module.microsoft.AzureTextToSpeech;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Controller
public class MainController {

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST} )
    public String index() throws Exception {

        return "main";
    }

    @RequestMapping(value = "/aws/transcribe", method = {RequestMethod.GET, RequestMethod.POST} )
    public String transcribe(Model model, @RequestParam("file") MultipartFile file, TranscribeRequestDto dto) throws Exception {

        AwsTranscribe awsTranscribe = new AwsTranscribe();
        String text = awsTranscribe.extractSpeechTextFromVoice(file, dto.getLanguage());

        model.addAttribute("text", text);

        return "main";
    }

    @RequestMapping(value = "/aws/polly", method = {RequestMethod.GET, RequestMethod.POST} )
    public String polly(Model model, PollyRequestDto dto) throws Exception {

        if(!dto.getText().equals("")){
            AwsPolly awsPolly = new AwsPolly();
            String file = awsPolly.extractVoiceFromText(dto.getText(), dto.getLanguage());

            model.addAttribute("file", file);
        }
        return "main";
    }

    @RequestMapping(value = "/azure/text", method = {RequestMethod.GET, RequestMethod.POST} )
    public String azureSpeechToText(Model model, @RequestParam("file") MultipartFile file, AzureSpeechToTextRequestDto dto) throws Exception {

        AuzreSpeechToText auzreSpeechToText = new AuzreSpeechToText();
        String text = auzreSpeechToText.speechToText(file, dto.getLanguage());

        model.addAttribute("azureText", text);

        return "main";
    }

    @RequestMapping(value = "/azure/speech", method = {RequestMethod.GET, RequestMethod.POST} )
    public String azureTextToSpeech(Model model, AuzreTextToSpeechRequestDto dto) throws Exception {

        if(!dto.getText().equals("")){

            AzureTextToSpeech azureTextToSpeech = new AzureTextToSpeech();
            String file = azureTextToSpeech.TextToSpeechSSML(dto.getText(), dto.getLanguage(), dto.getVoiceName());

            model.addAttribute("azureFile", file);
        }
        return "main";
    }
}
