import {Component, OnInit} from '@angular/core';
import {OrganizationService} from '../../rest/service/organization.service';
import {Organization} from '../../rest/model/Organization';
import {ActivatedRoute, Router} from '@angular/router';
import {OrganizationBoardInit} from '../../general/organization-board/OrganizationBoardInit';
import {ActorType} from '../../general/enum/types/ActorType';
import {DocumentType} from '../../general/enum/types/DocumentType';
import {DocumentTableInit} from '../../tables/document-table/DocumentTableInit';
import {NVHomeFlat} from '../../rest/model/NVHomeFlat';
import {NvflatService} from '../../rest/service/nvflat.service';
import {TokenType} from '../../general/enum/types/TokenType';
import {TokenTableInit} from '../../tables/token-table/TokenTableInit';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {OrganizationFlatTableInit} from '../../tables/organization-flat-table/OrganizationFlatTableInit';
import {FlatUploaderInit} from '../../uploaders/flat-uploader/FlatUploaderInit';
import {MatTabChangeEvent} from '@angular/material/tabs';

@Component({
  selector: 'app-organization-portal',
  templateUrl: './organization-portal.component.html',
  styleUrls: ['./organization-portal.component.css']
})
export class OrganizationPortalComponent implements OnInit {
  organizationId: string;
  flatId: string;
  detailId: string;

  flatIdentifier: string;

  flatUploaderInit: FlatUploaderInit;
  organizationFlatTableInit: OrganizationFlatTableInit;
  documentTableInit: DocumentTableInit;
  organizationBoardInit: OrganizationBoardInit;
  organizationTokensTableInit: TokenTableInit;

  organizationPortalLoaded: boolean;
  organizationPortalUnavailable: boolean;
  flatServiceUnavailable: boolean;

  currentTabIndex: number;

  private organizationPromise: Promise<HttpResponse<Organization>>;
  private defaultFlatPromise: Promise<HttpResponse<NVHomeFlat>>;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private organizationService: OrganizationService,
              private flatService: NvflatService) {
    this.route.params.subscribe(params => {
      this.organizationId = params.organizationid;
    });

    if (this.organizationId == null) {
      this.router.navigateByUrl('neplatny/pozadavek/');
    }
    this.init();
  }

  async ngOnInit(): Promise<any> {
    this.organizationPromise = this.organizationService
      .getOrganization(this.organizationId).toPromise();
    this.defaultFlatPromise = this.organizationPromise
      .then(this.loadDefaulFlat.bind(this));

    Promise.all([this.organizationPromise, this.defaultFlatPromise])
      .then(this.processResponse.bind(this)).finally(this.interpretResponse.bind(this));
  }

  init(): void {
    this.currentTabIndex = 0;
    this.organizationPortalLoaded = false;
    this.organizationPortalUnavailable = false;
    this.flatServiceUnavailable = false;
  }

  public tabChanged(tabChangeEvent: MatTabChangeEvent): void {
    this.currentTabIndex = tabChangeEvent.index;
    this.initCurrentTab();
  }

  private loadDefaulFlat(organizationResponse: HttpResponse<Organization>): Promise<HttpResponse<NVHomeFlat>> {
    if (organizationResponse.body == null) {
      return null;
    }
    return this.organizationService.getOrganizationDefaultFlat(organizationResponse.body.organizationId).toPromise();
  }

  private processResponse([organizationResponse, defaultFlatResponse]): void {

    let defaultFlatGeneralResponse;
    if (defaultFlatResponse == null) {
      this.flatServiceUnavailable = true;
    } else {
      defaultFlatGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(defaultFlatResponse.status);
    }

    const organizationGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(organizationResponse.status);

    if (organizationGeneralResponse === RestGeneralResponse.ok &&
      defaultFlatGeneralResponse === RestGeneralResponse.ok) {
      this.flatId = defaultFlatResponse.body.flatId;
      this.flatIdentifier = defaultFlatResponse.body.identifier;
      this.detailId = defaultFlatResponse.body.detailId;
    } else {
      if (organizationGeneralResponse === RestGeneralResponse.badRequest ||
        organizationGeneralResponse === RestGeneralResponse.notFound ||
        organizationGeneralResponse === RestGeneralResponse.notPermitted) {
        this.router.navigateByUrl('neplatny/pozadavek/');
      }

      if (this.flatServiceUnavailable === false) {
        if (defaultFlatGeneralResponse === RestGeneralResponse.serviceUnavailable ||
          defaultFlatGeneralResponse === RestGeneralResponse.internalException) {
          this.flatServiceUnavailable = true;
        }
      }

      if (organizationGeneralResponse === RestGeneralResponse.serverUnavailable) {
        this.organizationPortalUnavailable = true;
      }

      this.organizationPortalLoaded = true;
    }
  }

  private interpretResponse(): void {

    if (this.organizationPortalUnavailable) {
      return;
    }

    this.organizationPortalLoaded = true;
    this.initCurrentTab();
  }

  private initCurrentTab(): void {

    if (this.organizationPortalLoaded === false) {
      return;
    }

    if (this.currentTabIndex === 0 && this.flatUploaderInit == null) {
      this.initFlatUploader();
    }

    if (this.currentTabIndex === 0 && this.organizationFlatTableInit == null) {
      this.initOrganizationFlatTable();
    }

    if (this.currentTabIndex === 1 && this.documentTableInit == null) {
      this.initFlatDocumentTable(DocumentType.Flat, ActorType.OrganizationMember);
    }

    if (this.currentTabIndex === 2 && this.organizationBoardInit == null) {
      this.initOrganizationBoardTable(ActorType.OrganizationMember);
    }

    if (this.currentTabIndex === 3 && this.organizationTokensTableInit == null) {
      this.initOrganizationTokensTable(TokenType.Organization, ActorType.OrganizationMember);
    }
  }

  initFlatUploader(): void {
    this.flatUploaderInit = new FlatUploaderInit(this.organizationId);
  }

  initOrganizationFlatTable(): void {
    this.organizationFlatTableInit = new OrganizationFlatTableInit(this.organizationId);
  }

  initFlatDocumentTable(documentType: DocumentType, actorType: ActorType): void {
    this.documentTableInit = new DocumentTableInit(documentType, actorType);
    this.documentTableInit.organizationId = this.organizationId;
    this.documentTableInit.flatId = this.flatId;
    this.documentTableInit.flatIdentifier = this.flatIdentifier;
    this.documentTableInit.detailId = this.detailId;
  }

  initOrganizationBoardTable(actorType: ActorType): void {
    this.organizationBoardInit = new OrganizationBoardInit(actorType, this.organizationId);
  }

  initOrganizationTokensTable(tokenType: TokenType, actorType: ActorType): void {
    this.organizationTokensTableInit = new TokenTableInit(tokenType, actorType, this.organizationId);
  }

  initAfterFlatsUploaded(): void {
    this.initOrganizationFlatTable();
    this.initFlatDocumentTable(DocumentType.Flat, ActorType.OrganizationMember);
  }

  getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }
}
