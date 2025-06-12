import { Component, OnInit } from '@angular/core';
import { ButtonDirective } from 'primeng/button';
import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { Divider } from 'primeng/divider';
import { PrimeTemplate } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import { MessagesService } from '../../../layout/service/messages.service';
import { RolesService } from '../roles.service';
import { Privilege, Role } from '../RoleDtos';
import { Dialog } from 'primeng/dialog';
import { Chip } from 'primeng/chip';
import { UserGlobalService } from '../../service/user.service';
import { MultiSelectModule } from 'primeng/multiselect';
import { MessageService } from 'primeng/api';
import { FormsModule } from '@angular/forms';
import { ProgressSpinner } from 'primeng/progressspinner';
import { InputText } from 'primeng/inputtext';
import { SortEvent } from 'primeng/api';

@Component({
    selector: 'app-view-roles',
    imports: [ButtonDirective, DatePipe, Divider, PrimeTemplate, TableModule, Toast, Dialog, NgForOf, Chip, MultiSelectModule, NgIf, FormsModule, ProgressSpinner, InputText],
    templateUrl: './view-roles.component.html',
    styleUrl: './view-roles.component.scss',
    providers: [MessageService]
})
export class ViewRolesComponent implements OnInit {
    roles: Role[] = [];
    filteredRoles: Role[] = [];
    privileges: Privilege[] = [];
    allPrivileges: Privilege[] = [];
    selectedRole: Role | undefined;
    selectedPrivileges: Privilege[] = [];
    loading = false;
    visible = false;
    editMode = false;
    searchQuery = '';

    constructor(
        private messagesService: MessagesService,
        private rolesService: RolesService,
        private userGlobalService: UserGlobalService,
        private messageService: MessageService
    ) {}

    ngOnInit() {
        this.getRoles();
        this.getAllPrivileges();
    }

    getRoles() {
        this.loading = true;
        this.rolesService.getRoles().subscribe({
            next: (data) => {
                this.roles = data;
                this.filteredRoles = data;
                this.loading = false;
            },
            error: (error) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load roles' });
                this.loading = false;
            }
        });
    }

    onSearch() {
        if (!this.searchQuery.trim()) {
            this.filteredRoles = this.roles;
            return;
        }

        const query = this.searchQuery.toLowerCase().trim();
        this.filteredRoles = this.roles.filter(role => 
            role.name.toLowerCase().includes(query) ||
            (role.createdBy && role.createdBy.toLowerCase().includes(query)) ||
            (role.updatedBy && role.updatedBy.toLowerCase().includes(query))
        );
    }

    onSort(event: SortEvent) {
        const { field, order } = event;
        if (!field || !order) return;

        this.filteredRoles.sort((a, b) => {
            let valueA = a[field as keyof Role];
            let valueB = b[field as keyof Role];

            // Handle date fields
            if (field === 'createdOn' || field === 'updatedOn') {
                valueA = new Date(valueA as string).getTime();
                valueB = new Date(valueB as string).getTime();
            }

            // Handle string fields
            if (typeof valueA === 'string') {
                valueA = valueA.toLowerCase();
                valueB = valueB?.toString().toLowerCase() || '';
            }

            if (valueA < valueB) {
                return order === 1 ? -1 : 1;
            }
            if (valueA > valueB) {
                return order === 1 ? 1 : -1;
            }
            return 0;
        });
    }

    getAllPrivileges() {
        this.rolesService.getAllPrivileges().subscribe({
            next: (data) => {
                this.allPrivileges = data;
            },
            error: (error) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load privileges' });
            }
        });
    }

    viewRole(role: Role) {
        this.selectedRole = role;
        this.privileges = role.privileges || [];
        this.selectedPrivileges = this.privileges.map((p) => this.allPrivileges.find((ap) => ap.id === p.id) || p);
        this.editMode = false;
        this.visible = true;
    }

    toggleEditMode() {
        this.editMode = !this.editMode;
        if (this.editMode) {
            this.selectedPrivileges = this.privileges.map((p) => this.allPrivileges.find((ap) => ap.id === p.id) || p);
        } else {
            this.selectedPrivileges = this.privileges.map((p) => this.allPrivileges.find((ap) => ap.id === p.id) || p);
        }
    }

    savePrivileges() {
        if (this.selectedRole) {
            const updatedRole = {
                ...this.selectedRole,
                privilegeIds: this.selectedPrivileges.map((p) => p.id)
            };
            this.rolesService.updateRole(this.selectedRole.id, updatedRole).subscribe({
                next: (data) => {
                    this.selectedRole = data;
                    this.privileges = data.privileges || [];
                    this.selectedPrivileges = this.privileges.map((p) => this.allPrivileges.find((ap) => ap.id === p.id) || p);
                    this.editMode = false;
                    this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Privileges updated' });
                    this.getRoles();
                },
                error: (error) => {
                    this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to update privileges' });
                }
            });
        }
    }

    deleteRole(id: any) {
        this.rolesService.deleteRole(id).subscribe({
            next: () => {
                this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Role deleted' });
                this.getRoles();
            },
            error: (error) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete role' });
            }
        });
    }

    removePrivilege(privilege: Privilege) {
        if (this.selectedRole) {
            const updatedPrivileges = this.privileges.filter((p) => p.id !== privilege.id);
            const updatedRole = {
                ...this.selectedRole,
                privilegeIds: updatedPrivileges.map((p) => p.id)
            };

            this.rolesService.updateRole(this.selectedRole.id, updatedRole).subscribe({
                next: (data) => {
                    this.selectedRole = data;
                    this.privileges = data.privileges || [];
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Success',
                        detail: `Privilege ${privilege.name} removed successfully`
                    });
                    this.getRoles();
                },
                error: (error) => {
                    this.messageService.add({
                        severity: 'error',
                        summary: 'Error',
                        detail: 'Failed to remove privilege'
                    });
                }
            });
        }
    }
}
