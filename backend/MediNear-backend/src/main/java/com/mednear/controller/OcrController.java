package com.mednear.controller;

import com.mednear.dto.response.OcrIdentificationResponse;
import com.mednear.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping(
        value = "/identify",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<OcrIdentificationResponse> identify(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
            ocrService.identifyMedicine(file)
        );
    }
}
