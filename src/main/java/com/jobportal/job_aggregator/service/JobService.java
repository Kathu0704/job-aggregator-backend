package com.jobportal.job_aggregator.service;

import com.jobportal.job_aggregator.model.Job;
import com.jobportal.job_aggregator.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByCategory(String category) {
        return jobRepository.findByCategory(category);
    }

    public List<Job> getJobsByLocation(String location) {
        return jobRepository.findByLocation(location);
    }

    public List<Job> searchJobs(String keyword) {
        return jobRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public void saveJob(Job job) {
        boolean exists = jobRepository.existsByTitleAndCompany(
                job.getTitle(), job.getCompany()
        );
        if (!exists) {
            jobRepository.save(job);
        }
    }

    public void saveAllJobs(List<Job> jobs) {
        for (Job job : jobs) {
            saveJob(job);
        }
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public long getJobCount() {
        return jobRepository.count();
    }
}