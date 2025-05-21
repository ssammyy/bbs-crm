import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { UserGlobalService } from './app/pages/service/user.service';
import { environment } from './environments/environment';
import { NgxSpinnerModule } from 'ngx-spinner';
import { NgxSpinnerService } from 'ngx-spinner';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterModule, NgxSpinnerModule],
    template: `
        <router-outlet></router-outlet>
        <ngx-spinner
            bdColor="rgba(0, 0, 0, 0.8)"
            size="medium"
            color="#fff"
            type="ball-clip-rotate"
            [fullScreen]="true"
            [name]="'spinner'">
            <p style="color: white">Loading...</p>
        </ngx-spinner>
    `
})
export class AppComponent implements OnInit {
    constructor(
        private userService: UserGlobalService,
        private spinner: NgxSpinnerService
    ) {}

    ngOnInit() {
        this.spinner.show();
        console.log('Environment:', environment);
        const storedUser = this.userService.getUserFromStorage();
        if (storedUser) {
            this.userService.setUser(storedUser);
        }
        // Hide spinner after a short delay to ensure all resources are loaded
        setTimeout(() => {
            this.spinner.hide();
        }, 1000);
    }
}
