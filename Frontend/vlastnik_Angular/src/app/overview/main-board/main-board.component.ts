import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-main-board',
  templateUrl: './main-board.component.html',
  styleUrls: ['./main-board.component.css']
})
export class MainBoardComponent implements OnInit {

  tabId: number;

  flatLoaded: boolean;
  organizationLoaded: boolean;

  flatsCount: number;
  organizationsCount: number;

  constructor() {
  }

  ngOnInit(): void {
  }

  flatsLoaded(flatsCount): void {
    this.flatLoaded = true;
    this.flatsCount = flatsCount;
    this.analyzeFlatAndOrganizationLoadStatus();
  }

  organizationsLoaded(organizationsCount): void {
    this.organizationLoaded = true;
    this.organizationsCount = organizationsCount;
    this.analyzeFlatAndOrganizationLoadStatus();
  }

  private analyzeFlatAndOrganizationLoadStatus(): void {
    if (this.flatLoaded && this.organizationLoaded) {
      if (this.flatsCount === 0 && this.organizationsCount === 0) {
        this.tabId = 2;
      }

      if (this.flatsCount === 0 && this.organizationsCount > 0) {
        this.tabId = 1;
      }

      if (this.flatsCount > 0 && this.organizationsCount === 0) {
        this.tabId = 0;
      }

      if (this.flatsCount !== 0 && this.organizationsCount > 0) {
        this.tabId = 0;
      }
    }
  }
}
