package com.mednear.service;

import com.mednear.dto.response.OcrIdentificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class OcrService {

    private final WebClient webClient;

    @Value("${ocr.service.url:http://192.168.1.28:8000}")
    private String ocrServiceUrl;

    public OcrService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public OcrIdentificationResponse identifyMedicine(MultipartFile file) {

        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            return webClient
                    .post()
                    .uri(ocrServiceUrl + "/api/v1/identify")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(OcrIdentificationResponse.class)
                    .block();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded image", e);
        }
    }
}