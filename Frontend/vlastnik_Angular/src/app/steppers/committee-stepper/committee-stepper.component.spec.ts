import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommitteeStepperComponent } from './committee-stepper.component';

describe('CommitteeStepperComponent', () => {
  let component: CommitteeStepperComponent;
  let fixture: ComponentFixture<CommitteeStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CommitteeStepperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CommitteeStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
