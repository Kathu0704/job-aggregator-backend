package com.jobportal.job_aggregator.controller;

import com.jobportal.job_aggregator.model.ResumeAnalysisResult;
import com.jobportal.job_aggregator.service.ResumeAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeAnalyzerService resumeAnalyzerService;

    public ResumeController(ResumeAnalyzerService resumeAnalyzerService) {
        this.resumeAnalyzerService = resumeAnalyzerService;
    }

    // Step 1: Upload resume → get category + matching jobs
    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty())
                return ResponseEntity.badRequest().body("Please upload a file");

            String filename = file.getOriginalFilename();
            if (filename == null ||
                    (!filename.endsWith(".pdf") && !filename.endsWith(".docx")))
                return ResponseEntity.badRequest().body("Only PDF and DOCX supported");

            Map<String, Object> result = resumeAnalyzerService.parseResumeAndGetJobs(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    // Step 2: Select a job → get keyword suggestions
    @PostMapping("/match")
    public ResponseEntity<?> matchResumeToJob(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobId") Long jobId) {
        try {
            ResumeAnalysisResult result =
                    resumeAnalyzerService.analyzeResumeVsJob(resumeText, jobId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}
