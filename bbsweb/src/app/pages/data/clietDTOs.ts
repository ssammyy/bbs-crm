export interface Client {
    id: number;
    firstName: string;
    secondName: string;
    lastName: string;
    surName: string;
    email?: string; // Optional
    phoneNumber: string;
    dob: Date;
    preferredContact: PreferredContactMethod;
    location: string;
    locationType: string;
    country: string;
    county: string;
    clientStage: string;
    idNumber: number;
    nextStage: string;
    createdOn: Date;
    projectName: string;
    projectActive: boolean;
    clientSource: string;
    invoiceNumber:  string;
    productOffering: string;
    productTag: string;
    bankName: string;
    bankBranch: string;
    consultancySubtag: string;
    notes: string;
    followUpDate: Date
}
export interface Files{
    id?: number;
    fileName: string;
    fileType: string;
    fileUrl: string;
}

export interface PreliminaryType {
    requiresGovernmentApproval: boolean;
    id: number;
    name: string;
    description?: string;
}

export interface Invoice {
    clientConfirmedPayment: boolean;
    clientPaymentConfirmation: string;
    parentInvoiceId: number;
    balanceInvoices?: Invoice[];
    id: number;
    invoiceNumber: string;
    dateIssued: string; // ISO date string
    clientId: number;
    clientName: string;
    clientPhone: string;
    projectName: string;
    items: InvoiceItem[];
    total: number;
    clientApproved: boolean;
    directorApproved: boolean;
    invoiceReconciled: boolean;
    pendingBalance: boolean;
    balance: number;
    invoiceType: InvoiceType;
    rejectionRemarks?: string;
    cleared: boolean;
    preliminary?: Preliminary;
}

export interface InvoiceItem {
    id: number;
    invoice?: Invoice;
    description: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
}

export interface ProformaInvoice {
    total: string;
    id: number;
    clientId: number;
    createdAt: string; // ISO date string
    updatedAt: string; // ISO date string
    invoices: Invoice[];
}
export enum InvoiceType {
    PROFORMA = 'PROFORMA',
    SITE_VISIT = 'SITE_VISIT',
    ARCHITECTURAL_DRAWINGS = 'ARCHITECTURAL_DRAWINGS',
    BOQ = 'BOQ',
    OPEN = 'OPEN'
}



export enum PreferredContactMethod {
    CALL = 'CALL',
    SMS = 'SMS',
    EMAIL = 'EMAIL',
    WHATSAPP = 'WHATSAPP'
}
export interface Preliminary {
    id?: number;
    client: Client;
    preliminaryType: PreliminaryType;
    status: string;
    invoiced: boolean;
    invoiceClearedFlag: boolean;
    rejectionRemarks: string;

}



export interface Location {
    isKenyan: boolean;
    county?: string; // Only required if isKenyan = true
    country: string;
}

export interface Activity {
    id: number;
    clientId: number;
    description: string;
    timestamp: Date;
    user?: string;
}
