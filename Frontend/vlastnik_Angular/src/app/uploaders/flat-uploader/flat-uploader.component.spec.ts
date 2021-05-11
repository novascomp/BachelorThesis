import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlatUploaderComponent } from './flat-uploader.component';

describe('FileUploadComponent', () => {
  let component: FlatUploaderComponent;
  let fixture: ComponentFixture<FlatUploaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FlatUploaderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FlatUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
