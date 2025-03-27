import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: `<ul class="layout-menu">
        <ng-container *ngFor="let item of model; let i = index">
            <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
            <li *ngIf="item.separator" class="menu-separator"></li>
        </ng-container>
    </ul> `
})
export class AppMenu {
    model: MenuItem[] = [];

    ngOnInit() {
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
                    {
                        label: 'Manage Users',
                        icon: 'pi pi-fw pi-user',
                        items: [
                            {
                                label: 'Create user',
                                icon: 'pi pi-fw pi-user-plus',
                                routerLink: ['pages/create-user'],
                            },
                            {
                                label: 'View users',
                                icon: 'pi pi-fw pi-users',
                                routerLink: ['pages/create-user'],
                            }
                        ]
                    },
                    {
                        label: 'Roles And Privileges',
                        icon: 'pi pi-fw pi-verified',
                        items: [
                            {
                                label: 'Create Roles',
                                icon: 'pi pi-fw pi-file-plus',
                                routerLink: ['pages/create-role'],
                            },
                            {
                                label: 'View Roles',
                                icon: 'pi pi-fw pi-eye',
                                routerLink: ['pages/view-roles'],
                            },
                            {
                                label: 'Create Privileges',
                                icon: 'pi pi-fw pi-verified\n',
                                routerLink: ['pages/create-privilege'],
                            },
                            {
                                label: 'View Privileges',
                                icon: 'pi pi-fw pi-eye',
                                routerLink: ['pages/view-privileges'],
                            }
                        ]
                    }

                ]
            },
        ];
    }
}
