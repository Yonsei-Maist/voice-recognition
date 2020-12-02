package kr.ac.yonsei.global.module.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.yonsei.dto.TranscriptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@Slf4j
@Component
public class AwsTranscribe {

    @Value("${amazon.aws.accesskey}")
    private String accesskey;

    @Value("${amazon.aws.secretkey}")
    private String secretkey;

    @Value("${amazon.aws.region}")
    private String region;

    private static String bucketName = "transcribevoicebucket";

    private static AWSCredentials awsCredentials;
    private static Regions regions;
    private static AWSStaticCredentialsProvider awsStaticCredentialsProvider;


    @PostConstruct
    void init(){ //  Client 객체 생성
        awsCredentials = new BasicAWSCredentials(accesskey, secretkey);
        regions = Regions.fromName(region);
    }

    public AmazonTranscribe getTranscribeClient() {
        System.out.println("getTranscribeClient");
        return AmazonTranscribeClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(regions)
                .build();
    }

    public AmazonS3 getS3Client() {
        System.out.println("getS3Client");
        return AmazonS3ClientBuilder.standard()
                .withRegion(regions)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public void uploadFileToAwsBucket(MultipartFile file) {
        System.out.println("1) uploadFileToAwsBucket");
        String key = file.getOriginalFilename().replaceAll(" ", "_").toLowerCase();

        try {
            getS3Client().putObject(bucketName, key, file.getInputStream(), null);
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFileFromAwsBucket(String fileName) {

        System.out.println("4) Delete File from AWS Bucket {}"+fileName);
        String key = fileName.replaceAll(" ", "_").toLowerCase();
        getS3Client().deleteObject(bucketName, key);
    }

    public StartTranscriptionJobResult startTranscriptionJob(String key, String language) {

        System.out.println("2) Start Transcription Job By Key {}" + key);

        Media media = new Media().withMediaFileUri(getS3Client().getUrl(bucketName, key).toExternalForm());
        String jobName = key.concat(RandomString.make());

        StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest()
                .withLanguageCode(language).withTranscriptionJobName(jobName).withMedia(media);
        StartTranscriptionJobResult startTranscriptionJobResult = getTranscribeClient()
                .startTranscriptionJob(startTranscriptionJobRequest);

        return startTranscriptionJobResult;
    }

    public GetTranscriptionJobResult getTranscriptionJobResult(String jobName) {

        System.out.println("3) Get Transcription Job Result By Job Name : {}"+ jobName);

        GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest().withTranscriptionJobName(jobName);
        Boolean resultFound = false;

        TranscriptionJob transcriptionJob = new TranscriptionJob();

        GetTranscriptionJobResult getTranscriptionJobResult = new GetTranscriptionJobResult();

        while (resultFound == false) {
            getTranscriptionJobResult = getTranscribeClient().getTranscriptionJob(getTranscriptionJobRequest);
            transcriptionJob = getTranscriptionJobResult.getTranscriptionJob();

            if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.COMPLETED.name())) {
                return getTranscriptionJobResult;
            } else if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.FAILED.name())) {
                return null;
            } else if (transcriptionJob.getTranscriptionJobStatus()
                    .equalsIgnoreCase(TranscriptionJobStatus.IN_PROGRESS.name())) {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    log.debug("Interrupted Exception {}", e.getMessage());
                }
            }
        }
        return getTranscriptionJobResult;
    }

    TranscriptionResponseDto downloadTranscriptionResponse(String uri) throws Exception {
        System.out.println("5) Download Transcription Result from Transcribe URi {}" + uri);

        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        con.setRequestMethod("GET");
        con.setDoOutput(true);

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        TranscriptionResponseDto response = objectMapper.readValue(sb.toString(), TranscriptionResponseDto.class);

        return response;
    }

    void deleteTranscriptionJob(String jobName) {
        System.out.println("6) Delete Transcription Job from amazon Transcribe {}"+ jobName);
        DeleteTranscriptionJobRequest deleteTranscriptionJobRequest = new DeleteTranscriptionJobRequest()
                .withTranscriptionJobName(jobName);
        getTranscribeClient().deleteTranscriptionJob(deleteTranscriptionJobRequest);
    }


    public String extractSpeechTextFromVoice(MultipartFile file, String language) throws Exception {
        System.out.println("Request to extract Speech Text from Voice : {}" + file);

        String fileName = "attention_please";
        // 버킷 생성
        uploadFileToAwsBucket(file);
        // key 생성 transcription job에 사용
        String key = file.getOriginalFilename().replaceAll(" ", "_").toLowerCase();
        // 트랜잭션 시작
        StartTranscriptionJobResult startTranscriptionJobResult = startTranscriptionJob(key, language);
        // 작업이름
        String transcriptionJobName = startTranscriptionJobResult.getTranscriptionJob().getTranscriptionJobName();
        // 결과 반환
        GetTranscriptionJobResult getTranscriptionJobResult = getTranscriptionJobResult(transcriptionJobName);
        // 버킷 파일 삭제
        deleteFileFromAwsBucket(key);
        // 결과 파일의  url
        String transcriptFileUriString = getTranscriptionJobResult.getTranscriptionJob().getTranscript().getTranscriptFileUri();
        // 파일 다운로드
        TranscriptionResponseDto dto  = downloadTranscriptionResponse(transcriptFileUriString);
        // 결과 추출
        String result = dto.getResults().getTranscripts().get(0).getTranscript();
        // 트랜잭션 삭제
        deleteTranscriptionJob(transcriptionJobName);

        return result;
    }
}
