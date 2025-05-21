import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientCommitmentService } from './client-commitment.service';
import { DatePipe, formatDate, NgIf, NgSwitch, NgSwitchCase } from '@angular/common';
import { Calendar } from 'primeng/calendar';
import { Button } from 'primeng/button';
import { Toast } from 'primeng/toast';
import { Steps } from 'primeng/steps';
import { MenuItem, MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { InputText } from 'primeng/inputtext';
import { InputTextarea } from 'primeng/inputtextarea';
import { countryCodes } from '../../data/country-codes';

@Component({
    selector: 'app-client-commitment',
    imports: [ReactiveFormsModule, NgIf, Calendar, Button, NgSwitchCase, Toast, NgSwitch, Steps, DropdownModule, DatePipe, InputText, InputTextarea],
    templateUrl: './client-commitment.component.html',
    styleUrl: './client-commitment.component.scss'
})
export class ClientCommitmentComponent {
    clientCommitmentForm!: FormGroup;
    steps: MenuItem[] = [];
    activeIndex: number = 0;
    minDate: Date = new Date();
    countryCodes = countryCodes;

    constructor(
        private fb: FormBuilder,
        private clientCommitmentService: ClientCommitmentService,
        private messageService: MessageService
    ) {}

    ngOnInit(): void {
        // Initialize steps for p-steps
        this.steps = [{ label: 'Personal Information' }, { label: 'Commitment Details' }, { label: 'Review' }];

        // Initialize the form with all fields
        this.clientCommitmentForm = this.fb.group({
            name: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            countryCode: ['+254', Validators.required],
            phoneNumber: [
                '',
                [
                    Validators.required,
                    Validators.pattern(/^\d{9}$/) // Assuming Kenyan phone numbers (9 digits after country code)
                ]
            ],
            followUpDate: ['', Validators.required],
            notes: ['']
        });
    }

    nextStep(): void {
        if (this.activeIndex < this.steps.length - 1) {
            this.activeIndex++;
        }
    }

    prevStep(): void {
        if (this.activeIndex > 0) {
            this.activeIndex--;
        }
    }

    submitForm(): void {
        if (this.clientCommitmentForm.valid) {
            const formValue = this.clientCommitmentForm.value;
            const clientCommitmentData = {
                name: formValue.name,
                email: formValue.email,
                phone: `${formValue.countryCode}${formValue.phoneNumber}`,
                notes: formValue.notes,
                followUpDate: formatDate(formValue.followUpDate, "yyyy-MM-dd'T'HH:mm:ss", 'en-US')
            };

            this.clientCommitmentService.onboardClientCommitment(clientCommitmentData).subscribe({
                next: (response) => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Success',
                        detail: 'Client commitment onboarded successfully'
                    });
                    this.clientCommitmentForm.reset();
                    this.activeIndex = 0; // Reset to first step
                },
                error: (error) => {
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: 'Failed to onboard client commitment'
                    });
                    console.error('Error onboarding client commitment:', error);
                }
            });
        }
    }
}
