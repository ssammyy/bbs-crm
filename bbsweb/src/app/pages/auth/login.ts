import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService } from './auth.service';
import { Toast } from 'primeng/toast';
import { MessagesService } from '../../layout/service/messages.service';
import { UserService } from '../users/user.service';
import { UserGlobalService } from '../service/user.service';

@Component({
    selector: 'app-login',
    standalone: true,
    providers:[MessagesService],
    imports: [ButtonModule, CheckboxModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, AppFloatingConfigurator, Toast],
    template: `
        <app-floating-configurator />
        <p-toast/>
        <div
            class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
            <div class="flex flex-col items-center justify-center p-4">
                <div
                    style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20"
                         style="border-radius: 53px">
                        <div class="text-center flex flex-col items-center justify-center align-middle mb-8">

                            <br>
                            <span class="text-muted-color font-medium">Sign in to continue</span>
                        </div>

                        <div>
                            <label for="email1"
                                   class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Username</label>
                            <input pInputText id="email1" type="text" placeholder="Email address"
                                   class="w-full mb-8" [(ngModel)]="username" />

                            <label for="password1"
                                   class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Password</label>
                            <p-password id="password1" [(ngModel)]="password" placeholder="Password" [toggleMask]="true"
                                        styleClass="mb-4" [fluid]="true" [feedback]="false"></p-password>

                            <div class="flex flex-col sm:flex-row items-center justify-between mt-2 mb-8 gap-4">
                                <div class="flex items-center">
                                </div>
                                <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">Forgot password?</span>
                            </div>
                            <p-button label="Sign In" [loading]="loading" (onClick)="login()" styleClass="w-full" routerLink="/"></p-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>    `
})
export class Login {
    username: string = '';
    password: string = '';
    checked: boolean = false;
    loading = false;
    constructor(private authService: AuthService, private router: Router, private messagesService: MessagesService, private userGlobalService: UserGlobalService) {

    }

    login() {
        this.loading = true;
        this.authService
            .login({username: this.username, password: this.password})
            .subscribe( {
                next:(response: any)=>{
                    this.authService.saveToken(response.token);
                    this.messagesService.showSuccess('Logged in successfully!');
                    if (response.token) {
                        console.log('success >>> ');
                        this.userGlobalService.fetchUserDetails()
                        this.loading = false;


                    }

                    this.router.navigate(['/app']);
                },
                    error:(err)=>{
                    this.messagesService.showError(err.error.details);
                        this.loading = false;

                    }
            })

    }
}
