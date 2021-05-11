import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {OrganizationService} from '../../rest/service/organization.service';
import {NvflatService} from '../../rest/service/nvflat.service';
import {Flat} from '../../rest/model/Flat';
import {Detail} from '../../rest/model/Detail';
import {PersonTableInit} from '../../tables/person-table/PersonTableInit';
import {ActorType} from '../../general/enum/types/ActorType';
import {PersonType} from '../../general/enum/types/PersonType';
import {Organization} from '../../rest/model/Organization';
import {OrganizationBoardInit} from '../../general/organization-board/OrganizationBoardInit';
import {DocumentType} from '../../general/enum/types/DocumentType';
import {DocumentTableInit} from '../../tables/document-table/DocumentTableInit';
import {TokenType} from '../../general/enum/types/TokenType';
import {TokenTableInit} from '../../tables/token-table/TokenTableInit';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {NVHomeFlat} from '../../rest/model/NVHomeFlat';
import {MatTabChangeEvent} from '@angular/material/tabs';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';

@Component({
  selector: 'app-flat-portal',
  templateUrl: './flat-portal.component.html',
  styleUrls: ['./flat-portal.component.css']
})
export class FlatPortalComponent implements OnInit {

  flatResidentView: boolean;
  organizationMemberView: boolean;

  flat: Flat;
  detail: Detail;
  organization: Organization;

  personTableInit: PersonTableInit;
  organizationBoardInit: OrganizationBoardInit;
  flatTokensTableInit: TokenTableInit;
  messageTableInit: DocumentTableInit;

  flatTabResidentViewLoaded: boolean;
  flatTabResidentViewUnavailable: boolean;

  private flatResidentViewPromise: Promise<HttpResponse<Flat>>;
  private detailResidentViewPromise: Promise<HttpResponse<Detail>>;
  private organizationResidentViewPromise: Promise<HttpResponse<Organization>>;

  private organizationMemberViewPromise: Promise<HttpResponse<NVHomeFlat>>;

  organizationMemberViewLoaded: boolean;
  organizationMemberViewUnavailable: boolean;

  currentTabIndex: number;

  constructor(private route: ActivatedRoute, private router: Router,
              private organizationService: OrganizationService, private flatService: NvflatService) {
    this.init();

    this.route.params.subscribe(params => {
      this.organization.organizationId = params.organizationid;
      this.flat.flatId = params.flatid;

      if (this.organization.organizationId == null && this.flat.flatId == null) {
        this.router.navigateByUrl('neplatny/pozadavek/');
      }

      if (this.organization.organizationId == null) {
        this.flatResidentView = true;
      } else {
        this.organizationMemberView = true;
      }
    });
  }

  async ngOnInit(): Promise<any> {

    if (this.flatResidentView) {
      this.flatResidentViewPromise = this.flatService.getFlatById(this.flat.flatId).toPromise();
      this.detailResidentViewPromise = this.flatResidentViewPromise.then(this.loadFlatDetailResidentView.bind(this));
      this.organizationResidentViewPromise = this.flatResidentViewPromise.then(this.loadFlatOrganizationResidentView.bind(this));
      Promise.all([this.flatResidentViewPromise, this.detailResidentViewPromise, this.organizationResidentViewPromise])
        .then((this.processResidentViewResponse.bind(this)))
        .finally(this.interpretResidentViewResponse.bind(this));
    }

    if (this.organizationMemberView) {
      this.organizationMemberViewPromise = this.organizationService
        .getOrganizationFlatById(this.organization.organizationId, this.flat.flatId)
        .toPromise();
      this.organizationMemberViewPromise
        .then(this.processOrganizationMemberViewResponse.bind(this))
        .finally(this.interpretOrganizationMemberViewResponse.bind(this));
    }
  }

  init(): void {
    this.flat = new Flat();
    this.detail = new Detail();
    this.organization = new Organization();
    this.flat.identifier = '';
    this.flatTabResidentViewLoaded = false;
    this.flatTabResidentViewUnavailable = false;
    this.currentTabIndex = 0;
  }

  public tabChanged(tabChangeEvent: MatTabChangeEvent): void {
    this.currentTabIndex = tabChangeEvent.index;
    this.initCurrentTabResidentView();
  }

  private loadFlatDetailResidentView(flatResponse: HttpResponse<Flat>): Promise<HttpResponse<Detail>> {
    if (flatResponse.body == null) {
      return this.flatService.getFlatDetailByLink('').toPromise();
    }
    return this.flatService.getFlatDetailByLink(flatResponse.body.flatDetailLink).toPromise();
  }

  private loadFlatOrganizationResidentView(flatResponse: HttpResponse<Flat>): Promise<HttpResponse<Organization>> {
    if (flatResponse.body == null) {
      return this.flatService.getFlatOrganizationByLink('').toPromise();
    }
    return this.flatService.getFlatOrganizationByLink(flatResponse.body.flatOrganizationLink).toPromise();
  }

  private processResidentViewResponse([flatResponse, detailResponse, organizationResponse]): void {
    const flatGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(flatResponse.status);
    const detailGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(detailResponse.status);
    const organizationGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(organizationResponse.status);

    if (flatGeneralResponse === RestGeneralResponse.ok &&
      detailGeneralResponse === RestGeneralResponse.ok &&
      organizationGeneralResponse === RestGeneralResponse.ok) {
      this.flat = flatResponse.body;
      this.detail = detailResponse.body;
      this.organization = organizationResponse.body;
    } else {
      if (flatGeneralResponse === RestGeneralResponse.badRequest ||
        flatGeneralResponse === RestGeneralResponse.notFound ||
        flatGeneralResponse === RestGeneralResponse.notPermitted) {
        this.router.navigateByUrl('neplatny/pozadavek/');
      }
      this.flatTabResidentViewUnavailable = true;
      this.flatTabResidentViewLoaded = true;
    }
  }

  private interpretResidentViewResponse(): void {

    if (this.flatTabResidentViewUnavailable) {
      return;
    }

    this.flatTabResidentViewLoaded = true;
    this.initCurrentTabResidentView();
  }

  private processOrganizationMemberViewResponse(nvHomeFlatResponse: HttpResponse<NVHomeFlat>): void {
    const nvHomeFlatGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(nvHomeFlatResponse.status);

    if (nvHomeFlatGeneralResponse === RestGeneralResponse.ok) {
      this.flat.flatId = nvHomeFlatResponse.body.flatId;
      this.flat.identifier = nvHomeFlatResponse.body.identifier;
      this.detail.detailId = nvHomeFlatResponse.body.detailId;
      this.detail.size = nvHomeFlatResponse.body.size;
      this.detail.commonShareSize = nvHomeFlatResponse.body.commonShareSize;
    } else {
      if (nvHomeFlatGeneralResponse === RestGeneralResponse.badRequest ||
        nvHomeFlatGeneralResponse === RestGeneralResponse.notFound ||
        nvHomeFlatGeneralResponse === RestGeneralResponse.notPermitted) {
        this.router.navigateByUrl('neplatny/pozadavek/');
      }
      this.organizationMemberViewUnavailable = true;
      this.organizationMemberViewLoaded = true;
    }
  }

  private interpretOrganizationMemberViewResponse(): void {

    if (this.organizationMemberViewUnavailable) {
      return;
    }

    this.initResidentTable(PersonType.Resident, ActorType.OrganizationMember);
    this.initFlatTokensTable(TokenType.Flat, ActorType.OrganizationMember);
    this.organizationMemberViewLoaded = true;
  }

  private initCurrentTabResidentView(): void {

    if (this.flatTabResidentViewLoaded === false) {
      return;
    }

    if (this.currentTabIndex === 0 && this.personTableInit == null) {
      this.initResidentTable(PersonType.Resident, ActorType.FlatResident);
    }

    if (this.currentTabIndex === 1 && this.messageTableInit == null) {
      this.initFlatMessagesTable(DocumentType.Flat, ActorType.FlatResident);
    }

    if (this.currentTabIndex === 2 && this.organizationBoardInit == null) {
      this.organizationBoardInit = new OrganizationBoardInit(ActorType.FlatResident, this.organization.organizationId);
    }
  }

  private initResidentTable(personType: PersonType, actorType: ActorType): void {
    this.personTableInit = new PersonTableInit(personType, actorType);
    this.personTableInit.flatId = this.flat.flatId;
    this.personTableInit.detailId = this.detail.detailId;
    this.personTableInit.organizationId = this.organization.organizationId;
  }

  private initFlatMessagesTable(messageType: DocumentType, actorType: ActorType): void {
    this.messageTableInit = new DocumentTableInit(messageType, actorType);
    this.messageTableInit.detailId = this.detail.detailId;
    this.messageTableInit.flatId = this.flat.flatId;
    this.messageTableInit.organizationId = this.organization.organizationId;
    this.messageTableInit.flatIdentifier = this.flat.identifier;
  }

  private initFlatTokensTable(tokenType: TokenType, actorType: ActorType): void {
    this.flatTokensTableInit = new TokenTableInit(tokenType, actorType, this.organization.organizationId);
    this.flatTokensTableInit.flatId = this.flat.flatId;
    this.flatTokensTableInit.organizationId = this.organization.organizationId;
  }

  public getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }
}
