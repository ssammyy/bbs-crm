import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../../shared/interfaces/notification.interface';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = `${environment.apiUrl}/api/notifications`;

    constructor(private http: HttpClient) {}

    getUserNotifications(): Observable<Notification[]> {
        return this.http.get<Notification[]>(this.apiUrl);
    }

    getUnreadCount(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/unread/count`);
    }

    markAsRead(notificationId: number): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${notificationId}/read`, {});
    }

    markAllAsRead(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/read-all`, {});
    }
} 