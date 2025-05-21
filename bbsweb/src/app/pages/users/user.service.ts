import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { User } from '../service/user.service';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: "root",
})
export class UserService {
    // private apiUrl = "http://localhost:7878";
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    registerUser(userData: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/auth/register`, userData);
    }


    getUsers(): Observable<any> {
        return this.http.get<User[]>(`${this.apiUrl}/api/user/all`);
    }

    updateUser(id: number, user: Partial<User>): Observable<User> {
        return this.http.put<User>(`${this.apiUrl}/api/user/${id}`, user);
    }



    deleteUser(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/api/user/${id}`);
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
