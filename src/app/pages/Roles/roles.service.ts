import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

@Injectable({
    providedIn: "root",
})
export class RolesService {
    private apiUrl = "http://localhost:7878/api";

    constructor(private http: HttpClient) {}


    createPrivilege(payload: {name: string}): Observable<any> {
        return this.http.post(`${this.apiUrl}/privileges`, payload);
    }

    getPrivileges(): Observable<any> {
        return this.http.get(`${this.apiUrl}/privileges`)
    }

    createRole(payload: {name: string, privilegeIds: number[]}): Observable<any> {
        return this.http.post(`${this.apiUrl}/roles`, payload);
    }

    getRoles(): Observable<any> {
        return this.http.get(`${this.apiUrl}/roles`)
    }
}
