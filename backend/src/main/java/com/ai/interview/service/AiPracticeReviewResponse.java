package com.ai.interview.service;

import java.util.List;

public class AiPracticeReviewResponse {

    private Integer overallScore;
    private String summary;
    private String highlightExcerpt;
    private List<String> strengths;
    private List<String> improvements;
    private List<String> followUps;

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHighlightExcerpt() {
        return highlightExcerpt;
    }

    public void setHighlightExcerpt(String highlightExcerpt) {
        this.highlightExcerpt = highlightExcerpt;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements;
    }

    public List<String> getFollowUps() {
        return followUps;
    }

    public void setFollowUps(List<String> followUps) {
        this.followUps = followUps;
    }
}