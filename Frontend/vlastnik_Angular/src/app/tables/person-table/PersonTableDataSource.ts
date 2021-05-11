import {DataSource, CollectionViewer} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {Observable, BehaviorSubject} from 'rxjs';
import {OrganizationService} from '../../rest/service/organization.service';
import {NvflatService} from '../../rest/service/nvflat.service';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {HttpResponse} from '@angular/common/http';
import {GeneralListResponse} from '../../rest/model/GeneralListResponse';
import {Person} from '../../general/model/Person';

export class PersonTableDataSource extends DataSource<Person> {

  public content = new BehaviorSubject<Person[]>([]);
  private loadingContent = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingContent.asObservable();
  public totalElements: any;
  public unavailable: boolean;

  constructor(private organizationService: OrganizationService, private flatService: NvflatService) {
    super();
  }

  connect(collectionViewer: CollectionViewer): Observable<Person[]> {
    return this.content.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.content.complete();
    this.loadingContent.complete();
  }

  getFlatResidentsByFlatResident(organizationId, flatId, pageNumber, pageSize, sortColumn, sortDirection): void {
    this.loadingContent.next(true);
    this.flatService.getFlatResidents(flatId, pageNumber, pageSize, sortColumn, sortDirection).pipe(
      finalize(() => this.loadingContent.next(false)))
      .subscribe(page => {
        this.processResponse(page);
      });
  }

  getFlatResidentsByOrganizationMember(organizationId, flatId, pageNumber, pageSize, sortColumn, sortDirection): void {
    this.loadingContent.next(true);
    this.organizationService.getOrganizationFlatByIdResidents(organizationId, flatId, pageNumber, pageSize, sortColumn, sortDirection).pipe(
      finalize(() => this.loadingContent.next(false)))
      .subscribe(page => {
        this.processResponse(page);
      });
  }

  getOrganizationMembers(committeeId, pageNumber, pageSize, sortColumn, sortDirection): void {
    this.loadingContent.next(true);
    this.organizationService.getOrganizationMembers(committeeId, pageNumber, pageSize, sortColumn, sortDirection).pipe(
      finalize(() => this.loadingContent.next(false)))
      .subscribe(page => {
        this.processResponse(page);
      });
  }

  private processResponse(page: HttpResponse<GeneralListResponse<any>>): void {
    if (this.flatService.getStatusCodeToGeneralResponse(page.status) === RestGeneralResponse.ok) {
      this.unavailable = false;
      this.content.next(page.body.content);
      this.totalElements = page.body.totalElements;
    } else {
      this.content.next(null);
      this.unavailable = true;
    }
  }

}
