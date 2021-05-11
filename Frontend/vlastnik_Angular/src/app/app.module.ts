import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {AppRoutingModule} from './app-routing.module';
import {LoginComponent} from './security/login/login.component';
import {MatBadgeModule} from '@angular/material/badge';
import {DocumentUploaderComponent} from './uploaders/document-uploader/document-uploader.component';
import {MatInputModule} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatDatepicker, MatDatepickerModule} from '@angular/material/datepicker';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatSelectModule} from '@angular/material/select';
import {MatNativeDateModule} from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatToolbarModule} from '@angular/material/toolbar';
import {CallbackComponent} from './security/callback/callback.component';
import {HttpClientModule} from '@angular/common/http';
import {OrganizationRegistrationComponent} from './overview/organization-registration/organization-registration.component';
import {MatStepperModule} from '@angular/material/stepper';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MyOrganizationComponent} from './overview/my-organization/my-organization.component';
import {MatMenuModule} from '@angular/material/menu';
import {OrganizationPortalComponent} from './portals/organization-portal/organization-portal.component';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {FlatUploaderComponent} from './uploaders/flat-uploader/flat-uploader.component';
import {NgxDropzoneModule} from 'ngx-dropzone';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {TokenTableComponent} from './tables/token-table/token-table.component';
import {FlatPortalComponent} from './portals/flat-portal/flat-portal.component';
import {PersonTableComponent} from './tables/person-table/person-table.component';
import {DocumentTableComponent} from './tables/document-table/document-table.component';
import {MyFlatComponent} from './overview/my-flat/my-flat.component';
import {PersonStepperComponent} from './steppers/person-stepper/person-stepper.component';
import {MomentDateModule} from '@angular/material-moment-adapter';
import {MatTabsModule} from '@angular/material/tabs';
import {CommitteeComponent} from './general/committee/committee.component';
import {CommitteeStepperComponent} from './steppers/committee-stepper/committee-stepper.component';
import {ComponentStepperComponent} from './steppers/component-stepper/component-stepper.component';
import {OrganizationBoardComponent} from './general/organization-board/organization-board.component';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {TokenPinComponent} from './overview/token-pin/token-pin.component';
import {MainBoardComponent} from './overview/main-board/main-board.component';
import {RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module} from 'ng-recaptcha';
import {NotFoundComponent} from './not-found/not-found.component';
import {OrganizationFlatTableComponent} from './tables/organization-flat-table/organization-flat-table.component';
import {CategoryTableComponent} from './tables/category-table/category-table.component';
import {AboutComponent} from './about/about.component';
import {MatTooltipModule} from '@angular/material/tooltip';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DocumentUploaderComponent,
    CallbackComponent,
    OrganizationRegistrationComponent,
    MyOrganizationComponent,
    OrganizationPortalComponent,
    FlatUploaderComponent,
    TokenTableComponent,
    FlatPortalComponent,
    PersonTableComponent,
    DocumentTableComponent,
    MyFlatComponent,
    PersonStepperComponent,
    CommitteeComponent,
    CommitteeStepperComponent,
    ComponentStepperComponent,
    OrganizationBoardComponent,
    TokenPinComponent,
    MainBoardComponent,
    NotFoundComponent,
    OrganizationFlatTableComponent,
    CategoryTableComponent,
    AboutComponent,
  ],
  imports: [
    HttpClientModule,
    MomentDateModule,
    BrowserModule,
    BrowserAnimationsModule,
    MatGridListModule,
    MatCardModule,
    MatButtonModule,
    MatBadgeModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    MatIconModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatSelectModule,
    MatNativeDateModule,
    MatDialogModule,
    MatToolbarModule,
    MatStepperModule,
    FlexLayoutModule,
    MatMenuModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    DragDropModule,
    MatProgressSpinnerModule,
    NgxDropzoneModule,
    MatProgressBarModule,
    MatTabsModule,
    MatCheckboxModule,
    RecaptchaV3Module,
    MatTooltipModule,
    AppRoutingModule,
  ],
  providers: [{provide: RECAPTCHA_V3_SITE_KEY, useValue: ''}],
  bootstrap: [AppComponent]

})
export class AppModule {
}
