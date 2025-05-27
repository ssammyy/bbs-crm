import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { UserGlobalService } from '../../pages/service/user.service';
import { firstValueFrom } from 'rxjs';
import { Client } from '../../pages/data/clietDTOs';
import { Permissions } from '../../pages/data/permissions.enum';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: ` <ul class="layout-menu">
        <ng-container *ngFor="let item of model; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul>`
})
export class AppMenu {
    clientId!: number;
    model: MenuItem[] = [];
    userEmail: string = '';
    userRole!: string;

    constructor(private userService: UserGlobalService) {}

    async ngOnInit() {
        const user = await firstValueFrom(this.userService.getDetails());
        this.userEmail = user.email;

        await this.getClient();

        this.model = [
            {
                label: 'Home',
                items: [{ label: 'Dashboard', icon: 'pi pi-fw pi-home', routerLink: ['/app'] }]
            },
            {
                label: 'Pages',
                icon: 'pi pi-fw pi-briefcase',
                routerLink: ['/pages'],
                items: [
                    ...(this.userService.hasPrivilege(Permissions.MANAGE_CLIENTS)
                        ? [
                              {
                                  label: 'Manage Client',
                                  icon: 'pi pi-fw pi-verified',
                                  items: [
                                      ...(this.userService.hasPrivilege(Permissions.ONBOARD_CLIENT)
                                          ? [
                                                {
                                                    label: 'Onboard Client',
                                                    icon: 'pi pi-fw pi-file-plus',
                                                    routerLink: ['pages/onboard-client']
                                                }
                                            ]
                                          : []),
                                      ...(this.userService.hasPrivilege(Permissions.VIEW_CLIENTS)
                                          ? [
                                                {
                                                    label: 'View Clients',
                                                    icon: 'pi pi-fw pi-users',
                                                    routerLink: ['pages/view-clients']
                                                }
                                            ]
                                          : []),
                                      ...(this.userService.hasPrivilege(Permissions.VIEW_LEADS)
                                          ? [
                                                {
                                                    label: 'View Leads',
                                                    icon: 'pi pi-eye',
                                                    routerLink: ['pages/view-leads']
                                                }
                                            ]
                                          : [])
                                  ]
                              }
                          ]
                        : []),

                    ...(this.userService.hasPrivilege('view_profile')
                        ? [
                              {
                                  label: 'My Profile',
                                  icon: 'pi pi-fw pi-verified',
                                  items: [
                                      {
                                          label: 'View Activity',
                                          icon: 'pi pi-fw pi-eye',
                                          routerLink: ['pages/profile', this.clientId]
                                      }
                                  ]
                              }
                          ]
                        : []),

                    ...(this.userService.hasPrivilege(Permissions.MANAGE_USERS)
                        ? [
                              {
                                  label: 'Manage Users',
                                  icon: 'pi pi-fw pi-user',
                                  items: [
                                      ...(this.userService.hasPrivilege(Permissions.CREATE_USER)
                                          ? [
                                                {
                                                    label: 'Create User',
                                                    icon: 'pi pi-fw pi-user-plus',
                                                    routerLink: ['pages/create-user']
                                                }
                                            ]
                                          : []),
                                      ...(this.userService.hasPrivilege(Permissions.CREATE_USER)
                                          ? [
                                                {
                                                    label: 'View Users',
                                                    icon: 'pi pi-fw pi-users',
                                                    routerLink: ['pages/view-users']
                                                }
                                            ]
                                          : [])
                                  ]
                              }
                          ]
                        : []),

                    ...(this.userService.hasPrivilege(Permissions.MANAGE_ROLES_PRIVILEGES)
                        ? [
                              {
                                  label: 'Roles And Privileges',
                                  icon: 'pi pi-fw pi-verified',
                                  items: [
                                      ...(this.userService.hasPrivilege(Permissions.CREATE_ROLE)
                                          ? [
                                                {
                                                    label: 'Create Roles',
                                                    icon: 'pi pi-fw pi-file-plus',
                                                    routerLink: ['pages/create-role']
                                                }
                                            ]
                                          : []),
                                      ...(this.userService.hasPrivilege(Permissions.VIEW_ROLES)
                                          ? [
                                                {
                                                    label: 'View Roles',
                                                    icon: 'pi pi-fw pi-eye',
                                                    routerLink: ['pages/view-roles']
                                                }
                                            ]
                                          : []),

                                      ...(this.userService.hasPrivilege(Permissions.CREATE_PRIVILEGE)
                                          ? [
                                                {
                                                    label: 'Create Privileges',
                                                    icon: 'pi pi-fw pi-verified',
                                                    routerLink: ['pages/create-privilege']
                                                }
                                            ]
                                          : []),
                                      ...(this.userService.hasPrivilege(Permissions.VIEW_PRIVILEGES)
                                          ? [
                                                {
                                                    label: 'View Privileges',
                                                    icon: 'pi pi-fw pi-eye',
                                                    routerLink: ['pages/view-privileges']
                                                }
                                            ]
                                          : [])
                                  ]
                              }
                          ]
                        : []),
                    ...(this.userService.hasPrivilege(Permissions.VIEW_CLIENT_PROFILE) && this.userRole==='CLIENT'?[
                        {
                            label: 'My Profile',
                            icon: 'pi pi-fw pi-user',
                            routerLink: [`pages/profile/${this.clientId}`]
                        }
                    ]:[])
                ]
            }
        ];
    }

    async getClient(): Promise<void> {
        try {
            const result = await firstValueFrom(this.userService.getClientDetails(this.userEmail));
            this.userRole = this.userService.getRole();
            this.clientId = result.id;
        } catch (error) {
            console.error('Error fetching client details', error);
        }
    }


}
