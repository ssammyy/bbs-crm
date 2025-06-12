import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AgentApprovalService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getPendingApprovals(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/agent-approvals/pending`);
  }

  approveAgent(approvalId: number): Observable<any> {
      console.log('approvalId', approvalId);
    return this.http.post(`${this.apiUrl}/api/agent-approvals/${approvalId}/approve`, {});
  }

  rejectAgent(approvalId: number, reason: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/agent-approvals/${approvalId}/reject`, { reason });
  }
}
