import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganizationFlatTableComponent } from './organization-flat-table.component';

describe('OrganizationFlatsComponent', () => {
  let component: OrganizationFlatTableComponent;
  let fixture: ComponentFixture<OrganizationFlatTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OrganizationFlatTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrganizationFlatTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
