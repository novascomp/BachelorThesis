import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatPaginator, MatPaginatorIntl} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Content} from '@angular/compiler/src/render3/r3_ast';
import {OrganizationService} from '../../rest/service/organization.service';
import {OrganizationFlatTableDataSource} from './OrganizationFlatTableDataSource';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {OrganizationFlatTableInit} from './OrganizationFlatTableInit';
import {getCzechPaginatorIntl} from '../../general/components-setting/CzechPaginator';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';

@Component({
  selector: 'app-organization-flat',
  templateUrl: './organization-flat-table.component.html',
  styleUrls: ['./organization-flat-table.component.css'],
  providers: [
    {provide: MatPaginatorIntl, useValue: getCzechPaginatorIntl()},
  ],
})
export class OrganizationFlatTableComponent extends ModifiableResourcesComponent implements AfterViewInit, OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatTable) table: MatTable<Content>;
  dataSource: OrganizationFlatTableDataSource;

  organizationFlatTableInit: OrganizationFlatTableInit;

  organizationId: string;
  tableName: string;

  submitted: boolean;

  displayedColumns = ['identifier', 'size', 'commonShareSize', 'flatPortal'];

  constructor(private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  @Input()
  set initComponent(organizationFlatTableInit: OrganizationFlatTableInit) {
    this.organizationFlatTableInit = organizationFlatTableInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  async ngOnInit(): Promise<any> {
    this.dataSource = new OrganizationFlatTableDataSource(this.organizationService);
    this.tableName = GeneralComponentTitles.ORGANIZATION_FLATS;
  }

  ngAfterViewInit(): void {
    this.sort.sortChange.subscribe(() => this.loadPage());
  }

  initAll(): void {
    this.organizationId = this.organizationFlatTableInit.organizationId;
    this.submitted = false;
    Promise.resolve().then(() => this.loadPage());
  }

  pageChange(event): void {
    this.loadPage();
  }

  public getRecaptchaActionFlatsUpload(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_FLATS_UPLOAD;
  }

  public getRecaptchaActionFlatsDelete(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_FLATS_DELETE;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.submit(recaptchaAction, value)));
  }

  submit(recaptchaAction: RecaptchaAction, recaptchaToken: string): void {
    this.initGlobal();
    this.submitted = true;
    if (recaptchaAction === RecaptchaAction.RECAPTCHA_FLATS_UPLOAD) {
      this.organizationService.generateFlats(this.organizationId, recaptchaToken)
        .subscribe(this.generateResponse.bind(this));
    }

    if (recaptchaAction === RecaptchaAction.RECAPTCHA_FLATS_DELETE) {
      this.organizationService.deleteFlats(this.organizationId, recaptchaToken)
        .subscribe(this.generateResponse.bind(this));
    }
  }

  private generateResponse(response: HttpResponse<any>): void {
    const uploadGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (uploadGeneralResponse === RestGeneralResponse.ok) {
      this.afterDone.emit(response);
    } else {
      this.handleError(uploadGeneralResponse);
    }

    this.submitted = false;
  }

  loadPage(): void {
    this.initGlobal();
    this.dataSource.content.next(null);

    let sortColumn = this.sort.active;
    if (sortColumn === 'commonShareSize') {
      sortColumn = 'detailList.commonShareSize';
    }

    if (sortColumn === 'size') {
      sortColumn = 'detailList.size';
    }

    this.dataSource.getFlats(this.organizationId, this.paginator.pageIndex, this.paginator.pageSize, sortColumn, this.sort.direction);
  }

  getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }
}
