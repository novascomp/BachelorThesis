import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {OrganizationService} from '../../rest/service/organization.service';
import {Organization} from '../../rest/model/Organization';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {HttpResponse} from '@angular/common/http';
import {GeneralListResponse} from '../../rest/model/GeneralListResponse';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';

@Component({
  selector: 'app-organization-personal',
  templateUrl: './my-organization.component.html',
  styleUrls: ['./my-organization.component.css']
})
export class MyOrganizationComponent extends ModifiableResourcesComponent implements OnInit {

  organizations: Organization[];
  emptyOrganizations: boolean;
  organizationLoaded: boolean;

  @Output() afterDone = new EventEmitter<any>();

  private myorganizationsPromise: Promise<HttpResponse<GeneralListResponse<Organization>>>;

  constructor(private organizationService: OrganizationService, private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.init();
  }

  ngOnInit(): void {
    this.myorganizationsPromise = this.organizationService.getPersonalOrganization().toPromise();
    Promise.all([this.myorganizationsPromise]).then(this.processResponse.bind(this)).finally(this.interpretResponse.bind(this));
  }

  public init(): void {
    super.initGlobal();
    this.organizationLoaded = false;
  }

  private processResponse([myOrganizations]): void {
    const myOrganizationsGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(myOrganizations.status);
    if (myOrganizationsGeneralResponse === RestGeneralResponse.ok) {
      this.organizations = myOrganizations.body.content;
      this.afterDone.emit(this.organizations.length);
    } else {
      this.serverUnavailable = true;
      this.afterDone.emit(-1);
      this.organizationLoaded = true;
    }
  }

  private interpretResponse(): void {
    if (this.organizations == null) {
      this.emptyOrganizations = true;
      this.organizationLoaded = true;
      return;
    }

    if (this.organizations.length === 0 && this.serverUnavailable === false) {
      this.emptyOrganizations = true;
    }
    this.organizationLoaded = true;
  }

  public getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }

  getMyOrganizationsEmptyText(): string {
    return GeneralComponentTitles.MY_ORGANIZATIONS_EMPTY;
  }
}
