import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getContractByClientId(clientId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/api/contracts/by-client/${clientId}`);
  }

  createContract(contract: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/contracts`, contract);
  }

  updateContract(contractId: number, contract: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/api/contracts/${contractId}`, contract);
  }
} 