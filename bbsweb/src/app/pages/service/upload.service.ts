import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface LicenseFile {
    id: number;
    fileType: string;
    fileName: string;
    fileUrl: string;
    version: number;
    createdAt: string;
    updatedAt: string;
    userId?: number;
    username?: string;
    userEmail?: string;
    userRole?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UploadService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    upload( fileType: string, file: File,clientId?: number, preliminaryId?: number, userId?: number): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('fileType', fileType);

        let url = `${this.apiUrl}/api/files/upload?`;
        if (clientId) {
            url += `clientId=${clientId}`;
        }
        if (preliminaryId) {
            url += `&preliminaryId=${preliminaryId}`;
        }
        if (userId) {
            url += `&userId=${userId}`;
        }

        return this.http.post(url, formData);
    }

    getLicenseFiles(): Observable<LicenseFile[]> {
        return this.http.get<LicenseFile[]>(`${this.apiUrl}/api/files/licenses`);
    }

    updateDocument(clientId: number, file: File, fileType: string,  fileId: number | undefined, versionNotes?: string): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        if (versionNotes) {
            formData.append('versionNotes', versionNotes);
        }

        return this.http.post(`${this.apiUrl}/api/files/${clientId}/${fileType}/${fileId}/update`, formData);
    }
}
