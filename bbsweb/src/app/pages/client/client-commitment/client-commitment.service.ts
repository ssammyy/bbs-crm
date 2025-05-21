import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ClientCommitmentService {
  private apiUrl = `${environment.apiUrl}/api/client-commitments`;

  constructor(private http: HttpClient) {}

  onboardClientCommitment(clientCommitmentData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/onboard`, clientCommitmentData);
  }

  getClientCommitmentsToFollowUp(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/follow-ups`);
  }

  updateContactStatus(clientCommitmentId: number, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${clientCommitmentId}/status?status=${status}`, {});
  }

    updateClientCommitment(clientCommitmentId: number, updatedData: any): Observable<any> {
        return this.http.put(`${this.apiUrl}/${clientCommitmentId}`, updatedData);
    }
}
