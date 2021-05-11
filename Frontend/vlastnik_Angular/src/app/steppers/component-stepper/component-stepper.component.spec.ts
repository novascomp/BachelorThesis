import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComponentStepperComponent } from './component-stepper.component';

describe('ComponentStepperComponent', () => {
  let component: ComponentStepperComponent;
  let fixture: ComponentFixture<ComponentStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ComponentStepperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ComponentStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
