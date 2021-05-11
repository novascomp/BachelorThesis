import {DataSource, CollectionViewer} from '@angular/cdk/collections';
import {finalize} from 'rxjs/operators';
import {Observable, BehaviorSubject} from 'rxjs';
import {OrganizationService} from '../../rest/service/organization.service';
import {HttpResponse} from '@angular/common/http';
import {GeneralListResponse} from '../../rest/model/GeneralListResponse';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {NVHomeFlat} from '../../rest/model/NVHomeFlat';

export class OrganizationFlatTableDataSource extends DataSource<NVHomeFlat> {

  public content = new BehaviorSubject<NVHomeFlat[]>([]);
  private loadingContent = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingContent.asObservable();
  public totalElements: any;
  public unavailable: boolean;

  constructor(private organizationService: OrganizationService) {
    super();
  }

  connect(collectionViewer: CollectionViewer): Observable<NVHomeFlat[]> {
    return this.content.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.content.complete();
    this.loadingContent.complete();
  }

  getFlats(organizationId, pageNumber, pageSize, sortColumn, sortDirection): void {
    this.loadingContent.next(true);

    this.organizationService.getOrganizationFlats(organizationId, pageNumber, pageSize, sortColumn, sortDirection).pipe(
      finalize(() => this.loadingContent.next(false)))
      .subscribe(page => {
        this.processResponse(page);
      });
  }

  private processResponse(page: HttpResponse<GeneralListResponse<NVHomeFlat>>): void {
    if (this.organizationService.getStatusCodeToGeneralResponse(page.status) === RestGeneralResponse.ok) {
      this.unavailable = false;
      this.content.next(page.body.content);
      this.totalElements = page.body.totalElements;
    } else {
      this.content.next(null);
      this.unavailable = true;
    }
  }
}
