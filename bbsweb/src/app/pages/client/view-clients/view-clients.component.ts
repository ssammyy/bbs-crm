import { Component, OnInit } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { Dialog } from 'primeng/dialog';
import { Divider } from 'primeng/divider';
import { PrimeTemplate } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import { Activity, Client } from '../../data/clietDTOs';
import { ClientDetailsService } from '../client.service';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Files } from '../../data/clietDTOs';
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

@Component({
    selector: 'app-view-clients',
    imports: [
        ButtonDirective,
        Divider,
        PrimeTemplate,
        TableModule,
        Toast,
        Button,
        Dialog,
        NgIf,
        ReactiveFormsModule,
        NgForOf,
        InputText,
        DropdownModule,
        Calendar,
        Tab,
        TabList,
        TabPanels,
        TabPanel,
        Tabs,
        Tag,
        Listbox,
        DatePipe,
        NgClass,
        InvoiceFormComponent
    ],
    templateUrl: './view-clients.component.html',
    styleUrl: './view-clients.component.scss'
})
export class ViewClientsComponent implements OnInit {
    clients: Client[] = [];
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
        private fb: FormBuilder
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
            // locationType: ['', Validators.required],
            location: ['', Validators.required],
            preferredContact: ['', Validators.required],
            idNumber: ['', Validators.required],
            county: [''],
            country: [''],
            dob: ['']
        });
    }

    // viewClient(client: Client): void {
    //     this.visible = true;
    //     this.selectedClient = client;
    //     this.personalInfoForm.patchValue(client);
    //     this.clientId = client.id;
    //     this.clientStage = client.clientStage;
    //     (this.idNumber = client.idNumber);
    //     this.getClientFiles(client.id);
    //     this.displayClientDialog = true;
    //     this.getClientActivities(client.id);
    //     this.editMode = false;
    // }

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
                    // this.visible = false;
                },
                error: (error) => {
                    this.loading = false;
                    console.error('Error updating client', error);
                }
            });
        }
    }

    onLocationTypeChange(event: any) {
        // You may need to add additional logic here,
        // like clearing the country or county field when the location type changes.
        // example:
        if (event.value === 'INTERNATIONAL') {
            this.personalInfoForm.get('county')?.setValue(null);
        } else {
            this.personalInfoForm.get('country')?.setValue(null);
        }
    }

    deleteClient(id: number) {}

    getActiveClients(): void {
        this.loading = true;
        this.clientService.getActiveClients().subscribe({
            next: (clients: Client[]) => {
                this.loading = false;
                this.clients = clients;
            },
            error: (err) => {
                this.loading = false;
                this.clients = [];
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

    onUpload() {
        this.loading = true;
        this.uploadService
            .upload(this.idNumber, 'REQUIREMENTS', this.uploadedFile)
            .pipe(
                switchMap(() => {
                    this.loading = false;
                    this.messagesService.showSuccess('Document uploaded successfully.');
                    return this.clientService.getAllClients(); // wait for this to complete
                })
            )
            .subscribe({
                next: (clients: Client[]) => {
                    this.clients = clients;

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
}
