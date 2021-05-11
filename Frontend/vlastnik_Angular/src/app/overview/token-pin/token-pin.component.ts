import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Token} from '../../rest/model/Token';
import {NvflatService} from '../../rest/service/nvflat.service';
import {Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {OrganizationService} from '../../rest/service/organization.service';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';

@Component({
  selector: 'app-token-pin',
  templateUrl: './token-pin.component.html',
  styleUrls: ['./token-pin.component.css']
})
export class TokenPinComponent extends ModifiableResourcesComponent implements OnInit {

  isLinear = true;
  tokenForm: FormGroup;
  token = new Token();

  invalidToken: boolean;
  relationshipExists: boolean;

  constructor(private formBuilder: FormBuilder,
              private router: Router,
              private http: HttpClient,
              private nvflatService: NvflatService,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  async ngOnInit(): Promise<any> {
    this.tokenForm = this.formBuilder.group({
      tokenField: [
        null,
        [Validators.required, Validators.minLength(23), Validators.maxLength(23)]
      ],
    });
  }

  init(): void {
    super.initGlobal();
    this.invalidToken = false;
    this.relationshipExists = false;
  }

  public getRecaptchaActionPinToken(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_PIN_TOKEN;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.pinToken(value)));
  }

  pinToken(recaptchaToken: string): void {
    this.init();
    if (this.tokenForm.valid) {
      if (this.token.key.includes('NFLAT')) {
        this.pinFlatToken(recaptchaToken);
      } else if (this.token.key.includes('NHOME')) {
        this.pinOrganizationToken(recaptchaToken);
      } else {
        this.invalidToken = true;
        this.tokenForm.controls.tokenField.setErrors({incorrect: true});
      }
    }
  }

  pinFlatToken(recaptchaToken: string): void {
    this.invalidToken = false;
    this.relationshipExists = false;
    this.nvflatService.pinFlat(this.token, recaptchaToken)
      .subscribe(status => this.commonResponse(status));
  }

  private pinOrganizationToken(recaptchaToken: string): void {
    this.invalidToken = false;
    this.relationshipExists = false;
    this.organizationService.pinOrganization(this.token, recaptchaToken)
      .subscribe(status => this.commonResponse(status));
  }

  commonResponse(status): void {

    const restGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(status.status);
    if (restGeneralResponse === RestGeneralResponse.ok) {
      if (this.token.key.includes('NFLAT')) {
        const flatId = status.headers.get('Location');
        this.router.navigateByUrl('/moje/jednotky/' + flatId);
      }
      if (this.token.key.includes('NHOME')) {
        const organizationId = status.headers.get('Location');
        this.router.navigateByUrl('/organizace/' + organizationId);
      }
    } else {
      this.handleError(restGeneralResponse);
      if (this.badRequest || this.notFound) {
        this.invalidToken = true;
        this.tokenForm.controls.tokenField.setErrors({incorrect: true});
      }
      if (this.alreadyRegistered) {
        this.relationshipExists = true;
        this.tokenForm.controls.tokenField.setErrors({incorrect: true});
      }
    }
  }
}
