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

    // Master skills list
    private static final List<String> SKILLS_TAXONOMY = Arrays.asList(
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "spring", "spring boot", "hibernate", "node", "express", "django", "flask",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "git", "github",
            "html", "css", "bootstrap", "tailwind", "rest", "api", "microservices",
            "linux", "bash", "maven", "gradle", "junit", "testing", "agile", "scrum",
            "machine learning", "deep learning", "tensorflow", "pytorch", "data science",
            "pandas", "numpy", "tableau", "power bi", "excel", "communication",
            "leadership", "management", "marketing", "sales", "accounting", "finance",
            "hr", "recruitment", "operations", "logistics", "supply chain", "seo",
            "content writing", "graphic design", "figma", "photoshop", "project management"
    );

    public ResumeAnalyzerService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
        this.tika = new Tika();
    }

    public ResumeAnalysisResult analyzeResume(MultipartFile file) throws Exception {
        // Step 1: Extract text from resume
        String resumeText = tika.parseToString(file.getInputStream()).toLowerCase();

        // Step 2: Find skills in resume
        List<String> resumeSkills = new ArrayList<>();
        for (String skill : SKILLS_TAXONOMY) {
            if (resumeText.contains(skill.toLowerCase())) {
                resumeSkills.add(skill);
            }
        }

        // Step 3: Get trending skills from job database
        List<Job> allJobs = jobRepository.findAll();
        Map<String, Integer> skillFrequency = new HashMap<>();

        for (Job job : allJobs) {
            String jobText = "";
            if (job.getDescription() != null) jobText += job.getDescription().toLowerCase();
            if (job.getTitle() != null) jobText += job.getTitle().toLowerCase();

            for (String skill : SKILLS_TAXONOMY) {
                if (jobText.contains(skill.toLowerCase())) {
                    skillFrequency.put(skill, skillFrequency.getOrDefault(skill, 0) + 1);
                }
            }
        }

        // Step 4: Get top 20 trending skills
        List<String> trendingSkills = skillFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Step 5: Find missing skills
        List<String> missingSkills = trendingSkills.stream()
                .filter(skill -> !resumeSkills.contains(skill))
                .limit(10)
                .collect(Collectors.toList());

        // Step 6: Calculate match score
        long matchCount = trendingSkills.stream()
                .filter(resumeSkills::contains)
                .count();
        int matchScore = (int) ((matchCount * 100) / Math.max(trendingSkills.size(), 1));

        // Build result
        ResumeAnalysisResult result = new ResumeAnalysisResult();
        result.setResumeSkills(resumeSkills);
        result.setMissingSkills(missingSkills);
        result.setTrendingSkills(trendingSkills);
        result.setMatchScore(matchScore);
        result.setTotalJobsAnalyzed(allJobs.size());

        return result;
    }
}