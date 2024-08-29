package com.utils;

import com.jira.account.model.dto.AdminInfoDTO;
import com.jira.account.service.Account;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.io.FileOutputStream;
import java.io.IOException;
@Component
public class WebClientUtils {

    private final WebClient webClient;

    private final WebClient webClientForImage;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    public WebClientUtils(Account account) {
        AdminInfoDTO info = account.getAdminInfo(1);
        this.webClient = createJiraWebClient(info.getUrl(), info.getId(), info.getToken());
        this.webClientForImage = createJiraWebClientForImage(info.getUrl(), info.getId(), info.getToken());
    }

    public static WebClient createJiraWebClient(String baseUrl, String jiraId, String apiToken) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Basic " + getBase64Credentials(jiraId, apiToken))
                .build();
    }

    public static WebClient createJiraWebClientForImage(String baseUrl, String jiraId, String apiToken) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .defaultHeader("Authorization", "Basic " + getBase64Credentials(jiraId, apiToken))
                .build();
    }

    private static String getBase64Credentials(String jiraID, String jiraPass) {
        String credentials = jiraID + ":" + jiraPass;
        return new String(Base64.getEncoder().encode(credentials.getBytes()));
    }

    public <T> Mono<T> get(String uri, Class<T> responseType) {

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Mono<T> get(String uri, ParameterizedTypeReference<T> elementTypeRef) {

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(elementTypeRef);
    }

    /*
    * 서버의 정책: 특정 리소스에 접근하려면 인증 또는 특정 조건이 필요할 때 서버가 리소스에 직접 접근하는 것을 막고, 대신 다른 URL로 리디렉션하도록 설정될 수 있습니다.
    * 파일 다운로드 처리: 특히 파일이나 이미지 같은 리소스를 제공할 때, 서버는 실제 파일을 제공하기 위해 클라이언트를 다른 URL로 리디렉션하는 방식을 사용할 수 있습니다. 이 방식은 CDN(Content Delivery Network) 같은 곳에서 파일을 제공할 때 자주 사용됩니다.
    * 보안 또는 권한 문제: 어떤 경우에는 요청한 URL이 직접 접근할 수 없는 위치에 있거나, 접근 권한이 없을 때, 서버가 이를 다른 안전한 위치로 리디렉션 시킬 수 있습니다.
    * */
    public void downloadImage(String uri, String fileName) {
        String destinationFile = "C:/JIRA/images/"+ fileName;

        // 허용된 이미지 확장자 목록
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

        // 파일 이름에서 확장자 추출
        String fileExtension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = fileName.substring(i + 1).toLowerCase();
        }

        // 확장자가 허용된 이미지 확장자인지 확인
        if (!allowedExtensions.contains(fileExtension)) {
            logger.error(":: 이미지 다운로드 오류 :: 지원되지 않는 파일 확장자: {}", fileExtension);
            return;
        }

        try {
            // 이미지를 동기적으로 다운로드 -> 다운로드 요청
            // 서버로 부터 응답을 받아옴
            WebClient.ResponseSpec responseSpec = webClientForImage.get()
                    .uri(uri)
                    .retrieve();

            // 리디렉션 여부 확인
            // 응답 본문에서 로케이션 값을 확인함
            String redirectUri = responseSpec.toBodilessEntity() // 응답 본문 제외 헤더 데이터만 수신
                    .flatMap(responseEntity -> {
                        if (responseEntity.getStatusCode().is3xxRedirection()) { // 리다이렉션 여부 확인 303에러 발생 (웹클라언트는 리다이렉션 해주지 않음)
                            return Mono.just(responseEntity.getHeaders().getLocation().toString()); // 로케이션 값 반환
                        } else {
                            return Mono.empty();
                        }
                    }).block();

            byte[] imageBytes;

            // 리디렉션이 발생한 경우, 새로운 URI로 재요청
            if (redirectUri != null) {
                imageBytes = webClientForImage.get()
                        .uri(redirectUri)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
            } else {
                // 리디렉션이 없으면 원래 응답을 처리
                imageBytes = responseSpec.bodyToMono(byte[].class).block();
            }

            // 다운로드된 이미지를 파일로 저장
            if (imageBytes != null) {
                try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                    outputStream.write(imageBytes);
                    logger.info(":: 이미지 다운로드 성공 :: 경로: {}, " ,destinationFile);
                } catch (IOException e) {
                    logger.error(":: 이미지 다운로드 오류 :: 이미지 로컬 서버 저장 실패");
                }
            } else {
                logger.error(":: 이미지 다운로드 오류 :: 수신된 데이터가 없음");
            }
        } catch (Exception e) {
            logger.error(":: 이미지 다운로드 오류::");
        }
    }


    public <T> Mono<T> post(String uri, Object requestBody, Class<T> responseType) {

        return webClient.post()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseType);
    }


    public <T> Mono<T> put(String uri, Object requestBody, Class<T> responseType) {

        return webClient.put()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Mono<T> delete(String uri, Class<T> responseType) {

        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }
    public <T> Flux<T> postByFlux(String uri, Object requestBody, Class<T> responseType) {

        // 리쿼스트 객체를 JSON 타입으로 형변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 널들어있는 필드 제거
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody); // 데이터
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        Flux<DataBuffer> dataBufferFlux = getBufferFlux(jsonRequestBody, bufferFactory, 1024); // 1kb 단위로 데이터 읽어 데이터 버퍼 스트림 생성

        return webClient.post()
                .uri(uri)
                .body(BodyInserters.fromDataBuffers(dataBufferFlux))
                .retrieve()
                .bodyToFlux(responseType);
    }

    private Flux<DataBuffer> getBufferFlux(String data, DataBufferFactory bufferFactory, int bufferSize) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); 
        // 문자열 data를 UTF-8 인코딩의 바이트 배열로 변환 후 ByteArrayInputStream로 생성(바이트 배열을 순차적으로 읽을수있음)
        // ByteArrayInputStream 바이트 배열을 입력 스트림으로 처리할 수 있게 해주는 클래스
        // 자바에서 스트림(Stream)은 데이터를 순차적으로 읽고 쓰기 위한 추상화된 개념으로, InputStream은 바이트 단위로 데이터를 읽어들이는 스트림

        return DataBufferUtils.readInputStream(() -> bis, bufferFactory, bufferSize);
        //InputStream에서 데이터를 읽어서 bufferSize 크기의 DataBuffer로 나눈 후, 이들을 Flux<DataBuffer>로 반환
    }

    public Optional<Boolean> executePut(String uri, Object requestBody) {

        Mono<ResponseEntity<Void>> response = webClient.put()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .toEntity(Void.class);

        return response.map(entity -> entity.getStatusCode() == HttpStatus.NO_CONTENT) // 결과가 204인가 확인
                .blockOptional();
    }

    public Optional<Boolean> executeDelete(String uri) {

        Mono<ResponseEntity<Void>> response = webClient.delete()
                .uri(uri)
                .retrieve()
                .toEntity(Void.class);

        return response.map(entity -> entity.getStatusCode() == HttpStatus.NO_CONTENT) // 결과가 204인가 확인
                .blockOptional();
    }

}
