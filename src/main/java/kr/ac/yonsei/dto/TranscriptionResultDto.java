package kr.ac.yonsei.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptionResultDto implements Serializable {

    private List<TranscriptionTextDto> transcripts;
    private List<TranscriptionItemDto> items;

}
