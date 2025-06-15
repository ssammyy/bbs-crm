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
import { LogoSvgComponent } from '../../pages/auth/logo-svg.component';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [RouterModule, CommonModule, StyleClassModule, AppConfigurator, NotificationComponent, Chip, LogoSvgComponent],
    template: ` <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" (click)="layoutService.onMenuToggle()">
                <i class="pi pi-bars"></i>
            </button>
            <app-logo-svg width="60" height="28"/>
        </div>

        <div class="layout-topbar-actions">
            <p-chip class="capitalize" [label]="userRole.replaceAll('_', ' ').toLowerCase()" > </p-chip>
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
        public userService: UserGlobalService
    ) {
        this.getUserDetails();
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
