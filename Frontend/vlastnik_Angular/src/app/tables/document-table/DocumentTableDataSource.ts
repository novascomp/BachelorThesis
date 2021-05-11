import {DataSource, CollectionViewer} from '@angular/cdk/collections';
import {Observable, BehaviorSubject} from 'rxjs';
import {OrganizationService} from '../../rest/service/organization.service';
import {NvflatService} from '../../rest/service/nvflat.service';
import {LightweightComponent} from '../../rest/model/LightweightComponent';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {GeneralListResponse} from '../../rest/model/GeneralListResponse';
import {Document} from '../../rest/model/Document';
import {FileContent} from '../../rest/model/FileContent';
import {Share} from '../../rest/model/Share';

export class DocumentTableDataSource extends DataSource<Document> {

  content = new BehaviorSubject<Document[]>([]);
  private loadingContent = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingContent.asObservable();
  public totalElements: any;
  public unavailable: boolean;
  public documents: Document[];
  public totalElementsPreview: any;
  public sharesUnavailable: boolean;

  private flatServiceRequest: boolean;

  constructor(private organizationService: OrganizationService, private flatService: NvflatService) {
    super();
  }

  connect(collectionViewer: CollectionViewer): Observable<Document[]> {
    return this.content.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.content.complete();
    this.loadingContent.complete();
  }

  public getFlatDocuments(organizationId,
                          components: LightweightComponent[],
                          flaId,
                          pageNumber,
                          pageSize,
                          sortColumn,
                          sortDirection): void {
    this.sharesUnavailable = false;
    this.flatServiceRequest = true;
    this.loadingContent.next(true);
    this.flatService
      .getFlatDocumentsByComponents(organizationId, components, flaId, pageNumber, pageSize, sortColumn, sortDirection).toPromise()
      .then(this.processResponse.bind(this))
      .finally(this.interpretResponse.bind(this));
  }

  public getOrganizationDocuments(organizationId,
                                  components: LightweightComponent[],
                                  pageNumber,
                                  pageSize,
                                  sortColumn,
                                  sortDirection): void {
    this.sharesUnavailable = false;
    this.flatServiceRequest = false;
    this.loadingContent.next(true);
    this.organizationService
      .getOrganizationDocumentsByCategories(organizationId, components, pageNumber, pageSize, sortColumn, sortDirection).toPromise()
      .then(this.processResponse.bind(this))
      .finally(this.interpretResponse.bind(this));
  }

  private processResponse(page: HttpResponse<GeneralListResponse<Document>>): void {
    if (this.organizationService.getStatusCodeToGeneralResponse(page.status) === RestGeneralResponse.ok) {
      this.unavailable = false;
      this.documents = page.body.content;
      this.totalElementsPreview = page.body.totalElements;
    } else {
      this.unavailable = true;
    }
  }

  private interpretResponse(): Promise<any> {
    if (this.unavailable === false) {
      const componentsPromise = this.getComponents();
      const contentsPromise = this.getContents().then(this.getContentShares.bind(this));

      return Promise.all([componentsPromise, contentsPromise])
        .then(this.processComponentsAndContents.bind(this))
        .finally();
    } else {
      this.content.next(null);
      this.loadingContent.next(false);
    }
  }

  private processComponentsAndContents([components, contents]): void {
    this.content.next(this.documents);
    this.totalElements = this.totalElementsPreview;
    this.loadingContent.next(false);
  }

  private getComponents(): Promise<any> {
    let promises: Promise<HttpResponse<any>>[];
    promises = [];

    for (const selectedDocument of this.documents) {
      promises.push(this.getDocumentComponents(selectedDocument));
    }

    return Promise.all(promises);
  }

  private getContents(): Promise<any> {
    let promises: Promise<HttpResponse<any>>[];
    promises = [];

    for (const selectedDocument of this.documents) {
      promises.push(this.getDocumentContents(selectedDocument));
    }

    return Promise.all(promises);
  }

  private getContentShares(): Promise<any> {
    let promises: Promise<HttpResponse<any>>[];
    promises = [];

    for (const document of this.documents) {
      if (document.fileContents != null) {
        for (const fileContent of document.fileContents) {
          promises.push(this.getDocumentContentShares(document, fileContent));
        }
      }
    }

    return Promise.all(promises);
  }

  private getDocumentComponents(selectedDocument: Document): Promise<any> {
    if (this.flatServiceRequest) {
      return this.flatService
        .getDocumentComponents(selectedDocument.categoryComponentLink).toPromise()
        .then(this.processDocumentComponentResponse.bind(this))
        .then((value) => this.interpretDocumentComponentResponse(selectedDocument, value))
        .finally();
    } else {
      return this.organizationService
        .getDocumentComponents(selectedDocument.categoryComponentLink).toPromise()
        .then(this.processDocumentComponentResponse.bind(this))
        .then((value) => this.interpretDocumentComponentResponse(selectedDocument, value))
        .finally();
    }
  }

  private processDocumentComponentResponse(page: HttpResponse<GeneralListResponse<LightweightComponent>>)
    : LightweightComponent[] {
    const documentComponentGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(page.status);
    if (documentComponentGeneralResponse === RestGeneralResponse.ok) {
      return page.body.content;
    }
    return null;
  }

  private interpretDocumentComponentResponse(selectedDocument: Document, components): void {
    selectedDocument.categories = components;
  }

  private getDocumentContents(selectedDocument: Document): Promise<any> {
    if (this.flatServiceRequest) {
      return this.flatService
        .getDocumentContents(selectedDocument.contentLink).toPromise()
        .then(this.processDocumentContentResponse.bind(this))
        .then((value) => this.interpretDocumentContentResponse(selectedDocument, value))
        .finally();
    } else {
      return this.organizationService
        .getDocumentContents(selectedDocument.contentLink).toPromise()
        .then(this.processDocumentContentResponse.bind(this))
        .then((value) => this.interpretDocumentContentResponse(selectedDocument, value))
        .finally();
    }
  }

  private processDocumentContentResponse(page: HttpResponse<GeneralListResponse<FileContent>>)
    : FileContent[] {
    const documentComponentGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(page.status);
    if (documentComponentGeneralResponse === RestGeneralResponse.ok) {
      return page.body.content;
    }
    return null;
  }

  private interpretDocumentContentResponse(selectedDocument: Document, contents): void {
    selectedDocument.fileContents = contents;
  }

  private getDocumentContentShares(selectedDocument: Document, fileContent: FileContent): Promise<any> {
    if (this.flatServiceRequest) {
      return this.flatService
        .getDocumentContentShares(fileContent.sharesLink).toPromise()
        .then(this.processDocumentContentSharesResponse.bind(this))
        .then((value) => this.interpretDocumentContentSharesResponse(selectedDocument, value))
        .finally();
    } else {
      return this.organizationService
        .getDocumentContentShares(fileContent.sharesLink).toPromise()
        .then(this.processDocumentContentSharesResponse.bind(this))
        .then((value) => this.interpretDocumentContentSharesResponse(selectedDocument, value))
        .finally();
    }
  }

  private processDocumentContentSharesResponse(page: HttpResponse<GeneralListResponse<Share>>)
    : Share[] {
    const documentContentShareGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(page.status);
    if (documentContentShareGeneralResponse === RestGeneralResponse.ok) {
      return page.body.content;
    }

    if (documentContentShareGeneralResponse === RestGeneralResponse.serviceUnavailable ||
      documentContentShareGeneralResponse === RestGeneralResponse.internalException) {
      this.sharesUnavailable = true;
    }
    return null;
  }

  private interpretDocumentContentSharesResponse(selectedDocument: Document, contentShares): void {
    if (selectedDocument.shares == null) {
      selectedDocument.shares = [];
    }

    if (contentShares != null) {
      for (const share of contentShares) {
        selectedDocument.shares.push(share);
      }
    }
  }
}
