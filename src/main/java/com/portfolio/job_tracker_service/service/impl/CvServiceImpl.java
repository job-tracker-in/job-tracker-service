package com.portfolio.job_tracker_service.service.impl;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import com.portfolio.job_tracker_service.model.entity.UserCvEntity;
import com.portfolio.job_tracker_service.model.response.CvResponse;
import com.portfolio.job_tracker_service.repository.CvRepository;
import com.portfolio.job_tracker_service.service.CvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvServiceImpl implements CvService {

    private static final long MAX_SIZE_BYTES = 200 * 1024; // 200KB
    private final CvRepository cvRepository;

    @Override
    public CvResponse upload(MultipartFile file, UUID userId) {
        validate(file);

        byte[] pdfBytes;
        String extractedText;
        try {
            pdfBytes = file.getBytes();
            extractedText = extractText(pdfBytes);
        } catch (IOException e) {
            throw new JobApplicationException(ErrorCode.VALIDATION_FAILED,
                    HttpStatus.BAD_REQUEST, "Failed to read PDF file.", Map.of());
        }

        UserCvEntity entity = new UserCvEntity(userId, file.getOriginalFilename(), pdfBytes, extractedText, null);
        cvRepository.save(entity);

        return cvRepository.findByUserId(userId)
                .map(cv -> new CvResponse(cv.filename(), cv.uploadedAt()))
                .orElseThrow();
    }

    @Override
    public Optional<CvResponse> get(UUID userId) {
        return cvRepository.findByUserId(userId)
                .map(cv -> new CvResponse(cv.filename(), cv.uploadedAt()));
    }

    @Override
    public void delete(UUID userId) {
        cvRepository.deleteByUserId(userId);
    }

    @Override
    public Optional<String> getExtractedText(UUID userId) {
        return cvRepository.findByUserId(userId).map(UserCvEntity::extractedText);
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new JobApplicationException(ErrorCode.VALIDATION_FAILED,
                    HttpStatus.BAD_REQUEST, "File is empty.", Map.of());
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new JobApplicationException(ErrorCode.VALIDATION_FAILED,
                    HttpStatus.BAD_REQUEST,
                    "File too large. Max size is 200KB.",
                    Map.of("size", file.getSize(), "maxSize", MAX_SIZE_BYTES));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new JobApplicationException(ErrorCode.VALIDATION_FAILED,
                    HttpStatus.BAD_REQUEST, "Only PDF files are accepted.", Map.of());
        }
    }

    private String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        }
    }
}
