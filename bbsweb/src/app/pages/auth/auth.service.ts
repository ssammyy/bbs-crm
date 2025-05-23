import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    // private apiUrl = 'http://localhost:7878/auth/login';
    private apiUrl = environment.apiUrl + '/auth/login';
    private currentUser: any = null;
    private envUrl = environment.apiUrl ;

    constructor(private http: HttpClient, private router: Router) { }


    isAuthenticated(): boolean {
        const token = localStorage.getItem('auth_token');
        if (!token) return false;

        const decoded: any = jwtDecode(token);
        const now = Math.floor(Date.now() / 1000);
        if (decoded.exp < now) {
            this.logout();
            return false;
        }
        return true;
    }


    login(credentials: { username: string; password: string }): Observable<any> {
        return this.http.post(this.apiUrl, credentials);
    }

    // Store JWT token in local storage after login
    saveToken(token: string): void {
        localStorage.setItem('auth_token', token);
    }


    decodeToken(token: string): void {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = { username: payload.sub, privileges: payload.authorities };
    }



    hasPrivilege(privilege: string): boolean {
        return this.currentUser?.privileges?.includes(privilege);
    }

    logout(): void {
        localStorage.removeItem('auth_token');
        this.currentUser = null;
        this.router.navigate(['/']);
    }

    confirmEmail(token: string): Observable<any> {
        return this.http.post(`${this.envUrl}/auth/confirm-email`, { token });
    }

    updatePassword(token: string,  payload:{}): Observable<any> {
        return this.http.put(`${this.envUrl}/auth/set-password?token=${token}`, payload);
    }
}
