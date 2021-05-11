import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatPaginator, MatPaginatorIntl} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Content} from '@angular/compiler/src/render3/r3_ast';
import {OrganizationService} from '../../rest/service/organization.service';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {CategoryTableDataSource} from './CategoryTableDataSource';
import {CategoryTableInit} from './CategoryTableInit';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ComponentStepperInit} from '../../steppers/component-stepper/ComponentStepperInit';
import {Crud} from '../../general/enum/types/Crud';
import {getCzechPaginatorIntl} from '../../general/components-setting/CzechPaginator';

@Component({
  selector: 'app-categories-table',
  templateUrl: './category-table.component.html',
  styleUrls: ['./category-table.component.css'],
  providers: [
    {provide: MatPaginatorIntl, useValue: getCzechPaginatorIntl()},
  ],
})
export class CategoryTableComponent extends ModifiableResourcesComponent implements AfterViewInit, OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatTable) table: MatTable<Content>;
  dataSource: CategoryTableDataSource;

  organizationId: string;
  tableName: string;

  categoryTableInit: CategoryTableInit;
  componentStepperInit: ComponentStepperInit;

  categoryAddFormDisplay: boolean;

  displayedColumns = ['text', 'categoryId'];

  constructor(private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  @Input()
  set initComponent(categoryTableInit: CategoryTableInit) {
    this.categoryTableInit = categoryTableInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  async ngOnInit(): Promise<any> {
    this.dataSource = new CategoryTableDataSource(this.organizationService);
    this.tableName = GeneralComponentTitles.CATEGORIES_TABLE_TITLE;
  }

  ngAfterViewInit(): void {
    this.sort.sortChange.subscribe(() => this.loadPage());
  }

  initAll(): void {
    this.organizationId = this.categoryTableInit.organizationId;
    this.componentStepperInit = new ComponentStepperInit(Crud.Create, this.organizationId);
    Promise.resolve().then(() => this.loadPage());
  }

  pageChange(event): void {
    this.loadPage();
  }

  displayCategoryAddForm(): void {
    this.categoryAddFormDisplay = !this.categoryAddFormDisplay;
  }

  categoriesUpdated(event): void {
    this.afterDone.emit(true);
    this.categoryAddFormDisplay = false;
    this.loadPage();
  }

  public getRecaptchaActionComponent(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_COMPONENT;
  }

  public preSubmitFormDeleteCategory(recaptchaAction: RecaptchaAction, categoryId): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.deleteCategory(value, categoryId)));
  }

  private deleteCategory(recaptchaToken: string, categoryId): void {
    this.initGlobal();
    this.organizationService.deleteOrganizationCategory(this.organizationId, categoryId, recaptchaToken)
      .subscribe(this.categoryCommonResponse.bind(this));
  }

  private categoryCommonResponse(response: HttpResponse<any>): void {
    const commonCategoryGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (commonCategoryGeneralResponse === RestGeneralResponse.ok) {
      this.afterDone.emit(true);
      this.loadPage();
    } else {
      this.handleError(commonCategoryGeneralResponse);
    }
  }

  loadPage(): void {
    this.dataSource.content.next(null);
    const sortColumn = this.sort.active;
    this.dataSource
      .getOrganizationCategories(this.organizationId, this.paginator.pageIndex, this.paginator.pageSize, sortColumn, this.sort.direction);
  }

  getDefaultCategoryName(): string {
    return GeneralComponentTitles.DEFAULT_CATEGORY_NAME;
  }

  getaAresCategoryName(): string {
    return GeneralComponentTitles.ARES_CATEGORY_NAME;
  }
}
