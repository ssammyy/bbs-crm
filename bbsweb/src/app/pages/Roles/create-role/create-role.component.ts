import { Component, OnInit } from '@angular/core';
import { Button } from 'primeng/button';
import { Divider } from 'primeng/divider';
import { InputText } from 'primeng/inputtext';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Toast } from 'primeng/toast';
import { RolesService } from '../roles.service';
import { MultiSelect } from 'primeng/multiselect';
import { MessagesService } from '../../../layout/service/messages.service';

@Component({
    selector: 'app-create-role',
    imports: [Button, Divider, InputText, ReactiveFormsModule, Toast, MultiSelect, FormsModule],
    templateUrl: './create-role.component.html',
    styleUrl: './create-role.component.scss'
})
export class CreateRoleComponent implements OnInit {
    roleName: string = '';
    privileges: any[] = [];
    loading = false;
    selectedPrivileges: any[]= [];
    constructor(private rolesService: RolesService, private messagesService: MessagesService) { }

    ngOnInit() {
        this.getPrivileges();
    }

    private getPrivileges() {
        this.rolesService.getPrivileges().subscribe({
            next: (data) => {
                this.privileges = data;
            },
            error: (err) => {
                console.log('system error ', err);
            }
        });
    }

    createRole(): void {
        this.loading = true;
        const privilegesIds = this.privileges.map(item => item.id);
        const rn = this.roleName

        this.rolesService.createRole({name:rn, privilegeIds: privilegesIds  })
            .subscribe({
                next: (data) => {
                    if (data) {
                        this.loading = false;
                        this.selectedPrivileges = []
                        this.roleName = ''
                        this.messagesService.showSuccess('Role Successfully created');
                    }
                },
                error: (err) => {
                    this.loading = false;
                    this.messagesService.showError('Error creating role');
                    console.log('system error ', err);
                }
            })


    }
}
