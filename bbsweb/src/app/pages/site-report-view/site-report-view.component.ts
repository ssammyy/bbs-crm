import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MessageService } from 'primeng/api';
import { SiteReportService } from '../site-report-form/siteReport.service';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChipModule } from 'primeng/chip';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { FormsModule } from '@angular/forms';
import { Textarea } from 'primeng/textarea';

@Component({
    selector: 'app-site-report-view',
    templateUrl: './site-report-view.component.html',
    providers: [MessageService],
    imports: [CommonModule, CardModule, ChipModule, ButtonModule, DialogModule, ToastModule, TableModule, FormsModule, Textarea],
    standalone: true
})
export class SiteReportViewComponent implements OnInit {
    @Input() clientId!: number | undefined;
    @Output() statusChanged = new EventEmitter<void>();

    report: any = null;
    auditLogs: any[] = [];
    loadingApprove: boolean = false;
    loadingReject: boolean = false;
    showRejectDialogVisible: boolean = false;
    rejectionComments: string = '';

    constructor(
        private siteReportService: SiteReportService,
        private messageService: MessageService
    ) {}

    ngOnInit(): void {
        this.loadReport();
    }

    loadReport(): void {
        this.siteReportService.getReportByClientId(this.clientId!!).subscribe({
            next: (report) => {
                this.report = report;
            },
            error: (error) => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to load report'
                });
            }
        });
    }

    // loadAuditLogs(reportId: number): void {
    //     this.siteReportService.getAuditLogs(reportId).subscribe({
    //         next: (logs) => {
    //             this.auditLogs = logs;
    //         },
    //         error: (error) => {
    //             this.messageService.add({
    //                 severity: 'error',
    //                 summary: 'Error',
    //                 detail: 'Failed to load audit logs',
    //             });
    //         },
    //     });
    // }

    showRejectDialog(): void {
        this.rejectionComments = '';
        this.showRejectDialogVisible = true;
    }

    approveReport(): void {
        if (!this.report?.id) return;
        this.loadingApprove = true;
        this.siteReportService.approveReport(this.report.id).subscribe({
            next: (updatedReport) => {
                this.report = updatedReport;
                this.loadingApprove = false;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Report approved successfully'
                });
                this.statusChanged.emit();
                // this.loadAuditLogs(this.report.id);
            },
            error: (error) => {
                this.loadingApprove = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to approve report'
                });
            }
        });
    }

    rejectReport(): void {
        if (!this.report?.id || !this.rejectionComments) return;
        this.loadingReject = true;
        this.siteReportService.rejectReport(this.report.id, this.rejectionComments).subscribe({
            next: (updatedReport) => {
                this.report = updatedReport;
                this.showRejectDialogVisible = false;
                this.loadingReject = false;
                this.messageService.add({
                    severity: 'success',
                    summary: 'Success',
                    detail: 'Report rejected for amendment'
                });
                this.statusChanged.emit();
                // this.loadAuditLogs(this.report.id);
            },
            error: (error) => {
                this.loadingReject = false;
                this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Failed to reject report'
                });
            }
        });
    }
}
