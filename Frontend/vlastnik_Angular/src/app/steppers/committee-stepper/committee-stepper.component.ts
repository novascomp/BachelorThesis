import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Crud} from '../../general/enum/types/Crud';
import {CommitteeStepperInit} from './CommitteeStepperInit';
import {Committee} from '../../rest/model/Committee';
import {OrganizationService} from '../../rest/service/organization.service';
import {Organization} from '../../rest/model/Organization';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';

@Component({
  selector: 'app-committee-stepper',
  templateUrl: './committee-stepper.component.html',
  styleUrls: ['./committee-stepper.component.css']
})
export class CommitteeStepperComponent extends ModifiableResourcesComponent implements OnInit {

  isLinear = false;
  emailFormGroup: FormGroup;
  phoneFormGroup: FormGroup;

  minDate: Date;
  maxDate: Date;

  organizationId: string;

  committee: Committee;
  invalidForm: boolean;

  committeeStepperInit: CommitteeStepperInit;

  constructor(private formBuilder: FormBuilder,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.committee = new Committee();
  }

  @Input()
  set initComponent(committeeStepperInit: CommitteeStepperInit) {
    this.committeeStepperInit = committeeStepperInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  ngOnInit(): void {
    this.emailFormGroup = this.formBuilder.group({
      email: [
        null,
        [Validators.required, Validators.email]
      ],
    });
    this.phoneFormGroup = this.formBuilder.group({
      phone: [
        null,
        [Validators.required, Validators.pattern('^[+]?(?:[0-9] ?){8,11}[0-9]$')]
      ],
    });
  }

  initAll(): void {
    this.committee = this.committeeStepperInit.committee;
    this.isLinear = false;
    this.organizationId = this.committeeStepperInit.organizationId;
  }

  public getRecaptchaActionCommittee(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_COMMITTEE;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe(this.submit.bind(this));
  }

  private submit(recaptchaToken: string): void {
    if (this.emailFormGroup.valid && this.phoneFormGroup.valid) {
      this.invalidForm = false;
      this.updateCommittee(recaptchaToken);
    } else {
      this.invalidForm = true;
    }
  }

  private updateCommittee(recaptchaToken: string): void {
    const organization = new Organization();
    organization.organizationId = this.organizationId;
    this.committee.organization = organization;
    this.organizationService.updateOrganizationCommittee(this.committee, recaptchaToken).subscribe(this.crudCommonResponse.bind(this));
  }

  private crudCommonResponse(response: HttpResponse<any>): void {
    super.initGlobal();
    const crudGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (crudGeneralResponse === RestGeneralResponse.ok) {
      this.afterDone.emit(this.committee);
    } else {
      this.handleError(crudGeneralResponse);
    }
  }
}
