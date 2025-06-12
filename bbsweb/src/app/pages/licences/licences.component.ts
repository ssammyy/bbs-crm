import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { FileUploadModule } from 'primeng/fileupload';
import { ChipModule } from 'primeng/chip';
import { DividerModule } from 'primeng/divider';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { DropdownModule } from 'primeng/dropdown';
import { UploadService, LicenseFile } from '../service/upload.service';
import { MessagesService } from '../../layout/service/messages.service';
import { ClientDetailsService } from '../client/client.service';
import { UserService } from '../users/user.service';
import { Files } from '../data/clietDTOs';
import { Tooltip } from 'primeng/tooltip';
import { NgxSpinnerComponent } from 'ngx-spinner';
import { Toast } from 'primeng/toast';

interface LicenseDocument {
    type: string;
    label: string;
    description: string;
    required: boolean;
    role: string;
    allowMultiple: boolean;
}

interface User {
    id: number;
    username: string;
    email: string;
    role: {
        name: string;
    };
}

interface UserLicense {
    user: User;
    files: LicenseFile[];
}

// Extend the Files interface to include userId
interface ExtendedFiles extends Files {
    userId?: number;
}

@Component({
    selector: 'app-licences',
    standalone: true,
    imports: [CommonModule, FormsModule, ButtonModule, CardModule, FileUploadModule, ChipModule, DividerModule, MessagesModule, MessageModule, DropdownModule, Tooltip, NgxSpinnerComponent, Toast],
    templateUrl: './licences.component.html',
    styleUrl: './licences.component.scss'
})
export class LicencesComponent implements OnInit {
    licenseDocuments: LicenseDocument[] = [
        {
            type: 'STRUCTURAL_ENGINEER_LICENSE',
            label: 'Structural Engineer License',
            description: 'Valid practicing license for the Structural Engineer',
            required: true,
            role: 'STRUCTURAL_ENGINEER',
            allowMultiple: true
        },
        {
            type: 'ARCHITECT_LICENSE',
            label: 'Architect License',
            description: 'Valid practicing license for the Architect',
            required: true,
            role: 'ARCHITECT',
            allowMultiple: true
        },
        {
            type: 'NCA_CONTRACTOR_LICENSE',
            label: 'NCA Contractor License',
            description: 'Valid NCA License for the Contractor',
            required: true,
            role: 'CONTRACTOR',
            allowMultiple: false
        }
    ];

    licenseFiles: LicenseFile[] = [];
    uploadedFile: any;
    selectedDocument: LicenseFile | null = null;
    loading: boolean = false;
    clientId: number = 0;
    users: User[] = [];
    selectedUsers: { [key: string]: User | null } = {};
    userLicenses: { [key: string]: UserLicense[] } = {};

    constructor(
        private uploadService: UploadService,
        private messagesService: MessagesService,
        private clientService: ClientDetailsService,
        private userService: UserService
    ) {}

    ngOnInit() {
        this.clientId = 2; // Replace with actual client ID
        this.loadUsers();
        this.loadLicenseFiles();
    }

    loadUsers() {
        this.userService.getUsers().subscribe({
            next: (users: User[]) => {
                this.users = users;
                // Initialize selected users for each license type
                this.licenseDocuments.forEach((doc) => {
                    this.selectedUsers[doc.type] = null;
                    this.userLicenses[doc.type] = [];
                });
            },
            error: (error) => {
                console.error('Error loading users:', error);
                this.messagesService.showError('Error loading users');
            }
        });
    }

    loadLicenseFiles() {
        this.uploadService.getLicenseFiles().subscribe({
            next: (files: LicenseFile[]) => {
                this.licenseFiles = files;
                this.organizeUserLicenses();
            },
            error: (error) => {
                console.error('Error loading license files:', error);
                this.messagesService.showError('Error loading license files');
            }
        });
    }

    organizeUserLicenses() {
        // Reset user licenses
        this.licenseDocuments.forEach((doc) => {
            this.userLicenses[doc.type] = [];
        });

        // Group files by user and type
        this.licenseFiles.forEach((file) => {
            if (file.userId) {
                const user = this.users.find((u) => u.id === file.userId);
                if (user) {
                    const docType = file.fileType;
                    let userLicense = this.userLicenses[docType].find((ul) => ul.user.id === user.id);

                    if (!userLicense) {
                        userLicense = { user, files: [] };
                        this.userLicenses[docType].push(userLicense);
                    }
                    userLicense.files.push(file);
                }
            }
        });
    }

    onFileSelect(event: any, fileType: string): void {
        const file: File = event.files[0];
        this.uploadedFile = file;
        this.selectedDocument = {
            id: 0,
            fileType,
            fileName: file.name,
            fileUrl: '',
            version: 1,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };
    }

    getUploadButtonLabel(fileType: string, userId?: number): string {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            return this.isDocumentUploaded(fileType) ? 'Update NCA License' : 'Upload NCA License';
        }
        return this.isDocumentUploaded(fileType, userId) ? 'Update License' : 'Upload License';
    }

    onUpdateClick(fileType: string, userId?: number) {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            const ncaDoc = this.licenseFiles.find(doc => doc.fileType === fileType);
            if (ncaDoc) {
                this.selectedDocument = ncaDoc;
            }
        } else {
            const doc = this.licenseFiles.find(doc => doc.fileType === fileType && doc.userId === userId);
            if (doc) {
                this.selectedDocument = doc;
            }
        }
    }

    onUpload(fileType: string) {
        if (!this.uploadedFile) {
            return;
        }

        this.loading = true;
        const selectedUser = this.selectedUsers[fileType];

        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            // Upload NCA license without user or client ID
            this.uploadService.upload( fileType, this.uploadedFile)
                .subscribe({
                    next: (response) => {
                        this.loading = false;
                        this.uploadedFile = null;
                        this.selectedDocument = null;
                        this.loadLicenseFiles();
                        this.messagesService.showSuccess('NCA License uploaded successfully');
                    },
                    error: (error) => {
                        this.loading = false;
                        this.messagesService.showError('Failed to upload NCA License');
                    }
                });
        } else {
            // Handle other licenses with user and client IDs
            if (this.licenseDocuments.find(doc => doc.type === fileType)?.allowMultiple && !selectedUser) {
                this.messagesService.showWarn('Please select a professional first');
                this.loading = false;
                return;
            }

            this.uploadService.upload( fileType, this.uploadedFile, undefined, undefined, selectedUser?.id)
                .subscribe({
                    next: (response) => {
                        this.loading = false;
                        this.uploadedFile = null;
                        this.selectedDocument = null;
                        this.loadLicenseFiles();
                        this.messagesService.showSuccess('License uploaded successfully');
                    },
                    error: (error) => {
                        this.loading = false;
                        this.messagesService.showError('Failed to upload license');
                    }
                });
        }
    }

    isDocumentUploaded(fileType: string, userId?: number): boolean {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            return this.licenseFiles.some(doc => doc.fileType === fileType);
        }
        return this.licenseFiles.some(doc => doc.fileType === fileType && doc.userId === userId);
    }

    viewDocument(fileType: string, userId?: number) {
        if (fileType === 'NCA_CONTRACTOR_LICENSE') {
            const ncaDoc = this.licenseFiles.find(doc => doc.fileType === fileType);
            if (ncaDoc) {
                window.open(ncaDoc.fileUrl, '_blank');
            }
        } else {
            const doc = this.licenseFiles.find(doc => doc.fileType === fileType && doc.userId === userId);
            if (doc) {
                window.open(doc.fileUrl, '_blank');
            }
        }
    }

    getFilteredUsers(role: string): User[] {
        return this.users.filter((user) => user.role.name === role);
    }

    onUserSelect(fileType: string, user: User) {
        this.selectedUsers[fileType] = user;
    }

    getUserLicenses(fileType: string): UserLicense[] {
        return this.userLicenses[fileType] || [];
    }

    getNcaLicense(): LicenseFile | undefined {
        return this.licenseFiles.find(doc => doc.fileType === 'NCA_CONTRACTOR_LICENSE');
    }
}
