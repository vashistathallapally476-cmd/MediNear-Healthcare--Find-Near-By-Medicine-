package com.mednear.service;

import com.mednear.dto.response.FastApiOcrResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class OcrClientService {

    private static final Logger log = LoggerFactory.getLogger(OcrClientService.class);

    private final WebClient webClient;
    private final String ocrServiceUrl;
    private final long timeoutMs;

    public OcrClientService(
            @Value("${ocr.service.url:http://localhost:5000/api/v1/analyze}") String ocrServiceUrl,
            @Value("${ocr.service.timeout-ms:5000}") long timeoutMs) {
        this.ocrServiceUrl = ocrServiceUrl;
        this.timeoutMs = timeoutMs;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
    }

    public FastApiOcrResponse analyzeImage(MultipartFile file) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            // Provide both "image" and "file" part names so Python accepts either parameter name
            builder.part("image", file.getResource()).filename(file.getOriginalFilename());
            builder.part("file", file.getResource()).filename(file.getOriginalFilename());

            return webClient.post()
                    .uri(ocrServiceUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(FastApiOcrResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

        } catch (Exception e) {
            log.warn("OCR & ML microservice call to {} failed: {}", ocrServiceUrl, e.getMessage());
            FastApiOcrResponse fallback = new FastApiOcrResponse();
            fallback.setSuccess(false);
            fallback.setError("OCR microservice unavailable or timed out: " + e.getMessage());
            return fallback;
        }
    }
}
