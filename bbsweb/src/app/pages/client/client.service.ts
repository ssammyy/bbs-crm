import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Activity, Client, Files } from '../data/clietDTOs';
import { Observable } from 'rxjs';


@Injectable({
    providedIn: "root",
})
export class ClientDetailsService {
    private apiUrl = environment.apiUrl;
    constructor(private http: HttpClient) {}

    submitClientDetails(clientDetails: Client): Observable<Client>{
        return this.http.post<Client>(`${environment.apiUrl}/api/clients/register`, clientDetails);
    }

    getAllClients(): Observable<Client[]> {
        return this.http.get<Client[]>(`${environment.apiUrl}/api/clients`);
    }

    getLeads(): Observable<Client[]> {
        return this.http.get<Client[]>(`${environment.apiUrl}/api/clients/leads`);
    }

    getActiveClients(): Observable<Client[]> {
        return this.http.get<Client[]>(`${environment.apiUrl}/api/clients/active`);
    }


    getClient(id: number): Observable<Client> {
        return this.http.get<Client>(`${environment.apiUrl}/api/clients/${id}`);
    }

    updateClient(updatedClient: Client): Observable<Client>{
        return this.http.put<Client>(`${environment.apiUrl}/api/clients/update`, updatedClient);
    }

    getClientFiles(clientId: number): Observable<Files[]> {
        return this.http.get<Files[]>(`${environment.apiUrl}/api/files/${clientId}`);
    }
    getClientActivities(clientId: number): Observable<Activity[]> {
        return this.http.get<Activity[]>(`${environment.apiUrl}/api/clients/${clientId}/activities`);
    }

    updateClientStage(clientId: number, currentStage: string, newStage: string, message: string): Observable<Client> {
        return this.http.post<Client>(`${this.apiUrl}/api/clients/${clientId}/update-stage`, { currentStage, newStage, message });
    }

    deleteClient(id: number): Observable<Client> {
        return this.http.delete<Client>(`${this.apiUrl}/api/clients/${id}`);
    }
}
