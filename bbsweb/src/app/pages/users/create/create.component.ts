import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../user.service';
import { InputText } from 'primeng/inputtext';
import { NgForOf, NgIf } from '@angular/common';
import { DropdownModule } from 'primeng/dropdown';
import { Button, ButtonDirective } from 'primeng/button';
import { Role } from '../../Roles/RoleDtos';
import { RolesService } from '../../Roles/roles.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { Toast } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { ChipModule } from 'primeng/chip';
import { Message } from 'primeng/message';
import { InputNumber } from 'primeng/inputnumber';

@Component({
    selector: 'app-create',
    imports: [ReactiveFormsModule, InputText, NgIf, DropdownModule, ButtonDirective, Button, Toast, FileUploadModule, ChipModule, Message, NgForOf, InputNumber],
    templateUrl: './create.component.html',
    styleUrl: './create.component.scss'
})
export class CreateComponent implements OnInit {
    userForm: FormGroup;
    role!: Role;
    selectedFile: File | null = null;

    genders = [
        { label: 'Male', value: 'Male' },
        { label: 'Female', value: 'Female' },
        { label: 'Other', value: 'Other' }
    ];

    paymentMethods = [
        { label: 'Bank', value: 'Bank' },
        { label: 'Mpesa', value: 'Mpesa' }
    ];

    roles: Role[] = [];
    loading = false;

    // Track selected KYC documents
    selectedDocuments: { [key: string]: { file: File; fileUrl?: string } } = {};

    // Required KYC documents for Agent
    requiredKycDocuments = [
        { label: 'Signed Agreement', fileType: 'SIGNED_AGREEMENT' },
        { label: 'ID', fileType: 'ID' }
    ];

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private rolesService: RolesService,
        private messagesService: MessagesService
    ) {
        this.userForm = this.fb.group({
            username: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            phoneNumber: [''],
            gender: ['', Validators.required],
            role: ['', Validators.required],
            bio: [''],
            paymentMethod: [''],
            bankName: [''],
            bankAccountNumber: [''],
            bankBranch: [''],
            bankAccountHolderName: [''],
            nextOfKinIdNumber: [''],
            nextOfKinName: [''],
            nextOfKinPhoneNumber: [''],
            percentage: ['']
        });
    }

    ngOnInit() {
        this.getRoles();
        // Watch for role changes to enable/disable paymentMethod and next of kin validation
        this.userForm.get('role')?.valueChanges.subscribe((roleId) => {
            const selectedRole = this.roles.find((role) => role.id === roleId);
            if (selectedRole?.name === 'Agent') {
                this.userForm.get('paymentMethod')?.setValidators([Validators.required]);
                this.userForm.get('nextOfKinIdNumber')?.setValidators([Validators.required]);
                this.userForm.get('nextOfKinName')?.setValidators([Validators.required]);
                this.userForm.get('nextOfKinPhoneNumber')?.setValidators([Validators.required]);
            } else {
                this.userForm.get('paymentMethod')?.clearValidators();
                this.userForm.get('nextOfKinIdNumber')?.clearValidators();
                this.userForm.get('nextOfKinName')?.clearValidators();
                this.userForm.get('nextOfKinPhoneNumber')?.clearValidators();
            }
            this.userForm.get('paymentMethod')?.updateValueAndValidity();
            this.userForm.get('nextOfKinIdNumber')?.updateValueAndValidity();
            this.userForm.get('nextOfKinName')?.updateValueAndValidity();
            this.userForm.get('nextOfKinPhoneNumber')?.updateValueAndValidity();
        });

        // Watch for paymentMethod changes to enable/disable bank details validation
        this.userForm.get('paymentMethod')?.valueChanges.subscribe((method) => {
            if (method === 'Bank') {
                this.userForm.get('bankName')?.setValidators([Validators.required]);
                this.userForm.get('bankAccountNumber')?.setValidators([Validators.required]);
                this.userForm.get('bankBranch')?.setValidators([Validators.required]);
                this.userForm.get('bankAccountHolderName')?.setValidators([Validators.required]);
            } else {
                this.userForm.get('bankName')?.clearValidators();
                this.userForm.get('bankAccountNumber')?.clearValidators();
                this.userForm.get('bankBranch')?.clearValidators();
                this.userForm.get('bankAccountHolderName')?.clearValidators();
            }
            this.userForm.get('bankName')?.updateValueAndValidity();
            this.userForm.get('bankAccountNumber')?.updateValueAndValidity();
            this.userForm.get('bankBranch')?.updateValueAndValidity();
            this.userForm.get('bankAccountHolderName')?.updateValueAndValidity();
        });
    }

    getRoles() {
        this.rolesService.getRoles().subscribe({
            next: (res) => {
                this.roles = res;
            },
            error: (err) => {
                console.log('system error', err);
            }
        });
    }

    isAgentSelected(): boolean {
        const selectedRoleId = this.userForm.get('role')?.value;
        const selectedRole = this.roles.find((role) => role.id === selectedRoleId);
        return selectedRole?.name === 'AGENT';
    }

    isDocumentSelected(fileType: string): boolean {
        return !!this.selectedDocuments[fileType];
    }

    onSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.selectedDocuments[fileType] = { file };
        this.messagesService.showInfo(`${fileType.replace('_', ' ')} selected. It will be uploaded after user registration.`);
    }

    removeDocument(fileType: string): void {
        delete this.selectedDocuments[fileType];
        this.messagesService.showInfo(`${fileType.replace('_', ' ')} removed`);
    }

    get missingDocuments(): string[] {
        return this.requiredKycDocuments.filter((doc) => !this.selectedDocuments[doc.fileType]).map((doc) => doc.label);
    }

    async uploadAllDocuments(userId: number): Promise<boolean> {
        for (const [fileType, doc] of Object.entries(this.selectedDocuments)) {
            try {
                const response: any = await this.userService.uploadKycDocument(userId, fileType, doc.file).toPromise();
                this.selectedDocuments[fileType].fileUrl = response.fileUrl;
                this.messagesService.showSuccess(`${fileType.replace('_', ' ')} uploaded successfully`);
            } catch (error) {
                this.messagesService.showError(`Failed to upload ${fileType.replace('_', ' ')}`);
                return false;
            }
        }
        return true;
    }

    async onSubmit() {
        this.loading = true;
        if (this.userForm.invalid) {
            this.loading = false;
            this.messagesService.showError('Please fill in all required fields');
            return;
        }

        const userData = {
            username: this.userForm.value.username,
            email: this.userForm.value.email,
            phonenumber: this.userForm.value.phoneNumber,
            gender: this.userForm.value.gender,
            roleId: this.userForm.value.role,
            paymentMethod: this.userForm.value.paymentMethod,
            bankName: this.userForm.value.bankName,
            bankAccountNumber: this.userForm.value.bankAccountNumber,
            bankBranch: this.userForm.value.bankBranch,
            bankAccountHolderName: this.userForm.value.bankAccountHolderName,
            nextOfKinIdNumber: this.userForm.value.nextOfKinIdNumber,
            nextOfKinName: this.userForm.value.nextOfKinName,
            nextOfKinPhoneNumber: this.userForm.value.nextOfKinPhoneNumber
        };

        // Step 1: Register the user
        this.userService.registerUser(userData).subscribe({
            next: async (res) => {
                const userId = res.id; // Assuming the response includes the user ID
                if (!userId) {
                    this.messagesService.showError('Failed to retrieve user ID for document upload');
                    this.loading = false;
                    return;
                }

                // Step 2: If Agent, check for missing KYC documents and upload them
                if (this.isAgentSelected()) {
                    if (this.missingDocuments.length > 0) {
                        this.messagesService.showError(`Please select all required KYC documents: ${this.missingDocuments.join(', ')}`);
                        this.loading = false;
                        return;
                    }

                    const uploadSuccess = await this.uploadAllDocuments(userId);
                    if (!uploadSuccess) {
                        this.messagesService.showWarn('User registered, but some KYC documents failed to upload');
                        this.loading = false;
                        return;
                    }
                }

                this.loading = false;
                this.userForm.reset();
                this.selectedDocuments = {};
                const successMessage = res?.message || 'Successfully registered';
                this.messagesService.showSuccess(successMessage);
            },
            error: (err) => {
                this.loading = false;
                const errorMessage = err?.error?.message || 'Failed to register user';
                this.messagesService.showError(errorMessage);
            }
        });
    }

    protected readonly name = name;
}
