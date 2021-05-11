import {Component, NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './security/login/login.component';
import {CallbackComponent} from './security/callback/callback.component';
import {OktaAuthGuard} from './security/okta-auth.guard';
import {OrganizationRegistrationComponent} from './overview/organization-registration/organization-registration.component';
import {OrganizationPortalComponent} from './portals/organization-portal/organization-portal.component';
import {FlatPortalComponent} from './portals/flat-portal/flat-portal.component';
import {MainBoardComponent} from './overview/main-board/main-board.component';
import {NotFoundComponent} from './not-found/not-found.component';
import {AboutComponent} from './about/about.component';

const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  {path: 'login', component: LoginComponent},
  {path: 'prehled', component: MainBoardComponent, canActivate: [OktaAuthGuard]},
  {path: 'registrace', component: OrganizationRegistrationComponent, canActivate: [OktaAuthGuard]},
  {path: 'organizace/:organizationid/jednotky/:flatid', component: FlatPortalComponent, canActivate: [OktaAuthGuard]},
  {path: 'organizace/:organizationid', component: OrganizationPortalComponent, canActivate: [OktaAuthGuard]},
  {path: 'moje/jednotky/:flatid', component: FlatPortalComponent, canActivate: [OktaAuthGuard]},
  {path: 'jednotka/:organizationid/portal/:flatid', component: FlatPortalComponent, canActivate: [OktaAuthGuard]},
  {path: 'callback', component: CallbackComponent},
  {path: 'oaplikaci', component: AboutComponent},
  {path: 'neplatny/pozadavek', component: NotFoundComponent},
  {path: '**', redirectTo: '/neplatny/pozadavek'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
