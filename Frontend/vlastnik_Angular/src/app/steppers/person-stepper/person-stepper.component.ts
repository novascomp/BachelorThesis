import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from '@angular/material/core';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {OrganizationService} from '../../rest/service/organization.service';
import {NvflatService} from '../../rest/service/nvflat.service';
import {Resident} from '../../rest/model/Resident';
import {Person} from '../../general/model/Person';
import {MY_FORMATS} from '../../general/components-setting/MyDateFormats';
import {Crud} from '../../general/enum/types/Crud';
import {PersonType} from '../../general/enum/types/PersonType';
import {PersonStepperInit} from './PersonStepperInit';
import {Member} from '../../rest/model/Member';
import {MatStepper} from '@angular/material/stepper';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';

@Component({
  selector: 'app-person-add-stepper',
  templateUrl: './person-stepper.component.html',
  styleUrls: ['./person-stepper.component.css'],
  providers: [
    {provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    {provide: MAT_DATE_LOCALE, useValue: 'cs-CZ'}
  ],
})
export class PersonStepperComponent extends ModifiableResourcesComponent implements OnInit {
  @ViewChild('stepper') stepper: MatStepper;

  isLinear = false;
  firstNameFormGroup: FormGroup;
  lastNameFormGroup: FormGroup;
  emailFormGroup: FormGroup;
  phoneFormGroup: FormGroup;
  dateOfBirthFormGroup: FormGroup;

  minDate: Date;
  maxDate: Date;

  detailId: string;
  committeeId: string;

  organizationMemberAdd: boolean;

  person: Person;

  crudUpdate: boolean;

  personAddInit: PersonStepperInit;

  invalidForm: boolean;

  constructor(private formBuilder: FormBuilder,
              private flatService: NvflatService,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    const currentYear = new Date().getFullYear();
    this.minDate = new Date(currentYear - 125, 0, 1);
    this.maxDate = new Date();
  }

  @Input()
  set initComponent(personAddInit: PersonStepperInit) {
    this.personAddInit = personAddInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  ngOnInit(): void {
    this.firstNameFormGroup = this.formBuilder.group({
      firstName: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(32)]
      ],
    });
    this.lastNameFormGroup = this.formBuilder.group({
      lastName: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(32)]
      ],
    });
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
    this.dateOfBirthFormGroup = this.formBuilder.group({
      dateOfBirth: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(32)]
      ],
    });
  }

  initAll(): void {
    super.initGlobal();
    this.crudUpdate = false;

    if (this.personAddInit.crud === Crud.Create) {
      this.person = new Person();
      this.isLinear = true;
    }

    if (this.personAddInit.crud === Crud.Update) {
      this.person = this.personAddInit.person;
      this.isLinear = false;
      this.crudUpdate = true;
    }

    if (this.personAddInit.personType === PersonType.Resident) {
      this.detailId = this.personAddInit.detailId;
    }

    if (this.personAddInit.personType === PersonType.Member) {
      this.committeeId = this.personAddInit.committeeId;
    }
  }

  public getRecaptchaActionPerson(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_PERSON;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.submit(value)));
  }

  private submit(recaptchaToken: string): void {

    if (this.firstNameFormGroup.invalid ||
      this.lastNameFormGroup.invalid ||
      this.emailFormGroup.invalid ||
      this.phoneFormGroup.invalid ||
      this.dateOfBirthFormGroup.invalid) {
      this.invalidForm = true;
    } else {
      this.invalidForm = false;
      if (this.personAddInit.personType === PersonType.Resident && this.personAddInit.crud === Crud.Create) {
        this.addResident(recaptchaToken);
      }

      if (this.personAddInit.personType === PersonType.Resident && this.personAddInit.crud === Crud.Update) {
        this.updateResident(this.person, recaptchaToken);
      }

      if (this.personAddInit.personType === PersonType.Member && this.personAddInit.crud === Crud.Create) {
        this.addMember(recaptchaToken);
      }

      if (this.personAddInit.personType === PersonType.Member && this.personAddInit.crud === Crud.Update) {
        this.updateMember(this.person, recaptchaToken);
      }
    }
  }

  private addResident(recaptchaToken: string): void {
    const resident = new Resident();
    this.crudCommonResident(resident);
    resident.requiredFlatDetail = this.detailId;

    this.flatService.addResident(resident, recaptchaToken).subscribe(this.crudCommonResponse.bind(this));
  }

  private updateResident(person: Person, recaptchaToken: string): void {
    const resident = new Resident();
    this.crudCommonResident(resident);
    resident.residentId = person.personId;

    this.flatService.updateResident(resident, recaptchaToken).subscribe(this.crudCommonResponse.bind(this));
  }

  private addMember(recaptchaToken: string): void {
    const member = new Member();
    this.crudCommonMember(member);
    member.requiredCommittee = this.committeeId;

    this.organizationService.addMember(member, recaptchaToken).subscribe(this.crudCommonResponse.bind(this));
  }

  private updateMember(person: Person, recaptchaToken: string): void {
    const member = new Member();
    this.crudCommonMember(member);
    member.memberId = person.personId;

    this.organizationService.updateMember(member, recaptchaToken).subscribe(this.crudCommonResponse.bind(this));
  }

  private crudCommonResponse(response: HttpResponse<any>): void {
    super.initGlobal();
    const crudGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (crudGeneralResponse === RestGeneralResponse.ok) {
      this.afterDone.emit(this.person);
    } else {
      this.handleError(crudGeneralResponse);
    }
  }

  private crudCommonResident(resident: Resident): void {
    resident.firstName = this.person.firstName;
    resident.lastName = this.person.lastName;
    resident.email = this.person.email;
    resident.phone = this.person.phone;
    resident.dateOfBirth = this.person.dateOfBirth;
  }


  private crudCommonMember(member: Member): void {
    member.firstName = this.person.firstName;
    member.lastName = this.person.lastName;
    member.email = this.person.email;
    member.phone = this.person.phone;
    member.dateOfBirth = this.person.dateOfBirth;
  }

}
