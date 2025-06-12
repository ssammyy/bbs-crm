import { Component, Input, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { environment } from '../../../../../environments/environment';

export interface MilestoneItemDTO {
  name: string;
  completed: boolean;
}

export interface MilestoneChecklistDTO {
  clientId: number;
  clientName?: string;
  siteLocation?: string;
  milestones: MilestoneItemDTO[];
}

@Component({
    selector: 'app-client-milestone-checklist',
    templateUrl: './client-milestone-checklist.component.html',
    imports: [NgIf, NgClass, NgForOf],
    styleUrls: ['./client-milestone-checklist.component.scss']
})
export class ClientMilestoneChecklistComponent implements OnInit {
    @Input() clientId!: number;
    checklist?: MilestoneChecklistDTO;
    loading = false;
    error?: string;


    constructor(private http: HttpClient) {}

    ngOnInit(): void {
        if (this.clientId) {
            this.fetchChecklist();
        }
    }

    fetchChecklist() {
        const api = environment.apiUrl;
        this.loading = true;
        this.error = undefined;
        this.http.get<MilestoneChecklistDTO>(`${api}/api/clients/${this.clientId}/milestone-checklist`).subscribe({
            next: (data) => {
                this.checklist = data;
                this.loading = false;
            },
            error: (err) => {
                console.error('Error fetching checklist:', err);
                this.error = err.status === 404
                    ? 'Checklist not found for this client.'
                    : 'Failed to load checklist. Please try again later.';
                this.loading = false;
            }
        });
    }

    get progressPercent(): string {
        if (!this.checklist || !this.checklist.milestones.length) return '0%';
        const completed = this.checklist.milestones.filter(m => m.completed).length;
        const percent = (completed / this.checklist.milestones.length) * 100;
        return percent + '%';
    }

    get completedMilestonesCount(): number {
        return this.checklist?.milestones.filter(m => m.completed).length ?? 0;
    }

    get totalMilestonesCount(): number {
        return this.checklist?.milestones.length ?? 0;
    }
}
