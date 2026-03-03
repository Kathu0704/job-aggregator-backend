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
public class RemotiveJobFetcher {

    private final JobService jobService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RemotiveJobFetcher(JobService jobService) {
        this.jobService = jobService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void fetchJobs() {
        System.out.println("Fetching jobs from Remotive...");
        fetchByCategory("software-dev", "IT");
        fetchByCategory("data", "IT");
        fetchByCategory("devops", "IT");
        fetchByCategory("marketing", "Non-IT");
        fetchByCategory("finance", "Non-IT");
        System.out.println("Remotive fetch complete!");
    }

    private void fetchByCategory(String category, String jobCategory) {
        try {
            String url = "https://remotive.com/api/remote-jobs?category=" + category + "&limit=20";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode jobs = root.get("jobs");

            List<Job> jobList = new ArrayList<>();

            if (jobs != null && jobs.isArray()) {
                for (JsonNode node : jobs) {
                    Job job = new Job();
                    job.setTitle(getText(node, "title"));
                    job.setCompany(getText(node, "company_name"));
                    job.setLocation(getText(node, "candidate_required_location"));
                    job.setSalary(getText(node, "salary"));
                    job.setDescription(stripHtml(getText(node, "description")));
                    job.setUrl(getText(node, "url"));
                    job.setCategory(jobCategory);
                    job.setSource("Remotive");
                    job.setFetchedAt(LocalDateTime.now());

                    if (job.getLocation() == null || job.getLocation().isEmpty()) {
                        job.setLocation("Remote");
                    }
                    if (job.getSalary() == null || job.getSalary().isEmpty()) {
                        job.setSalary("Not specified");
                    }

                    jobList.add(job);
                }
            }

            jobService.saveAllJobs(jobList);
            System.out.println("Saved " + jobList.size() + " jobs from Remotive category: " + category);

        } catch (Exception e) {
            System.out.println("Error fetching Remotive jobs: " + e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return val != null ? val.asText() : "";
    }

    // Remove HTML tags from description
    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}