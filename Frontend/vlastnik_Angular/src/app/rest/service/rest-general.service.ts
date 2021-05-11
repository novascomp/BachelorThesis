import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Observable, of, throwError} from 'rxjs';
import {RestGeneralResponse} from '../response/RestGeneralResponse';
import {OktaAuthService} from '../../security/okta-auth.service';

@Injectable({
  providedIn: 'root'
})
export class RestGeneralService {

  baseURL: string;
  globalHeaders: HttpHeaders;

  constructor(baseURL: string, public http: HttpClient, public oktaAuth: OktaAuthService) {
    this.baseURL = baseURL;
    this.globalHeaders = new HttpHeaders();
    this.oktaAuth.$isAuthenticated.subscribe(val => {
      this.oktaAuth.oktaAuth.tokenManager.get('accessToken')
        .then(token => {
          if (token) {
            this.globalHeaders = new HttpHeaders({
              Authorization: 'Bearer ' + token.value,
              'Content-Type': 'application/json',
            });
          } else {
            console.log('Neautorizovaný přístup');
          }
        });
    });
  }

  public handleErrorGeneral(error: HttpErrorResponse): Observable<any> {
    if (error.error instanceof ErrorEvent) {
      console.error('An response occurred:', error.error.message);
    } else {

      if (error.status === 400 ||
        error.status === 401 ||
        error.status === 403 ||
        error.status === 404 ||
        error.status === 409 ||
        error.status === 423 ||
        error.status === 500 ||
        error.status === 503) {
        return of(error);
      }

      console.error(
        `Backend returned code ${error.status}`);
    }
    console.error('Something bad happened; please try again later.');
    return of(error);
  }

  public getStatusCodeToGeneralResponse(statusCode): RestGeneralResponse {

    if (statusCode == null) {
      return null;
    }

    if (statusCode === 0) {
      return RestGeneralResponse.serverUnavailable;
    }

    if (statusCode === 400) {
      return RestGeneralResponse.badRequest;
    }

    if (statusCode === 401) {
      return RestGeneralResponse.unauthorized;
    }

    if (statusCode === 403) {
      return RestGeneralResponse.notPermitted;
    }

    if (statusCode === 404) {
      return RestGeneralResponse.notFound;
    }

    if (statusCode === 409) {
      return RestGeneralResponse.alreadyRegistered;
    }

    if (statusCode === 423) {
      return RestGeneralResponse.recaptchaFailed;
    }

    if (statusCode === 500) {
      return RestGeneralResponse.internalException;
    }

    if (statusCode === 503) {
      return RestGeneralResponse.serviceUnavailable;
    }

    return RestGeneralResponse.ok;
  }
}
