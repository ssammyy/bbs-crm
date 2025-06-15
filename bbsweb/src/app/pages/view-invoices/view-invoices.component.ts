import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { DialogModule } from 'primeng/dialog';
import { CommonModule } from '@angular/common';
import { InvoiceService } from '../invoice-form/invoice.service';
import { Client, Invoice, ProformaInvoice } from '../data/clietDTOs';
import { Panel } from 'primeng/panel';
import { forkJoin, of } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import { MessagesService } from '../../layout/service/messages.service';
import { Router } from '@angular/router';
import { Message } from 'primeng/message';
import { InputNumber } from 'primeng/inputnumber';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { Calendar } from 'primeng/calendar';
import { InputText } from 'primeng/inputtext';
import { InvoiceType } from '../invoice-form/data';
import { UserGlobalService } from '../service/user.service';
import { Permissions } from '../data/permissions.enum';
import { Toast } from 'primeng/toast';

interface Preliminary {
    id: number;
    preliminaryType: { id: number; name: string; description?: string };
    status: string;
    createdAt: string;
}

interface Receipt {
    paymentDate: Date;
    paymentMethod: string;
    amountPaid: number;
    transactionId?: string;
}

interface PaymentMethod {
    label: string;
    value: string;
}

interface SortOption {
    label: string;
    value: string;
}

@Component({
    selector: 'app-view-invoices',
    standalone: true,
    imports: [CommonModule, DataViewModule, ButtonModule, ChipModule, DialogModule, Panel, Message, InputNumber, DropdownModule, FormsModule, Calendar, InputText, Toast],
    templateUrl: './view-invoices.component.html',
    styleUrls: ['./view-invoices.component.scss']
})
export class ViewInvoicesComponent implements OnInit, OnChanges {
    @Input() client!: Client;
    @Input() isActive: boolean = false;
    @Output() invoiceReconciled = new EventEmitter<void>();
    @Output() receiptGenerated = new EventEmitter<void>();
    invoices: Invoice[] = [];
    filteredInvoices: Invoice[] = [];
    displayReceiptDialog: boolean = false;
    displayInvoiceDialog: boolean = false;
    displayPaymentDialog: boolean = false;
    selectedInvoice: any = null;
    payment: {
        paymentMethod: string;
        amountPaid: number;
        reference: string;
    } = {
        paymentMethod: '',
        amountPaid: 0,
        reference: ''
    };
    paymentMethods: PaymentMethod[] = [
        { label: 'M-PESA', value: 'M_PESA' },
        { label: 'Bank Transfer', value: 'BANK_TRANSFER' },
        { label: 'Cash', value: 'CASH' },
        { label: 'Cheque', value: 'CHEQUE' }
    ];
    sortOptions: SortOption[] = [
        { label: 'Invoice Number (Asc)', value: 'invoiceNumber_asc' },
        { label: 'Invoice Number (Desc)', value: 'invoiceNumber_desc' },
        { label: 'Date Issued (Newest)', value: 'dateIssued_desc' },
        { label: 'Date Issued (Oldest)', value: 'dateIssued_asc' },
        { label: 'Total Amount (High to Low)', value: 'total_desc' },
        { label: 'Total Amount (Low to High)', value: 'total_asc' },
        { label: 'Reconciled (Yes First)', value: 'reconciled_desc' },
        { label: 'Reconciled (No First)', value: 'reconciled_asc' },
        { label: 'Director Approved (Yes First)', value: 'directorApproved_desc' },
        { label: 'Director Approved (No First)', value: 'directorApproved_asc' }
    ];
    searchTerm: string = '';
    sortOption: string = 'dateIssued_desc';
    receipt: Receipt = {
        paymentDate: new Date(),
        paymentMethod: '',
        amountPaid: 0,
        transactionId: ''
    };
    loading: boolean = false;
    proformaInvoice!: ProformaInvoice;
    balance!: number;
    fullAmountPaid = false;
    isClientRole: boolean = false;

    constructor(
        private invoiceService: InvoiceService,
        private messagesService: MessagesService,
        private globalUserDetails: UserGlobalService,
        private router: Router
    ) {}

    ngOnInit() {
        this.getUserRole();
        console.log('client >>>>>', this.client);
        this.loadInvoices();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['isActive'] && changes['isActive'].currentValue === true && !changes['isActive'].previousValue) {
            this.loadInvoices();
        }
    }

    getUserRole() {
        this.globalUserDetails.getDetails().subscribe({
            next: (response) => {
                this.isClientRole = response?.role?.name === 'CLIENT';
            },
            error: (error) => {
                console.error('Error fetching user role:', error);
            }
        });
    }

    loadInvoices(): void {
        console.log('getting invoices...');
        this.loading = true;
        forkJoin({
            invoices: this.invoiceService.getInvoicesByClient(this.client.id),
            proformaInvoice: this.invoiceService.getProformaInvoice(this.client.id)
        })
            .pipe(
                delay(500),
                catchError((error) => {
                    console.error('Error loading invoices:', error);
                    this.messagesService.showError('Error loading invoices.');
                    this.loading = false;
                    return of(null);
                })
            )
            .subscribe({
                next: (result) => {
                    if (result) {
                        this.invoices = (result.invoices || []).filter((invoice) => !invoice.parentInvoiceId);
                        console.log('Filtered invoices (non-balance):', this.invoices);
                        this.invoices.forEach((invoice) => {
                            this.invoiceService.getBalanceInvoices(invoice.id).subscribe({
                                next: (balanceInvoices) => {
                                    invoice.balanceInvoices = balanceInvoices;
                                    console.log(`Balance invoices for ${invoice.id}:`, balanceInvoices);
                                    this.applySearchAndSort();
                                },
                                error: (error) => {
                                    console.error('Error fetching balance invoices:', error);
                                }
                            });
                        });
                        this.proformaInvoice = result.proformaInvoice || null;
                        this.applySearchAndSort();
                    }
                    this.loading = false;
                },
                error: (error) => {
                    this.messagesService.showError('Error loading invoices.', error);
                    console.error('Unexpected error:', error);
                    this.loading = false;
                }
            });
    }

    applySearchAndSort(): void {
        let result = [...this.invoices];

        // Filter out unapproved invoices for clients
        if (this.isClientRole) {
            result = result.filter((invoice) => invoice.directorApproved);
        }

        // Apply search filter
        if (this.searchTerm.trim()) {
            const term = this.searchTerm.toLowerCase();
            result = result.filter((invoice) => invoice.invoiceNumber.toLowerCase().includes(term) || invoice.preliminary?.preliminaryType.name.toLowerCase().includes(term));
        }

        // Apply sort
        switch (this.sortOption) {
            case 'invoiceNumber_asc':
                result.sort((a, b) => a.invoiceNumber.localeCompare(b.invoiceNumber));
                break;
            case 'invoiceNumber_desc':
                result.sort((a, b) => b.invoiceNumber.localeCompare(a.invoiceNumber));
                break;
            case 'dateIssued_asc':
                result.sort((a, b) => new Date(a.dateIssued).getTime() - new Date(b.dateIssued).getTime());
                break;
            case 'dateIssued_desc':
                result.sort((a, b) => new Date(b.dateIssued).getTime() - new Date(a.dateIssued).getTime());
                break;
            case 'total_asc':
                result.sort((a, b) => a.total - b.total);
                break;
            case 'total_desc':
                result.sort((a, b) => b.total - a.total);
                break;
            case 'reconciled_asc':
                result.sort((a, b) => (a.invoiceReconciled === b.invoiceReconciled ? 0 : a.invoiceReconciled ? 1 : -1));
                break;
            case 'reconciled_desc':
                result.sort((a, b) => (a.invoiceReconciled === b.invoiceReconciled ? 0 : a.invoiceReconciled ? -1 : 1));
                break;
            case 'directorApproved_asc':
                result.sort((a, b) => (a.directorApproved === b.directorApproved ? 0 : a.directorApproved ? 1 : -1));
                break;
            case 'directorApproved_desc':
                result.sort((a, b) => (a.directorApproved === b.directorApproved ? 0 : a.directorApproved ? -1 : 1));
                break;
        }

        this.filteredInvoices = result;
    }

    onSearchChange(): void {
        this.applySearchAndSort();
    }

    onSortChange(): void {
        this.applySearchAndSort();
    }

    createBalanceInvoice(invoice: Invoice): void {
        this.loading = true;
        this.invoiceService.createBalanceInvoice(invoice.id).subscribe({
            next: (result: Blob) => {
                this.messagesService.showSuccess('Balance invoice created successfully.');
                const blob = new Blob([result], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                const newTab = window.open(pdfUrl, '_blank');
                if (newTab) {
                    this.messagesService.showSuccess('Opening balance invoice in new tab.');
                } else {
                    this.messagesService.showError('Failed to open PDF. Please allow pop-ups.');
                }
                setTimeout(() => window.URL.revokeObjectURL(pdfUrl), 10000);
                this.loadInvoices();
                this.loading = false;
            },
            error: (error) => {
                this.loading = false;
                this.messagesService.showError('Failed to create balance invoice: ' + (error.error?.message || 'Unknown error'));
                console.error('Error creating balance invoice:', error);
            }
        });
    }

    clearInvoice(invoice: Invoice): void {
        this.selectedInvoice = invoice;
        this.fullAmountPaid = true;
        this.receipt = {
            paymentDate: new Date(),
            paymentMethod: '',
            amountPaid: invoice.balance,
            transactionId: ''
        };
        this.calculateBalance();
        this.displayReceiptDialog = true;
    }
    viewInvoicePdf(type: string, invoiceApprovalType?: string): void {
        this.invoiceService.getInvoicePdfNew(this.client.id, type, type === 'COUNTY_INVOICE' ? invoiceApprovalType : undefined).subscribe({
            next: (result: Blob) => {
                const blob = new Blob([result], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                const newTab = window.open(pdfUrl, '_blank');
                if (newTab) {
                    this.messagesService.showSuccess('Opening in new tab.');
                } else {
                    this.messagesService.showError('Failed to open PDF. Please allow pop-ups.');
                }
                setTimeout(() => window.URL.revokeObjectURL(pdfUrl), 10000);
            },
            error: (error) => {
                console.error('Error fetching PDF:', error);
                this.messagesService.showError('Error loading invoice PDF.');
            }
        });
    }


    viewInvoicePdfById(invoiceId: number): void {
        this.invoiceService.getInvoicePdfById(invoiceId).subscribe({
            next: (result: Blob) => {
                const blob = new Blob([result], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                const newTab = window.open(pdfUrl, '_blank');
                if (newTab) {
                    this.messagesService.showSuccess('Opening in new tab.');
                } else {
                    this.messagesService.showError('Failed to open PDF. Please allow pop-ups.');
                }
                setTimeout(() => window.URL.revokeObjectURL(pdfUrl), 10000);
            },
            error: (error) => {
                console.error('Error fetching PDF:', error);
                this.messagesService.showError('Error loading invoice PDF.');
            }
        });
    }

    viewPreliminaryInvoice(preliminaryId: number | undefined): void {
        console.log('inaingia hapa preliminary id ', preliminaryId);
        if (!preliminaryId) return;
        this.invoiceService.getPreliminaryInvoicePdf(this.client.id, preliminaryId).subscribe({
            next: (result: Blob) => {
                const blob = new Blob([result], { type: 'application/pdf' });
                const pdfUrl = window.URL.createObjectURL(blob);
                const newTab = window.open(pdfUrl, '_blank');
                if (newTab) {
                    this.messagesService.showSuccess('Opening in new tab.');
                } else {
                    this.messagesService.showError('Failed to open PDF. Please allow pop-ups.');
                }
                setTimeout(() => window.URL.revokeObjectURL(pdfUrl), 10000);
            },
            error: (error) => {
                console.error('Error fetching PDF:', error);
                this.messagesService.showError('Error loading invoice PDF.');
            }
        });
    }

    createNewInvoice() {}

    approveInvoice(invoice: Invoice): void {
        console.log('invoice >> ', {invoice});
        this.loading = true;

        if (invoice.invoiceType == InvoiceType.COUNTY_INVOICE.toString()) {
            this.invoiceService.approveCountyInvoice(this.client.id, invoice.governmentApprovalType!!).subscribe({
                next: (result: any) => {
                    this.loading = false;
                    this.messagesService.showSuccess('Invoice approved successfully.');
                    this.loadInvoices();
                },
                error: (error: any) => {
                    this.loading = false;
                    this.messagesService.showError('Failed to approve invoice. Please try again later.');

                }
            });
        } else if(invoice.invoiceType == InvoiceType.MAIN_PROFORMA.toString()) {
            this.invoiceService.approveMainProformaInvoice(this.client.id).subscribe({
                next: (result: any) => {
                    this.loading = false;
                    this.messagesService.showSuccess('Invoice approved successfully.');
                    this.loadInvoices();
                },
                error: (error: any) => {
                    this.loading = false;
                    this.messagesService.showError('Failed to approve invoice. Please try again.');
                }
            })

        } else {
            this.invoiceService.approvePreliminaryInvoice(this.client.id, invoice.preliminary?.id).subscribe({
                next: (result) => {
                    if (result) {
                        this.loading = false;
                        this.messagesService.showSuccess('Invoice approved successfully.');
                        this.loadInvoices();
                    }
                },
                error: (error) => {
                    this.loading = false;
                    this.messagesService.showError('Failed to approve invoice. Please try again later.');
                }
            });
        }
    }

    hasBalanceInvoices(invoice: Invoice): boolean | undefined {
        return invoice.balanceInvoices && invoice.balanceInvoices.length > 0;
    }

    toggleFullAmount(): void {
        if (this.selectedInvoice) {
            this.receipt.amountPaid = this.fullAmountPaid ? this.selectedInvoice.balance : 0;
            this.calculateBalance();
        }
    }

    calculateBalance(): void {
        if (this.selectedInvoice) {
            this.balance = this.selectedInvoice.balance - this.receipt.amountPaid;
            if (this.receipt.amountPaid > this.selectedInvoice.balance) {
                this.receipt.amountPaid = this.selectedInvoice.balance;
                this.balance = 0;
                this.messagesService.showContrast('Amount paid cannot exceed the invoice balance.');
            }
        }
    }

    hasPrivilege(privilege: string): boolean {
        return this.globalUserDetails.hasPrivilege(privilege);
    }

    generateReceipt() {
        // if (!this.selectedInvoice || !this.receipt.paymentMethod || !this.receipt.amountPaid) {
        //     this.messagesService.showContrast('Please fill in all required fields.');
        //     return;
        // }
        const receiptDTO = {
            invoiceId: this.selectedInvoice.id,
            paymentDate: this.receipt.paymentDate.toISOString().split('T')[0],
            paymentMethod: this.receipt.paymentMethod,
            amountPaid: this.receipt.amountPaid,
            transactionId: this.receipt.transactionId
        };
        this.invoiceService.generateReceipt(receiptDTO).subscribe({
            next: (result) => {
                if (result) {
                    this.messagesService.showSuccess('Receipt generated successfully.');
                    this.selectedInvoice.balance = result.invoiceId ? this.selectedInvoice.balance - this.receipt.amountPaid : this.selectedInvoice.balance;
                    this.selectedInvoice.invoiceReconciled = this.selectedInvoice.balance <= 0;
                    this.selectedInvoice.pendingBalance = !this.selectedInvoice.invoiceReconciled;
                    this.displayReceiptDialog = false;
                    this.loadInvoices();
                    this.invoiceReconciled.emit();
                    this.receiptGenerated.emit();
                }
            },
            error: (error) => {
                console.error(error);
                this.messagesService.showError('Failed to generate receipt, try again later.');
            }
        });
    }

    openPaymentDialog(invoice: any): void {
        this.selectedInvoice = invoice;
        this.payment = {
            paymentMethod: '',
            amountPaid: 0,
            reference: ''
        };
        this.displayPaymentDialog = true;
    }

    confirmPayment(): void {
        if (!this.payment.paymentMethod || !this.payment.amountPaid) {
            console.log('Payment method not implemented.');
            this.messagesService.showContrast('Please fill in all required fields');
            return;
        }

        if (this.payment.amountPaid > this.selectedInvoice.balance) {
            console.log('Payment method not implemented 22.');
            this.messagesService.showContrast('Amount paid cannot exceed the balance');
            return;
        }
        console.log('sneding confirmation payment');
        this.invoiceService.confirmPayment(this.selectedInvoice.id, this.payment).subscribe({
            next: (response) => {
                this.messagesService.showSuccess('Payment confirmed successfully');
                this.displayPaymentDialog = false;
                this.loadInvoices();
            },
            error: (error) => {
                this.messagesService.showError('Failed to confirm payment');
                console.error('Error confirming payment:', error);
            }
        });
    }

    protected readonly InvoiceType = InvoiceType;
    protected readonly Permissions = Permissions;
}
