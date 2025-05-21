import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from '../../../environments/environment';
import { Privilege, Role } from './RoleDtos';

@Injectable({
    providedIn: "root",
})
export class RolesService {
    // private apiUrl = "http://localhost:7878/api";
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}


    createPrivilege(payload: {name: string}): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/privileges`, payload);
    }

    updatePrivilege(id: number, payload: {name: string}): Observable<any> {
        return this.http.put(`${this.apiUrl}/api/privileges/${id}`, payload);
    }

    deletePrivilege(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/api/privileges/${id}`);
    }

    getPrivileges(): Observable<any> {
        console.log("Getting privileges");
        return this.http.get(`${this.apiUrl}/api/privileges`)
    }

    createRole(payload: {name: string, privilegeIds: number[]}): Observable<any> {
        return this.http.post(`${this.apiUrl}/api/roles`, payload);
    }

    getRoles(): Observable<any> {
        return this.http.get(`${this.apiUrl}/api/roles`)
    }

    getAllPrivileges(): Observable<Privilege[]> {
        return this.http.get<Privilege[]>(`${this.apiUrl}/api/privileges`);
    }

    updateRole(id: number, role: any): Observable<Role> {
        return this.http.put<Role>(`${this.apiUrl}/api/roles/${id}`, role);
    }
    deleteRole(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}
