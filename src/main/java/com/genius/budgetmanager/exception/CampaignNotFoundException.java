package com.genius.budgetmanager.exception;
 
public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException(Long id) {
        super("Campaign not found: " + id);
    }
}
 