import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Client } from '../data/clietDTOs';
import { jwtDecode } from 'jwt-decode';

export interface Privilege {
    id: number;
    name: string;
}

export interface Role {
    id: number;
    name: string;
    privileges: Privilege[];
}

export interface User {
    id: number;
    username: string;
    createdOn: Date;
    createdBy: string;
    phonenumber: string;
    email: string;
    role: Role;
}

@Injectable({
    providedIn: 'root'
})
export class UserGlobalService {
    // private apiUrl = 'http://localhost:7878';
    private apiUrl = environment.apiUrl;


    role!: Role



    private userSubject = new BehaviorSubject<User | null>(null);
    user$ = this.userSubject.asObservable();

    constructor(private http: HttpClient) {}

    fetchUserDetails(): any {
        console.log('Fetching user details');
        this.getDetails()
        .subscribe({
            next: (data) =>{
                // localStorage.setItem('user', JSON.stringify(data)); // Save user to local storage
                 this.userSubject.next(null);
            }
        })
    }

    getDetails(): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/user/me`)
    }

    clearUser(): void {
        localStorage.removeItem('user'); // Remove user
        this.userSubject.next(null);
    }

    getUserFromStorage(): any {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    }
    setUser(user: any): void {
        localStorage.setItem('user', JSON.stringify(user)); // Save user in local storage
        this.userSubject.next(null); // Update observable
    }


    hasPrivilege(privilege: string): boolean {
        const token = localStorage.getItem('auth_token');
        console.log('privs ', this.getPrivileges(token!!));
        return this.getPrivileges(token!!).includes(privilege);
    }
    getPrivileges(token: string): string[] {
        try {
            const decoded: any = jwtDecode(token);
            return decoded.privileges || [];
        } catch (error) {
            console.error('Invalid JWT', error);
            return [];
        }
    }

    getClientDetails(userEmail: string): Observable<Client> {
        return this.http.get<Client>(`${this.apiUrl}/api/clients/${userEmail}/details`)

    }
}
