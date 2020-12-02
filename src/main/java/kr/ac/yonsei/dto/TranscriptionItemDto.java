package kr.ac.yonsei.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptionItemDto implements Serializable {

    private String start_time;
    private String end_time;
    private Object alternatives;
    private String type;

}
