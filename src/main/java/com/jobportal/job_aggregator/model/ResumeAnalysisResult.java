package com.jobportal.job_aggregator.model;

import java.util.List;

public class ResumeAnalysisResult {

    private int matchScore;
    private List<String> resumeSkills;
    private List<String> missingSkills;
    private List<String> trendingSkills;
    private int totalJobsAnalyzed;

    public ResumeAnalysisResult() {}

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public List<String> getResumeSkills() { return resumeSkills; }
    public void setResumeSkills(List<String> resumeSkills) { this.resumeSkills = resumeSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getTrendingSkills() { return trendingSkills; }
    public void setTrendingSkills(List<String> trendingSkills) { this.trendingSkills = trendingSkills; }

    public int getTotalJobsAnalyzed() { return totalJobsAnalyzed; }
    public void setTotalJobsAnalyzed(int totalJobsAnalyzed) { this.totalJobsAnalyzed = totalJobsAnalyzed; }
}