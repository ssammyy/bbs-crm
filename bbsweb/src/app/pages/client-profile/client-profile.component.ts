import { Component, OnInit } from '@angular/core';
import { Button } from 'primeng/button';
import { Calendar } from 'primeng/calendar';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { Dialog } from 'primeng/dialog';
import { Divider } from 'primeng/divider';
import { DropdownModule } from 'primeng/dropdown';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { InvoiceFormComponent } from '../invoice-form/invoice-form.component';
import { Listbox } from 'primeng/listbox';
import { PrimeTemplate } from 'primeng/api';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import { ClientDetailsService } from '../client/client.service';
import { MessagesService } from '../../layout/service/messages.service';
import { UploadService } from '../service/upload.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Activity, Client, Files } from '../data/clietDTOs';
import { switchMap } from 'rxjs/operators';
import { Toast } from 'primeng/toast';
import { Textarea } from 'primeng/textarea';
import { InvoiceService } from '../invoice-form/invoice.service';
import { UserGlobalService } from '../service/user.service';
import { kenyanCounties } from '../data/kenyan-counties';
import { allCountries } from '../data/all-countries';
import { countryCodes } from '../data/country-codes';
import { InvoiceType } from '../invoice-form/data';
import { SiteReportFormComponent } from '../site-report-form/site-report-form.component';
import { SiteReportViewComponent } from '../site-report-view/site-report-view.component';
import { PreliminaryManagementComponent } from '../preliminaries-management/preliminaries-management.component';
import { Message } from 'primeng/message';
import { ViewInvoicesComponent } from '../view-invoices/view-invoices.component';
import { ChipModule } from 'primeng/chip';
import { TableModule } from 'primeng/table';
import { FileUploadModule } from 'primeng/fileupload';
import { NonKycDocumentPipe } from '../site-report-form/non-kyc-document.pipe';
import { KYC_DOCUMENTS, KycDocument } from '../site-report-form/kyc-documents';
import { ReceiptsComponent } from '../receipts/receipts.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { NgxSpinnerService } from 'ngx-spinner';
import { Permissions } from '../data/permissions.enum';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Drawer } from 'primeng/drawer';
import {
    ClientMilestoneChecklistComponent
} from '../dashboard/components/client-milestone-checklist/client-milestone-checklist.component';
import { ContractSigningComponent } from '../contract-signing/contract-signing.component';

@Component({
    selector: 'app-client-profile',
    imports: [
        Button,
        Calendar,
        DatePipe,
        Dialog,
        Divider,
        DropdownModule,
        FormsModule,
        InputText,
        InvoiceFormComponent,
        Listbox,
        NgForOf,
        NgIf,
        PrimeTemplate,
        ReactiveFormsModule,
        Tab,
        TabList,
        TabPanel,
        Tabs,
        Tag,
        NgClass,
        Toast,
        Textarea,
        SiteReportFormComponent,
        SiteReportViewComponent,
        PreliminaryManagementComponent,
        Message,
        TabPanels,
        ViewInvoicesComponent,
        ChipModule,
        TableModule,
        FileUploadModule,
        NonKycDocumentPipe,
        ReceiptsComponent,
        NgxSpinnerModule,
        ProgressSpinner,
        Drawer,
        ClientMilestoneChecklistComponent,
        ContractSigningComponent
    ],
    templateUrl: './client-profile.component.html',
    styleUrls: ['./client-profile.component.scss'],
    standalone: true
})
export class ClientProfileComponent implements OnInit {
    activeTab: string = '0';
    activities: Activity[] = [];
    locationTypes = [
        { label: 'Kenya', value: 'KENYA' },
        { label: 'International', value: 'INTERNATIONAL' }
    ];
    genders = [
        { label: 'Male', value: 'Male' },
        { label: 'Female', value: 'Female' },
        { label: 'Other', value: 'Other' }
    ];
    contactMethods = [
        { label: 'Call', value: 'CALL' },
        { label: 'SMS', value: 'SMS' },
        { label: 'Email', value: 'EMAIL' },
        { label: 'WhatsApp', value: 'WHATSAPP' }
    ];
    kenyanCounties = kenyanCounties;
    allCountries = allCountries;
    countryCodes: any[] = countryCodes;
    idNumber: number = 0;
    personalInfoForm!: FormGroup;
    editMode: boolean = false;
    loading: boolean = false;
    selectedClient: Client | null = null;
    clientFiles: Files[] = [];
    clientIDMock: number = 10;
    activityFeedVisible: boolean = true;
    selectedDocument: Files | null = null;
    clientStage: string = '';
    displayEditDialog: boolean = false;
    newFile: File | null = null;
    clientId: number = 0;
    uploadedFile: any;
    rejectDialog = false;
    loadingData = false;
    invoiceRejectionRemarks = '';
    clients: Client[] = [];
    userRole!: string;
    siteVisitDialog = false;
    siteVisitRemarks = '';
    drawingsDialog = false;
    drawingsRemarks = '';
    boqDialog = false;
    boqRemarks = '';
    contractDialog = false;
    contractRemarks = '';

    // KYC Documents Configuration
    kycDocuments: KycDocument[] = KYC_DOCUMENTS;

    displayVersionDialog: boolean = false;
    versionNotes: string = '';

    constructor(
        private clientService: ClientDetailsService,
        private messagesService: MessagesService,
        private invoiceService: InvoiceService,
        private uploadService: UploadService,

        private userGlobalService: UserGlobalService,
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private spinner: NgxSpinnerService
    ) {}

    async ngOnInit() {
        this.loadingData = true;
        try {
            const clientId = this.route.snapshot.paramMap.get('id');
            if (clientId) {
                await this.fetchClientById(Number(clientId));
            } else if (history.state.client) {
                // If we have history state, still fetch fresh data
                await this.fetchClientById(history.state.client.id);
            }

            this.personalInfoForm = this.fb.group({
                firstName: ['', Validators.required],
                secondName: ['', Validators.required],
                lastName: ['', Validators.required],
                email: ['', Validators.email],
                gender: ['', Validators.required],
                countryCode: ['+254'],
                phoneNumber: ['', Validators.required],
                location: ['', Validators.required],
                preferredContact: ['', Validators.required],
                idNumber: ['', Validators.required],
                county: [''],
                country: [''],
                dob: ['']
            });
            if (this.selectedClient) {
                this.patchValues(this.selectedClient);
            }

            await Promise.all([this.getClientFiles(<number>this.selectedClient?.id), this.getClientActivities(<number>this.selectedClient?.id), this.getUserDetails()]);
        } finally {
            this.loadingData = false;
        }
    }

    patchValues(client: Client): void {
        this.personalInfoForm.patchValue(client);
        this.clientId = client.id;
        this.clientStage = client.clientStage;
        this.idNumber = client.idNumber;
    }

    editClient(): void {
        this.editMode = true;
    }

    onLocationTypeChange(event: any) {
        if (event.value === 'INTERNATIONAL') {
            this.personalInfoForm.get('county')?.setValue(null);
        } else {
            this.personalInfoForm.get('country')?.setValue(null);
        }
    }

    saveClient(): void {
        this.loading = true;
        this.personalInfoForm.addControl('surName', new FormControl(this.personalInfoForm.get('lastName')?.value));
        this.personalInfoForm.addControl('locationType', new FormControl(this.personalInfoForm.get('location')?.value));
        if (this.personalInfoForm.valid && this.selectedClient) {
            const updatedClient = { ...this.selectedClient, ...this.personalInfoForm.value };
            this.clientService.updateClient(updatedClient).subscribe({
                next: () => {
                    this.messagesService.showSuccess('Client updated');
                    this.loading = false;
                    this.editMode = false;
                    this.clientService.getClient(this.clientId).subscribe({
                        next: (client: Client) => {
                            this.selectedClient = client;
                        }
                    });
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Error updating client', error);
                }
            });
        }
    }

    getClientFiles(clientId: number): Promise<void> {
        return new Promise((resolve) => {
            this.spinner.show();
            this.clientService.getClientFiles(clientId).subscribe({
                next: (files: Files[]) => {
                    console.log(' client files ', { files });
                    this.clientFiles = files;
                    this.spinner.hide();
                    resolve();
                },
                error: (error) => {
                    console.error('Error fetching files:', error);
                    this.spinner.hide();
                    resolve();
                }
            });
        });
    }

    editDocument(doc: Files | null): void {
        this.selectedDocument = doc;
        this.displayEditDialog = true;
    }

    uploadNewFile(): void {
        this.loading = true;
        if (this.newFile && this.selectedDocument) {
            this.uploadService.updateDocument(this.clientId, this.newFile, this.selectedDocument.fileType, this.selectedDocument.id).subscribe({
                next: () => {
                    this.loading = false;
                    this.displayEditDialog = false;
                    this.getClientFiles(this.clientId);
                    this.getClientActivities(this.clientId);
                    this.messagesService.showSuccess('Document updated successfully.');
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Error updating document', error);
                }
            });
        }
    }

    onUpload(fileType: string) {
        this.loading = true;
        if (!this.uploadedFile) {
            this.messagesService.showError('No file selected for upload.');
            this.loading = false;
            return;
        }
        this.uploadService
            .upload( fileType, this.uploadedFile, this.clientId)
            .pipe(
                switchMap(() => {
                    this.loading = false;
                    this.messagesService.showSuccess('Document uploaded successfully.');
                    this.getClientActivities(this.clientId);
                    // Reset selection
                    this.uploadedFile = null;
                    this.selectedDocument = null;
                    return this.clientService.getAllClients();
                })
            )
            .subscribe({
                next: (clients: Client[]) => {
                    this.clients = clients;
                    this.selectedClient = this.clients.find((client: Client) => client.id === this.selectedClient?.id) ?? null;
                    if (this.selectedClient) {
                        this.clientStage = this.selectedClient.clientStage;
                    }
                    this.getClientFiles(this.clientId); // Refresh file list
                },
                error: (err) => {
                    this.loading = false;
                    console.error('Something went wrong:', err);
                    this.messagesService.showError('Error uploading document.');
                }
            });
    }
    onSelect(event: any) {
        this.uploadedFile = event.target.files[0];
    }

    onFileChange(event: any): void {
        this.newFile = event.target.files[0];
    }

    onKycFileSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = { fileType, fileName: file.name, fileUrl: '' }; // Temporary object to track selection
    }

    getClientActivities(clientId: number): Promise<void> {
        return new Promise((resolve) => {
            this.spinner.show();
            this.clientService.getClientActivities(clientId).subscribe({
                next: (response) => {
                    this.activities = response;
                    this.spinner.hide();
                    resolve();
                },
                error: (error) => {
                    console.error('Error fetching activities:', error);
                    this.spinner.hide();
                    resolve();
                }
            });
        });
    }

    handleEventEmit() {
        this.clientService.getClient(<number>this.selectedClient?.id).subscribe({
            next: (response) => {
                this.selectedClient = response;
                this.clientStage = response.clientStage;
                this.getClientFiles(this.clientId);
            },
            error: (error) => {
                console.log('Error updating client', error);
            }
        });
    }

    reloadClientData() {
        if (this.selectedClient?.id) {
            this.fetchClientById(this.selectedClient.id).then((r) => {});
        }
    }

    viewInvoice() {
        this.invoiceService.getInvoicePdfNew(<number>this.selectedClient?.id, 'SITE_VISIT').subscribe({
            next: (response: Blob) => {
                const url = window.URL.createObjectURL(response);
                window.open(url, '_blank');
            },
            error: () => {
                console.log('Error getting invoice PDF');
            }
        });
    }

    acceptInvoice(stage: string, invoiceType: InvoiceType) {
        this.invoiceService.acceptInvoice(<number>this.selectedClient?.id, stage, invoiceType).subscribe({
            next: (response) => {
                this.selectedClient = response;
                this.clientStage = response.clientStage;
                this.getClientActivities(this.clientId);
                this.messagesService.showSuccess('Invoice approved successfully.');
            },
            error: () => {
                this.messagesService.showError('Error approving invoice.');
            }
        });
    }

    rejectInvoice(stage: string) {
        this.invoiceService.rejectInvoice(<number>this.selectedClient?.id, this.invoiceRejectionRemarks, stage).subscribe({
            next: (response) => {
                this.selectedClient = response;
                this.clientStage = response.clientStage;
                this.getClientActivities(this.clientId);
                this.rejectDialog = false;
                this.invoiceRejectionRemarks = '';
                this.messagesService.showSuccess('Invoice rejected successfully.');
            },
            error: () => {
                this.messagesService.showError('Error rejecting invoice.');
            }
        });
    }

    completeSiteVisit() {
        this.uploadService.upload( 'SITE_VISIT_REPORT', this.uploadedFile, this.idNumber).subscribe({
            next: () => {
                this.clientService.updateClientStage(<number>this.selectedClient?.id, 'GENERATE_DRAWINGS_INVOICE', 'PENDING_CLIENT_DRAWINGS_PAYMENT', `Site visit completed: ${this.siteVisitRemarks}`).subscribe({
                    next: (response) => {
                        this.selectedClient = response;
                        this.clientStage = response.clientStage;
                        this.getClientActivities(this.clientId);
                        this.siteVisitDialog = false;
                        this.siteVisitRemarks = '';
                        this.uploadedFile = null;
                        this.messagesService.showSuccess('Site visit completed successfully.');
                    },
                    error: () => {
                        this.messagesService.showError('Error completing site visit.');
                    }
                });
            },
            error: () => {
                this.messagesService.showError('Error uploading site visit report.');
            }
        });
    }

    async fetchClientById(id: number): Promise<void> {
        try {
            const response: Client = <Client>await this.clientService.getClient(id).toPromise();
            this.selectedClient = response;
            this.clientStage = response.clientStage;
            console.log('Client Stage from fetch:', this.clientStage);
            this.patchValues(response);
        } catch (error) {
            console.error('Error fetching client:', error);
            this.messagesService.showError('Error fetching client data.');
        }
    }

    getUserDetails(): Promise<void> {
        return new Promise((resolve) => {
            this.spinner.show();
            this.userGlobalService.getDetails().subscribe({
                next: (response) => {
                    this.userRole = response?.role?.name;
                    this.spinner.hide();
                    resolve();
                },
                error: (error) => {
                    console.error('Error fetching user details:', error);
                    this.spinner.hide();
                    resolve();
                }
            });
        });
    }

    goToInvoices() {
        this.activeTab = '3';
    }

    viewDocument(fileType: string) {
        const doc = this.clientFiles.find((f) => f.fileType === fileType);
        if (doc) {
            window.open(doc.fileUrl, '_blank');
        } else {
            this.messagesService.showError('Document not found.');
        }
    }

    isKycDocumentUploaded(fileType: string): boolean {
        return this.clientFiles.some((file) => file.fileType === fileType);
    }

    protected readonly InvoiceType = InvoiceType;

    onTabChange(event: any) {
        console.log('Tab Change', event);
        this.activeTab = event;
        console.log('active tab ', this.activeTab);
        if (event.value === '0' && this.selectedClient) {
            // Reload client data when activity tab is selected
            this.reloadClientData();
            this.getClientActivities(this.selectedClient.id);
            this.getClientFiles(this.selectedClient.id);
            this.clientService.getClient(this.selectedClient.id).subscribe({
                next: (client: Client) => {
                    this.selectedClient = client;
                    this.clientStage = client.clientStage;
                    console.log('Client Stage after tab change:', this.clientStage);
                }
            });
        }
    }

    hasPrivileges(privilege: string): boolean {
        return this.userGlobalService.hasPrivilege(privilege);
    }

    addDocumentVersion(file: any) {
        this.selectedDocument = file;
        this.displayVersionDialog = true;
        this.versionNotes = '';
        this.newFile = null;
    }

    uploadNewVersion() {
        if (!this.newFile || !this.selectedDocument) {
            this.messagesService.showError('Please select a file to upload.');
            return;
        }
        this.loading = true;

        this.uploadService.updateDocument(this.clientId, this.newFile, this.selectedDocument.fileType, this.selectedDocument.id, this.versionNotes).subscribe({
            next: () => {
                this.loading = false;
                this.displayVersionDialog = false;
                this.getClientFiles(this.clientId);
                this.getClientActivities(this.clientId);
                this.messagesService.showSuccess('New version uploaded successfully.');
                // Reset form
                this.versionNotes = '';
                this.newFile = null;
                this.selectedDocument = null;
            },
            error: (error) => {
                this.loading = false;
                console.error('Error uploading new version', error);
                this.messagesService.showError('Error uploading new version.');
            }
        });
    }

    protected readonly Permissions = Permissions;
    checklistVisible = false;

    onContractSigned() {
        // Handle contract signing completion
        this.handleEventEmit();
    }
}
