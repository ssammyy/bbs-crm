import { Component, inject, OnInit } from '@angular/core';
import { Button, ButtonDirective } from 'primeng/button';
import { DatePipe } from '@angular/common';
import { Divider } from 'primeng/divider';
import { ConfirmationService, PrimeTemplate } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Toast } from 'primeng/toast';
import { User } from '../../service/user.service';
import { UserService } from '../user.service';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DropdownModule } from 'primeng/dropdown';
import { Role } from '../../Roles/RoleDtos';
import { RolesService } from '../../Roles/roles.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { ConfirmPopupModule } from 'primeng/confirmpopup';

@Component({
    selector: 'app-view-users',
    imports: [ButtonDirective, DatePipe, Divider, PrimeTemplate, TableModule, Toast, Dialog, InputText, ReactiveFormsModule, DropdownModule, Button, ConfirmPopupModule],
    templateUrl: './view-users.component.html',
    styleUrl: './view-users.component.scss',
    providers:[ConfirmationService]
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
                this.getUsers()
            },
            error: err => {
                console.log('system error', err);
            }
            }
        )
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
        this.loading = true;
        this.userService.getUsers().subscribe({
            next: (data) => {
                this.loading = false;
                this.users = data;
            },
            error: (error) => {
                this.loading = false;
                console.log('system error ', error);
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
                this.deleteUser(user)
            },
            reject: () => {
            }
        });
    }
}
