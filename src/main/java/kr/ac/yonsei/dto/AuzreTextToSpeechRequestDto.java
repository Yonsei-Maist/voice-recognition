package kr.ac.yonsei.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuzreTextToSpeechRequestDto {

    private String text;
    private String language;
    private String voiceName;

    public String getLanguage() {
        language = voiceName.substring(0, 5);
        return language;
    }
}
