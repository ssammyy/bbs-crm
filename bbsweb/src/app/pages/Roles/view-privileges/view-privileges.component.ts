import { Component, OnInit } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { Divider } from 'primeng/divider';
import { InputText } from 'primeng/inputtext';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Toast } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { DatePipe, NgIf } from '@angular/common';
import { RolesService } from '../roles.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { Dialog } from 'primeng/dialog';
import { ConfirmationService } from 'primeng/api';
import { Router, RouterLink } from '@angular/router';
import { Privilege } from '../RoleDtos';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'app-view-privileges',
    imports: [Button, Divider, InputText, ReactiveFormsModule, FormsModule, Toast, TableModule, DatePipe, ButtonDirective, Dialog, ConfirmPopupModule, NgIf, RouterLink],
    templateUrl: './view-privileges.component.html',
    styleUrl: './view-privileges.component.scss',
    providers: [ConfirmationService]
})
export class ViewPrivilegesComponent implements OnInit {
    privileges: Privilege[] = [];
    loading = false;
    visible = false;
    selectedPrivilege: Privilege | null = null;
    editMode = false;
    privilegeName = '';

    constructor(
        private rolesService: RolesService,
        private messagesService: MessagesService,
        private confirmationService: ConfirmationService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.getPrivileges();
    }

    getPrivileges(): void {
        this.loading = true;
        this.rolesService.getPrivileges().subscribe({
            next: (data) => {
                this.privileges = data;
                this.loading = false;
            },
            error: (error: any) => {
                console.log(error);
                this.messagesService.showError('Failed to load privileges');
                this.privileges = [];
                this.loading = false;
            }
        });
    }

    showCreateDialog() {
        this.router.navigate(['pages/create-privilege']);
    }

    showEditDialog(privilege: Privilege) {
        this.editMode = true;
        this.selectedPrivilege = privilege;
        this.privilegeName = privilege.name;
        this.visible = true;
    }

    savePrivilege() {
        if (!this.privilegeName.trim()) {
            this.messagesService.showError('Privilege name cannot be empty');
            return;
        }

        this.loading = true;
        if (this.editMode && this.selectedPrivilege) {
            // Update existing privilege
            this.rolesService.updatePrivilege(this.selectedPrivilege.id, { name: this.privilegeName }).subscribe({
                next: () => {
                    this.messagesService.showSuccess('Privilege updated successfully');
                    this.visible = false;
                    this.getPrivileges();
                },
                error: (error: any) => {
                    this.messagesService.showError('Failed to update privilege');
                    this.loading = false;
                }
            });
        } else {
            // Create new privilege
            this.rolesService.createPrivilege({ name: this.privilegeName }).subscribe({
                next: () => {
                    this.messagesService.showSuccess('Privilege created successfully');
                    this.visible = false;
                    this.getPrivileges();
                },
                error: (error: any) => {
                    this.messagesService.showError('Failed to create privilege');
                    this.loading = false;
                }
            });
        }
    }

    confirmDelete(event: Event, privilege: Privilege) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: `Are you sure you want to delete the privilege "${privilege.name}"?`,
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.deletePrivilege(privilege.id);
            }
        });
    }

    deletePrivilege(id: number) {
        this.loading = true;
        this.rolesService.deletePrivilege(id).subscribe({
            next: () => {
                this.messagesService.showSuccess('Privilege deleted successfully');
                this.getPrivileges();
            },
            error: (error: HttpErrorResponse) => {
                this.loading = false;
                if (error.error?.message?.includes('foreign key constraint')) {
                    this.messagesService.showError('Cannot delete this privilege because it is assigned to one or more roles. Please remove it from all roles first.');
                } else {
                    this.messagesService.showError('Failed to delete privilege');
                }
            }
        });
    }
}
