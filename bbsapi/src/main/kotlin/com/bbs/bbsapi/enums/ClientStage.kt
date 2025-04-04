package com.bbs.bbsapi.enums

enum class ClientStage {
    CONSULTATION, // Collecting requirements
    PROPOSAL_PRESENTATION, // Proposal, Quotation, or Presentation
    FEEDBACK_COLLECTION, // If client is not satisfied
    REGISTRATION, // Adding client to database
    SITE_VISIT, // Initial site visit
    DRAWINGS_AND_RENDERS, // Sketches, approvals, printing & stamping
    BILL_OF_QUANTITIES, // Preparing cost estimates
    CONTRACT_SIGNING, // Signing the contract of works
    FINANCING_PROCESS, // Loan approval, invoicing, disbursement
    CONSTRUCTION_START, // Recruiting staff, setting up site
    FOUNDATION, // Laying the foundation
    STRUCTURE_BUILDING, // Walling, partitioning
    ROOFING_AND_FITTINGS, // Roofing, windows, doors
    FINISHING, // Painting, final touches
    FINAL_INSPECTION, // Ensuring everything is complete
    COMMISSIONING, // Handing over the project
    COMPLETED // Project completion
}
