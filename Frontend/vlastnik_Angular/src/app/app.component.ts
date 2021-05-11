import {Component, enableProdMode, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {OktaAuthService} from './security/okta-auth.service';
import {OnExecuteData, ReCaptchaV3Service} from 'ng-recaptcha';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'vlastnik';
  start: boolean;
  isAuthenticated: boolean;

  private subscription: Subscription;

  constructor(public router: Router,
              public oktaAuth: OktaAuthService,
              private recaptchaV3Service: ReCaptchaV3Service) {
  }

  ngOnInit(): void {
    this.oktaAuth.$isAuthenticated.subscribe(val => {
      this.isAuthenticated = val;
    });

    this.subscription = this.recaptchaV3Service.onExecute
      .subscribe((data: OnExecuteData) => {
      });

  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}

//https://developer.okta.com/code/angular/okta_angular_auth_js/


