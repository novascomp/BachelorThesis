import {Component, EventEmitter, Input, Output} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {FormControl} from '@angular/forms';
import {OrganizationService} from '../../rest/service/organization.service';
import {FlatUploaderInit} from './FlatUploaderInit';
import {ModifiableResourcesComponent} from '../../general/components-general/ModifiableResourcesComponent';
import {GeneralComponentTitles} from '../../general/enum/titles/GeneralComponentTitles';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {RecaptchaAction} from '../../rest/security/RecaptchaAction';
import {ReCaptchaV3Service} from 'ng-recaptcha';

@Component({
  selector: 'app-flat-uploader',
  templateUrl: './flat-uploader.component.html',
  styleUrls: ['./flat-uploader.component.css']
})
export class FlatUploaderComponent extends ModifiableResourcesComponent {
  IsWait = false;
  multiple = false;
  uploadAvailable = true;
  text = new FormControl();

  name: string;
  dropzoneTitle: string;

  files: File[] = [];
  maxUploadSize: number;
  currentUploadSize: number;

  uploadDone: boolean;
  organizationId: string;

  flatUploaderInit: FlatUploaderInit;

  constructor(private organizationService: OrganizationService,
              private recaptchaV3Service: ReCaptchaV3Service) {
    super();
  }

  @Input()
  set initComponent(flatUploaderInit: FlatUploaderInit) {
    this.flatUploaderInit = flatUploaderInit;
    this.organizationId = this.flatUploaderInit.organizationId;
    this.name = GeneralComponentTitles.UPLOAD_FLATS;
    this.dropzoneTitle = GeneralComponentTitles.UPLOAD_FLATS_TITLE;
    this.maxUploadSize = 10000000; // 10MB
  }

  @Output() afterDone = new EventEmitter<any>();

  public getRecaptchaFlatsUpload(): RecaptchaAction {
    return RecaptchaAction.RECAPTCHA_FLATS_UPLOAD;
  }

  public preSelect(event, recaptchaAction: RecaptchaAction): void {
    this.recaptchaV3Service.execute(recaptchaAction)
      .subscribe((value) => this.onSelect(event, value));
  }

  onSelect(event, recaptchaToken: string): void {
    this.errorText = null;
    this.currentUploadSize = 0;

    if (this.files.length >= 1) {
      return;
    }

    for (const file of event.addedFiles) {
      this.currentUploadSize += file.size;
    }

    if (this.currentUploadSize > this.maxUploadSize) {
      this.errorText = GeneralComponentTitles.UPLOAD_FLAT_FILE_ERROR_MAX_SIZE_EXCEEDED;
      return;
    }

    this.files.push(...event.addedFiles);
    const formData = new FormData();

    for (const file of this.files) {
      formData.append('file', file);
    }

    this.initGlobal();

    this.uploadAvailable = false;
    this.IsWait = true;

    this.organizationService.uploadFlats(this.organizationId, formData, recaptchaToken)
      .subscribe(this.uploadResponse.bind(this));
  }

  private uploadResponse(response: HttpResponse<any>): void {
    const uploadGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (uploadGeneralResponse === RestGeneralResponse.ok) {
      this.uploadDone = true;
      this.afterDone.emit(true);
    } else {
      this.handleError(uploadGeneralResponse);
    }

    this.IsWait = false;
    this.uploadAvailable = true;
    this.files.splice(0);
  }

  onRemove(event): void {
    this.files.splice(this.files.indexOf(event), 1);
  }

  getWrongFormat(): string {
    return GeneralComponentTitles.UPLOAD_FLATS_WRONG_FORMAT;
  }

}
