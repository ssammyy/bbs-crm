import { Component, inject, OnInit } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { DatePipe, NgIf } from '@angular/common';
import { Divider } from 'primeng/divider';
import { ConfirmationService, PrimeTemplate } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import { User } from '../../service/user.service';
import { UserService } from '../user.service';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DropdownModule } from 'primeng/dropdown';
import { Role } from '../../Roles/RoleDtos';
import { RolesService } from '../../Roles/roles.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { ProgressSpinner } from 'primeng/progressspinner';
import { catchError, shareReplay } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
    selector: 'app-view-users',
    imports: [ButtonDirective, DatePipe, Divider, PrimeTemplate, TableModule, Toast, Dialog, InputText, ReactiveFormsModule, DropdownModule, Button, ConfirmPopupModule, NgIf, ProgressSpinner, FormsModule],
    templateUrl: './view-users.component.html',
    styleUrl: './view-users.component.scss',
    providers: [ConfirmationService]
})
export class ViewUsersComponent implements OnInit {
    users: User[] = [];
    user!: User;
    loading = false;
    visible = false;
    editUserForm!: FormGroup;
    roles: Role[] | undefined;

    constructor(
        private userService: UserService,
        private roleService: RolesService,
        private messagesService: MessagesService,
        private confirmationService: ConfirmationService,
        private fb: FormBuilder
    ) {}

    deleteUser(user: User) {
        this.userService.deleteUser(user.id).subscribe({
            next: () => {
                this.messagesService.showSuccess('User deleted');
                this.getUsers();
            },
            error: (err) => {
                console.log('system error', err);
            }
        });
    }

    editUser(user: User) {
        this.user = user;
        this.initForm();
        this.visible = true;
    }

    ngOnInit() {
        this.getUsers();
        this.initForm();
        this.getRoles();
        // Uncomment the line below to test caching
        // this.testCaching();
    }

    initForm() {
        this.editUserForm = this.fb.group({
            username: [this.user?.username || '', Validators.required],
            email: [this.user?.email || '', [Validators.required, Validators.email]],
            phonenumber: [this.user?.phonenumber || '', Validators.required],
            role: [this.user?.role || '', Validators.required]
        });
    }

    getUsers(): void {
        this.loadingUsers = true;
        this.userService.getUsers().subscribe({
            next: (data) => {
                this.loadingUsers = false;
                // Filter out users with CLIENT role
                this.users = data.filter(user => user.role?.name !== 'CLIENT');
                this.filteredUsers = this.users;
            },
            error: (error) => {
                this.loadingUsers = false;
                this.messagesService.showError('Failed to load users. Please try again later.');
                console.error('Error loading users:', error);
            }
        });
    }

    getRoles(): void {
        this.roleService.getRoles().subscribe({
            next: (data) => {
                this.roles = data;
            }
        });
    }

    updateUser() {
        this.loading = true;
        this.userService.updateUser(this.user.id, this.editUserForm.value).subscribe({
            next: () => {
                this.loading = false;
                this.messagesService.showSuccess('User updated successfully.');
                this.visible = false;
                this.getUsers();
            },
            error: (error) => {
                this.loading = false;
                console.log('system error ', error);
            }
        });
    }
    searchQuery: string = '';
    filteredUsers: any[] = [];

    onSearch() {
        if (!this.searchQuery) {
            this.filteredUsers = this.users;
            return;
        }

        const query = this.searchQuery.toLowerCase();
        this.filteredUsers = this.users.filter((user) => user.username.toLowerCase().includes(query) || user.email.toLowerCase().includes(query) || user.phonenumber.toLowerCase().includes(query) || user.role?.name.toLowerCase().includes(query));
    }

    onSort(event: any) {
        const { field, order } = event;
        this.filteredUsers.sort((a, b) => {
            let valueA = field.includes('.') ? field.split('.').reduce((obj: any, key: any) => obj?.[key], a) : a[field];
            let valueB = field.includes('.') ? field.split('.').reduce((obj: any, key: any) => obj?.[key], b) : b[field];

            if (valueA === null || valueA === undefined) valueA = '';
            if (valueB === null || valueB === undefined) valueB = '';

            return order === 1 ? valueA.localeCompare(valueB) : valueB.localeCompare(valueA);
        });
    }

    confirm2(event: Event, user: User) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: 'Do you want to delete this record?',
            icon: 'pi pi-info-circle',
            rejectButtonProps: {
                label: 'Cancel',
                severity: 'secondary',
                outlined: true
            },
            acceptButtonProps: {
                label: 'Delete',
                severity: 'danger'
            },
            accept: () => {
                this.deleteUser(user);
            },
            reject: () => {}
        });
    }

    // Add a method to test caching
    loadingUsers = false;
    testCaching() {
        console.log('Testing cache - First call');
        this.getUsers();

        // Wait for 2 seconds and make another call
        setTimeout(() => {
            console.log('Testing cache - Second call (should use cache)');
            this.getUsers();
        }, 2000);
    }
}
