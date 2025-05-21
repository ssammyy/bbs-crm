import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../service/notification.service';
import { Notification } from '../../../shared/interfaces/notification.interface';
import { interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { OverlayPanelModule, OverlayPanel } from 'primeng/overlaypanel';
import { BadgeModule } from 'primeng/badge';
import { RippleModule } from 'primeng/ripple';

@Component({
    selector: 'app-notification',
    standalone: true,
    imports: [CommonModule, ButtonModule, OverlayPanelModule, BadgeModule, RippleModule],
    template: `
        <div class="relative">
            <button
                pButton
                pRipple
                type="button"
                icon="pi pi-bell"
                class="p-button-rounded p-button-text"
                (click)="op.toggle($event)"
                pBadge
                [value]="unreadCount"
                [severity]="'danger'"
            ></button>

            <p-overlayPanel #op [showCloseIcon]="true" [dismissable]="true" styleClass="notification-panel">
                <ng-template pTemplate>
                    <div class="p-3">
                        <div class="flex justify-content-between align-items-center mb-3">
                            <h3 class="text-lg font-semibold m-0">Notifications</h3>
                            <button
                                *ngIf="unreadCount > 0"
                                pButton
                                pRipple
                                type="button"
                                label="Mark all as read"
                                class="p-button-text p-button-sm"
                                (click)="markAllAsRead()"
                            ></button>
                        </div>

                        <div class="notification-list" style="max-height: 400px; overflow-y: auto;">
                            <div
                                *ngIf="notifications.length === 0"
                                class="text-center text-500 p-3"
                            >
                                No notifications
                            </div>

                            <div
                                *ngFor="let notification of notifications"
                                (click)="markAsRead(notification)"
                                class="p-3 border-bottom-1 surface-border cursor-pointer"
                                [class.bg-blue-50]="!notification.isRead"
                                pRipple
                            >
                                <div class="flex align-items-start">
                                    <i [class]="'pi ' + getNotificationIcon(notification.type) + ' mr-3 text-500'"></i>
                                    <div class="flex-1">
                                        <h4 class="font-medium text-900 m-0">
                                            {{ notification.title }}
                                        </h4>
                                        <p class="text-sm text-600 mt-2 mb-2">
                                            {{ notification.message }}
                                        </p>
                                        <p class="text-xs text-400 m-0">
                                            {{ notification.createdOn | date:'medium' }}
                                        </p>
                                    </div>
                                    <div
                                        *ngIf="!notification.isRead"
                                        class="w-2rem h-2rem border-circle bg-primary"
                                    ></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </ng-template>
            </p-overlayPanel>
        </div>
    `,
    styles: [`
        :host ::ng-deep {
            .notification-panel {
                width: 400px;
            }
            
            .notification-list {
                &::-webkit-scrollbar {
                    width: 6px;
                }
                
                &::-webkit-scrollbar-track {
                    background: #f1f1f1;
                }
                
                &::-webkit-scrollbar-thumb {
                    background: #888;
                    border-radius: 3px;
                }
            }
        }
    `]
})
export class NotificationComponent implements OnInit {
    @ViewChild('op') op!: OverlayPanel;
    notifications: Notification[] = [];
    unreadCount: number = 0;

    constructor(private notificationService: NotificationService) {}

    ngOnInit(): void {
        this.loadNotifications();
        this.loadUnreadCount();

        // Refresh notifications every 30 seconds
        interval(30000).pipe(
            switchMap(() => this.notificationService.getUserNotifications())
        ).subscribe(notifications => {
            this.notifications = notifications;
        });

        // Refresh unread count every 30 seconds
        interval(30000).pipe(
            switchMap(() => this.notificationService.getUnreadCount())
        ).subscribe(count => {
            this.unreadCount = count;
        });
    }

    loadNotifications(): void {
        this.notificationService.getUserNotifications().subscribe(
            notifications => {
                this.notifications = notifications;
            }
        );
    }

    loadUnreadCount(): void {
        this.notificationService.getUnreadCount().subscribe(
            count => {
                this.unreadCount = count;
            }
        );
    }

    markAsRead(notification: Notification): void {
        if (!notification.isRead) {
            this.notificationService.markAsRead(notification.id).subscribe(() => {
                notification.isRead = true;
                this.unreadCount = Math.max(0, this.unreadCount - 1);
            });
        }
    }

    markAllAsRead(): void {
        this.notificationService.markAllAsRead().subscribe(() => {
            this.notifications.forEach(notification => notification.isRead = true);
            this.unreadCount = 0;
        });
    }

    getNotificationIcon(type: string): string {
        switch (type) {
            case 'SYSTEM':
                return 'pi-cog';
            case 'INVOICE':
                return 'pi-file';
            case 'APPROVAL':
                return 'pi-check-circle';
            default:
                return 'pi-bell';
        }
    }
}
