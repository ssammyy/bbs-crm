import { Injectable } from '@angular/core';
import { MessageService } from 'primeng/api';

@Injectable({
    providedIn: 'root',

})

export class MessagesService {
    constructor(private messageService: MessageService) {}
    showMessage(severity: string, summary: string, detail: string) {
        this.messageService.add({ severity, summary, detail });
    }

    showSuccess(detail: string, summary: string = 'Success') {
        this.showMessage('success', summary, detail);
    }

    showInfo(detail: string, summary: string = 'Info') {
        this.showMessage('info', summary, detail);
    }

    showWarn(detail: string, summary: string = 'Warning') {
        this.showMessage('warn', summary, detail);
    }

    showError(detail: string, summary: string = 'Error') {
        this.showMessage('error', summary, detail);
    }

    showContrast(detail: string, summary: string = 'Contrast') {
        this.showMessage('contrast', summary, detail);
    }

    showSecondary(detail: string, summary: string = 'Secondary') {
        this.showMessage('secondary', summary, detail);
    }


}
