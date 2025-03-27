import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

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
    email: string;
    role: Role;
}

@Injectable({
    providedIn: 'root'
})
export class UserGlobalService {
    private apiUrl = 'http://localhost:7878';


    private userSubject = new BehaviorSubject<User | null>(null);
    user$ = this.userSubject.asObservable();

    constructor(private http: HttpClient) {}

    fetchUserDetails(): any {
        console.log('Fetching user details');
        this.getDetails()
        .subscribe({
            next: (data) =>{
                 this.userSubject.next(data);
            }
        })
    }

    getDetails(): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/user/me`)
    }


    getUser(): User | null {
        return this.userSubject.value;
    }

    getUserRole(): Role | null {
        return this.userSubject.value?.role || null;
    }

    getUserPrivileges(): string[] {
        return this.userSubject.value?.role.privileges.map((p) => p.name) || [];
    }

    hasPrivilege(privilege: string): boolean {
        return this.getUserPrivileges().includes(privilege);
    }
}
