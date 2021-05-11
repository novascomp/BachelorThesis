import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Organization} from '../../rest/model/Organization';
import {OrganizationService} from '../../rest/service/organization.service';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';

@Component({
  selector: 'app-organization-registration',
  templateUrl: './organization-registration.component.html',
  styleUrls: ['./organization-registration.component.css']
})
export class OrganizationRegistrationComponent extends ModifiableResourcesComponent implements OnInit {

  isLinear = true;
  icoForm: FormGroup;
  organization = new Organization();

  icoValidated: boolean;
  loading: boolean;

  constructor(private formBuilder: FormBuilder,
              private router: Router,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.init(false);
  }

  async ngOnInit(): Promise<any> {
    this.icoForm = this.formBuilder.group({
      icoField: [
        null,
        [Validators.required, Validators.minLength(8), Validators.maxLength(8)]
      ],
    });
  }

  public init(icoValidated: boolean): void {
    this.icoValidated = icoValidated;
    this.loading = false;
    super.initGlobal();
  }

  public getRecaptchaActionIcoVerify(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_VERIFY_ICO;
  }

  public getRecaptchaActionRegisterOrganization(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_REGISTER_ORGANIZATION;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.submit(recaptchaAction, value)));
  }

  private submit(recaptchaAction: RecaptchaAction, recaptchaToken: string): void {
    if (recaptchaAction === RecaptchaAction.RECAPTCHA_VERIFY_ICO) {
      this.verifyOrganization(recaptchaToken);
    }

    if (recaptchaAction === RecaptchaAction.RECAPTCHA_REGISTER_ORGANIZATION) {
      this.addOrganization(recaptchaToken);
    }
  }

  private verifyOrganization(recaptchaToken: string): void {
    this.init(false);
    this.loading = true;
    this.organizationService.verifyOrganizationExistence(this.organization, recaptchaToken).subscribe(status => {
      const restGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(status.status);
      if (restGeneralResponse === RestGeneralResponse.ok) {
        this.icoValidated = true;
      } else {
        this.icoValidated = false;
        this.handleError(restGeneralResponse);
      }
      this.loading = false;
    });
  }

  private addOrganization(recaptchaToken: string): void {
    this.init(true);
    this.loading = true;
    this.organizationService.addOrganization(this.organization, recaptchaToken).subscribe(status => {
      const restGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(status.status);
      if (restGeneralResponse === RestGeneralResponse.ok) {
        const organizationId = status.headers.get('Location');
        if (organizationId == null) {
          this.internalException = true;
          this.loading = false;
        } else {
          this.router.navigateByUrl('/organizace/' + organizationId);
        }
      } else {
        this.loading = false;
        this.handleError(restGeneralResponse);
        if (this.notPermitted || this.alreadyRegistered) {
          this.icoForm.controls.icoField.setErrors({incorrect: true});
        }
      }
    });
  }
}


