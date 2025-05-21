import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MessagesService } from '../../layout/service/messages.service';
import { forkJoin, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Receipt } from '../data/Receipt';
import { ReceiptService } from './receipt.service';
import { Panel } from 'primeng/panel';

interface SortOption {
    label: string;
    value: string;
}

interface FilterOption {
    label: string;
    value: string;
}

@Component({
    selector: 'app-receipts',
    standalone: true,
    imports: [CommonModule, DataViewModule, ButtonModule, DropdownModule, InputTextModule, FormsModule, Panel],
    templateUrl: './receipts.component.html',
    styleUrls: ['./receipts.component.scss']
})
export class ReceiptsComponent implements OnInit {
    @Input() clientId!: number | undefined;
    receipts: Receipt[] = [];
    filteredReceipts: Receipt[] = [];
    searchTerm: string = '';
    sortOption: string = 'paymentDate_desc';
    filterOption: string = 'all';
    sortOptions: SortOption[] = [
        { label: 'Payment Date (Newest)', value: 'paymentDate_desc' },
        { label: 'Payment Date (Oldest)', value: 'paymentDate_asc' },
        { label: 'Amount Paid (High to Low)', value: 'amountPaid_desc' },
        { label: 'Amount Paid (Low to High)', value: 'amountPaid_asc' },
        { label: 'Payment Method (A-Z)', value: 'paymentMethod_asc' },
        { label: 'Payment Method (Z-A)', value: 'paymentMethod_desc' },
        { label: 'Receipt ID (Asc)', value: 'id_asc' },
        { label: 'Receipt ID (Desc)', value: 'id_desc' }
    ];
    filterOptions: FilterOption[] = [
        { label: 'All Receipts', value: 'all' },
        { label: 'Reconciled Invoices', value: 'reconciled' },
        { label: 'Cash', value: 'CASH' },
        { label: 'Bank Transfer', value: 'BANK_TRANSFER' },
        { label: 'M-Pesa', value: 'M_PESA' },
        { label: 'Credit Card', value: 'CREDIT_CARD' }
    ];
    loading: boolean = false;

    constructor(
        private http: HttpClient,
        private messagesService: MessagesService,
        private receiptService: ReceiptService
    ) {}

    ngOnInit() {
        this.loadReceipts();
    }

    loadReceipts(): void {
        this.loading = true;
        this.receiptService.getReceipts(this.clientId!!).subscribe({
            next: (data) => {
                console.log('loaded receipts', data);
                this.receipts = data;
                this.loading = false;
                this.applySearchSortFilter();
            },
            error: (error) => {
                console.error('Error loading receipts ', error);
            }
        });
    }

    applySearchSortFilter(): void {
        // Ensure receipts is an array
        if (!Array.isArray(this.receipts)) {
            console.warn('Receipts is not an array:', this.receipts);
            this.filteredReceipts = [];
            return;
        }

        let result = [...this.receipts];

        // Apply search
        if (this.searchTerm.trim()) {
            const term = this.searchTerm.toLowerCase();
            result = result.filter((receipt) => receipt.invoiceNumber.toLowerCase().includes(term) || receipt.clientName.toLowerCase().includes(term) || (receipt.transactionId && receipt.transactionId.toLowerCase().includes(term)));
        }

        // Apply filter
        if (this.filterOption === 'reconciled') {
            result = result.filter((receipt) => receipt.invoiceReconciled);
        } else if (this.filterOption !== 'all') {
            result = result.filter((receipt) => receipt.paymentMethod === this.filterOption);
        }

        // Apply sort
        switch (this.sortOption) {
            case 'paymentDate_asc':
                result.sort((a, b) => new Date(a.paymentDate).getTime() - new Date(b.paymentDate).getTime());
                break;
            case 'paymentDate_desc':
                result.sort((a, b) => new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime());
                break;
            case 'amountPaid_asc':
                result.sort((a, b) => a.amountPaid - b.amountPaid);
                break;
            case 'amountPaid_desc':
                result.sort((a, b) => b.amountPaid - a.amountPaid);
                break;
            case 'paymentMethod_asc':
                result.sort((a, b) => a.paymentMethod.localeCompare(b.paymentMethod));
                break;
            case 'paymentMethod_desc':
                result.sort((a, b) => b.paymentMethod.localeCompare(a.paymentMethod));
                break;
            case 'id_asc':
                result.sort((a, b) => a.id - b.id);
                break;
            case 'id_desc':
                result.sort((a, b) => b.id - a.id);
                break;
        }

        this.filteredReceipts = result;
        console.log('Filtered receipts:', this.filteredReceipts);
    }

    onSearchChange(): void {
        this.applySearchSortFilter();
    }

    onSortChange(): void {
        this.applySearchSortFilter();
    }

    onFilterChange(): void {
        this.applySearchSortFilter();
    }

    viewInvoice(invoiceId: number): void {
        this.http.get<Blob>(`/api/invoices/pdf/${invoiceId}`, { responseType: 'blob' as 'json' }).subscribe({
            next: (result) => {
                const blob = new Blob([result], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                const newTab = window.open(pdfUrl, '_blank');
                if (newTab) {
                    this.messagesService.showSuccess('Opening invoice in new tab.');
                } else {
                    this.messagesService.showError('Failed to open PDF. Please allow pop-ups.');
                }
                setTimeout(() => window.URL.revokeObjectURL(pdfUrl), 10000);
            },
            error: (error) => {
                console.error('Error fetching invoice PDF:', error);
                this.messagesService.showError('Error loading invoice PDF.');
            }
        });
    }
}
