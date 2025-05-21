import { Component } from '@angular/core';
import { InputText } from 'primeng/inputtext';
import { Divider } from 'primeng/divider';
import { Button } from 'primeng/button';
import { RolesService } from '../roles.service';
import { MessageService } from 'primeng/api';
import { MessagesService } from '../../../layout/service/messages.service';
import { FormsModule } from '@angular/forms';
import { Toast } from 'primeng/toast';

@Component({
    selector: 'app-create-privileges',
    imports: [InputText, Divider, Button, FormsModule, Toast],
    templateUrl: './create-privileges.component.html',
    styleUrl: './create-privileges.component.scss'
})
export class CreatePrivilegesComponent {
    loading = false;
    privilegeName: string = '';

    constructor(
        private rolesService: RolesService,
        private messagesService: MessagesService
    ) {}
    createPrivilege() {
        this.rolesService.createPrivilege({ name: this.privilegeName }).subscribe({
            next: (response: any) => {
                this.messagesService.showSuccess('Privilege created successfully!');
                this.privilegeName = '';
                this.loading = false;
            },
            error: (err) => {
                console.log(err);
                this.messagesService.showError(err);
                this.loading = false;
            }
        });
    }
}
