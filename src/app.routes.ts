import { Routes } from '@angular/router';
import { AppLayout } from './app/layout/component/app.layout';
import { Dashboard } from './app/pages/dashboard/dashboard';
import { Notfound } from './app/pages/notfound/notfound';
import { Login } from './app/pages/auth/login';
import { AuthGuard } from './app/pages/auth/auth.guard';

export const appRoutes: Routes = [

    {
        path: '',
        redirectTo: '/auth',
        pathMatch: 'full',
    },
    {
        path: 'app',
        component: AppLayout,
        canActivate: [AuthGuard],
        children: [
            { path: '', component: Dashboard },
            { path: 'pages', loadChildren: () => import('./app/pages/pages.routes') }
        ]
    },
    { path: 'notfound', component: Notfound },
    // { path: 'auth', loadChildren: () => import('./app/pages/auth/auth.routes') },
    { path: 'auth', component: Login },
    { path: '**', redirectTo: '/notfound' }
];
