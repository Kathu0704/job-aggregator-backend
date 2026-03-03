package com.jobportal.job_aggregator.controller;

import com.jobportal.job_aggregator.AdzunaJobFetcher;
import com.jobportal.job_aggregator.ArbeitnowJobFetcher;
import com.jobportal.job_aggregator.RemotiveJobFetcher;
import com.jobportal.job_aggregator.model.Job;
import com.jobportal.job_aggregator.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;
    private final AdzunaJobFetcher adzunaFetcher;
    private final RemotiveJobFetcher remotiveFetcher;
    private final ArbeitnowJobFetcher arbeitnowFetcher;

    public JobController(JobService jobService, AdzunaJobFetcher adzunaFetcher, RemotiveJobFetcher remotiveFetcher, ArbeitnowJobFetcher arbeitnowFetcher) {
        this.jobService = jobService;
        this.adzunaFetcher = adzunaFetcher;
        this.remotiveFetcher = remotiveFetcher;
        this.arbeitnowFetcher = arbeitnowFetcher;
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Job>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(jobService.getJobsByCategory(category));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Job>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(jobService.getJobsByLocation(location));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> searchJobs(@RequestParam String keyword) {
        return ResponseEntity.ok(jobService.searchJobs(keyword));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getJobCount() {
        return ResponseEntity.ok(jobService.getJobCount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok("Job deleted successfully");
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> fetchJobs() {
        adzunaFetcher.fetchJobs();
        remotiveFetcher.fetchJobs();
        arbeitnowFetcher.fetchJobs();
        return ResponseEntity.ok("All jobs fetched from Adzuna + Remotive + Arbeitnow!");
    }
}
