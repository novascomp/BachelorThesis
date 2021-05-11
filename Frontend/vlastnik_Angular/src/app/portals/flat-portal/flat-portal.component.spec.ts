import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlatPortalComponent } from './flat-portal.component';

describe('FlatPortalComponent', () => {
  let component: FlatPortalComponent;
  let fixture: ComponentFixture<FlatPortalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FlatPortalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FlatPortalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
