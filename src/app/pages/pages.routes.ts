import { Routes } from '@angular/router';
import { CreateComponent } from './users/create/create.component';
import { CreatePrivilegesComponent } from './Roles/create-privileges/create-privileges.component';
import { ViewPrivilegesComponent } from './Roles/view-privileges/view-privileges.component';
import { CreateRoleComponent } from './Roles/create-role/create-role.component';
import { ViewRolesComponent } from './Roles/view-roles/view-roles.component';

export default [
    // { path: '**', redirectTo: '/notfound' },
    { path: 'create-user', component: CreateComponent },
    { path: 'create-privilege', component: CreatePrivilegesComponent },
    { path: 'view-privileges', component: ViewPrivilegesComponent },
    { path: 'create-role', component: CreateRoleComponent },
    { path: 'view-roles', component: ViewRolesComponent },
] as Routes;
