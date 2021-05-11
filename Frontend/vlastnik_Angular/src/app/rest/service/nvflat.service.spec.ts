import { TestBed } from '@angular/core/testing';

import { NvflatService } from './nvflat.service';

describe('NvflatService', () => {
  let service: NvflatService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NvflatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
