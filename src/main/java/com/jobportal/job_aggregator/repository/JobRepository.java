package com.jobportal.job_aggregator.repository;

import com.jobportal.job_aggregator.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find jobs by category (IT or Non-IT)
    List<Job> findByCategory(String category);

    // Find jobs by location
    List<Job> findByLocation(String location);

    // Check if job already exists (avoid duplicates)
    boolean existsByTitleAndCompany(String title, String company);

    // Search jobs by keyword in title
    List<Job> findByTitleContainingIgnoreCase(String keyword);
}