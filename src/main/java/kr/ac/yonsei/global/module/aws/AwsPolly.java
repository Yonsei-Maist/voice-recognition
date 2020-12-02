package kr.ac.yonsei.global.module.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class AwsPolly {

    @Value("${amazon.aws.accesskey}")
    private String accesskey;

    @Value("${amazon.aws.secretkey}")
    private String secretkey;

    @Value("${amazon.aws.region}")
    private String region;

    private static String bucketName = "transcribevoicebucket";
    private static AWSCredentials awsCredentials;
    private static Regions regions;
    private AmazonPollyClient polly;
    private Voice voice;

    @PostConstruct
    void init() {
        awsCredentials = new BasicAWSCredentials(accesskey, secretkey);
        regions = Regions.fromName(region);
    }

    public AmazonS3 getS3Client() {
        System.out.println("getS3Client");
        return AmazonS3ClientBuilder.standard()
                .withRegion(regions)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public void settingPolly(String language) {
        polly = new AmazonPollyClient(awsCredentials,
                new ClientConfiguration());
        polly.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));

        DescribeVoicesRequest allVoicesRequest  = new DescribeVoicesRequest();
        DescribeVoicesRequest koKRVoicesRequest = new DescribeVoicesRequest().withLanguageCode(language);

        DescribeVoicesResult describeVoicesResult = polly.describeVoices(koKRVoicesRequest);
        voice = describeVoicesResult.getVoices().get(0);
    }

    public InputStream synthesize(String text, OutputFormat format) throws IOException {
        SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
                .withText(text)
                .withVoiceId(voice.getId())
                .withOutputFormat(format);

        SynthesizeSpeechResult synthesizeSpeechResult = polly.synthesizeSpeech(synthesizeSpeechRequest);

        return synthesizeSpeechResult.getAudioStream();
    }

    public String uploadStreamToAwsBucket(InputStream speechStream, String fileName) {

        getS3Client().deleteObject(bucketName, fileName); // 이름 같은 버킷 객체 삭제

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("wav");

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, speechStream, new ObjectMetadata());
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // 버킷 객체 접근 권한 생성
        getS3Client().putObject(putObjectRequest); // 이름 버킷 객체 생성

        String url = getS3Client().getUrl(bucketName, fileName).toString(); // 버킷 객체 Url

        return url;
    }

    public String uploadFileToAwsBucket(File file, String fileName) {

        getS3Client().deleteObject(bucketName, fileName); // 이름 같은 버킷 객체 삭제

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("mp3");

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file);
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead); // 버킷 객체 접근 권한 생성
        getS3Client().putObject(putObjectRequest); // 이름 버킷 객체 생성

        String url = getS3Client().getUrl(bucketName, fileName).toString(); // 버킷 객체 Url

        return url;
    }

    public String extractVoiceFromText(String text, String language) throws Exception {

        String fileName = "speak.mp3"+ RandomString.make();

        this.settingPolly(language);
        InputStream speechStream = this.synthesize(text, OutputFormat.Mp3);
        String speakFileUrl = uploadStreamToAwsBucket(speechStream, fileName);

        return speakFileUrl;
    }
}
