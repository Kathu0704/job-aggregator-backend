package com.jobportal.job_aggregator.service;

import com.jobportal.job_aggregator.model.Job;
import com.jobportal.job_aggregator.model.ResumeAnalysisResult;
import com.jobportal.job_aggregator.repository.JobRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeAnalyzerService {

    private final JobRepository jobRepository;
    private final Tika tika;

    private static final List<String> IT_SKILLS = Arrays.asList(
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "spring", "spring boot", "hibernate", "node", "express", "django", "flask",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "git", "github",
            "html", "css", "bootstrap", "tailwind", "rest", "api", "microservices",
            "linux", "bash", "maven", "gradle", "junit", "agile", "scrum",
            "machine learning", "deep learning", "tensorflow", "pytorch", "data science",
            "pandas", "numpy", "devops", "kafka", "graphql", "flutter", "kotlin"
    );

    private static final List<String> NON_IT_SKILLS = Arrays.asList(
            "accounting", "finance", "excel", "tally", "gst", "tds", "payroll",
            "marketing", "sales", "seo", "social media", "content writing",
            "hr", "recruitment", "operations", "logistics", "supply chain",
            "graphic design", "figma", "photoshop", "illustrator",
            "project management", "leadership", "communication", "teamwork",
            "customer service", "business development", "crm", "erp",
            "banking", "insurance", "legal", "compliance", "audit"
    );

    public ResumeAnalyzerService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
        this.tika = new Tika();
    }

    // Step 1: Parse resume and detect category + return matched jobs
    public Map<String, Object> parseResumeAndGetJobs(MultipartFile file) throws Exception {
        String resumeText = tika.parseToString(file.getInputStream()).toLowerCase();

        // Count IT vs Non-IT skill matches
        long itCount = IT_SKILLS.stream()
                .filter(s -> resumeText.contains(s.toLowerCase())).count();
        long nonItCount = NON_IT_SKILLS.stream()
                .filter(s -> resumeText.contains(s.toLowerCase())).count();

        String detectedCategory = itCount >= nonItCount ? "IT" : "Non-IT";

        // Get skills found in resume
        List<String> skillSet = detectedCategory.equals("IT") ? IT_SKILLS : NON_IT_SKILLS;
        List<String> resumeSkills = skillSet.stream()
                .filter(s -> resumeText.contains(s.toLowerCase()))
                .collect(Collectors.toList());

        // Get matching jobs from DB
        List<Job> matchingJobs = jobRepository.findByCategory(detectedCategory)
                .stream()
                .limit(20)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("detectedCategory", detectedCategory);
        result.put("resumeSkills", resumeSkills);
        result.put("matchingJobs", matchingJobs);
        result.put("resumeText", resumeText);

        return result;
    }

    // Step 2: Compare resume against selected job and suggest keywords
    public ResumeAnalysisResult analyzeResumeVsJob(String resumeText, Long jobId) throws Exception {
        Job selectedJob = jobRepository.findById(jobId)
                .orElseThrow(() -> new Exception("Job not found"));

        String jobText = "";
        if (selectedJob.getDescription() != null)
            jobText += selectedJob.getDescription().toLowerCase();
        if (selectedJob.getTitle() != null)
            jobText += selectedJob.getTitle().toLowerCase();

        String category = selectedJob.getCategory();
        List<String> skillSet = category.equals("IT") ? IT_SKILLS : NON_IT_SKILLS;

        // Skills in resume
        List<String> resumeSkills = skillSet.stream()
                .filter(s -> resumeText.contains(s.toLowerCase()))
                .collect(Collectors.toList());

        // Skills required by the job
        List<String> jobSkills = skillSet.stream()
                .filter(s -> jobText.contains(s.toLowerCase()))
                .collect(Collectors.toList());

        // Missing skills = job needs but resume doesn't have
        List<String> missingSkills = jobSkills.stream()
                .filter(s -> !resumeSkills.contains(s))
                .collect(Collectors.toList());

        // Match score
        int matchScore = jobSkills.isEmpty() ? 0 :
                (int) ((resumeSkills.stream().filter(jobSkills::contains).count() * 100)
                        / jobSkills.size());

        ResumeAnalysisResult result = new ResumeAnalysisResult();
        result.setResumeSkills(resumeSkills);
        result.setMissingSkills(missingSkills);
        result.setTrendingSkills(jobSkills);
        result.setMatchScore(matchScore);
        result.setTotalJobsAnalyzed(1);

        return result;
    }
}