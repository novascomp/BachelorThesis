import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenPinComponent } from './token-pin.component';

describe('TokenPinComponent', () => {
  let component: TokenPinComponent;
  let fixture: ComponentFixture<TokenPinComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokenPinComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenPinComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
