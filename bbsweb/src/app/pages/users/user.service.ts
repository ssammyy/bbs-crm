import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, BehaviorSubject, tap } from "rxjs";
import { User } from '../service/user.service';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: "root",
})
export class UserService {
    // private apiUrl = "http://localhost:7878";
    private apiUrl = environment.apiUrl;
    private usersCache$ = new BehaviorSubject<User[]>([]);
    private isInitialized = false;

    constructor(private http: HttpClient) {}

    registerUser(userData: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/auth/register`, userData);
    }

    getUsers(): Observable<User[]> {
        if (!this.isInitialized) {
            console.log('Making HTTP request to fetch users...');
            this.http.get<User[]>(`${this.apiUrl}/api/user/all`).pipe(
                tap(() => console.log('HTTP request completed'))
            ).subscribe({
                next: (users) => {
                    this.usersCache$.next(users);
                    this.isInitialized = true;
                },
                error: (error) => {
                    console.error('Error fetching users:', error);
                    this.isInitialized = false;
                }
            });
        }
        return this.usersCache$.asObservable();
    }

    // Method to force refresh the cache
    refreshUsers(): Observable<User[]> {
        console.log('Forcing refresh of users cache...');
        this.isInitialized = false;
        return this.getUsers();
    }

    updateUser(id: number, user: Partial<User>): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/api/user/${id}`, user).pipe(
            tap(() => this.refreshUsers())
        );
    }

    deleteUser(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/api/user/${id}`).pipe(
            tap(() => this.refreshUsers())
        );
    }

    uploadKycDocument(userId: number, fileType: string, file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('fileType', fileType);
        return this.http.post(`${this.apiUrl}/api/files/upload?userId=${userId}`, formData);
    }

    getAgents(): Observable<User[]> {
        return this.http.get<User[]>(`${this.apiUrl}/api/user/agents`);
    }
}
