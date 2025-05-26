import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
export interface DashboardStats {
    clientStageDistribution: { [key: string]: number };
    clientSourceDistribution: { [key: string]: number };
    invoiceStats: {
        totalRevenue: number;
        pendingToBeClearedAmount: number;
    };
    recentActivities: {
        id: number;
        clientId: number;
        description: string;
        timestamp: string;
        user: string | null;
    }[];
    clearedVsUnclearedBreakdown: { [key: string]: number };
    highestRevenueClient: {
        clientId: number;
        clientName: string;
        totalRevenue: number;
    };
    fileStats: {
        totalFiles: number;
        fileTypeBreakdown: { [key: string]: number };
    };
    commitMentClients: number;
    projectDetails?: {
        projectName: string;
        clientStage: string;
        productOffering: string;
        projectActive: boolean;
    };
    totalAgents: number;
    totalSystemUsers: number;
    superAdmins: {
        id: number;
        username: string;
        name: string;
    }[];
    revenueOverTime: {
        period: string;
        value: number;
    }[];
    clientsOverTime: {
        period: string;
        value: number;
    }[];
}

export interface AgentClient {
    id: number;
    firstName: string;
    secondName: string | null;
    lastName: string;
    siteVisitDone: boolean;
    email: string;
    phoneNumber: string;
    dob: string;
    gender: string;
    preferredContact: string;
    location: string;
    country: string;
    county: string;
    countryCode: string;
    idNumber: number;
    clientStage: string;
    nextStage: string | null;
    clientSource: string;
    projectName: string;
    projectActive: boolean;
    productOffering: string;
    productTag: string;
    bankName: string | null;
    bankBranch: string | null;
    consultancySubtags: string[];
    notes: string | null;
    followUpDate: string;
    contactStatus: string;
    createdOn: string;
    updatedOn: string;
    createdBy: string;
    updatedBy: string;
    isDeleted: boolean;
}

export interface AgentActivity {
    id: number;
    clientId: number;
    client: AgentClient;
    description: string;
    timestamp: string;
    user: string | null;
}

export interface AgentDashboardStats {
    numberOfClients: number;
    commissionPercentage: number;
    clients: AgentClient[];
    invoiceStats: {
        totalRevenue: number;
        pendingToBeClearedAmount: number;
    };
    recentActivities: AgentActivity[];
    revenueOverTime: {
        period: string;
        value: number;
    }[];
}


@Injectable({
    providedIn: 'root'
})
export class DashboardService {
    private apiUrl = `${environment.apiUrl}/api/dashboard`;

    constructor(private http: HttpClient) {}

    getDashboardStats(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
    }

    getAgentDashboardStats(): Observable<AgentDashboardStats> {
        return this.http.get<AgentDashboardStats>(`${this.apiUrl}/agent-stats`);
    }
}
