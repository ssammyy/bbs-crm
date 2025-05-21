import { Component, OnInit } from '@angular/core';
import { Password } from 'primeng/password';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MessagesService } from '../../../layout/service/messages.service';
import { Button } from 'primeng/button';

@Component({
    selector: 'app-password-reset',
    imports: [Password, FormsModule, Button],
    templateUrl: './password-reset.component.html',
    styleUrl: './password-reset.component.scss'
})
export class PasswordResetComponent implements OnInit {
    oldPassword = '';
    newPassword = '';
    confirmPassword = '';
    loading = false;
    token!: string;
    error = '';

    constructor(
        private route: ActivatedRoute,
        private authService: AuthService,
        private messagesService: MessagesService,
        private router: Router
    ) {}

    ngOnInit() {
        this.token = this.route.snapshot.paramMap.get('token') || '';
        console.log('token ', this.token);
        if (!this.token) {
            this.messagesService.showError('No token provided');
            this.error = 'Missing verification token.';
        }
    }

    updatePassword() {
        this.loading = true;

        if (this.newPassword !== this.confirmPassword) {
            // Handle password mismatch error
            console.error('New passwords do not match');
            this.loading = false;
            return;
        }
        const payload = {
            newPassword: this.newPassword
        };

        this.authService.updatePassword(this.token, payload).subscribe({
            next: (response) => {
                // Handle successful password update
                console.log('Password updated successfully, proceed to login', response);
                this.loading = false;
                this.router.navigate(['/']);

                // Optionally redirect or show a success message
            },
            error: (error) => {
                // Handle password update error
                console.error('Error updating password', error);
                this.loading = false;
                // Optionally show an error message
            }
        });
    }
}
