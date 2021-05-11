import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganizationPortalComponent } from './organization-portal.component';

describe('OrganizationFlatsComponent', () => {
  let component: OrganizationPortalComponent;
  let fixture: ComponentFixture<OrganizationPortalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OrganizationPortalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganizationPortalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
