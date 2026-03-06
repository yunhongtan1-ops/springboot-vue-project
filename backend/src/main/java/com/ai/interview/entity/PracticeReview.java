package com.ai.interview.entity;

import java.time.LocalDateTime;

public class PracticeReview {

    private Long id;
    private Long practiceRecordId;
    private ReviewStatus status;
    private Integer overallScore;
    private String summary;
    private String highlightExcerpt;
    private String strengthsJson;
    private String improvementsJson;
    private String followUpsJson;
    private String generatorName;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPracticeRecordId() {
        return practiceRecordId;
    }

    public void setPracticeRecordId(Long practiceRecordId) {
        this.practiceRecordId = practiceRecordId;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

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

    public String getStrengthsJson() {
        return strengthsJson;
    }

    public void setStrengthsJson(String strengthsJson) {
        this.strengthsJson = strengthsJson;
    }

    public String getImprovementsJson() {
        return improvementsJson;
    }

    public void setImprovementsJson(String improvementsJson) {
        this.improvementsJson = improvementsJson;
    }

    public String getFollowUpsJson() {
        return followUpsJson;
    }

    public void setFollowUpsJson(String followUpsJson) {
        this.followUpsJson = followUpsJson;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}