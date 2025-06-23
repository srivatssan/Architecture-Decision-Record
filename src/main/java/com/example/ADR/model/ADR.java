package com.example.ADR.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "architecture_decisions")
public class ADR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;                   // Short title of the ADR
    private String status;                  // e.g., Proposed, Accepted, Superseded, Deprecated
    private String context;                 // Problem description
    private String decision;                // The decision taken
    private String consequences;            // Implications of decision

    private String authorEmail;             // Registered user's email

    private String componentId;             // Architecture component ID this ADR affects
    private String linkedStories;           // Comma-separated user story or Jira IDs
    private String impactArea;              // e.g., Security, Infrastructure, Data
    private String tags;                    // CSV: "api,cloud,java"
    private String relatedAdrs;             // Comma-separated related ADR IDs
    private String reviewedBy;              // Email or names of reviewers

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Column(name = "deleted")
    private boolean deleted = false;
    private String confluenceLink;          // Optional link to Confluence or Loop
    private String architectureIntakeId;    // Intake ID from internal process
    private String proofLink;               // Link to evidence or design artifacts
    private String dispositionStatus;       // Active, Expired
    private LocalDate dispositionTargetDate; // Date for expiration, if applicable

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    @Transient
    private String userRole;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getLinkedStories() {
        return linkedStories;
    }

    public void setLinkedStories(String linkedStories) {
        this.linkedStories = linkedStories;
    }

    public String getImpactArea() {
        return impactArea;
    }

    public void setImpactArea(String impactArea) {
        this.impactArea = impactArea;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRelatedAdrs() {
        return relatedAdrs;
    }

    public void setRelatedAdrs(String relatedAdrs) {
        this.relatedAdrs = relatedAdrs;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getConfluenceLink() {
        return confluenceLink;
    }

    public void setConfluenceLink(String confluenceLink) {
        this.confluenceLink = confluenceLink;
    }

    public String getArchitectureIntakeId() {
        return architectureIntakeId;
    }

    public void setArchitectureIntakeId(String architectureIntakeId) {
        this.architectureIntakeId = architectureIntakeId;
    }

    public String getProofLink() {
        return proofLink;
    }

    public void setProofLink(String proofLink) {
        this.proofLink = proofLink;
    }

    public String getDispositionStatus() {
        return dispositionStatus;
    }

    public void setDispositionStatus(String dispositionStatus) {
        this.dispositionStatus = dispositionStatus;
    }

    public LocalDate getDispositionTargetDate() {
        return dispositionTargetDate;
    }

    public void setDispositionTargetDate(LocalDate dispositionTargetDate) {
        this.dispositionTargetDate = dispositionTargetDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
// Getters and Setters
}
