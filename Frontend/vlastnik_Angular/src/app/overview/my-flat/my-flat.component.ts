import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {NvflatService} from '../../rest/service/nvflat.service';
import {Flat} from '../../rest/model/Flat';
import {Organization} from '../../rest/model/Organization';
import {GeneralListResponse} from '../../rest/model/GeneralListResponse';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {OrganizationService} from '../../rest/service/organization.service';
import {AresVrForFEPruposes} from '../../rest/model/AresVrForFEPruposes';
import {ReCaptchaV3Service} from 'ng-recaptcha';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';

@Component({
  selector: 'app-my-flat',
  templateUrl: './my-flat.component.html',
  styleUrls: ['./my-flat.component.css']
})
export class MyFlatComponent implements OnInit {

  flats: Flat[];
  organizations: Organization[];
  map: Map<string, Flat[]>;

  committeeFlats: number;
  emptyFlats: boolean;
  flatLoaded: boolean;

  serverUnavailable: boolean;
  unauthorized: boolean;

  @Output() afterDone = new EventEmitter<any>();

  private myFlatsPromise: Promise<HttpResponse<GeneralListResponse<Flat>>>;
  private myOrganizationsPromise: Promise<HttpResponse<GeneralListResponse<Organization>>>;

  constructor(private service: NvflatService,
              private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    this.init();
  }

  async ngOnInit(): Promise<any> {
    this.myFlatsPromise = this.service.getPersonalFlat().toPromise();
    this.myOrganizationsPromise = this.service.getPersonalOrganizations().toPromise();
    Promise.all([this.myFlatsPromise, this.myOrganizationsPromise])
      .then(this.processResponse.bind(this))
      .then(this.interpretResponse.bind(this))
      .then(this.loadAres.bind(this))
      .finally(this.finalInterpretation.bind(this));
  }

  init(): void {
    this.organizations = [];
    this.map = new Map();
    this.committeeFlats = 0;
    this.flatLoaded = false;
    this.serverUnavailable = false;
    this.unauthorized = false;
  }

  private processResponse([myFlats, myOrganizations]): void {
    const myFlatsGeneralResponse = this.service.getStatusCodeToGeneralResponse(myFlats.status);
    const myOrganizationsGeneralResponse = this.service.getStatusCodeToGeneralResponse(myOrganizations.status);
    if (myFlatsGeneralResponse === RestGeneralResponse.ok && myOrganizationsGeneralResponse === RestGeneralResponse.ok
      || myFlatsGeneralResponse === RestGeneralResponse.notFound || myOrganizationsGeneralResponse === RestGeneralResponse.notFound) {
      this.flats = myFlats.body.content;
      this.organizations = myOrganizations.body.content;
    } else if (myFlatsGeneralResponse === RestGeneralResponse.unauthorized ||
      myOrganizationsGeneralResponse === RestGeneralResponse.unauthorized) {
      this.unauthorized = true;
      this.afterDone.emit(-1);
      this.flatLoaded = true;
    } else {
      this.serverUnavailable = true;
      this.afterDone.emit(-1);
      this.flatLoaded = true;
    }
  }

  private loadAres(): Promise<any> {
    let promises: Promise<any>[];
    promises = [];
    for (const organization of this.organizations) {
      if (this.map.get(organization.organizationId).length !== 0) {
        promises.push(this.getAresOrganizationName(organization).then(value => organization.aresVrForFEPruposes = value.body));
      }
    }
    return Promise.all(promises);
  }

  private interpretResponse(): void {
    if (this.serverUnavailable) {
      return;
    }
    for (const organization of this.organizations) {
      this.map.set(organization.organizationId, []);
    }

    for (const flat of this.flats) {
      if (flat.identifier !== GeneralComponentTitles.COMMITTEE_FLAT) {
        this.map.get(flat.organizationId).push(flat);
      } else {
        this.committeeFlats++;
      }
    }
  }

  private finalInterpretation(): void {
    if (this.serverUnavailable) {
      return;
    }

    if (this.flats == null) {
      this.emptyFlats = true;
      this.afterDone.emit(0);
    } else if (this.flats.length === 0 || this.flats.length === this.committeeFlats) {
      this.emptyFlats = true;
      this.afterDone.emit(0);
    } else {
      this.map.forEach((value: any, key: any) => {
        this.sortFlats(value, key);
      });
      this.afterDone.emit(this.flats.length);
    }

    this.flatLoaded = true;
  }

  private getAresOrganizationName(organization: Organization): Promise<HttpResponse<AresVrForFEPruposes>> {
    return this.organizationService.getAresResponse(organization).toPromise();
  }

  private sortFlats(value: any, key: any): number {
    return value.sort((n1, n2) => {
      if (n1.identifier > n2.identifier) {
        return 1;
      }
      if (n1.identifier < n2.identifier) {
        return -1;
      }
      return 0;
    });
  }

  getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }

  getUnauthorizedText(): string {
    return GeneralComponentTitles.UNAUTHORIZED;
  }

  getMyFlatsEmptyText(): string {
    return GeneralComponentTitles.MY_FLATS_EMPTY;
  }
}
