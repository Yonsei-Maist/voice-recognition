package kr.ac.yonsei.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscriptionResponseDto implements Serializable {

    private String jobName;
    private String accountId;
    private TranscriptionResultDto results;
    private String status;

}
