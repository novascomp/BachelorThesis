import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {OrganizationService} from '../../rest/service/organization.service';
import {LightweightComponent} from '../../rest/model/LightweightComponent';
import {ComponentStepperInit} from './ComponentStepperInit';
import {Crud} from '../../general/enum/types/Crud';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {OnExecuteData, ReCaptchaV3Service} from 'ng-recaptcha';
import {Subscription} from 'rxjs';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';

@Component({
  selector: 'app-component-stepper',
  templateUrl: './component-stepper.component.html',
  styleUrls: ['./component-stepper.component.css']
})
export class ComponentStepperComponent extends ModifiableResourcesComponent implements OnInit, OnDestroy {

  isLinear = false;
  componentGroup: FormGroup;
  lightweightComponent: LightweightComponent;

  componentStepperInit: ComponentStepperInit;
  organizationId: string;

  private subscription: Subscription;
  invalidForm: boolean;

  constructor(private formBuilder: FormBuilder,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.lightweightComponent = new LightweightComponent();
  }

  @Input()
  set initComponent(componentStepperInit: ComponentStepperInit) {
    this.componentStepperInit = componentStepperInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  ngOnInit(): void {
    this.componentGroup = this.formBuilder.group({
      component: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(32)]
      ],
    });

    this.subscription = this.recaptchaV3Service.onExecute
      .subscribe((data: OnExecuteData) => {
        this.submit(data.token);
      });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  initAll(): void {

    if (this.componentStepperInit.crud === Crud.Create) {
      this.lightweightComponent = new LightweightComponent();
      this.isLinear = true;
    }

    this.organizationId = this.componentStepperInit.organizationId;
  }

  public getRecaptchaActionComponent(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_COMPONENT;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe();
  }

  private submit(recaptchaToken: string): void {
    this.initGlobal();

    if (this.componentGroup.valid) {
      this.invalidForm = false;
      if (this.componentStepperInit.crud === Crud.Create) {
        this.addComponent(recaptchaToken);
      }
    } else {
      this.invalidForm = true;
    }
  }

  addComponent(recaptchaToken: string): void {
    this.organizationService.addOrganizationCategory(this.organizationId, this.lightweightComponent, recaptchaToken)
      .subscribe(this.crudCommonResponse.bind(this));
  }

  private crudCommonResponse(response: HttpResponse<any>): void {
    super.initGlobal();
    const crudGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (crudGeneralResponse === RestGeneralResponse.ok) {
      this.afterDone.emit(this.lightweightComponent);
    } else {
      this.handleError(crudGeneralResponse);
    }
  }
}
