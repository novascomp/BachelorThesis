import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Organization} from '../model/Organization';
import {GeneralListResponse} from '../model/GeneralListResponse';
import {Token} from '../model/Token';
import {NVHomeFlat} from '../model/NVHomeFlat';
import {Resident} from '../model/Resident';
import {Member} from '../model/Member';
import {Committee} from '../model/Committee';
import {catchError} from 'rxjs/operators';
import {LightweightComponent} from '../model/LightweightComponent';
import {Document} from '../model/Document';
import {FileContent} from '../model/FileContent';
import {Share} from '../model/Share';
import {OrganizationTokenPost} from '../../general/model/OrganizationTokenPost';
import {OktaAuthService} from '../../security/okta-auth.service';
import {RestGeneralService} from './rest-general.service';
import {AresVrForFEPruposes} from '../model/AresVrForFEPruposes';
import {environment} from '../../../environments/environment';

@Injectable({providedIn: 'root'})
export class OrganizationService extends RestGeneralService {

  constructor(public http: HttpClient, public oktaAuth: OktaAuthService) {
    super(environment.nvhomeServicePath, http, oktaAuth);
  }

  public getAresResponse(organization: Organization)
    : Observable<HttpResponse<AresVrForFEPruposes>> {
    const body = JSON.stringify(organization);
    return this.http
      .post<AresVrForFEPruposes>(this.baseURL + 'registration/ares', body, {
        headers: this.globalHeaders,
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public verifyOrganizationExistence(organization: Organization, recaptchaToken: string)
    : Observable<any> {
    const body = JSON.stringify(organization);
    return this.http
      .post<any>(this.baseURL + 'registration/verifyico', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addOrganization(organization: Organization, recaptchaToken: string)
    : Observable<any> {
    const body = JSON.stringify(organization);
    return this.http
      .post<any>(this.baseURL + 'registration', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public pinOrganization(token: Token, recaptchaToken: string)
    : Observable<any> {
    const body = JSON.stringify(token);
    return this.http
      .post<any>(this.baseURL + 'tokens/user/add', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public getPersonalOrganization()
    : Observable<HttpResponse<GeneralListResponse<Organization>>> {
    return this.http
      .get<GeneralListResponse<Organization>>(this.baseURL + 'organizations/personal',
        {
          headers: this.globalHeaders,
          params: new HttpParams()
            .set('page', '0')
            .set('size', '1000')
            .set('sort', 'ico,asc'),
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationFlatById(organizationId, flatId)
    : Observable<HttpResponse<NVHomeFlat>> {
    return this.http
      .get<NVHomeFlat>(this.baseURL + 'organizations/' + organizationId + '/flats/' + flatId, {
        headers: this.globalHeaders,
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationFlatByIdResidents(organizationId, flatId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Resident>>> {
    return this.http
      .get<GeneralListResponse<Resident>>(this.baseURL + 'organizations/' + organizationId + '/flats/' + flatId + '/residents',
        {
          headers: this.globalHeaders,
          params: new HttpParams()
            .set('page', pageNumber.toString())
            .set('size', pageSize.toString())
            .set('sort', sortColumn + ',' + sortDirection),
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationMembers(committeeId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Member>>> {
    return this.http
      .get<GeneralListResponse<Member>>(this.baseURL + 'committees/' + committeeId + '/members', {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationFlatTokens(organizationId, flatId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Token>>> {
    return this.http
      .get<GeneralListResponse<Token>>(this.baseURL + 'organizations/' + organizationId + '/flats/' + flatId + '/tokens', {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationTokens(organizationId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Token>>> {
    return this.http
      .get<GeneralListResponse<Token>>(this.baseURL + 'organizations/' + organizationId + '/tokens', {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addFlatToken(organizationId, flatId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/flats/' + flatId + '/tokens', null, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addOrganizationToken(organizationId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const organizationTokenPost = new OrganizationTokenPost();
    const organization = new Organization();
    organization.organizationId = organizationId;
    organizationTokenPost.organization = organization;
    const body = JSON.stringify(organizationTokenPost);
    return this.http
      .post<any>(this.baseURL + 'tokens', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteFlatToken(organizationId, flatId, tokenId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<any>(this.baseURL + 'organizations/' + organizationId + '/flats/' + flatId + '/tokens/' + tokenId, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteOrganizationToken(tokenId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<any>(this.baseURL + 'tokens/' + tokenId, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationFlats(organizationId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<NVHomeFlat>>> {
    return this.http
      .get<GeneralListResponse<NVHomeFlat>>(this.baseURL + 'organizations/' + organizationId + '/flats', {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganization(organizationId: string)
    : Observable<HttpResponse<Organization>> {
    return this.http
      .get<Organization>(this.baseURL + 'organizations/' + organizationId, {
        headers: this.globalHeaders,
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationDefaultFlat(organizationId)
    : Observable<HttpResponse<NVHomeFlat>> {
    return this.http
      .get<NVHomeFlat>(this.baseURL + 'organizations/' + organizationId + '/default/flat', {
        headers: this.globalHeaders,
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationCommittee(organizationId)
    : Observable<HttpResponse<Committee>> {
    return this.http
      .get<Committee>(this.baseURL + 'organizations/' + organizationId + '/committee', {
        headers: this.globalHeaders,
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addMember(member: Member, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(member);
    return this.http
      .post<any>(this.baseURL + 'members', body, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public updateMember(member: Member, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(member);
    return this.http
      .put<any>(this.baseURL + 'members', body, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteMember(memberId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<any>(this.baseURL + 'members/' + memberId, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public updateOrganizationCommittee(committee: Committee, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(committee);
    return this.http
      .put<any>(this.baseURL + 'committees', body, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addOrganizationCategory(organizationId, component: LightweightComponent, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(component);
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/documents/components/categories', body, {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getAllOrganizationCategories(organizationId)
    : Observable<HttpResponse<GeneralListResponse<LightweightComponent>>> {
    return this.http
      .get<GeneralListResponse<LightweightComponent>>(this.baseURL + 'organizations/' + organizationId + '/documents/components/categories',
        {
          headers: this.globalHeaders,
          params: new HttpParams()
            .set('page', '0')
            .set('size', '1000')
            .set('sort', 'text,asc'),
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getOrganizationCategories(organizationId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<LightweightComponent>>> {
    return this.http
      .get<GeneralListResponse<LightweightComponent>>(this.baseURL + 'organizations/' + organizationId + '/documents/components/categories',
        {
          headers: this.globalHeaders,
          params: new HttpParams()
            .set('page', pageNumber.toString())
            .set('size', pageSize.toString())
            .set('sort', sortColumn + ',' + sortDirection),
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteOrganizationCategory(organizationId, categoryId: string, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<HttpResponse<any>>(this.baseURL + 'organizations/' + organizationId + '/documents/components/categories/' + categoryId, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public generateFlats(organizationId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/random/flats', null, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteFlats(organizationId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<any>(this.baseURL + 'organizations/' + organizationId + '/flats', {
        headers: this.globalHeaders,
        observe: 'response',
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getMembersFromAresRecord(organizationId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/members/ares', null, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public generateAresRecord(organizationId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/generate/ares', null, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public uploadFlats(organizationId, formData: FormData, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/upload/flats', formData, {
        headers: {
          Authorization: this.globalHeaders.get('Authorization'),
        }, params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addDocument(organizationId, document: Document, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(document);
    return this.http
      .post<any>(this.baseURL + 'organizations/' + organizationId + '/documents', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addComponentToDocument(documentLink: string, componentId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const component = new LightweightComponent();
    component.componentId = componentId;
    const body = JSON.stringify(component);
    return this.http
      .post<any>(documentLink + '/components/categories', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public addContentToDocument(documentLink: string, file: File, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<any>(documentLink + '/contents', formData, {
        headers: {
          Authorization: this.globalHeaders.get('Authorization'),
        },
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public getOrganizationDocumentsByCategories(organizationId, categories: LightweightComponent[],
                                              pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Document>>> {
    const body = JSON.stringify(categories);
    return this.http
      .post<GeneralListResponse<Document>>(this.baseURL + 'organizations/' + organizationId + '/documents/bycategories', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public getDocumentComponents(documentLink: string)
    : Observable<HttpResponse<GeneralListResponse<LightweightComponent>>> {
    return this.http
      .get<GeneralListResponse<LightweightComponent>>(documentLink, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', '0')
          .set('size', '1000')
          .set('sort', 'text,asc'),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public getDocumentContents(documentLink: string): Observable<HttpResponse<GeneralListResponse<FileContent>>> {
    return this.http
      .get<GeneralListResponse<FileContent>>(documentLink, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', '0')
          .set('size', '1000')
          .set('sort', 'fileNvfId,asc'),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public getDocumentContentShares(documentLink: string)
    : Observable<HttpResponse<GeneralListResponse<Share>>> {
    return this.http
      .get<GeneralListResponse<Share>>(documentLink, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', '0')
          .set('size', '1000')
          .set('sort', 'link,asc'),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public deleteOrganizationDocumentById(organizationId, documentId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete(this.baseURL + 'organizations/' + organizationId + '/documents/' + documentId, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }
}
