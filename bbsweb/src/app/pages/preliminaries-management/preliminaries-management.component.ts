import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Client, Files, Invoice, Preliminary, PreliminaryType, ProformaInvoice } from '../data/clietDTOs';
import { Toast } from 'primeng/toast';
import { Fieldset } from 'primeng/fieldset';
import { Message } from 'primeng/message';
import { DropdownModule } from 'primeng/dropdown';
import { PreliminaryService } from './preliminary.service';
import { FormsModule } from '@angular/forms';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { Button, ButtonDirective } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { MessagesService } from '../../layout/service/messages.service';
import { Chip } from 'primeng/chip';
import { Panel } from 'primeng/panel';
import { Dialog } from 'primeng/dialog';
import { FileUpload } from 'primeng/fileupload';
import { UploadService } from '../service/upload.service';
import { AccordionModule } from 'primeng/accordion';
import { Router } from '@angular/router';
import { UserGlobalService } from '../service/user.service';
import { Checkbox } from 'primeng/checkbox';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { Permissions } from '../data/permissions.enum';

@Component({
    selector: 'app-preliminary-management',
    templateUrl: './preliminaries-management.component.html',
    imports: [Toast, ConfirmDialogModule, Message, DropdownModule, FormsModule, NgIf, Button, InputText, AccordionModule, Textarea, Chip, NgForOf, Panel, Dialog, FileUpload, ButtonDirective, NgClass, Checkbox],
    providers: [MessageService, ConfirmationService]
})
export class PreliminaryManagementComponent implements OnInit, OnChanges {
    @Input() client!: Client;
    clientId: number = 1;
    preliminaryTypes: PreliminaryType[] = [];
    selectedPreliminary: number | null = null;
    selectedPreliminaryType!: PreliminaryType;
    preliminaries: Preliminary[] = [];
    showAddForm: boolean = false;
    newPreliminary: PreliminaryType = { requiresGovernmentApproval: false, id: 0, name: '', description: '' };

    invoices: Invoice[] = [];
    documents: Files[] = [];
    proformaInvoice: ProformaInvoice | null = null;
    uploadedFile: File | null = null;
    loading = false;
    modalPreliminary: any | null = null;
    displayDialog = false;
    clientFiles: Files[] = [];
    userRole!: string;
    displayRejectDialog: boolean = false;
    rejectRemarks: string = '';
    rejectApprovalStage: string = '';

    constructor(
        private http: HttpClient,
        private messageService: MessageService,
        private messagesService: MessagesService,
        private preliminaryService: PreliminaryService,
        private uploadService: UploadService,
        private router: Router,
        private userGlobalService: UserGlobalService,
        private confirmationService: ConfirmationService
    ) {}

    ngOnInit() {
        this.loadPreliminaryTypes();
        this.loadPreliminaries();
        this.getUserDetails();
    }
    ngOnChanges(changes: SimpleChanges): void {
        console.log('reloading changes ');
        if (changes['client'] && changes['client'].currentValue) {
            this.loadPreliminaries();
        }
    }

    loadPreliminaryTypes(): void {
        this.preliminaryService.getPreliminaryTypes().subscribe({
            next: response => {
                this.preliminaryTypes = response;
            },
            error: error => {
                console.log(error);
                this.messageService.add({
                    severity: 'error',
                    summary: error.error.details,
                })

            }
        })
    }

    toggleAddPreliminaryForm(): void {
        this.showAddForm = !this.showAddForm;
        if (!this.showAddForm) {
            this.newPreliminary = { requiresGovernmentApproval: false, id: 0, name: '', description: '' };
        }
    }

    addPreliminaryType(): void {
        if (this.newPreliminary.name) {
            this.preliminaryService.addPreliminaryType(this.newPreliminary).subscribe((newType) => {
                this.preliminaryTypes.push(newType);
                this.selectedPreliminary = newType.id;
                this.selectedPreliminaryType = newType;
                this.toggleAddPreliminaryForm();
            });
        }
    }
    hasPrivileges(privilege: string): boolean {
        return this.userGlobalService.hasPrivilege(privilege)
    }

    initiateActivity(): void {
        if (this.selectedPreliminaryType) {
            this.loading = true;
            const newPreliminary: Preliminary = {
                client: this.client,
                preliminaryType: this.selectedPreliminaryType,
                status: 'INITIATED',
                invoiced: false,
                invoiceClearedFlag: false,
                rejectionRemarks: ""
            };
            console.log("selectedPreliminaryType>>>>> ", this.selectedPreliminaryType);

            this.preliminaryService.initiatePreliminary(this.selectedPreliminaryType, this.client.id).subscribe({
                next: (resp) => {
                    this.loading = false;
                    this.loadPreliminaries();
                    this.displayDialog = false;
                    this.messagesService.showSuccess('Preliminary added successfully.');
                },
                error: (err) => {
                    console.log('errror prelims >>>',  err.error.details );
                    this.loading = false;
                    this.messageService.add({
                        severity: 'warn',
                        summary: err.error.details,
                    });
                },
                complete: () => {}
            });
        }
    }

    loadPreliminaries() {
        this.preliminaryService.getClientPreliminaries(this.client.id).subscribe({
            next: (resp) => {
                this.preliminaries = resp;
            },
            error: (err) => {
                console.log(err);
                this.messageService.add({
                    severity: 'error',
                    summary: err.error.details,
                })
            }
        });
    }

    showPreliminaryDialog(preliminary: Preliminary): void {
        this.modalPreliminary = preliminary;
        this.displayDialog = true;
        this.getPreliminaryFiles();
    }

    getStatusChipClass(status: string): string {
        switch (status) {
            case 'INITIATED':
                return 'bg-blue-100 text-blue-800';
            case 'PENDING_T_D_APPROVAL':
                return 'bg-orange-100 text-orange-800';
            case 'PENDING_M_D_APPROVAL':
                return 'bg-purple-100 text-purple-800';
            case 'COMPLETE':
                return 'bg-green-100 text-green-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    }

    loadProformaInvoice() {
        this.http.get<ProformaInvoice>(`/api/preliminaries/${this.clientId}/proforma`).subscribe((proforma) => {
            this.proformaInvoice = proforma;
        });
    }

    getUserDetails() {
        this.userGlobalService.getDetails().subscribe({
            next: (response) => {
                this.userRole = response?.role?.name;
            }
        });
    }

    generateInvoice(preliminary: Preliminary, amount: number) {
        this.http.post<Invoice>(`/api/preliminaries/${preliminary.id}/invoice`, { amount }).subscribe({
            next: () => {
                this.messageService.add({ severity: 'success', summary: 'Invoice Generated' });
                this.loadProformaInvoice();
            },
            error: () => this.messageService.add({ severity: 'error', summary: 'Error Generating Invoice' })
        });
    }

    onFileSelect(event: any) {
        this.uploadedFile = event.files[0];
    }

    uploadDocument(preliminary: Preliminary) {
        if (!this.uploadedFile) return;
        this.uploadService.upload(this.client.id, 'PRELIMINARY', this.uploadedFile, preliminary.id).subscribe({
            next: (resp) => {
                this.messageService.add({ severity: 'success', summary: 'Document Uploaded successfully.' });
            },
            error: (err) => {
                console.log(err);
                this.messagesService.showError(err);
            }
        });
    }
    getPreliminaryFiles(): void {
        this.loading = true;
        this.preliminaryService.getPreliminaryFiles(this.client.id, this.modalPreliminary?.id).subscribe({
            next: (files: Files[]) => {
                this.clientFiles = files;
                this.loading = false;
            }
        });
    }

    submitTechnicalWorks(): void {
        if (this.modalPreliminary && this.uploadedFile) {
            this.loading = true;
            this.uploadDocument(this.modalPreliminary);
            this.preliminaryService.submitTechnicalWorks(this.client.id, this.modalPreliminary).subscribe({
                next: () => {
                    this.loading = false;
                    this.messageService.add({ severity: 'success', summary: 'Technical Works Submitted Successfully' });
                    this.loadPreliminaries();
                    this.displayDialog = false;
                },
                error: (err) => {
                    this.loading = false;
                    console.error('Technical works submission error:', err);
                    this.messageService.add({ severity: 'error', summary: 'Submission Failed', detail: err.message || 'An error occurred' });
                }
            });
        }
    }

    acceptTechnicalWorks(type: string): void {
        this.loading = true;
        if (type === 'MANAGING_DIRECTOR' && !this.modalPreliminary.invoiceClearedFlag) {
            this.confirm();
            return;
        }
        this.preliminaryService.approveTechnicalWorks(this.modalPreliminary, type).subscribe({
            next: () => {
                this.loading = false;
                this.displayDialog = false;
                this.loadPreliminaries();
                this.messagesService.showSuccess('Preliminary approved successfully');
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({ severity: 'error', summary: err.message || 'An error occurred' });
            }
        });
    }

    showRejectDialog(type: string): void {
        this.rejectApprovalStage = type;
        this.rejectRemarks = '';
        this.displayRejectDialog = true;
    }

    confirmReject(): void {
        this.loading = true;
        this.preliminaryService.rejectTechnicalWorks(this.modalPreliminary, this.rejectApprovalStage, this.rejectRemarks).subscribe({
            next: () => {
                this.loading = false;
                this.displayDialog = false;
                this.displayRejectDialog = false;
                this.loadPreliminaries();
                this.messagesService.showSuccess('Preliminary rejected successfully');
            },
            error: (err) => {
                this.loading = false;
                this.messageService.add({ severity: 'error', summary: 'Rejection Failed', detail: err.message || 'An error occurred' });
            }
        });
    }

    confirm() {
        console.log('trying to render popup');
        this.confirmationService.confirm({
            header: 'Confirmation',
            message: 'This preliminary has not been paid for, proceed?',
            icon: 'pi pi-exclamation-circle',
            rejectButtonProps: {
                label: 'Cancel',
                icon: 'pi pi-times',
                outlined: true,
                size: 'small'
            },
            acceptButtonProps: {
                label: 'Save',
                icon: 'pi pi-check',
                size: 'small'
            },
            accept: () => {
                this.preliminaryService.approveTechnicalWorks(this.modalPreliminary, "MANAGING_DIRECTOR").subscribe({
                    next: () => {
                        this.loading = false;
                        this.displayDialog = false;
                        this.loadPreliminaries();
                        this.messagesService.showSuccess('Preliminary approved successfully');
                    },
                    error: (err) => {
                        this.loading = false;
                        this.messageService.add({ severity: 'error', summary: err.message || 'An error occurred' });
                    }
                });
            },
            reject: () => {
                this.loading = false;
            }
        });
    }

    createNewPreliminary() {}
    bypassInvoiceClearance(): void {
        if (this.modalPreliminary && this.userRole === 'SUPER_ADMIN') {
            this.loading = true;
            this.preliminaryService.bypassInvoiceClearance(this.modalPreliminary).subscribe({
                next: () => {
                    this.loading = false;
                    this.messageService.add({ severity: 'success', summary: 'Preliminary Completed Without Invoice Clearance' });
                    this.loadPreliminaries();
                    this.displayDialog = false;
                },
                error: (err) => {
                    this.loading = false;
                    this.messageService.add({ severity: 'error', summary: 'Failed to Bypass Invoice Clearance', detail: err.message || 'An error occurred' });
                }
            });
        }
    }

    navigateToInvoiceForm(): void {
        this.router.navigate(['/app/pages/create-invoice'], {
            state: {
                preliminary: {
                    id: this.modalPreliminary.id,
                    name: this.modalPreliminary.preliminaryType.name,
                    description: this.modalPreliminary.preliminaryType.description
                },
                client: this.modalPreliminary.client
            }
        });
        this.displayDialog = false;
    }

    protected readonly Permissions = Permissions;
}
