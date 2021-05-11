import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {Resident} from '../../rest/model/Resident';
import {MatPaginator, MatPaginatorIntl} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Content} from '@angular/compiler/src/render3/r3_ast';
import {OrganizationService} from '../../rest/service/organization.service';
import {PersonTableDataSource} from './PersonTableDataSource';
import {NvflatService} from '../../rest/service/nvflat.service';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from '@angular/material/core';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {MY_FORMATS} from '../../general/components-setting/MyDateFormats';
import {Person} from '../../general/model/Person';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {getCzechPaginatorIntl} from '../../general/components-setting/CzechPaginator';
import {PersonStepperInit} from '../../steppers/person-stepper/PersonStepperInit';
import {PersonType} from '../../general/enum/types/PersonType';
import {Crud} from '../../general/enum/types/Crud';
import {PersonTableInit} from './PersonTableInit';
import {ActorType} from '../../general/enum/types/ActorType';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {HttpResponse} from '@angular/common/http';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';

@Component({
  selector: 'app-person-table',
  templateUrl: './person-table.component.html',
  styleUrls: ['./person-table.component.css'],
  providers: [
    {provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    {provide: MatPaginatorIntl, useValue: getCzechPaginatorIntl()},
  ],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class PersonTableComponent extends ModifiableResourcesComponent implements AfterViewInit, OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatTable) table: MatTable<Content>;
  dataSource: PersonTableDataSource;

  flatResidentTableForResident: boolean;
  flatResidentTableForMember: boolean;

  organizationMemberTableForResident: boolean;
  organizationMemberTableForMember: boolean;

  flatId: string;
  detailId: string;

  organizationId: string;
  committeeId: string;

  personAddFormDisplay: boolean;

  tableName: string;
  displayedColumns: string[] = ['firstName', 'lastName'];
  expandedElement: Resident | null;

  personAddInit: PersonStepperInit;
  personTableInit: PersonTableInit;

  personToDelete: any;
  aresDone: boolean;

  ready: boolean;

  constructor(private flatService: NvflatService,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  @Input()
  set initComponent(personTableInit: PersonTableInit) {
    this.personTableInit = personTableInit;
    this.initAll();
  }

  async ngOnInit(): Promise<any> {
    this.dataSource = new PersonTableDataSource(this.organizationService, this.flatService);

    if (this.flatResidentTableForResident || this.flatResidentTableForMember) {
      this.flatId = this.personTableInit.flatId;
      this.detailId = this.personTableInit.detailId;
    }
  }

  ngAfterViewInit(): void {
    this.sort.sortChange.subscribe(() => this.loadPage());
  }

  initAll(): void {
    this.ready = false;
    this.aresDone = true;

    if (this.personTableInit.personType === PersonType.Resident && this.personTableInit.actorType === ActorType.FlatResident) {
      this.tableName = GeneralComponentTitles.FLATS_RESIDENTS;
      this.flatResidentTableForResident = true;
    }

    if (this.personTableInit.personType === PersonType.Resident && this.personTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.FLATS_RESIDENTS;
      this.flatResidentTableForMember = true;
    }

    if (this.personTableInit.personType === PersonType.Member && this.personTableInit.actorType === ActorType.FlatResident) {
      this.tableName = GeneralComponentTitles.COMMITTEE_MEMBERS;
      this.organizationMemberTableForResident = true;
    }

    if (this.personTableInit.personType === PersonType.Member && this.personTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.COMMITTEE_MEMBERS;
      this.organizationMemberTableForMember = true;
    }

    if (this.flatResidentTableForMember || this.organizationMemberTableForResident || this.organizationMemberTableForMember) {
      this.organizationId = this.personTableInit.organizationId;
      this.committeeId = this.personTableInit.committeeId;
    }

    Promise.resolve().then(() => this.loadPage());
  }

  pageChange(event): void {
    this.loadPage();
  }

  personAdd(): void {
    this.initGlobal();

    this.personAddFormDisplay = !this.personAddFormDisplay;

    if (this.flatResidentTableForResident) {
      this.addResident();
    }

    if (this.organizationMemberTableForMember) {
      this.addMember();
    }
  }

  private addResident(): void {
    this.personAddInit = new PersonStepperInit(Crud.Create, PersonType.Resident, new Person());
    this.personAddInit.detailId = this.detailId;
  }

  private addMember(): void {
    this.personAddInit = new PersonStepperInit(Crud.Create, PersonType.Member, new Person());
    this.personAddInit.committeeId = this.committeeId;
  }

  personModify(element): void {
    this.personAddFormDisplay = true;
    const person = new Person();
    person.firstName = element.firstName;
    person.lastName = element.lastName;
    person.email = element.email;
    person.phone = element.phone;
    person.dateOfBirth = element.dateOfBirth;

    if (this.flatResidentTableForResident) {
      person.personId = element.residentId;
      this.modifyResident(person);
    }

    if (this.organizationMemberTableForMember) {
      person.personId = element.memberId;
      this.modifyMember(person);
    }
  }

  private modifyResident(person: Person): void {
    this.personAddInit = new PersonStepperInit(Crud.Update, PersonType.Resident, person);
    this.personAddInit.detailId = this.detailId;
  }

  private modifyMember(person: Person): void {
    this.personAddInit = new PersonStepperInit(Crud.Update, PersonType.Member, person);
    this.personAddInit.committeeId = this.committeeId;
  }

  public getRecaptchaActionPerson(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_PERSON;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction, element): void {
    this.personToDelete = element;
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe(value => this.submit(value));
  }

  private submit(recaptchaToken: string): void {
    if (this.personToDelete != null) {
      this.deletePerson(recaptchaToken);
    } else {
      this.getMembersFromAres(recaptchaToken);
    }
  }

  private getMembersFromAres(recaptchaToken: string): void {
    this.aresDone = false;
    this.initGlobal();

    if (this.organizationMemberTableForMember) {
      this.organizationService.getMembersFromAresRecord(this.organizationId, recaptchaToken)
        .toPromise()
        .then(this.personCommonResponse.bind(this))
        .finally(() => this.aresDone = true);
    }
  }

  private deletePerson(recaptchaToken: string): void {
    this.initGlobal();

    if (this.flatResidentTableForResident) {
      this.deleteResident(this.personToDelete.residentId, recaptchaToken);
    }

    if (this.organizationMemberTableForMember) {
      this.deleteMember(this.personToDelete.memberId, recaptchaToken);
    }
  }

  private deleteResident(residentId, recaptchaToken: string): void {
    this.flatService.deleteResident(residentId, recaptchaToken)
      .subscribe(this.personCommonResponse.bind(this));
  }

  private deleteMember(memberId, recaptchaToken: string): void {
    this.organizationService.deleteMember(memberId, recaptchaToken)
      .subscribe(this.personCommonResponse.bind(this));
  }

  private personCommonResponse(response: HttpResponse<any>): void {
    const commonPersonGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (commonPersonGeneralResponse === RestGeneralResponse.ok) {
      this.loadPage();
    } else {
      this.handleError(commonPersonGeneralResponse);
    }
  }

  personUpdated(event): void {
    this.personAddFormDisplay = false;
    this.loadPage();
  }

  loadPage(): void {
    this.ready = true;
    this.initGlobal();
    this.dataSource.content.next(null);

    if (this.flatResidentTableForResident) {
      this.dataSource.getFlatResidentsByFlatResident(this.organizationId,
        this.flatId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        this.sort.active,
        this.sort.direction);
    }

    if (this.flatResidentTableForMember) {
      this.dataSource.getFlatResidentsByOrganizationMember(this.organizationId,
        this.flatId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        this.sort.active,
        this.sort.direction);
    }

    if (this.organizationMemberTableForResident || this.organizationMemberTableForMember) {
      this.dataSource.getOrganizationMembers(this.committeeId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        this.sort.active,
        this.sort.direction);
    }
  }
}
