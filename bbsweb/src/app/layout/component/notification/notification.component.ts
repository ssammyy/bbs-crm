import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../service/notification.service';
import { Notification } from '../../../shared/interfaces/notification.interface';
import { interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { DrawerModule, Drawer } from 'primeng/drawer';
import { BadgeModule } from 'primeng/badge';
import { RippleModule } from 'primeng/ripple';
import { OverlayBadge } from 'primeng/overlaybadge';
import { Divider } from 'primeng/divider';

@Component({
    selector: 'app-notification',
    standalone: true,
    imports: [CommonModule, ButtonModule, DrawerModule, BadgeModule, RippleModule, OverlayBadge, Divider],
    template: `
        <div class="notification-wrapper">
            <p-overlaybadge class="flex pt-2 " [value]="unreadCount" (click)="toggleDrawer()">
                <i class=" pi pi-bell" style="font-size: 1.5rem"></i>
            </p-overlaybadge>

            <p-drawer #drawer [position]="'right'" [style]="{ width: '380px' }" [modal]="true" [showCloseIcon]="true" [(visible)]="visible">
                <div class="drawer-content">
                    <div class="drawer-header">
                        <div class="header-content">
                            <strong class="title">Notifications</strong>
                            &nbsp;
                            <strong class="count" *ngIf="unreadCount > 0">{{ unreadCount }} unread</strong>
                        </div>
                        <button *ngIf="unreadCount > 0" pButton pRipple type="button" class="p-button-text p-button-sm mark-all" (click)="markAllAsRead()">
                            <i class="pi pi-check mr-2"></i>
                            Mark all as read
                        </button>
                    </div>

                    <div class="notification-list">
                        <div *ngIf="notifications.length === 0" class="empty-state">
                            <div class="empty-icon">
                                <i class="pi pi-bell"></i>
                            </div>
                            <p>All caught up!</p>
                            <span>You're up to date with all your notifications</span>
                        </div>

                        <div *ngFor="let notification of notifications" (click)="markAsRead(notification)" class="notification-item" [class.unread]="!notification.isRead" pRipple>
                            <div class="notification-content">
                                <div class="icon-wrapper" [class]="notification.type.toLowerCase()">
                                    <i [class]="'pi ' + getNotificationIcon(notification.type)"></i>
                                </div>
                                <div class="notification-details">
                                    <div class="notification-header">
                                        <small>{{ notification.title }}</small>
                                        <span class="time">{{ notification.createdAt | date: 'shortTime' }}</span>
                                    </div>
                                </div>
                            </div>
                            <p-divider layout="horizontal" />
                        </div>
                    </div>
                </div>
            </p-drawer>
        </div>
    `,
    styles: [
        `
            :host ::ng-deep {
                .notification-wrapper {
                    position: relative;
                    display: inline-block;
                }

                .notification-button {
                    position: relative;
                }

                .p-badge {
                    position: absolute;
                    top: 0;
                    right: 0;
                    transform: translate(50%, -50%);
                    z-index: 1000;
                }

                .drawer-content {
                    height: 100%;
                    display: flex;
                    flex-direction: column;
                    background-color: var(--surface-ground);
                }

                .drawer-header {
                    padding: 1rem 1.25rem;
                    background-color: var(--surface-card);
                    border-bottom: 1px solid var(--surface-border);
                    display: flex;
                    flex-direction: column;
                    gap: 0.5rem;

                    .header-content {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }

                    .title {
                        font-size: 0.875rem;
                        font-weight: 600;
                        color: var(--text-color);
                    }

                    .count {
                        font-size: 0.75rem;
                        color: var(--primary-color);
                        font-weight: 500;
                    }

                    .mark-all {
                        font-size: 0.75rem;
                        padding: 0.25rem 0.5rem;
                        color: var(--text-color-secondary);

                        &:hover {
                            background-color: var(--surface-hover);
                        }
                    }
                }

                .notification-list {
                    flex: 1;
                    overflow-y: auto;
                    padding: 0.5rem;

                    &::-webkit-scrollbar {
                        width: 4px;
                    }

                    &::-webkit-scrollbar-track {
                        background: transparent;
                    }

                    &::-webkit-scrollbar-thumb {
                        background: var(--surface-border);
                        border-radius: 2px;
                    }
                }

                .empty-state {
                    height: 100%;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    padding: 2rem;
                    text-align: center;

                    .empty-icon {
                        width: 48px;
                        height: 48px;
                        border-radius: 50%;
                        background-color: var(--surface-card);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin-bottom: 1rem;

                        i {
                            font-size: 1.5rem;
                            color: var(--text-color-secondary);
                        }
                    }

                    p {
                        margin: 0;
                        font-size: 1rem;
                        font-weight: 600;
                        color: var(--text-color);
                    }

                    span {
                        font-size: 0.875rem;
                        color: var(--text-color-secondary);
                        margin-top: 0.5rem;
                    }
                }

                .notification-item {
                    padding: 0.75rem;
                    border-radius: 8px;
                    margin-bottom: 0.25rem;
                    transition: all 0.2s;
                    cursor: pointer;
                    background-color: var(--surface-card);

                    &:hover {
                        background-color: var(--surface-hover);
                    }

                    &.unread {
                        background-color: var(--primary-50);
                    }
                }

                .notification-content {
                    display: flex;
                    gap: 0.75rem;
                }

                .icon-wrapper {
                    width: 32px;
                    height: 32px;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    flex-shrink: 0;

                    &.system {
                        background-color: var(--blue-100);
                        color: var(--blue-700);
                    }

                    &.invoice {
                        background-color: var(--green-100);
                        color: var(--green-700);
                    }

                    &.approval {
                        background-color: var(--orange-100);
                        color: var(--orange-700);
                    }

                    i {
                        font-size: 1rem;
                    }
                }

                .notification-details {
                    flex: 1;
                    min-width: 0;
                }

                .notification-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 0.25rem;

                    h4 {
                        margin: 0;
                        font-size: 0.875rem;
                        font-weight: 600;
                        color: var(--text-color);
                        line-height: 1.4;
                    }

                    .time {
                        font-size: 0.75rem;
                        color: var(--text-color-secondary);
                        white-space: nowrap;
                        margin-left: 0.5rem;
                    }
                }

                .message {
                    margin: 0;
                    font-size: 0.813rem;
                    color: var(--text-color-secondary);
                    line-height: 1.4;
                }
            }
        `
    ]
})
export class NotificationComponent implements OnInit {
    @ViewChild('drawer') drawer!: Drawer;
    notifications: Notification[] = [];
    unreadCount: number = 0;
    visible: boolean = false;

    constructor(private notificationService: NotificationService) {}

    ngOnInit(): void {
        this.loadNotifications();
        this.loadUnreadCount();

        // Refresh notifications every 30 seconds
        interval(30000)
            .pipe(switchMap(() => this.notificationService.getUserNotifications()))
            .subscribe((notifications) => {
                this.notifications = notifications;
            });

        // Refresh unread count every 30 seconds
        interval(30000)
            .pipe(switchMap(() => this.notificationService.getUnreadCount()))
            .subscribe((count) => {
                this.unreadCount = count;
            });
    }

    loadNotifications(): void {
        this.notificationService.getUserNotifications().subscribe((notifications) => {
            this.notifications = notifications;
        });
    }

    loadUnreadCount(): void {
        this.notificationService.getUnreadCount().subscribe((count) => {
            this.unreadCount = count;
        });
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
            this.notifications.forEach((notification) => (notification.isRead = true));
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

    toggleDrawer(): void {
        this.visible = !this.visible;
    }
}
