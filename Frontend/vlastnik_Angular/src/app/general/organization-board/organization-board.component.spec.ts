import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganizationBoardComponent } from './organization-board.component';

describe('OrganizationBoardComponent', () => {
  let component: OrganizationBoardComponent;
  let fixture: ComponentFixture<OrganizationBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OrganizationBoardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganizationBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
