package com.jobportal.job_aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.job_aggregator.model.Job;
import com.jobportal.job_aggregator.service.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdzunaJobFetcher {

    @Value("${adzuna.app.id}")
    private String appId;

    @Value("${adzuna.app.key}")
    private String appKey;

    private final JobService jobService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AdzunaJobFetcher(JobService jobService) {
        this.jobService = jobService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Runs every 6 hours automatically
    @Scheduled(cron = "0 0 */6 * * ?")
    public void fetchJobs() {
        System.out.println("Fetching jobs from Adzuna...");
        fetchByKeyword("software developer", "IT");
        fetchByKeyword("java developer", "IT");
        fetchByKeyword("marketing manager", "Non-IT");
        fetchByKeyword("accountant", "Non-IT");
        System.out.println("Job fetching complete!");
    }

    private void fetchByKeyword(String keyword, String category) {
        try {
            String url = "https://api.adzuna.com/v1/api/jobs/in/search/1" +
                    "?app_id=" + appId +
                    "&app_key=" + appKey +
                    "&results_per_page=20" +
                    "&what=" + keyword.replace(" ", "%20") +
                    "&content-type=application/json";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");

            List<Job> jobs = new ArrayList<>();

            if (results != null && results.isArray()) {
                for (JsonNode node : results) {
                    Job job = new Job();
                    job.setTitle(getText(node, "title"));
                    job.setCompany(getCompanyName(node));
                    job.setLocation(getLocation(node));
                    job.setSalary(getSalary(node));
                    job.setDescription(getText(node, "description"));
                    job.setUrl(getText(node, "redirect_url"));
                    job.setCategory(category);
                    job.setSource("Adzuna");
                    job.setFetchedAt(LocalDateTime.now());
                    jobs.add(job);
                }
            }

            jobService.saveAllJobs(jobs);
            System.out.println("Saved " + jobs.size() + " jobs for: " + keyword);

        } catch (Exception e) {
            System.out.println("Error fetching jobs for " + keyword + ": " + e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return val != null ? val.asText() : "";
    }

    private String getCompanyName(JsonNode node) {
        JsonNode company = node.get("company");
        if (company != null && company.get("display_name") != null) {
            return company.get("display_name").asText();
        }
        return "";
    }

    private String getLocation(JsonNode node) {
        JsonNode location = node.get("location");
        if (location != null && location.get("display_name") != null) {
            return location.get("display_name").asText();
        }
        return "";
    }

    private String getSalary(JsonNode node) {
        JsonNode min = node.get("salary_min");
        JsonNode max = node.get("salary_max");
        if (min != null && max != null) {
            return min.asText() + " - " + max.asText();
        }
        return "Not specified";
    }
}