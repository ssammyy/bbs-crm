import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Files, Preliminary, PreliminaryType } from '../data/clietDTOs';

@Injectable({
    providedIn: 'root',
})
export class PreliminaryService {
    apiUrl = environment.apiUrl;
    constructor(private http: HttpClient) {}

    getProformaInvoice(clientId: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/invoices/proforma/${clientId}`);
    }
    getPreliminaryTypes(): Observable<PreliminaryType[]> {
        return this.http.get<PreliminaryType[]>(`${this.apiUrl}/api/preliminaries/types`);
    }

    addPreliminaryType(newPreliminary: PreliminaryType) :Observable<PreliminaryType> {
        return this.http.post<PreliminaryType>(`${this.apiUrl}/api/preliminaries/create`,  newPreliminary );
    }

    initiatePreliminary(newClientPreliminary: any, clientId: number) : Observable<Preliminary> {
        console.log("newClientPreliminary>>>>> ", newClientPreliminary);
        return this.http.post<Preliminary>(`${this.apiUrl}/api/preliminaries/${clientId}/initiate`, {
            preliminaryType: newClientPreliminary,
            invoiced: false
        });
    }

    getClientPreliminaries(clientId: number) : Observable<Preliminary[]> {
        return this.http.get<Preliminary[]>(`${this.apiUrl}/api/preliminaries/${clientId}`);
    }

    submitTechnicalWorks (clientId: number, preliminary: Preliminary):Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/api/preliminaries/${clientId}/technical-works`, preliminary);
    }

    getPreliminaryFiles(clientId: number, preliminaryId: number): Observable<Files[]> {
        return this.http.get<Files[]>(`${this.apiUrl}/api/preliminaries/files/${clientId}/${preliminaryId}`);
    }

    approveTechnicalWorks(preliminary: Preliminary, approvalStage: string): Observable<Preliminary> {
        return this.http.post<Preliminary>(`${this.apiUrl}/api/preliminaries/approve/${approvalStage}`, preliminary);
    }

    rejectTechnicalWorks(preliminary: Preliminary, approvalStage: string, remarks: string): Observable<Preliminary> {
        return this.http.post<Preliminary>(`${this.apiUrl}/api/preliminaries/reject/${approvalStage}`, {
            preliminary,
            remarks
        });
    }

    bypassInvoiceClearance(preliminary: Preliminary): Observable<Preliminary> {
        return this.http.post<Preliminary>(`${this.apiUrl}/api/preliminaries/bypass-invoice`, preliminary);
    }
}
