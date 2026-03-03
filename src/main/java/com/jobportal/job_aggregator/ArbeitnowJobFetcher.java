package com.jobportal.job_aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.job_aggregator.model.Job;
import com.jobportal.job_aggregator.service.JobService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArbeitnowJobFetcher {

    private final JobService jobService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ArbeitnowJobFetcher(JobService jobService) {
        this.jobService = jobService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void fetchJobs() {
        System.out.println("Fetching jobs from Arbeitnow...");
        fetchPage(1);
        fetchPage(2);
        fetchPage(3);
        System.out.println("Arbeitnow fetch complete!");
    }

    private void fetchPage(int page) {
        try {
            String url = "https://www.arbeitnow.com/api/job-board-api?page=" + page;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode jobs = root.get("data");

            List<Job> jobList = new ArrayList<>();

            if (jobs != null && jobs.isArray()) {
                for (JsonNode node : jobs) {
                    Job job = new Job();
                    job.setTitle(getText(node, "title"));
                    job.setCompany(getText(node, "company_name"));
                    job.setLocation(getText(node, "location"));
                    job.setSalary("Not specified");
                    job.setDescription(stripHtml(getText(node, "description")));
                    job.setUrl(getText(node, "url"));
                    job.setSource("Arbeitnow");
                    job.setFetchedAt(LocalDateTime.now());

                    // Categorize based on tags
                    String tags = node.get("tags") != null ? node.get("tags").toString().toLowerCase() : "";
                    String title = job.getTitle().toLowerCase();
                    if (isITJob(title, tags)) {
                        job.setCategory("IT");
                    } else {
                        job.setCategory("Non-IT");
                    }

                    if (job.getLocation() == null || job.getLocation().isEmpty()) {
                        job.setLocation("Remote");
                    }

                    jobList.add(job);
                }
            }

            jobService.saveAllJobs(jobList);
            System.out.println("Saved " + jobList.size() + " jobs from Arbeitnow page: " + page);

        } catch (Exception e) {
            System.out.println("Error fetching Arbeitnow jobs: " + e.getMessage());
        }
    }

    private boolean isITJob(String title, String tags) {
        String[] itKeywords = {
                "developer", "engineer", "software", "data", "cloud", "devops",
                "frontend", "backend", "fullstack", "java", "python", "react",
                "aws", "azure", "kubernetes", "docker", "analyst", "architect",
                "programmer", "tech", "database", "security", "network", "mobile"
        };
        for (String keyword : itKeywords) {
            if (title.contains(keyword) || tags.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getText(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return val != null ? val.asText() : "";
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}