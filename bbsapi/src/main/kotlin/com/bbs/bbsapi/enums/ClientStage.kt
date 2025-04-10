package com.bbs.bbsapi.enums

enum class ClientStage {
    REQUIREMENTS_GATHERING,
    PROFORMA_INVOICE_GENERATION,
    INVOICE_PENDING_DIRECTOR_APPROVAL,
    GENERATE_SITE_VISIT_INVOICE,
    PENDING_CLIENT_SITE_VISIT_PAYMENT,//payment happens off the system, the SYSTEM_ADMIN just presses a button to notify the system that the client has paid
    PENDING_SITE_VISIT, //once site visit is done, a button is clicked with attachments and remarks as evidence (optional)
    GENERATE_DRAWINGS_INVOICE,
    PENDING_CLIENT_DRAWINGS_PAYMENT,
    ARCHITECTURAL_DRAWINGS_SKETCH,//RENDERS,
    PENDING_CLIENT_SKETCH_APPROVAL,// CLIENT MAY REJECT - move back to @ARCHITECTURAL_DRAWINGS_SKETCH
    UPLOAD_WORKING_DRAWINGS,//upon client sketch/renders approval
    WORKING_DRAWINGS_PENDING_TECHNICAL_DIRECTOR_APPROVAL,
    PENDING_DRAWINGS_PRINTING_AND_STAMPING,
    DRAWINGS_PENDING_AUTHORITY_APPROVAL,//government approvals - if rejected - move back to @UPLOAD_ACTUAL_DRAWINGS_RENDERS
//  other approvals which do not halt the process flow - track them separately
    GENERATE_BOQ_PREPARATION_INVOICE,
    PENDING_CLIENT_BOQ_PREPARATION_PAYMENT,
    UPLOAD_BOQ,  //Done by QS after preparing the BOQ
    BOQ_PENDING_TECHNICAL_DIRECTOR_APPROVAL,
    PENDING_CLIENT_BOQ_APPROVALS, //client has to approve the BOQ
    PRINTING_AND_STAMPING,
    CONTRACT_SIGNING
}