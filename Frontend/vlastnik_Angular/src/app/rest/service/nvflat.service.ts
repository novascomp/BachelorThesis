import {Injectable} from '@angular/core';
import {Organization} from '../model/Organization';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Token} from '../model/Token';
import {GeneralListResponse} from '../model/GeneralListResponse';
import {Flat} from '../model/Flat';
import {Resident} from '../model/Resident';
import {Detail} from '../model/Detail';
import {catchError} from 'rxjs/operators';
import {LightweightComponent} from '../model/LightweightComponent';
import {Share} from '../model/Share';
import {Document} from '../model/Document';
import {FileContent} from '../model/FileContent';
import {OktaAuthService} from '../../security/okta-auth.service';
import {RestGeneralService} from './rest-general.service';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NvflatService extends RestGeneralService {

  constructor(public http: HttpClient, public oktaAuth: OktaAuthService) {
    super(environment.nvflatServicePath, http, oktaAuth);
  }

  public pinFlat(token: Token, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
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

  public getPersonalFlat()
    : Observable<HttpResponse<GeneralListResponse<Flat>>> {
    return this.http
      .get<GeneralListResponse<Flat>>(this.baseURL + 'flats/personal',
        {
          headers: this.globalHeaders,
          params: new HttpParams()
            .set('page', '0')
            .set('size', '1000')
            .set('sort', 'identifier'),
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getPersonalOrganizations()
    : Observable<HttpResponse<GeneralListResponse<Organization>>> {
    return this.http
      .get<GeneralListResponse<Organization>>(this.baseURL + 'flats/personal/organizations',
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

  public getFlatById(flatId)
    : Observable<HttpResponse<Flat>> {
    return this.http
      .get<Flat>(this.baseURL + 'flats/' + flatId,
        {
          headers: this.globalHeaders,
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getFlatDetailByLink(link)
    : Observable<HttpResponse<Detail>> {
    return this.http
      .get<Detail>(link,
        {
          headers: this.globalHeaders,
          observe: 'response',
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getFlatOrganizationByLink(link)
    : Observable<HttpResponse<Organization>> {
    return this.http
      .get<Organization>(link,
        {
          headers: this.globalHeaders,
          observe: 'response'
        }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public getFlatResidents(flatId, pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Resident>>> {
    return this.http
      .get<GeneralListResponse<Resident>>(this.baseURL + 'flats/' + flatId + '/residents', {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('page', pageNumber.toString())
          .set('size', pageSize.toString())
          .set('sort', sortColumn + ',' + sortDirection),
        observe: 'response'
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public addResident(resident: Resident, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(resident);
    return this.http
      .post<any>(this.baseURL + 'residents', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public updateResident(resident: Resident, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    const body = JSON.stringify(resident);
    return this.http
      .put<any>(this.baseURL + 'residents', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(super.handleErrorGeneral)
      );
  }

  public deleteResident(residentId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete<any>(this.baseURL + 'residents/' + residentId, {
        headers: this.globalHeaders,
        params: new HttpParams()
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

  public addComponentToDocument(documentLink, componentId, recaptchaToken)
    : Observable<HttpResponse<any>> {
    const component = new LightweightComponent();
    component.componentId = componentId;
    const body = JSON.stringify(component);
    return this.http
      .post<any>(documentLink + '/components/', body, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }

  public addContentToDocument(documentLink: string, file: File, recaptchaToken)
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

  public getFlatComponents(organizationId)
    : Observable<HttpResponse<GeneralListResponse<LightweightComponent>>> {
    return this.http
      .get<GeneralListResponse<LightweightComponent>>(this.baseURL + 'organizations/' + organizationId + '/documents/components', {
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

  public getFlatComponentsPersonal(organizationId, detailid)
    : Observable<HttpResponse<GeneralListResponse<LightweightComponent>>> {
    return this.http
      .get<GeneralListResponse<LightweightComponent>>
      (this.baseURL + 'organizations/' + organizationId + '/documents/components/personal/' + detailid,
        {
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

  public getFlatDocumentsByComponents(organizationId, components: LightweightComponent[], flatId,
                                      pageNumber, pageSize, sortColumn, sortDirection)
    : Observable<HttpResponse<GeneralListResponse<Document>>> {
    const body = JSON.stringify(components);
    return this.http
      .post<GeneralListResponse<Document>>(this.baseURL + 'organizations/' + organizationId + '/documents/byflats/' + flatId, body,
        {
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

  public getDocumentComponents(documentLink)
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

  public getDocumentContents(documentLink)
    : Observable<HttpResponse<GeneralListResponse<FileContent>>> {
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

  public getDocumentContentShares(documentLink)
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

  public deleteFlatDocumentById(organizationId, documentId, detailId, recaptchaToken: string)
    : Observable<HttpResponse<any>> {
    return this.http
      .delete(this.baseURL + 'organizations/' + organizationId + '/documents/' + documentId + '/details/' + detailId, {
        headers: this.globalHeaders,
        params: new HttpParams()
          .set('recaptcha_token', recaptchaToken),
        observe: 'response',
      }).pipe(
        catchError(this.handleErrorGeneral)
      );
  }
}
