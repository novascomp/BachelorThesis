import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {OktaAuthService} from '../okta-auth.service';

export interface Tile {
  color: string;
  cols: number;
  rows: number;
  loginText: string;
  logoutText: string;
  loginButton: boolean;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  isAuthenticated: boolean;
  tiles: Tile[] = [
    {loginButton: true, loginText: 'Přihásit se', logoutText: 'Odhlásit se', cols: 4, rows: 1, color: 'white'},
    {loginButton: false, loginText: 'Registrovat', logoutText: 'Odhlásit se', cols: 4, rows: 1, color: 'white'},
  ];

  constructor(public oktaAuth: OktaAuthService, private router: Router) {
  }

  ngOnInit(): void {
    this.oktaAuth.$isAuthenticated.subscribe(val => {
      this.isAuthenticated = val;
      if (this.isAuthenticated) {
        this.router.navigateByUrl('/prehled');
      }
    });
  }
}
