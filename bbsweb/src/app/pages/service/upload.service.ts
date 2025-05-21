import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UploadService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    upload(clientId: number, fileType: string, file: File, preliminaryId?: number) {
        console.log({ clientId, fileType, file });
        const formData = new FormData();
        formData.append('file', file, file.name);

        let url = `${this.apiUrl}/api/files/upload?clientId=${clientId}&fileType=${fileType}`;
        if (preliminaryId !== undefined) {
            url += `&preliminaryId=${preliminaryId}`;
        }

        return this.http.post<void>(url, formData);
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
