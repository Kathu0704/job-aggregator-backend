package com.jobportal.job_aggregator.controller;

import com.jobportal.job_aggregator.model.ResumeAnalysisResult;
import com.jobportal.job_aggregator.service.ResumeAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeAnalyzerService resumeAnalyzerService;

    public ResumeController(ResumeAnalyzerService resumeAnalyzerService) {
        this.resumeAnalyzerService = resumeAnalyzerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please upload a file");
            }

            String filename = file.getOriginalFilename();
            if (filename == null ||
                    (!filename.endsWith(".pdf") && !filename.endsWith(".docx"))) {
                return ResponseEntity.badRequest().body("Only PDF and DOCX files are supported");
            }

            ResumeAnalysisResult result = resumeAnalyzerService.analyzeResume(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error analyzing resume: " + e.getMessage());
        }
    }
}