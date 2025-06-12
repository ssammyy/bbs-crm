import { Routes } from '@angular/router';
import { CreateComponent } from './users/create/create.component';
import { CreatePrivilegesComponent } from './Roles/create-privileges/create-privileges.component';
import { ViewPrivilegesComponent } from './Roles/view-privileges/view-privileges.component';
import { CreateRoleComponent } from './Roles/create-role/create-role.component';
import { ViewRolesComponent } from './Roles/view-roles/view-roles.component';
import { ViewUsersComponent } from './users/view-users/view-users.component';
import { OnboardClientComponent } from './client/onboard-client/onboard-client.component';
import { ViewClientsComponent } from './client/view-clients/view-clients.component';
import { ClientProfileComponent } from './client-profile/client-profile.component';
import { CommitmentClientViewComponent } from './client/commitment-client-view/commitment-client-view.component';
import { ClientCommitmentComponent } from './client/client-commitment/client-commitment.component';
import { InvoiceFormComponent } from './invoice-form/invoice-form.component';
import { LicencesComponent } from './licences/licences.component';

export default [
    // { path: '**', redirectTo: '/notfound' },
    { path: 'create-user', component: CreateComponent },
    { path: 'create-privilege', component: CreatePrivilegesComponent },
    { path: 'view-privileges', component: ViewPrivilegesComponent },
    { path: 'create-role', component: CreateRoleComponent },
    { path: 'view-roles', component: ViewRolesComponent },
    { path: 'view-users', component: ViewUsersComponent },
    { path: 'onboard-client', component: OnboardClientComponent },
    { path: 'view-clients', component: ViewClientsComponent },
    { path: 'profile/:id', component: ClientProfileComponent },
    { path: 'commit-client', component: ClientCommitmentComponent },
    { path: 'view-leads', component: CommitmentClientViewComponent },
    { path: 'create-invoice', component: InvoiceFormComponent },
    { path: 'licences', component: LicencesComponent },



] as Routes;
