import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {OrganizationService} from '../../rest/service/organization.service';
import {MatPaginator, MatPaginatorIntl} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTable} from '@angular/material/table';
import {Content} from '@angular/compiler/src/render3/r3_ast';
import {TokenTableDataSource} from './TokenTableDataSource';
import {TokenTableInit} from './TokenTableInit';
import {ActorType} from '../../general/enum/types/ActorType';
import {TokenType} from '../../general/enum/types/TokenType';
import {getCzechPaginatorIntl} from '../../general/components-setting/CzechPaginator';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {HttpResponse} from '@angular/common/http';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';

@Component({
  selector: 'app-token-table',
  templateUrl: './token-table.component.html',
  styleUrls: ['./token-table.component.css'],
  providers: [
    {provide: MatPaginatorIntl, useValue: getCzechPaginatorIntl()},
  ],
})
export class TokenTableComponent extends ModifiableResourcesComponent implements AfterViewInit, OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatTable) table: MatTable<Content>;
  dataSource: TokenTableDataSource;
  organizationId: string;
  flatId: string;

  flatTokensView: boolean;
  organizationTokensView: boolean;

  tableName: string;

  flatTokensTableInit: TokenTableInit;

  displayedColumns: string[] = ['key', 'mapped', 'tokenId'];

  constructor(private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  @Input()
  set initComponent(flatTokensTableInit: TokenTableInit) {
    this.flatTokensTableInit = flatTokensTableInit;
    this.initAll();
  }

  async ngOnInit(): Promise<any> {
    this.dataSource = new TokenTableDataSource(this.organizationService);
  }

  ngAfterViewInit(): void {
    this.sort.sortChange.subscribe(() => this.loadPage());
  }

  initAll(): void {

    super.initGlobal();
    this.organizationId = this.flatTokensTableInit.organizationId;

    if (this.flatTokensTableInit.tokenType === TokenType.Flat &&
      this.flatTokensTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.FLAT_TOKENS_TABLE_NAME;
      this.flatTokensView = true;
      this.flatId = this.flatTokensTableInit.flatId;
    }

    if (this.flatTokensTableInit.tokenType === TokenType.Organization &&
      this.flatTokensTableInit.actorType === ActorType.OrganizationMember) {
      this.tableName = GeneralComponentTitles.COMMITTEE_TOKENS_TABLE_NAME;
      this.organizationTokensView = true;
    }

    Promise.resolve().then(() => this.loadPage());
  }

  pageChange(event): void {
    this.loadPage();
  }

  public getRecaptchaActionToken(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_TOKEN;
  }

  public preSubmitFormAddToken(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe(this.addToken.bind(this));
  }

  public preSubmitFormDeleteToken(recaptchaAction: RecaptchaAction, tokenId): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.deleteToken(value, tokenId)));
  }

  addToken(recaptchaToken: string): void {
    this.initGlobal();

    if (this.flatTokensView) {
      this.addFlatToken(recaptchaToken);
    }

    if (this.organizationTokensView) {
      this.addOrganizationToken(recaptchaToken);
    }
  }

  deleteToken(recaptchaToken: string, tokenId): void {
    this.initGlobal();

    if (this.flatTokensView) {
      this.deleteFlatToken(recaptchaToken, tokenId);
    }

    if (this.organizationTokensView) {
      this.deleteOrganizationToken(recaptchaToken, tokenId);
    }
  }

  private addFlatToken(recaptchaToken: string): void {
    this.organizationService.addFlatToken(this.organizationId, this.flatId, recaptchaToken)
      .subscribe(this.tokenCommonResponse.bind(this));
  }

  private addOrganizationToken(recaptchaToken: string): void {
    this.organizationService.addOrganizationToken(this.organizationId, recaptchaToken)
      .subscribe(this.tokenCommonResponse.bind(this));
  }

  private deleteFlatToken(recaptchaToken: string, tokenId): void {
    this.organizationService.deleteFlatToken(this.organizationId, this.flatId, tokenId, recaptchaToken)
      .subscribe(this.tokenCommonResponse.bind(this));
  }

  private deleteOrganizationToken(recaptchaToken: string, tokenId): void {
    this.organizationService.deleteOrganizationToken(tokenId, recaptchaToken)
      .subscribe(this.tokenCommonResponse.bind(this));
  }

  private tokenCommonResponse(response: HttpResponse<any>): void {
    const commonTokenGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (commonTokenGeneralResponse === RestGeneralResponse.ok) {
      this.loadPage();
    } else {
      this.handleError(commonTokenGeneralResponse);
    }
  }

  loadPage(): void {
    this.initGlobal();

    let sortColumn = this.sort.active;
    if (sortColumn === 'date') {
      sortColumn = 'general.date';
    }

    if (this.flatTokensView) {
      this.dataSource.getFlatTokens(
        this.organizationId,
        this.flatId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        sortColumn,
        this.sort.direction);
    }

    if (this.organizationTokensView) {
      this.dataSource.getOrganizationsTokens(
        this.organizationId,
        this.paginator.pageIndex,
        this.paginator.pageSize,
        sortColumn,
        this.sort.direction);
    }
  }
}
