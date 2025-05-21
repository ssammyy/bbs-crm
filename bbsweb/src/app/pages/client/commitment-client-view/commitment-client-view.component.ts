import { Component } from '@angular/core';
import { ClientCommitmentService } from '../client-commitment/client-commitment.service';
import { TableModule } from 'primeng/table';
import { DropdownModule } from 'primeng/dropdown';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, formatDate, NgIf } from '@angular/common';
import { MessageService } from 'primeng/api';
import { Toast } from 'primeng/toast';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { Calendar } from 'primeng/calendar';
import { InputTextarea } from 'primeng/inputtextarea';
import { Badge } from 'primeng/badge';
import { Client } from '../../data/clietDTOs';
import { ClientDetailsService } from '../client.service';
import { Textarea } from 'primeng/textarea';


@Component({
    selector: 'app-commitment-client-view',
    imports: [TableModule, DropdownModule, FormsModule, DatePipe, Toast, Button, Dialog, NgIf, Calendar, ReactiveFormsModule, InputTextarea, Badge, Textarea],
    templateUrl: './commitment-client-view.component.html',
    styleUrl: './commitment-client-view.component.scss'
})
export class CommitmentClientViewComponent {
    clientCommitments: Client[] = [];
    statusOptions = [
        { label: 'Onboarded', value: 'ONBOARDED' },
        { label: 'Contacted', value: 'CONTACTED' },
        { label: 'In Talks', value: 'IN_TALKS' },
        { label: 'Lead', value: 'LEAD' }
    ];
    displayEditDialog: boolean = false;
    editForm!: FormGroup;
    selectedClientCommitment: any = null;
    minDate: Date = new Date();

    constructor(
        private clientCommitmentService: ClientCommitmentService,
        private messageService: MessageService,
        private clientService: ClientDetailsService,
        private fb: FormBuilder
    ) {}

    ngOnInit(): void {
        this.loadClientCommitments();
        this.initializeEditForm();
    }

    initializeEditForm(): void {
        this.editForm = this.fb.group({
            followUpDate: ['', Validators.required],
            contactStatus: ['', Validators.required],
            notes: ['']
        });
    }

    loadClientCommitments(): void {
        // this.clientCommitmentService.getClientCommitmentsToFollowUp().subscribe({
        this.clientService.getLeads().subscribe({
            next: (clientCommitments) => {
                this.clientCommitments = clientCommitments;
            },
            error: (error) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load client commitments'
                });
                console.error('Error loading client commitments:', error);
            }
        });
    }

    showEditDialog(clientCommitment: any): void {
        this.selectedClientCommitment = clientCommitment;
        this.editForm.patchValue({
            followUpDate: new Date(clientCommitment.followUpDate),
            contactStatus: clientCommitment.contactStatus,
            notes: clientCommitment.notes || ''
        });
        this.displayEditDialog = true;
    }

    saveChanges(): void {
        if (this.editForm.valid && this.selectedClientCommitment) {
            const updatedData = {
                followUpDate: formatDate(this.editForm.value.followUpDate, "yyyy-MM-dd'T'HH:mm:ss", 'en-US'),
                contactStatus: this.editForm.value.contactStatus,
                notes: this.editForm.value.notes
            };
            // this.personalInfoForm.addControl('surName', new FormControl(this.personalInfoForm.get('lastName')?.value));
            // this.personalInfoForm.addControl('locationType', new FormControl(this.personalInfoForm.get('location')?.value));
            this.selectedClientCommitment.surName = this.selectedClientCommitment.lastName;
            this.selectedClientCommitment.locationType = this.selectedClientCommitment.location;
            const updatedClient = { ...this.selectedClientCommitment, ...updatedData };

            this.clientService.updateClient(updatedClient).subscribe({
                next: (response) => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Success',
                        detail: 'Client commitment updated successfully'
                    });
                    this.displayEditDialog = false;
                    this.loadClientCommitments();
                },
                error: (error) => {
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: 'Failed to update client commitment'
                    });
                    console.error('Error updating client commitment:', error);
                }
            });
        }
    }

    updateStatus(clientCommitment: any): void {
        this.clientCommitmentService.updateContactStatus(clientCommitment.id, clientCommitment.contactStatus).subscribe({
            next: (response) => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Status updated successfully'
                });
                this.loadClientCommitments();
            },
            error: (error) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to update status'
                });
                console.error('Error updating status:', error);
            }
        });
    }

    getBadgeSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
        switch (status) {
            case 'ONBOARDED':
                return 'success'; // Green
            case 'CONTACTED':
                return 'info'; // Blue
            case 'IN_TALKS':
                return 'warn'; // Orange
            case 'LEAD':
                return 'danger'; // Red
            default:
                return 'secondary'; // Fallback (Gray)
        }
    }
}
