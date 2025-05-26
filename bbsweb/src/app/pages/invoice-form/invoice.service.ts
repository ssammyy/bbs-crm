import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Client, Invoice } from '../data/clietDTOs';
import { InvoiceType } from './data';

export interface PaymentConfirmation {
    paymentMethod: string;
    amountPaid: number;
    reference: string;
}

@Injectable({
    providedIn: 'root'
})
export class InvoiceService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    createInvoice(invoiceData: any): Observable<Blob> {
        return this.http.post(`${this.apiUrl}/api/invoices`, invoiceData, {
            responseType: 'blob'
        });
    }

    getInvoicePdf(clientId: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/api/invoices/${clientId}/pdf`, {
            responseType: 'blob'
        });
    }

    getInvoicePdfNew(clientId: number, invoiceType: string): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/api/invoices/${clientId}/${invoiceType}/pdf`, {
            responseType: 'blob'
        });
    }

    getInvoicePdfById(invoiceId: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/api/invoices/${invoiceId}/pdf`, {
            responseType: 'blob'
        });
    }

    getPreliminaryInvoicePdf(clientId: number, preliminaryId: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/api/invoices/${clientId}/prelim/${preliminaryId}/pdf`, {
            responseType: 'blob'
        });
    }

    getProformaInvoice(clientId: number): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/invoices/proforma/${clientId}`);
    }

    acceptInvoice(clientId: number, stage: string, invoiceType: InvoiceType): Observable<Client> {
        return this.http.post<Client>(`${this.apiUrl}/api/invoices/${clientId}/accept`, { stage, invoiceType });
    }

    approvePreliminaryInvoice(clientId: number, preliminaryId: number | undefined): Observable<Client> {
        return this.http.post<Client>(`${this.apiUrl}/api/preliminaries/${clientId}/approve-invoice/${preliminaryId}`, {});
    }

    rejectInvoice(clientId: number, remarks: string, stage: string): Observable<Client> {
        return this.http.post<Client>(`${this.apiUrl}/api/invoices/${clientId}/reject`, { remarks, stage });
    }

    getInvoicesByClient(clientId: number): Observable<Invoice[]> {
        return this.http.get<Invoice[]>(`${this.apiUrl}/api/invoices/all-client-invoices/${clientId}`);
    }

    clearInvoice(id: number): Observable<Invoice> {
        return this.http.delete<Invoice>(`${this.apiUrl}/api/invoices/${id}`, {});
    }

    generateReceipt(receipt: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/api/receipts`, receipt);
    }

    createBalanceInvoice(parentInvoiceId: number): Observable<Blob> {
        return this.http.post(`${this.apiUrl}/api/invoices/balance/${parentInvoiceId}`, {}, {
            responseType: 'blob'
        });
    }

    getBalanceInvoices(parentInvoiceId: number): Observable<Invoice[]> {
        return this.http.get<Invoice[]>(`${this.apiUrl}/api/invoices/balance/${parentInvoiceId}`);
    }

    confirmPayment(invoiceId: number, payment: PaymentConfirmation): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/invoices/${invoiceId}/confirm-payment`, payment);
    }
}
