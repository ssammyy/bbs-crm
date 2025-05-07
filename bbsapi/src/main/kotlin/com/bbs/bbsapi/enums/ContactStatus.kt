package com.bbs.bbsapi.enums

enum class ContactStatus {
    ONBOARDED,    // Customer has been onboarded into the system
    CONTACTED,    // Follow-up email sent or customer contacted
    IN_TALKS,     // Customer is in active discussions
    LEAD // Customer has not been contacted yet
}