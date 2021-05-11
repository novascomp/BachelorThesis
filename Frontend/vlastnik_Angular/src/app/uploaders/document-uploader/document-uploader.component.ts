import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LightweightComponent} from '../../rest/model/LightweightComponent';
import {OrganizationService} from '../../rest/service/organization.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Document} from '../../rest/model/Document';
import {ComponentStepperInit} from '../../steppers/component-stepper/ComponentStepperInit';
import {Crud} from '../../general/enum/types/Crud';
import {DocumentUploaderInit} from './DocumentUploaderInit';
import {DocumentType} from '../../general/enum/types/DocumentType';
import {ActorType} from '../../general/enum/types/ActorType';
import {NvflatService} from '../../rest/service/nvflat.service';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ReCaptchaV3Service} from 'ng-recaptcha';

@Component({
  selector: 'app-document-uploader',
  templateUrl: './document-uploader.component.html',
  styleUrls: ['./document-uploader.component.css']
})
export class DocumentUploaderComponent extends ModifiableResourcesComponent implements OnInit {

  name: string;
  componentName: string;
  dropzoneTitle: string;

  flatDocumentTableForResident: boolean;
  flatDocumentTableForMember: boolean;

  organizationDocumentTableForResident: boolean;
  organizationDocumentTableForMember: boolean;

  categories = new FormControl();
  undefinedComponent: LightweightComponent;
  documentFormGroup: FormGroup;

  invalidForm: boolean;

  categoryAddFormDisplay: boolean;

  components: LightweightComponent[];
  document: Document;

  organizationId: string;

  files: File[] = [];
  maxUploadSize: number;
  currentUploadSize: number;


  documentUploaderInit: DocumentUploaderInit;
  componentStepperInit: ComponentStepperInit;

  submitted: boolean;
  detailId: string;

  flatsComponentsLoaded: boolean;
  organizationComponentsLoaded: boolean;

  private recaptchaSavedToken: string;

  constructor(private formBuilder: FormBuilder,
              private organizationService: OrganizationService,
              private flatService: NvflatService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
    this.document = new Document();
    this.name = GeneralComponentTitles.DOCUMENT_FORM;
  }

  @Input()
  set initComponent(documentUploaderInit: DocumentUploaderInit) {
    this.documentUploaderInit = documentUploaderInit;
    this.dropzoneTitle = GeneralComponentTitles.UPLOAD_FILES;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  ngOnInit(): void {
    this.documentFormGroup = this.formBuilder.group({
      heading: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(256)]
      ],
      body: [
        null,
        [Validators.required, Validators.minLength(1), Validators.maxLength(256)]
      ],
    });

    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      this.loadFlatComponents();
    }

    if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
      this.loadCategories();
    }
  }

  init(): void {
    this.initGlobal();
    this.invalidForm = false;
    this.submitted = false;
  }

  initAll(): void {
    this.maxUploadSize = 30000000; // 30MB
    this.organizationId = this.documentUploaderInit.organizationId;

    if (this.documentUploaderInit.documentType === DocumentType.Flat
      && this.documentUploaderInit.actorType === ActorType.FlatResident) {
      this.componentName = GeneralComponentTitles.CHOOSE_FLAT;
      this.flatDocumentTableForResident = true;
      this.detailId = this.documentUploaderInit.detailId;
    }

    if (this.documentUploaderInit.documentType === DocumentType.Flat
      && this.documentUploaderInit.actorType === ActorType.OrganizationMember) {
      this.componentName = GeneralComponentTitles.CHOOSE_FLAT;
      this.flatDocumentTableForMember = true;
      this.detailId = this.documentUploaderInit.detailId;
    }

    if (this.documentUploaderInit.documentType === DocumentType.Home
      && this.documentUploaderInit.actorType === ActorType.FlatResident) {
      this.componentName = GeneralComponentTitles.CHOOSE_CATEGORY;
      this.organizationDocumentTableForResident = true;
      this.componentStepperInit = new ComponentStepperInit(Crud.Create, this.organizationId);
      this.componentStepperInit.organizationId = this.organizationId;
    }

    if (this.documentUploaderInit.documentType === DocumentType.Home
      && this.documentUploaderInit.actorType === ActorType.OrganizationMember) {
      this.componentName = GeneralComponentTitles.CHOOSE_CATEGORY;
      this.organizationDocumentTableForMember = true;
      this.componentStepperInit = new ComponentStepperInit(Crud.Create, this.organizationId);
      this.componentStepperInit.organizationId = this.organizationId;
    }

  }

  public getRecaptchaActionDocument(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_DOCUMENT;
  }

  public preSubmitForm(recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value => this.submit(value)));
  }

  submit(recaptchaToken: string): void {
    this.init();
    this.submitted = true;
    this.recaptchaSavedToken = recaptchaToken;


    if (this.categories.value != null) {
      if (this.categories.value.length > 5) {
        this.init();
        if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
          this.errorText = GeneralComponentTitles.MAX_FLATS_COUNT_EXCEEDED;
        }

        if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
          this.errorText = GeneralComponentTitles.MAX_COMPONENTS_COUNT_EXCEEDED;
        }
        return;
      }
    }

    if (this.documentFormGroup.valid && this.errorText == null) {
      if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
        this.addFlatDocument(recaptchaToken);
      }

      if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
        this.addOrganizationDocument(recaptchaToken);
      }
    } else {
      this.init();
      this.errorText = this.getBadRequestText();
    }
  }

  onSelect(event): void {
    this.errorText = null;
    this.currentUploadSize = 0;

    if (this.files.length + event.addedFiles.length > 3) {
      this.errorText = GeneralComponentTitles.UPLOAD_FILES_MAX_COUNT_EXCEEDED;
      return;
    }

    for (const file of this.files) {
      this.currentUploadSize += file.size;
    }

    for (const file of event.addedFiles) {
      this.currentUploadSize += file.size;
    }

    if (this.currentUploadSize > this.maxUploadSize) {
      this.errorText = GeneralComponentTitles.UPLOAD_FILES_ERROR_MAX_SIZE_EXCEEDED;
      return;
    }

    this.files.push(...event.addedFiles);
  }

  onRemove(event): void {
    this.files.splice(this.files.indexOf(event), 1);
  }

  displayCategory(): void {
    this.categoryAddFormDisplay = !this.categoryAddFormDisplay;
  }

  categoriesUpdated(event): void {
    this.categoryAddFormDisplay = false;
    this.loadCategories();
  }

  private loadCategories(): void {
    this.organizationService.getAllOrganizationCategories(this.organizationId).toPromise()
      .then(this.processGetOrganizationComponentsResponse.bind(this))
      .finally(this.interpretOrganizationComponentsResponse.bind(this));
  }

  private processGetOrganizationComponentsResponse(organizationComponentsResponse: HttpResponse<any>): void {
    const organizationComponentsGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(organizationComponentsResponse.status);
    if (organizationComponentsGeneralResponse === RestGeneralResponse.ok) {
      this.components = organizationComponentsResponse.body.content;
      this.organizationComponentsLoaded = true;
    } else {
      this.organizationComponentsLoaded = false;
      this.handleError(organizationComponentsGeneralResponse);
    }
  }

  private interpretOrganizationComponentsResponse(): void {
    if (this.organizationComponentsLoaded) {
      for (const component of this.components) {
        component.componentId = component.categoryId;
      }

      for (let i = 0; i < this.components.length; i++) {
        if (this.components[i].text.toUpperCase() === GeneralComponentTitles.DEFAULT_CATEGORY_NAME) {
          this.undefinedComponent = this.components[i];
          this.components.splice(i, 1);
        }
      }
    }
  }

  loadFlatComponents(): void {
    this.flatService.getFlatComponents(this.organizationId).toPromise()
      .then(this.processGetFlatComponentsResponse.bind(this))
      .finally(this.interpretFlatComponentsResponse.bind(this));
  }

  private processGetFlatComponentsResponse(flatComponentsResponse: HttpResponse<any>): void {
    const flatComponentsGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(flatComponentsResponse.status);
    if (flatComponentsGeneralResponse === RestGeneralResponse.ok) {
      this.components = flatComponentsResponse.body.content;
      this.flatsComponentsLoaded = true;
    } else {
      this.flatsComponentsLoaded = false;
      this.handleError(flatComponentsGeneralResponse);
    }
  }

  private interpretFlatComponentsResponse(): void {
    if (this.flatsComponentsLoaded) {

      let componentToDelete: LightweightComponent;
      for (const component of this.components) {
        component.componentId = component.categoryId;
        if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
          if (component.text === this.documentUploaderInit.flatIdentifier) {
            componentToDelete = component;
          }
        }
      }

      if (componentToDelete != null) {
        this.components.splice(this.components.indexOf(componentToDelete), 1);
      }
    }
  }

  private addFlatDocument(recaptchaToken: string): void {
    this.document.detailId = this.detailId;

    this.flatService.addDocument(this.organizationId, this.document, recaptchaToken).toPromise()
      .then(this.processDocumentAddResponse.bind(this))
      .then(this.interpretDocumentAddResponse.bind(this))
      .finally();
  }

  private addOrganizationDocument(recaptchaToken: string): void {
    this.organizationService.addDocument(this.organizationId, this.document, recaptchaToken).toPromise()
      .then(this.processDocumentAddResponse.bind(this))
      .then(this.interpretDocumentAddResponse.bind(this))
      .finally();
  }

  private processDocumentAddResponse(documentAddResponse: HttpResponse<any>): string {
    const documentAddGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(documentAddResponse.status);
    let documentLink;
    if (documentAddGeneralResponse === RestGeneralResponse.ok) {
      documentLink = documentAddResponse.headers.get('Location');
    } else {
      this.handleError(documentAddGeneralResponse);
      this.submitted = false;
    }
    return documentLink;
  }

  private interpretDocumentAddResponse(documentLink: string): void {
    if (documentLink != null) {
      this.document.fileId = documentLink.split('/')[documentLink.split('/').length - 1];
      const recaptchaToken = this.recaptchaSavedToken;
      const componentsPromise = this.addComponents(documentLink, recaptchaToken);
      const contentsPromise = this.addContents(documentLink, recaptchaToken);
      Promise.all([componentsPromise, contentsPromise])
        .then(this.processComponentsAndContents.bind(this))
        .then(this.interpretComponentsAndContentsResponse.bind(this))
        .finally();
    }
  }

  private processComponentsAndContents([components, contents]): boolean {
    if (components != null) {
      let i = 0;
      for (const componentResponse of components) {
        const componentsGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(componentResponse.status);
        if (componentsGeneralResponse === RestGeneralResponse.ok) {
        } else {
          if (this.errorText == null) {
            if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
              this.errorText = GeneralComponentTitles.DOCUMENT_FLATS_UPLOAD_FAILED;
            }
            if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
              this.errorText = GeneralComponentTitles.CATEGORY_UPLOAD_FAILED;
            }
          }
          this.errorText = this.errorText + ' ' + this.components[i].text;
        }
        i++;
      }

      if (this.errorText != null) {
        return false;
      }
    }

    if (contents != null) {
      let i = 0;
      for (const contentResponse of contents) {
        const contentsGeneralResponse = this.flatService.getStatusCodeToGeneralResponse(contentResponse.status);
        if (contentsGeneralResponse === RestGeneralResponse.ok) {
        } else {
          if (this.errorText == null) {
            this.errorText = GeneralComponentTitles.FILE_UPLOAD_FAILED;
          }
          this.errorText = this.errorText + ' ' + this.files[i].name;
        }
        i++;
      }
    }

    if (this.errorText != null) {
      return false;
    }

    return true;
  }

  private interpretComponentsAndContentsResponse(success: boolean): void {
    if (success) {
      this.afterDone.emit(this.document);
    } else {

      // rollback
      if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
        this.flatService.deleteFlatDocumentById(this.organizationId, this.document.fileId, this.detailId, this.recaptchaSavedToken)
          .subscribe(this.commonDeleteResponse.bind(this));
      }

      if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
        this.organizationService.deleteOrganizationDocumentById(this.organizationId, this.document.fileId, this.recaptchaSavedToken)
          .subscribe(this.commonDeleteResponse.bind(this));
      }
    }

    this.submitted = false;
  }

  private commonDeleteResponse(componentsResponse: HttpResponse<any>): void {
    const deleteDocumentGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(componentsResponse.status);
    if (deleteDocumentGeneralResponse === RestGeneralResponse.ok) {
    } else {
      this.handleError(deleteDocumentGeneralResponse);
    }
  }

  private addComponents(documentLink: string, recaptchaToken): Promise<any> {
    let promises: Promise<HttpResponse<any>>[];
    promises = [];

    if (this.categories.value != null) {
      for (const selectedComponentId of this.categories.value) {
        promises.push(this.addComponentToDocument(documentLink, selectedComponentId, recaptchaToken));
      }

      if (this.categories.value.length === 0) {
        if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
          promises.push(this.addComponentToDocument(documentLink, this.undefinedComponent.componentId, recaptchaToken));
        }
      }
    } else {
      if (this.organizationDocumentTableForResident || this.organizationDocumentTableForMember) {
        promises.push(this.addComponentToDocument(documentLink, this.undefinedComponent.componentId, recaptchaToken));
      }
    }

    return Promise.all(promises);
  }

  private addComponentToDocument(documentLink: string, componentId, recaptchaToken): Promise<HttpResponse<any>> {
    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      return this.flatService.addComponentToDocument(documentLink, componentId, recaptchaToken).toPromise();
    }
    return this.organizationService.addComponentToDocument(documentLink, componentId, recaptchaToken).toPromise();
  }

  private addContents(documentLink: string, recaptchaToken: string): Promise<any> {

    let promises: Promise<HttpResponse<any>>[];
    promises = [];
    if (this.files != null) {
      for (const file of this.files) {
        promises.push(this.addContentToDocument(documentLink, file, recaptchaToken));
      }
    }

    return Promise.all(promises);
  }

  private addContentToDocument(documentLink: string, file: File, recaptchaToken: string): Promise<HttpResponse<any>> {
    if (this.flatDocumentTableForResident || this.flatDocumentTableForMember) {
      return this.flatService.addContentToDocument(documentLink, file, recaptchaToken).toPromise();
    }
    return this.organizationService.addContentToDocument(documentLink, file, recaptchaToken).toPromise();
  }
}
