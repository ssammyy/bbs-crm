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
import { UploadService, LicenseFile } from '../service/upload.service';
import { AccordionModule } from 'primeng/accordion';
import { Router } from '@angular/router';
import { UserGlobalService } from '../service/user.service';
import { Checkbox } from 'primeng/checkbox';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { Permissions } from '../data/permissions.enum';
import { KYC_DOCUMENTS, KycDocument } from '../site-report-form/kyc-documents';
import { ClientDetailsService } from '../client/client.service';
import { switchMap } from 'rxjs/operators';
import { UserService } from '../users/user.service';
import { User } from '../service/user.service';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { FileUploadModule } from 'primeng/fileupload';
import { ChipModule } from 'primeng/chip';
import { DividerModule } from 'primeng/divider';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { DialogModule } from 'primeng/dialog';
import { PanelModule } from 'primeng/panel';
import { ToastModule } from 'primeng/toast';
import { NgxSpinnerModule } from 'ngx-spinner';
import { TooltipModule } from 'primeng/tooltip';
import { NonKycDocumentPipe } from '../site-report-form/non-kyc-document.pipe';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
    selector: 'app-preliminary-management',
    templateUrl: './preliminaries-management.component.html',
    styleUrls: ['./preliminaries-management.component.scss'],
    imports: [
        Toast,
        ConfirmDialogModule,
        Message,
        DropdownModule,
        FormsModule,
        NgIf,
        Button,
        InputText,
        AccordionModule,
        Textarea,
        Chip,
        NgForOf,
        Panel,
        Dialog,
        FileUpload,
        ButtonDirective,
        NgClass,
        Checkbox,
        CommonModule,
        ButtonModule,
        CardModule,
        FileUploadModule,
        ChipModule,
        DividerModule,
        MessagesModule,
        MessageModule,
        DialogModule,
        PanelModule,
        ToastModule,
        ConfirmDialogModule,
        NgxSpinnerModule,
        TooltipModule,
        NonKycDocumentPipe,
        InputNumberModule
    ],
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
    boqTotalAmount: number | null = null;

    invoices: Invoice[] = [];
    documents: Files[] = [];
    proformaInvoice: ProformaInvoice | null = null;
    uploadedFile: File | null = null;
    loading = false;
    modalPreliminary: any | null = null;
    displayDialog = false;
    clientFiles: Files[] = [];
    clientKycFiles: Files[] = [];
    userRole!: string;
    displayRejectDialog: boolean = false;
    rejectRemarks: string = '';
    rejectApprovalStage: string = '';

    kycDocuments: KycDocument[] = KYC_DOCUMENTS;
    selectedDocument: Files | null = null;

    selectedEngineer: User | null = null;
    selectedArchitect: User | null = null;
    selectedLicense: LicenseFile | null = null;
    licenseFiles: LicenseFile[] = [];
    users: User[] = [];

    countyApprovalStatus: 'APPROVED' | 'REJECTED' | 'PENDING' | null = null;
    countyRejectionRemarks: string = '';

    constructor(
        private http: HttpClient,
        private messageService: MessageService,
        private messagesService: MessagesService,
        private preliminaryService: PreliminaryService,
        private uploadService: UploadService,
        private router: Router,
        private userGlobalService: UserGlobalService,
        private confirmationService: ConfirmationService,
        private clientService: ClientDetailsService,
        private userService: UserService
    ) {}

    ngOnInit() {
        this.loadPreliminaryTypes();
        this.loadPreliminaries();
        this.getUserDetails();
        if (this.client) {
            this.getClientFiles(this.client.id);
        }
        this.loadLicenseFiles();
        this.loadUsers();
    }
    ngOnChanges(changes: SimpleChanges): void {
        console.log('reloading changes ');
        if (changes['client'] && changes['client'].currentValue) {
            this.loadPreliminaries();
        }
    }

    loadPreliminaryTypes(): void {
        this.preliminaryService.getPreliminaryTypes().subscribe({
            next: (response) => {
                this.preliminaryTypes = response;
            },
            error: (error) => {
                console.log(error);
                this.messageService.add({
                    severity: 'error',
                    summary: error.error.details
                });
            }
        });
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
        return this.userGlobalService.hasPrivilege(privilege);
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
                rejectionRemarks: '',
                clientInvoicedForApproval: false,
                clientPaidForApproval: false,
                countyApprovedDocumentUploaded: false,
                boqAmount: 0,
            };
            console.log('selectedPreliminaryType>>>>> ', this.selectedPreliminaryType);

            this.preliminaryService.initiatePreliminary(this.selectedPreliminaryType, this.client.id).subscribe({
                next: (resp) => {
                    this.loading = false;
                    this.loadPreliminaries();
                    this.displayDialog = false;
                    this.messagesService.showSuccess('Preliminary added successfully.');
                },
                error: (err) => {
                    console.log('errror prelims >>>', err.error.details);
                    this.loading = false;
                    this.messageService.add({
                        severity: 'warn',
                        summary: err.error.details
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
                    summary: err.error.details
                });
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
                console.log('role>>>>>>>++ ', response?.role?.name);
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
        this.uploadService.upload('PRELIMINARY', this.uploadedFile, this.client.id, preliminary.id).subscribe({
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
        if (!this.uploadedFile) return;
        this.loading = true;

        if (this.modalPreliminary?.preliminaryType.name === 'BOQ_PREPARATION' && !this.boqTotalAmount) {
            this.messageService.add({
                severity: 'error',
                summary: 'Please enter the BOQ total amount'
            });
            this.loading = false;
            return;
        }
        if(this.modalPreliminary?.preliminaryType.name === 'BOQ_PREPARATION' && !this.boqTotalAmount){
            this.uploadService.upload('PRELIMINARY', this.uploadedFile, this.client.id, this.modalPreliminary?.id).subscribe({
                next: (resp) => {
                    if (this.modalPreliminary?.preliminaryType.name === 'BOQ_PREPARATION') {
                        this.preliminaryService.submitBOQAmount(this.modalPreliminary.id, this.boqTotalAmount!).subscribe({
                            next: () => {
                                this.messageService.add({ severity: 'success', summary: 'BOQ amount and document uploaded successfully.' });
                                this.loading = false;
                                this.displayDialog = false;
                                this.loadPreliminaries();
                            },
                            error: (err) => {
                                this.loading = false;
                                this.messageService.add({ severity: 'error', summary: err.message || 'Failed to submit BOQ amount' });
                            }
                        });
                    } else {
                        this.messageService.add({ severity: 'success', summary: 'Document uploaded successfully.' });
                        this.loading = false;
                        this.displayDialog = false;
                        this.loadPreliminaries();
                    }
                },
                error: (err) => {
                    this.loading = false;
                    this.messageService.add({ severity: 'error', summary: err.message || 'Failed to upload document' });
                }
            });

        }else {
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
        if (type === 'SUBMITTED_TO_COUNTY') {
            this.confirmationService.confirm({
                message: 'Please confirm that all required documents are available and ready for submission to the county.',
                header: 'Confirm Document Availability and Submission',
                icon: 'pi pi-exclamation-triangle',
                acceptLabel: 'Yes, Submitted',
                rejectLabel: 'No, Cancel',
                accept: () => {
                    this.loading = true;
                    this.preliminaryService.approveTechnicalWorks(this.modalPreliminary, type).subscribe({
                        next: () => {
                            this.loading = false;
                            this.displayDialog = false;
                            this.loadPreliminaries();
                            this.messagesService.showSuccess('Documents submitted to county successfully.');
                        },
                        error: (err) => {
                            this.loading = false;
                            this.messageService.add({ severity: 'error', summary: err.message || 'An error occurred' });
                        }
                    });
                }
            });
            return;
        }

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
                this.preliminaryService.approveTechnicalWorks(this.modalPreliminary, 'MANAGING_DIRECTOR').subscribe({
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

    navigateToInvoiceFormCountyApproval(prelimType: string): void {
        // Find the county invoice file
        console.log('county prelim type ', prelimType);
        this.router.navigate(['/app/pages/create-invoice'], {
            state: {
                client: this.client,
                invoiceType: 'COUNTY_INVOICE',
                countyApprovalType: prelimType
            }
        });
    }

    getClientFiles(clientId: number): void {
        this.clientService.getClientFiles(clientId).subscribe({
            next: (files: Files[]) => {
                this.clientKycFiles = files;
            },
            error: (error) => {
                console.error('Error fetching files:', error);
            }
        });
    }

    onKycFileSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = { fileType, fileName: file.name, fileUrl: '' }; // Temporary object to track selection
    }

    onUpload(fileType: string) {
        this.loading = true;
        if (!this.uploadedFile) {
            this.messagesService.showError('No file selected for upload.');
            this.loading = false;
            return;
        }
        if (fileType ==='APPROVED_DOC'){
            fileType =`APPROVED_${this.modalPreliminary.preliminaryType.name}`;
            console.log('filetype ', fileType);
        }
        this.uploadService
            .upload(fileType, this.uploadedFile, this.client.id, this.modalPreliminary.id)
            .pipe(
                switchMap(() => {
                    this.loading = false;
                    this.loadPreliminaries();
                    this.modalPreliminary = this.preliminaries.find((prelim) => prelim.id === this.modalPreliminary.id);

                    if (fileType=="COUNTY_INVOICE" || fileType=="COUNTY_RECEIPT" || fileType==='APPROVED_ARCHITECTURAL_DRAWINGS' || fileType==='APPROVED_STRUCTURAL_DESIGNS') {
                        this.displayDialog = false;
                    }
                    this.messagesService.showSuccess('Invoices uploaded successfully.');
                    // Reset selection
                    this.uploadedFile = null;
                    this.selectedDocument = null;
                    return this.clientService.getClientFiles(this.client.id);
                })
            )
            .subscribe({
                next: (files: Files[]) => {
                    this.clientKycFiles = files;
                },
                error: (err) => {
                    this.loading = false;
                    console.error('Something went wrong:', err);
                    this.messagesService.showError('Error uploading document.');
                }
            });
    }

    isKycDocumentUploaded(fileType: string): boolean {
        return this.clientKycFiles.some((file) => file.fileType === fileType);
    }

    areAllKycDocumentsUploaded(): boolean {
        return this.kycDocuments.every((doc) => this.isKycDocumentUploaded(doc.fileType));
    }

    viewDocument(fileType: string) {
        const doc = this.clientKycFiles.find((f) => f.fileType === fileType);
        if (doc) {
            window.open(doc.fileUrl, '_blank');
        } else {
            this.messagesService.showError('Document not found.');
        }
    }

    loadLicenseFiles() {
        this.uploadService.getLicenseFiles().subscribe({
            next: (files: LicenseFile[]) => {
                this.licenseFiles = files;
            },
            error: (error) => {
                console.error('Error loading license files:', error);
                this.messagesService.showError('Error loading license files');
            }
        });
    }

    getFilteredUsers(role: string): User[] {
        return this.users.filter((user) => user.role.name === role);
    }

    onLicenseFileSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.uploadedFile = file;
        this.selectedLicense = {
            id: 0,
            fileType,
            fileName: file.name,
            fileUrl: '',
            version: 1,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };
    }

    onLicenseUpload(fileType: string) {
        if (!this.uploadedFile) {
            return;
        }

        this.loading = true;
        let userId: number | undefined;

        if (fileType === 'STRUCTURAL_ENGINEER_LICENSE' && this.selectedEngineer) {
            userId = this.selectedEngineer.id;
        } else if (fileType === 'ARCHITECT_LICENSE' && this.selectedArchitect) {
            userId = this.selectedArchitect.id;
        }

        this.uploadService.upload(fileType, this.uploadedFile, this.clientId, undefined, userId).subscribe({
            next: (response: any) => {
                this.loading = false;
                this.uploadedFile = null;
                this.selectedLicense = null;
                this.loadLicenseFiles();
                this.messagesService.showSuccess('License uploaded successfully');
            },
            error: (error: Error) => {
                this.loading = false;
                this.messagesService.showError('Failed to upload license');
            }
        });
    }

    isLicenseUploaded(fileType: string, userId?: number): boolean {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            return this.licenseFiles.some((doc) => doc.fileType === fileType);
        }
        return this.licenseFiles.some((doc) => doc.fileType === fileType && doc.userId === userId);
    }

    viewLicense(fileType: string, userId?: number) {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            const ncaDoc = this.licenseFiles.find((doc) => doc.fileType === fileType);
            if (ncaDoc) {
                window.open(ncaDoc.fileUrl, '_blank');
            }
        } else {
            const doc = this.licenseFiles.find((doc) => doc.fileType === fileType && doc.userId === userId);
            if (doc) {
                window.open(doc.fileUrl, '_blank');
            }
        }
    }

    isAtLeastOneLicenseUploaded(): boolean {
        return (
            this.isLicenseUploaded('NCA_CONTRACTOR_LICENSE') ||
            (this.selectedEngineer !== null && this.isLicenseUploaded('STRUCTURAL_ENGINEER_LICENSE', this.selectedEngineer.id)) ||
            (this.selectedArchitect !== null && this.isLicenseUploaded('ARCHITECT_LICENSE', this.selectedArchitect.id))
        );
    }

    loadUsers() {
        this.userService.getUsers().subscribe({
            next: (users: User[]) => {
                this.users = users;
            },
            error: (error: Error) => {
                console.error('Error loading users:', error);
                this.messagesService.showError('Error loading users');
            }
        });
    }

    isCountyInvoiceUploaded(): boolean {
        return this.modalPreliminary?.files?.some((file: Files) => file.fileType === 'COUNTY_INVOICE') ?? false;
    }

    onCountyInvoiceSelect(event: any): void {
        const file: File = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = { fileType: 'COUNTY_INVOICE', fileName: file.name, fileUrl: '' };
    }

    isCountyReceiptUploaded(): boolean {
        return this.modalPreliminary?.files?.some((file: Files) => file.fileType === 'COUNTY_RECEIPT') || false;
    }

    onCountyReceiptSelect(event: { files: File[] }): void {
        const file = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = {
            fileName: file.name,
            fileType: 'COUNTY_RECEIPT',
            fileUrl: URL.createObjectURL(file)
        };
    }

    isApprovedDocumentUploaded(): boolean {
        return this.modalPreliminary?.files?.some((file: Files) => file.fileType === 'COUNTY_APPROVED_DOCUMENT') || false;
    }

    onApprovedDocumentSelect(event: { files: File[] }): void {
        const file = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = {
            fileName: file.name,
            fileType: 'COUNTY_APPROVED_DOCUMENT',
            fileUrl: URL.createObjectURL(file)
        };
    }

    submitCountyApprovalStatus(): void {
        if (!this.countyApprovalStatus) {
            this.messagesService.showError('Please select an approval status');
            return;
        }

        if (this.countyApprovalStatus === 'APPROVED' && !this.isApprovedDocumentUploaded()) {
            this.messagesService.showError('Please upload the approved document');
            return;
        }

        if (this.countyApprovalStatus === 'REJECTED' && !this.countyRejectionRemarks) {
            this.messagesService.showError('Please provide rejection remarks');
            return;
        }

        this.loading = true;
        this.preliminaryService.updateCountyApprovalStatus(
            this.modalPreliminary.id,
            this.countyApprovalStatus,
            this.countyRejectionRemarks
        ).subscribe({
            next: () => {
                this.loading = false;
                this.messagesService.showSuccess('County approval status updated successfully');
                this.displayDialog = false;
                this.loadPreliminaries();
            },
            error: (error: { error: { details: string } }) => {
                this.loading = false;
                this.messagesService.showError(error.error.details || 'Failed to update county approval status');
            }
        });
    }

    protected readonly Permissions = Permissions;
}
