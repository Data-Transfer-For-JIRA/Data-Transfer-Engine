package com.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class WebClientUtils {

    public static WebClient createJiraWebClient(String baseUrl, String jiraId, String apiToken) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Basic " + getBase64Credentials(jiraId, apiToken))
                .build();
    }

    private static String getBase64Credentials(String jiraID, String jiraPass) {
        String credentials = jiraID + ":" + jiraPass;
        return new String(Base64.getEncoder().encode(credentials.getBytes()));
    }

    public static <T> Mono<T> get(WebClient webClient, String uri, Class<T> responseType) {

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> get(WebClient webClient, String uri, ParameterizedTypeReference<T> elementTypeRef) {

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(elementTypeRef);
    }

    public static <T> Mono<T> post(WebClient webClient, String uri, Object requestBody, Class<T> responseType) {

        return webClient.post()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseType);
    }


    public static <T> Mono<T> put(WebClient webClient, String uri, Object requestBody, Class<T> responseType) {

        return webClient.put()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> delete(WebClient webClient, String uri, Class<T> responseType) {

        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }
    public static <T> Flux<T> postByFlux(WebClient webClient, String uri, Object requestBody, Class<T> responseType) {

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

    private static Flux<DataBuffer> getBufferFlux(String data, DataBufferFactory bufferFactory, int bufferSize) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); 
        // 문자열 data를 UTF-8 인코딩의 바이트 배열로 변환 후 ByteArrayInputStream로 생성(바이트 배열을 순차적으로 읽을수있음)
        // ByteArrayInputStream 바이트 배열을 입력 스트림으로 처리할 수 있게 해주는 클래스
        // 자바에서 스트림(Stream)은 데이터를 순차적으로 읽고 쓰기 위한 추상화된 개념으로, InputStream은 바이트 단위로 데이터를 읽어들이는 스트림

        return DataBufferUtils.readInputStream(() -> bis, bufferFactory, bufferSize);
        //InputStream에서 데이터를 읽어서 bufferSize 크기의 DataBuffer로 나눈 후, 이들을 Flux<DataBuffer>로 반환
    }

    public static Optional<Boolean> executePut(WebClient webClient, String uri, Object requestBody) {

        Mono<ResponseEntity<Void>> response = webClient.put()
                .uri(uri)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .toEntity(Void.class);

        return response.map(entity -> entity.getStatusCode() == HttpStatus.NO_CONTENT) // 결과가 204인가 확인
                .blockOptional();
    }

}
