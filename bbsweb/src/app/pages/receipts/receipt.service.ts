import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Receipt } from '../data/Receipt';

@Injectable({
    providedIn: 'root'
})
export class ReceiptService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {
    }


    getReceipts(clientId: number): Observable<Receipt[]> {
        return this.http.get<Receipt[]>(`${this.apiUrl}/api/receipts/client/${clientId}`, {})
    }
}
