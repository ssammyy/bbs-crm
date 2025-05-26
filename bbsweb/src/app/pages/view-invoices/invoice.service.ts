import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PaymentConfirmation {
    paymentMethod: string;
    amountPaid: number;
    reference: string;
}

@Injectable({
    providedIn: 'root'
})
export class InvoiceService {
    private apiUrl = `${environment.apiUrl}/api`;

    constructor(private http: HttpClient) {}

    confirmPayment(invoiceId: number, payment: PaymentConfirmation): Observable<any> {
        return this.http.post(`${this.apiUrl}/invoices/${invoiceId}/confirm-payment`, payment);
    }
} 