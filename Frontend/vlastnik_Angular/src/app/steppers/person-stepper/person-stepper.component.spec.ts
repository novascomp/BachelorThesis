import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonStepperComponent } from './person-stepper.component';

describe('PersonAddStepperComponent', () => {
  let component: PersonStepperComponent;
  let fixture: ComponentFixture<PersonStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PersonStepperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
