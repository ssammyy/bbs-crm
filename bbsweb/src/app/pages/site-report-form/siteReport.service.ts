import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class SiteReportService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    getReportByClientId(clientId: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/site-report/client/${clientId}`);
    }

    submitReport(report: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/site-report`, report);
    }

    approveReport(reportId: number): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/site-report/${reportId}/approve`, {});
    }

    rejectReport(reportId: number, comments: string): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/site-report/${reportId}/reject`, { comments });
    }

    getAuditLogs(reportId: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/site-report/${reportId}/audit-logs`);
    }
}
