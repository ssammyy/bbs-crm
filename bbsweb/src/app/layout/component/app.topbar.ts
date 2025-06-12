import { Component } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StyleClassModule } from 'primeng/styleclass';
import { AppConfigurator } from './app.configurator';
import { LayoutService } from '../service/layout.service';
import { AuthService } from '../../pages/auth/auth.service';
import { NotificationComponent } from './notification/notification.component';
import { Chip } from 'primeng/chip';
import { UserGlobalService } from '../../pages/service/user.service';
import { UserService } from '../../pages/users/user.service';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [RouterModule, CommonModule, StyleClassModule, AppConfigurator, NotificationComponent, Chip],
    template: ` <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" (click)="layoutService.onMenuToggle()">
                <i class="pi pi-bars"></i>
            </button>
            <a class="layout-topbar-logo" routerLink="/">
                <svg width="40" height="40" viewBox="0 0 90 90" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M30.0847 86.8932H12.6162V4.85437L36.8781 18.932L38.3338 47.5728V15.0485L12.6162 0L0 15.534V100H30.0847V86.8932Z" fill="#991212"/>
                    <path d="M15.5276 15.1847V9.70874L24.3359 14.5631V19.9029L15.5276 15.1847Z" fill="#991212"/>
                    <path d="M26.3016 20.8738V15.534L35.11 20.3883V25.7282L26.3016 20.8738Z" fill="#991212"/>
                    <path d="M64.6504 69.578V99.8325L30.0847 100V86.8932L50.5571 86.9083V75.4527L38.519 67.8156L30.2978 75.4527V84.2647H16.4981V69.578L38.519 50.4854L64.6504 69.578Z" fill="#212466"/>
                    <path d="M74.0092 79.6116V61.2533H81.3567C82.7067 61.2533 83.8327 61.4535 84.7347 61.8539C85.6367 62.2543 86.3147 62.8101 86.7687 63.5212C87.2227 64.2264 87.4497 65.0391 87.4497 65.9594C87.4497 66.6766 87.3064 67.307 87.0196 67.8508C86.7329 68.3887 86.3386 68.8309 85.8369 69.1775C85.341 69.5181 84.7736 69.7602 84.1344 69.9036V70.0829C84.8333 70.1128 85.4874 70.31 86.0967 70.6745C86.712 71.039 87.2108 71.55 87.5931 72.2074C87.9754 72.8587 88.1666 73.6356 88.1666 74.538C88.1666 75.5121 87.9246 76.3816 87.4408 77.1465C86.9629 77.9055 86.255 78.5061 85.3172 78.9483C84.3793 79.3905 83.2234 79.6116 81.8495 79.6116H74.0092ZM77.889 76.4384H81.052C82.1332 76.4384 82.9218 76.2322 83.4176 75.8199C83.9134 75.4015 84.1613 74.8458 84.1613 74.1525C84.1613 73.6446 84.0388 73.1964 83.7939 72.8079C83.549 72.4195 83.1995 72.1147 82.7455 71.8936C82.2975 71.6725 81.7629 71.562 81.1416 71.562H77.889V76.4384ZM77.889 68.9355H80.7653C81.2969 68.9355 81.7689 68.8429 82.181 68.6576C82.5992 68.4664 82.9277 68.1974 83.1667 67.8508C83.4116 67.5042 83.534 67.0889 83.534 66.6048C83.534 65.9415 83.2981 65.4066 82.8262 65.0003C82.3602 64.5939 81.6972 64.3907 80.837 64.3907H77.889V68.9355Z" fill="#212466"/>
                </svg>
            </a>
        </div>

        <div class="layout-topbar-actions">
            <p-chip [label]="userRole" > </p-chip>
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" (click)="toggleDarkMode()">
                    <i [ngClass]="{ 'pi ': true, 'pi-moon': layoutService.isDarkTheme(), 'pi-sun': !layoutService.isDarkTheme() }"></i>
                </button>
                <div class="relative">
                    <app-configurator />
                </div>
            </div>

            <button class="layout-topbar-menu-button layout-topbar-action" pStyleClass="@next" enterFromClass="hidden" enterActiveClass="animate-scalein" leaveToClass="hidden" leaveActiveClass="animate-fadeout" [hideOnOutsideClick]="true">
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <app-notification />
                    <button type="button" class="layout-topbar-action" (click)="authService.logout()">
                        <i class="pi pi-user"></i>
                        <span>logout</span>
                    </button>
                </div>
            </div>
        </div>
    </div>`
})
export class AppTopbar {
    items!: MenuItem[];
    userRole!: '';

    constructor(
        public layoutService: LayoutService,
        public authService: AuthService,
        public userService: UserGlobalService,
    ) {
        this.getUserDetails()
    }

    getUserDetails(): Promise<void> {
        return new Promise((resolve) => {
            this.userService.getDetails().subscribe({
                next: (response) => {
                    console.log('user role>>>> ', { response });
                    this.userRole = response?.role?.name;
                    resolve();
                },
                error: (error) => {
                    console.error('Error fetching user details:', error);
                    resolve();
                }
            });
        });
    }

    toggleDarkMode() {
        this.layoutService.layoutConfig.update((state) => ({ ...state, darkTheme: !state.darkTheme }));
    }
}
