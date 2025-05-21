import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
    selector: 'app-confirm-email',
    imports: [NgIf],
    template: `
        <div class="p-4 text-center">
            <p *ngIf="loading">Verifying your email...</p>
            <p *ngIf="success">✅ Email confirmed! Redirecting to password update...</p>
            <p *ngIf="error">❌ Something went wrong: {{ error }}</p>
        </div>
    `
})
export class ConfirmEmailComponent implements OnInit {
    loading = true;
    success = false;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit() {
        const token = this.route.snapshot.queryParamMap.get('token');
        if (token) {
            this.authService.confirmEmail(token).subscribe({
                next: () => {
                    console.log('success');
                    this.success = true;
                    this.loading = false;
                    setTimeout(() => this.router.navigate(['/set-password', token]), 2500);
                },
                error: (err) => {
                    console.log("error", err);
                    this.error = err.error?.message || 'Email confirmation failed';
                    this.loading = false;
                }
            });
        } else {
            this.error = 'No token provided';
            this.loading = false;
        }
    }
}
