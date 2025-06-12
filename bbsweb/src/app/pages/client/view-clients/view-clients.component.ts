import { Component, OnInit } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { Dialog } from 'primeng/dialog';
import { Divider } from 'primeng/divider';
import { PrimeTemplate } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import { Activity, Client, Files } from '../../data/clietDTOs';
import { ClientDetailsService } from '../client.service';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { Calendar } from 'primeng/calendar';
import { UploadService } from '../../service/upload.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { Tab, TabList, TabPanel, TabPanels, Tabs } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import { Listbox } from 'primeng/listbox';
import { FileUpload } from 'primeng/fileupload';
import { switchMap } from 'rxjs/operators';
import { InvoiceFormComponent } from '../../invoice-form/invoice-form.component';
import { Router } from '@angular/router';
import { kenyanCounties } from '../../data/kenyan-counties';
import { allCountries } from '../../data/all-countries';
import { countryCodes } from '../../data/country-codes';
import { ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextModule } from 'primeng/inputtext';
import { SortEvent } from 'primeng/api';
import { ProgressSpinner } from 'primeng/progressspinner';

@Component({
    selector: 'app-view-clients',
    imports: [ButtonDirective, Divider, PrimeTemplate, TableModule, Toast, ReactiveFormsModule, DropdownModule, ConfirmDialogModule, InputTextModule, FormsModule, NgIf, ProgressSpinner],
    providers: [ConfirmationService],
    templateUrl: './view-clients.component.html',
    styleUrl: './view-clients.component.scss'
})
export class ViewClientsComponent implements OnInit {
    clients: Client[] = [];
    filteredClients: Client[] = [];
    searchQuery: string = '';
    loading = false;
    visible = false;
    personalInfoForm!: FormGroup;
    selectedClient: Client | null = null;
    displayClientDialog: boolean = false;
    editMode: boolean = false;
    activityFeedVisible: boolean = true;
    clientFiles: Files[] = [];
    selectedDocument: Files | null = null;
    clientStage: string = '';
    displayEditDialog: boolean = false;
    newFile: File | null = null;
    clientId: number = 0;
    editDocumentMode = false;
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

    constructor(
        private clientService: ClientDetailsService,
        private messagesService: MessagesService,
        private uploadService: UploadService,
        private router: Router,
        private fb: FormBuilder,
        private confirmationService: ConfirmationService
    ) {}

    ngOnInit() {
        this.getActiveClients();

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
    }

    viewClient(client: Client): void {
        this.router.navigate(['app/pages/profile', client.id], { state: { client } });
    }

    editClient(): void {
        this.editMode = true;
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
                    this.getActiveClients();
                    this.editMode = false;
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Error updating client', error);
                }
            });
        }
    }

    onLocationTypeChange(event: any) {
        if (event.value === 'INTERNATIONAL') {
            this.personalInfoForm.get('county')?.setValue(null);
        } else {
            this.personalInfoForm.get('country')?.setValue(null);
        }
    }

    deleteClient(id: number) {
        this.confirmationService.confirm({
            message: 'Are you sure you want to delete this client?',
            header: 'Delete Confirmation',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.loading = true;
                this.clientService.deleteClient(id).subscribe({
                    next: () => {
                        this.messagesService.showSuccess('Client deleted successfully');
                        this.getActiveClients();
                    },
                    error: (error) => {
                        this.messagesService.showError('Failed to delete client');
                        this.loading = false;
                    }
                });
            }
        });
    }

    getActiveClients(): void {
        this.loadingData = true;
        this.clientService.getActiveClients().subscribe({
            next: (clients: Client[]) => {
                this.clients = clients;
                this.filteredClients = [...clients];
                this.loadingData = false;

            },
            error: (err) => {
                this.loadingData = false;
                this.clients = [];
                this.filteredClients = [];
                console.log('Error getting clients ', err);
            }
        });
    }

    getClientFiles(clientId: number): void {
        this.loading = true;
        this.clientService.getClientFiles(clientId).subscribe({
            next: (files: Files[]) => {
                this.clientFiles = files;
                this.loading = false;
            }
        });
    }

    onFileChange(event: any): void {
        this.newFile = event.target.files[0];
    }

    editDocument(doc: Files): void {
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
                    this.messagesService.showSuccess('Document updated successfully.');
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Error updating document', error);
                }
            });
        }
    }

    uploadedFile: any;
    loadingData = false;

    onUpload() {
        this.loading = true;
        this.uploadService
            .upload( 'REQUIREMENTS', this.uploadedFile, this.idNumber)
            .pipe(
                switchMap(() => {
                    this.loading = false;
                    this.messagesService.showSuccess('Document uploaded successfully.');
                    return this.clientService.getAllClients();
                })
            )
            .subscribe({
                next: (clients: Client[]) => {
                    this.clients = clients;
                    this.filteredClients = [...clients];
                    this.selectedClient = this.clients.find((client: Client) => client.id === this.selectedClient?.id) ?? null;
                    if (this.selectedClient) {
                        console.log('client reloaded >>> ', this.selectedClient.clientStage);
                        this.viewClient(this.selectedClient);
                        this.clientStage = this.selectedClient.clientStage;
                    }
                },
                error: (err) => {
                    this.loading = false;
                    console.error('Something went wrong:', err);
                }
            });
    }

    onSelect(event: any) {
        this.uploadedFile = event.target.files[0];
    }

    getClientActivities(clientId: number) {
        this.clientService.getClientActivities(clientId).subscribe({
            next: (response) => {
                this.activities = response;
            }
        });
    }

    onSearch() {
        if (!this.searchQuery.trim()) {
            this.filteredClients = [...this.clients];
            return;
        }
        const query = this.searchQuery.toLowerCase().trim();
        this.filteredClients = this.clients.filter(
            (client) =>
                (client.firstName?.toLowerCase() + ' ' + client.lastName?.toLowerCase()).includes(query) ||
                client.phoneNumber?.toLowerCase().includes(query) ||
                (client.location || 'KENYA').toLowerCase().includes(query) ||
                (client.clientStage || 'REGISTRATION').toLowerCase().includes(query) ||
                (client.email || 'System').toLowerCase().includes(query)
        );
    }

    onSort(event: SortEvent) {
        const field = event.field;
        const order = event.order || 1;
        this.filteredClients.sort((a, b) => {
            const valA = this.getFieldValue(a, <string>field);
            const valB = this.getFieldValue(b, <string>field);
            if (valA < valB) return -1 * order;
            if (valA > valB) return 1 * order;
            return 0;
        });
    }

    private getFieldValue(client: Client, field: string): string {
        if (field === 'firstName') {
            return `${client.firstName || ''} ${client.lastName || ''}`.toLowerCase().trim();
        }
        const value = client[field as keyof Client];
        return (value !== undefined && value !== null ? value.toString() : '').toLowerCase();
    }
}
