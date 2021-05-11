import { TestBed } from '@angular/core/testing';

import { RestGeneralService } from './rest-general.service';

describe('RestGeneralService', () => {
  let service: RestGeneralService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RestGeneralService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
