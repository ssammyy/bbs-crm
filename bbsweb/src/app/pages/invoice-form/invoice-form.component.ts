import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Client, PreliminaryType } from '../data/clietDTOs';
import { InvoiceItem, InvoiceType } from './data';
import { DecimalPipe, NgIf } from '@angular/common';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { InvoiceService } from './invoice.service';
import { MessagesService } from '../../layout/service/messages.service';
import { Toast } from 'primeng/toast';
import { ClientDetailsService } from '../client/client.service';
import { Router } from '@angular/router';
import { InputNumber } from 'primeng/inputnumber';
import { PreliminaryService } from '../preliminaries-management/preliminary.service';

@Component({
    selector: 'app-invoice-form',
    imports: [DecimalPipe, Button, TableModule, InputText, ReactiveFormsModule, DropdownModule, FormsModule, NgIf, Toast, InputNumber],
    templateUrl: './invoice-form.component.html',
    styleUrl: './invoice-form.component.scss',
    standalone: true
})
export class InvoiceFormComponent implements OnInit {
    @Input() client!: Client;
    @Input() invoiceType!: InvoiceType;
    @Input() preliminaryId?: number;
    @Output() invoiceGenerated = new EventEmitter<void>();

    invoiceForm: FormGroup;
    items: InvoiceItem[] = [];
    total: number = 0;
    subtotal: number = 0;
    discountPercentage: number = 0;
    discountAmount: number = 0;
    finalTotal: number = 0;
    date!: Date;
    today!: string;
    invoiceNumber!: string;
    loading = false;
    preliminaryTypes: PreliminaryType[] = [];
    selectedPreliminaryType: PreliminaryType | null = null;

    private defaultProformaItems: InvoiceItem[] = [
        { description: 'Site Visit Fee', quantity: 1, unitPrice: 0, totalPrice: 0 },
        { description: 'Architectural Drawings', quantity: 1, unitPrice: 0, totalPrice: 0 },
        { description: 'Preparation of BOQ', quantity: 1, unitPrice: 0, totalPrice: 0 }
    ];

    constructor(
        private fb: FormBuilder,
        private invoiceService: InvoiceService,
        private preliminaryService: PreliminaryService,
        private messagesServices: MessagesService,
        private clientService: ClientDetailsService,
        private router: Router
    ) {
        this.invoiceForm = this.fb.group({});
    }

    ngOnInit(): void {
        const navigationState = history.state;

        if (navigationState.client) {
            this.client = navigationState.client;
            if (this.invoiceType == null) {
                this.invoiceType = InvoiceType.PRELIMINARY;
            }
        }
        this.today = new Date().toISOString().split('T')[0];
        this.invoiceNumber = this.generateInvoiceNumber();

        if (navigationState.preliminary) {
            this.preliminaryId = navigationState.preliminary.id;
            this.items = [
                {
                    description: navigationState.preliminary.name,
                    quantity: 1,
                    unitPrice: 0,
                    totalPrice: 0
                }
            ];
            this.calculateTotal();
        } else {
            this.invoiceType = InvoiceType.SITE_VISIT;
            this.loadInvoiceData();
        }

        // Load preliminary types
        this.loadPreliminaryTypes();
    }

    loadPreliminaryTypes(): void {
        this.preliminaryService.getPreliminaryTypes().subscribe(
            (types) => {
                this.preliminaryTypes = types;
            },
            (error) => {
                console.error('Error loading preliminary types:', error);
                this.messagesServices.showError('Failed to load preliminary types');
            }
        );
    }

    loadInvoiceData(): void {
        if (this.invoiceType === InvoiceType.SITE_VISIT) {
            this.addSiteVisitItem();
        }
    }

    addItem(): void {
        if (this.invoiceType === InvoiceType.SITE_VISIT) {
            this.items.push({
                description: '',
                quantity: 1,
                unitPrice: 0,
                totalPrice: 0,
                isPreliminary: true,
                preliminaryType: null
            });
        } else {
            this.items.push({
                description: '',
                quantity: 1,
                unitPrice: 0,
                totalPrice: 0,
                isPreliminary: false,
                preliminaryType: null
            });
        }
        this.calculateTotal();
    }

    addSiteVisitItem() {
        this.items.push({
            description: 'SITE_VISIT',
            quantity: 1,
            unitPrice: 0,
            totalPrice: 0,
            isPreliminary: false
        });
        this.calculateTotal();
    }

    onPreliminaryTypeSelect(item: InvoiceItem, type: PreliminaryType): void {
        item.description = type.name || '';
        item.preliminaryType = type;
        this.updateItem(this.items.indexOf(item));
    }

    removeItem(index: number): void {
        this.items.splice(index, 1);
        this.calculateTotal();
    }

    updateItem(index: number): void {
        const item = this.items[index];
        item.totalPrice = item.quantity * item.unitPrice;
        this.calculateTotal();
    }

    calculateTotal(): void {
        this.subtotal = this.items.reduce((sum, item) => sum + item.totalPrice, 0);
        this.calculateDiscount();
    }

    calculateDiscount(): void {
        if (this.discountPercentage > 0) {
            this.discountAmount = (this.subtotal * this.discountPercentage) / 100;
        }
        this.finalTotal = this.subtotal - this.discountAmount;
    }

    onDiscountPercentageChange(): void {
        this.calculateDiscount();
    }

    onDiscountAmountChange(): void {
        if (this.discountAmount > 0) {
            this.discountPercentage = (this.discountAmount / this.subtotal) * 100;
        } else {
            this.discountPercentage = 0;
        }
        this.calculateDiscount();
    }

    generateInvoiceNumber(): string {
        this.date = new Date();
        const datePart = this.date.toISOString().slice(2, 10).replace(/-/g, '');
        const randomPart = Math.floor(100 + Math.random() * 900);
        return `${datePart}-${randomPart}`;
    }

    onSubmit(): void {
        this.loading = true;
        if (this.invoiceForm.valid && this.items.length > 0 && this.client) {
            const invoiceData = {
                invoiceNumber: this.invoiceNumber,
                invoiceType: this.invoiceType,
                dateIssued: this.today,
                clientId: this.client.id,
                clientName: `${this.client.firstName} ${this.client.lastName}`.trim(),
                clientPhone: this.client.phoneNumber,
                projectName: this.client.projectName,
                items: this.items.map((item) => ({
                    description: item.description,
                    quantity: item.quantity,
                    unitPrice: item.unitPrice,
                    totalPrice: item.totalPrice
                })),
                total: this.finalTotal,
                preliminaryId: this.preliminaryId,
                discountPercentage: this.discountPercentage,
                discountAmount: this.discountAmount,
                subtotal: this.subtotal,
                finalTotal: this.finalTotal
            };
            this.invoiceService.createInvoice(invoiceData).subscribe(
                (response: Blob) => {
                    this.loading = false;
                    this.invoiceGenerated.emit();
                    this.messagesServices.showSuccess('Invoice created successfully, waiting for director approval.');
                    if (this.invoiceType !== InvoiceType.SITE_VISIT) {
                        this.router.navigate([`/app/pages/profile/${this.client.id}`]);
                    }
                },
                (error) => {
                    this.loading = false;
                    console.error('Error generating invoice:', error);
                    this.messagesServices.showError('Failed to create invoice.');
                }
            );
        } else {
            this.loading = false;
            this.messagesServices.showError('Please ensure all required fields are filled and a client is selected.');
        }
    }
}
