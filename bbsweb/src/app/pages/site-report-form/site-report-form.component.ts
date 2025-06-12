import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { SiteReportService } from './siteReport.service';
import { Client } from '../data/clietDTOs';
import { NgClass, NgIf, CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { ChipModule } from 'primeng/chip';
import { UploadService } from '../service/upload.service';
import { Textarea } from 'primeng/textarea';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelect } from 'primeng/multiselect';
import { KYC_DOCUMENTS, KycDocument } from './kyc-documents';

@Component({
    selector: 'app-site-report-form',
    templateUrl: './site-report-form.component.html',
    providers: [MessageService],
    imports: [
        ReactiveFormsModule,
        NgIf,
        NgClass,
        CommonModule,
        CardModule,
        InputTextModule,
        ButtonModule,
        MessageModule,
        ToastModule,
        FileUploadModule,
        ChipModule,
        Textarea,
        DropdownModule,
        MultiSelect
    ],
    standalone: true
})
export class SiteReportFormComponent implements OnInit {
    @Input() client: Client | null = null;
    @Output() reportGenerated = new EventEmitter<void>();

    siteReportForm: FormGroup;
    hasExistingReport: boolean = false;
    existingReportId: number | null = null;
    loading = false;
    submitted = false;

    // Use shared KYC documents
    kycDocuments: KycDocument[] = KYC_DOCUMENTS;

    // Track selected documents (not yet uploaded)
    selectedDocuments: { [key: string]: { file: File; fileUrl?: string } } = {};
    infrastructureOptions: { name: string; value: string }[] = [
        { name: 'Electricity', value: 'ELECTRICITY' },
        { name: 'Water', value: 'WATER' },
        { name: 'Security', value: 'SECURITY' },
        { name: 'Internet', value: 'INTERNET' },
        { name: 'Sewage', value: 'SEWAGE' },
        { name: 'Road Access', value: 'ROAD_ACCESS' },
        { name: 'Public Transport', value: 'PUBLIC_TRANSPORT' }
    ];

    constructor(
        private fb: FormBuilder,
        private siteReportService: SiteReportService,
        private uploadService: UploadService,
        private messageService: MessageService
    ) {
        this.siteReportForm = this.fb.group({
            location: ['', Validators.required],
            clientName: [{ value: '', disabled: true }, Validators.required],
            soilType: ['', Validators.required],
            measurements: ['', [Validators.required]],
            topography: ['', Validators.required],
            infrastructure: [[], Validators.required],
            notes: ['']
        });
    }

    ngOnInit(): void {
        if (this.client) {
            this.siteReportForm.patchValue({ clientName: `${this.client.firstName} ${this.client.lastName}` });
            this.getClientReport(this.client);
        }
    }

    getClientReport(client: Client) {
        this.siteReportService.getReportByClientId(<number>client.id).subscribe({
            next: (report) => {
                if (report) {
                    this.hasExistingReport = true;
                    this.existingReportId = report.id;
                    this.siteReportForm.patchValue({
                        location: report.location,
                        clientName: report.clientName,
                        soilType: report.soilType,
                        measurements: report.siteMeasurements,
                        topography: report.topography,
                        infrastructure: report.infrastructure,
                        notes: report.notes || ''
                    });
                    // Preload existing documents if any
                    if (report.documents) {
                        report.documents.forEach((doc: any) => {
                            this.selectedDocuments[doc.fileType] = {
                                file: new File([], doc.fileName),
                                fileUrl: doc.fileUrl
                            };
                        });
                    }
                }
            },
            error: (error) => {
                console.error('Error fetching report:', error);
            }
        });
    }

    isDocumentSelected(fileType: string): boolean {
        return !!this.selectedDocuments[fileType];
    }

    onSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.selectedDocuments[fileType] = { file };
        this.messageService.add({
            severity: 'info',
            summary: 'File Selected',
            detail: `${fileType.replace('_', ' ')} selected. It will be uploaded when you submit the report.`
        });
    }

    removeDocument(fileType: string): void {
        delete this.selectedDocuments[fileType];
        this.messageService.add({
            severity: 'info',
            summary: 'Removed',
            detail: `${fileType.replace('_', ' ')} removed`
        });
    }

    async uploadAllDocuments(clientId: number): Promise<boolean> {
        for (const [fileType, doc] of Object.entries(this.selectedDocuments)) {
            // Skip if the document already has a fileUrl (previously uploaded)
            if (doc.fileUrl) continue;

            try {
                const response: any = await this.uploadService.upload( fileType, doc.file, clientId).toPromise();
                this.selectedDocuments[fileType].fileUrl = response.fileUrl || response.message;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: `${fileType.replace('_', ' ')} uploaded successfully`
                });
            } catch (error) {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: `Failed to upload ${fileType.replace('_', ' ')}`
                });
                return false;
            }
        }
        return true;
    }

    async onSubmit(): Promise<void> {
        this.submitted = true;

        if (this.siteReportForm.valid && this.client?.id) {
            this.loading = true;

            // Upload any selected documents (optional)
            if (Object.keys(this.selectedDocuments).length > 0) {
                const uploadSuccess = await this.uploadAllDocuments(this.client.id);
                if (!uploadSuccess) {
                    this.loading = false;
                    return;
                }
            }

            // Submit the report
            const formValue = this.siteReportForm.getRawValue();
            const reportData = {
                location: formValue.location,
                clientName: formValue.clientName,
                soilType: formValue.soilType,
                siteMeasurements: formValue.measurements,
                topography: formValue.topography,
                infrastructure: formValue.infrastructure,
                notes: formValue.notes,
                clientId: this.client?.id,
                id: this.existingReportId
            };

            this.siteReportService.submitReport(reportData).subscribe({
                next: (response) => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Success',
                        detail: this.hasExistingReport ? 'Report updated successfully' : 'Report submitted successfully'
                    });
                    this.reportGenerated.emit();
                    this.hasExistingReport = true;
                    this.loading = false;
                    this.existingReportId = response.id;
                },
                error: (error) => {
                    this.loading = false;
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: error.error?.message || 'Failed to submit report'
                    });
                }
            });
        } else {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Please fill in all required fields'
            });
        }
    }
}
