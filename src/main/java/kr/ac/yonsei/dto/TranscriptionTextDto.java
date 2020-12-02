package kr.ac.yonsei.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptionTextDto implements Serializable {

    private String transcript;
}
