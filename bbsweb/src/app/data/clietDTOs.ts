export interface Client {
    id?: number;
    firstName?: string;
    lastName?: string;
    email?: string;
    phoneNumber?: string;
    location?: string;
    locationType?: string;
    surName?: string;
    followUpDate?: string;
    contactStatus?: string;
    notes?: string;
    [key: string]: any; // Allow dynamic properties
} 