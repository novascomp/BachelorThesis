import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyFlatComponent } from './my-flat.component';

describe('MyFlatComponent', () => {
  let component: MyFlatComponent;
  let fixture: ComponentFixture<MyFlatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyFlatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyFlatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
