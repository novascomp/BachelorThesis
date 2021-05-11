import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatPaginatorIntl} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Content} from '@angular/compiler/src/render3/r3_ast';
import {OrganizationService} from '../../rest/service/organization.service';
import {NvflatService} from '../../rest/service/nvflat.service';
import {ActorType} from '../../general/enum/types/ActorType';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {getCzechPaginatorIntl} from '../../general/components-setting/CzechPaginator';
import {Document} from '../../rest/model/Document';
import {LightweightComponent} from '../../rest/model/LightweightComponent';
import {DocumentTableDataSource} from './DocumentTableDataSource';
import {FormControl} from '@angular/forms';
import {DocumentTableInit} from './DocumentTableInit';
import {DocumentType} from '../../general/enum/types/DocumentType';
import {Crud} from '../../general/enum/types/Crud';
import {DocumentUploaderInit} from '../../uploaders/document-uploader/DocumentUploaderInit';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from '@angular/material/core';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {MY_FORMATS} from '../../general/components-setting/MyDateFormats';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {CategoryTableInit} from '../category-table/CategoryTableInit';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ReCaptchaV3Service} from 'ng-recaptcha';

@Component({
  selector: 'app-document-table',
  templateUrl: './document-table.component.html',
  styleUrls: ['./document-table.component.css'],
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
export class DocumentTableComponent extends ModifiableResourcesComponent implements AfterViewInit, OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatTable) table: MatTable<Content>;
  dataSource: DocumentTableDataSource;

  flatDocumentTableForResident: boolean;
  flatDocumentTableForMember: boolean;

  organizationDocumentTableForResident: boolean;
  organizationDocumentTableForMember: boolean;

  detailId: string;
  organizationId: string;

  expandedElement: Document | null;

  categoryTableDisplay: boolean;
  documentAddFormDisplay: boolean;

  tableName: string;
  filterName: string;
  componentName: string;
  displayedColumns: string[] = ['heading', 'date'];
  componentsSelect = new FormControl();

  components: LightweightComponent[];
  selected: string[];

  documentTableInit: DocumentTableInit;
  categoryTableInit: CategoryTableInit;
  documentUploaderInit: DocumentUploaderInit;

  aresDone: boolean;
  deleteDone: boolean;

  selectedFlatId: string;
  onlyForThisFlat: boolean;

  componentsLoaded: boolean;

  ready: boolean;

  constructor(private organizationService: OrganizationService,
              private flatService: NvflatService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.deleteDone = true;
    this.onlyForThisFlat = true;
    this.selectedFlatId = 'all';
  }

  @Input()
  set initComponent(documentTableInit: DocumentTableInit) {
    this.ready = false;
    this.documentTableInit = documentTableInit;
    this.initAll();
  }

  async ngOnInit(): Promise<any> {
    this.dataSource = new DocumentTableDataSource(this.organizationService, this.flatService);
    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      this.selectedFlatId = this.documentTableInit.flatId;
    }
  }

  ngAfterViewInit(): void {
    this.sort.sortChange.subscribe(() => this.loadPage());
  }

  initAll(): void {
    this.aresDone = true;
    this.deleteDone = true;
    this.detailId = this.documentTableInit.detailId;
    this.organizationId = this.documentTableInit.organizationId;

    if (this.documentTableInit.documentType === DocumentType.Flat && this.documentTableInit.actorType === ActorType.FlatResident) {
      this.tableName = GeneralComponentTitles.FLAT_DOCUMENT_TABLE_NAME;
      this.filterName = GeneralComponentTitles.FILTER_NAME_BY_FLATS;
      this.componentName = GeneralComponentTitles.PART_OF_FLATS;
      this.flatDocumentTableForResident = true;
      this.documentUploaderInit = new DocumentUploaderInit(Crud.Create, DocumentType.Flat, ActorType.FlatResident);
      this.documentUploaderInit.detailId = this.detailId;
      this.documentUploaderInit.flatIdentifier = this.documentTableInit.flatIdentifier;
    }

    if (this.documentTableInit.documentType === DocumentType.Flat && this.documentTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.COMMITTEE_DOCUMENT_TABLE_NAME;
      this.filterName = GeneralComponentTitles.FILTER_NAME_BY_FLATS;
      this.componentName = GeneralComponentTitles.PART_OF_FLATS;
      this.flatDocumentTableForMember = true;
      this.documentUploaderInit = new DocumentUploaderInit(Crud.Create, DocumentType.Flat, ActorType.OrganizationMember);
      this.documentUploaderInit.detailId = this.detailId;
      this.documentUploaderInit.flatIdentifier = this.documentTableInit.flatIdentifier;
    }

    if (this.documentTableInit.documentType === DocumentType.Home && this.documentTableInit.actorType === ActorType.FlatResident) {
      this.tableName = GeneralComponentTitles.ORGANIZATION_DOCUMENT_TABLE_NAME;
      this.filterName = GeneralComponentTitles.FILTER_NAME_BY_CATEGORIES;
      this.componentName = GeneralComponentTitles.PART_OF_CATEGORIES;
      this.organizationDocumentTableForResident = true;
      this.documentUploaderInit = new DocumentUploaderInit(Crud.Create, DocumentType.Home, ActorType.FlatResident);
    }

    if (this.documentTableInit.documentType === DocumentType.Home && this.documentTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.ORGANIZATION_DOCUMENT_TABLE_NAME;
      this.filterName = GeneralComponentTitles.FILTER_NAME_BY_CATEGORIES;
      this.componentName = GeneralComponentTitles.PART_OF_CATEGORIES;
      this.organizationDocumentTableForMember = true;
      this.documentUploaderInit = new DocumentUploaderInit(Crud.Create, DocumentType.Home, ActorType.OrganizationMember);
      this.categoryTableInit = new CategoryTableInit(this.organizationId, ActorType.OrganizationMember);
    }

    this.documentUploaderInit.organizationId = this.organizationId;
    const componentsLoaded = this.loadComponents();
    componentsLoaded.then(this.loadPage.bind(this));
  }

  pageChange(event): void {
    this.loadPage();
  }

  onlyForThisFlatChange(): void {

    if (this.onlyForThisFlat) {
      this.selectedFlatId = this.documentTableInit.flatId;
    } else {
      this.selectedFlatId = 'all';
    }

    this.loadPage();
  }

  selectionChange(event): void {
    this.loadPage();
  }

  documentAdd(): void {
    this.documentAddFormDisplay = !this.documentAddFormDisplay;
  }

  categoryTable(): void {
    this.categoryTableDisplay = !this.categoryTableDisplay;
  }

  documentUpdated(event): void {
    const componentsLoaded = this.loadComponents();
    componentsLoaded.then(this.loadPage.bind(this));
    this.documentAddFormDisplay = false;
  }

  categoryUpdated(event): void {
    const componentsLoaded = this.loadComponents();
    componentsLoaded.then(this.loadPage.bind(this));
    this.documentAddFormDisplay = false;
  }

  public getRecaptchaActionAres(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_ARES;
  }

  public getRecaptchaActionDocument(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_DOCUMENT;
  }

  public preSubmitForm(documentId, recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.submit(recaptchaAction, documentId, value)));
  }

  private submit(captchaAction: RecaptchaAction, documentId, recaptchaToken: string): void {

    if (captchaAction === RecaptchaAction.RECAPTCHA_ARES) {
      this.getAresRecord(recaptchaToken);
    }

    if (captchaAction === RecaptchaAction.RECAPTCHA_DOCUMENT) {
      this.deleteDocument(documentId, recaptchaToken);
    }
  }

  private getAresRecord(recaptchaToken: string): void {
    this.aresDone = false;
    this.initGlobal();

    if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
      this.organizationService.generateAresRecord(this.organizationId, recaptchaToken)
        .toPromise()
        .then(this.commonSubmitResponse.bind(this))
        .finally(() => this.aresDone = true);
    }
  }

  private deleteDocument(documentId, recaptchaToken: string): void {
    this.deleteDone = false;
    this.initGlobal();

    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      this.flatService.deleteFlatDocumentById(this.organizationId, documentId, this.detailId, recaptchaToken)
        .toPromise()
        .then(this.commonSubmitResponse.bind(this))
        .finally(() => this.deleteDone = true);
    }

    if (this.organizationDocumentTableForMember) {
      this.organizationService.deleteOrganizationDocumentById(this.organizationId, documentId, recaptchaToken)
        .toPromise()
        .then(this.commonSubmitResponse.bind(this))
        .finally(() => this.deleteDone = true);
    }
  }

  private commonSubmitResponse(componentsResponse: HttpResponse<any>): void {
    const deleteDocumentGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(componentsResponse.status);
    if (deleteDocumentGeneralResponse === RestGeneralResponse.ok) {
      this.dataSource.content.next(null);
      const componentsLoaded = this.loadComponents();
      componentsLoaded.then(this.loadPage.bind(this));
    } else {
      this.handleError(deleteDocumentGeneralResponse);
    }
  }

  private loadComponents(): Promise<any> {

    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      return this.flatService.getFlatComponentsPersonal(this.organizationId, this.detailId).toPromise()
        .then(this.processComponentsResponse.bind(this))
        .finally(this.interpretComponentsResponse.bind(this));
    }

    if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
      return this.organizationService.getAllOrganizationCategories(this.organizationId).toPromise()
        .then(this.processComponentsResponse.bind(this))
        .finally(this.interpretComponentsResponse.bind(this));
    }
  }

  private processComponentsResponse(componentsResponse: HttpResponse<any>): void {
    const componentsGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(componentsResponse.status);
    if (componentsGeneralResponse === RestGeneralResponse.ok) {
      this.components = componentsResponse.body.content;
      this.selected = [];
      for (const component of this.components) {
        this.selected.push(component.categoryId);
      }
      this.componentsSelect.setValue(this.selected);
      this.componentsLoaded = true;
    } else {
      this.componentsLoaded = false;
      this.handleError(componentsGeneralResponse);
    }
  }

  private interpretComponentsResponse(): void {
    if (this.componentsLoaded) {
      for (const component of this.components) {
        component.componentId = component.categoryId;
      }
    }
  }

  loadPage(): void {
    this.initGlobal();
    this.ready = true;
    this.dataSource.content.next(null);

    let sortColumn = this.sort.active;
    if (sortColumn === 'date') {
      sortColumn = 'general.date';
    }

    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      if (this.componentsLoaded) {
        this.loadFlatDocuments(sortColumn);
      }
    }

    if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
      if (this.componentsLoaded) {
        this.loadOrganizationDocuments(sortColumn);
      }
    }
  }

  private loadFlatDocuments(sortColumn: string): void {
    this.dataSource
      .getFlatDocuments(this.organizationId,
        this.componentsSelect.value,
        this.selectedFlatId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        sortColumn,
        this.sort.direction);
  }

  private loadOrganizationDocuments(sortColumn: string): void {
    this.dataSource
      .getOrganizationDocuments(this.organizationId,
        this.componentsSelect.value,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        sortColumn,
        this.sort.direction);
  }
}
