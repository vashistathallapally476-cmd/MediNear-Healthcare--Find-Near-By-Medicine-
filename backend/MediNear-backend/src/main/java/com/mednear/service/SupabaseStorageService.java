package com.mednear.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    public SupabaseStorageService(
            WebClient.Builder webClientBuilder
    ) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Upload a pharmacy image to Supabase Storage.
     *
     * Storage path:
     *
     * pharmacy-images/
     *     stores/
     *         {storeId}/
     *             {random-file-name}.jpg
     */
    public String uploadPharmacyImage(
            MultipartFile file,
            Long storeId
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Pharmacy image is required"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
            !(
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp")
            )
        ) {
            throw new IllegalArgumentException(
                "Only JPG, PNG and WEBP images are allowed"
            );
        }

        /*
         * Maximum image size = 5 MB.
         */
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Pharmacy image must be smaller than 5 MB"
            );
        }

        String extension =
            getExtension(file.getOriginalFilename());

        String fileName =
            "stores/" +
            storeId +
            "/" +
            UUID.randomUUID() +
            extension;

        String uploadUrl =
            supabaseUrl +
            "/storage/v1/object/" +
            bucket +
            "/" +
            fileName;

        ByteArrayResource resource =
            new ByteArrayResource(file.getBytes()) {

                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

        webClient
            .post()
            .uri(uploadUrl)
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + serviceKey
            )
            .header(
                "apikey",
                serviceKey
            )
            .header(
                "x-upsert",
                "true"
            )
            .contentType(
                MediaType.parseMediaType(contentType)
            )
            .bodyValue(resource)
            .retrieve()
            .toBodilessEntity()
            .block();

        /*
         * Public URL that frontend can use directly.
         */
        return supabaseUrl +
            "/storage/v1/object/public/" +
            bucket +
            "/" +
            fileName;
    }

    private String getExtension(String filename) {

        if (filename == null ||
            !filename.contains(".")) {

            return ".jpg";
        }

        return filename
            .substring(
                filename.lastIndexOf(".")
            )
            .toLowerCase();
    }
}