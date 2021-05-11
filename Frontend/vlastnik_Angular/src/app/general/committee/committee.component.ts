import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommitteeStepperInit} from '../../steppers/committee-stepper/CommitteeStepperInit';
import {Committee} from '../../rest/model/Committee';
import {OrganizationService} from '../../rest/service/organization.service';
import {CommitteeInit} from './CommitteeInit';
import {ActorType} from '../enum/types/ActorType';
import {HttpResponse} from '@angular/common/http';
import {RestGeneralResponse} from '../../rest/response/RestGeneralResponse';
import {GeneralComponentTitles} from '../enum/titles/GeneralComponentTitles';
import {Organization} from '../../rest/model/Organization';
import {AresVrForFEPruposes} from '../../rest/model/AresVrForFEPruposes';

@Component({
  selector: 'app-committee',
  templateUrl: './committee.component.html',
  styleUrls: ['./committee.component.css']
})
export class CommitteeComponent implements OnInit {

  @Output() newItemEvent = new EventEmitter<boolean>();
  @Output() committeeLoadEvent = new EventEmitter<Committee>();
  displayCommitteeMembers: boolean;

  componentName: string;

  displayCommitteeMemberForm: boolean;
  organization: Organization;
  committee: Committee;
  organizationId: string;

  committeeStepperInit: CommitteeStepperInit;
  committeeExists: boolean;

  actorType: ActorType;

  flatResidentView: boolean;
  organizationMemberView: boolean;

  unavailable: boolean;

  @Input()
  set initComponent(committeeInit: CommitteeInit) {
    this.organizationId = committeeInit.organizationId;
    this.actorType = committeeInit.actorType;
    this.initAll();
  }

  constructor(private organizationService: OrganizationService) {
  }

  async ngOnInit(): Promise<any> {
  }

  initAll(): void {
    this.componentName = GeneralComponentTitles.COMMITTEE_HEADING;
    this.committee = new Committee();
    this.organization = new Organization();
    this.organization.aresVrForFEPruposes = new AresVrForFEPruposes();

    if (this.actorType === ActorType.FlatResident) {
      this.flatResidentView = true;
    }

    if (this.actorType === ActorType.OrganizationMember) {
      this.organizationMemberView = true;
    }

    this.loadOrganization();
    this.loadOrganizationCommittee();
  }

  displayClick(value: boolean): void {
    this.displayCommitteeMembers = !value;
    this.newItemEvent.emit(this.displayCommitteeMembers);
  }

  displayCommitteeMemberFormClick(): void {
    this.displayCommitteeMemberForm = !this.displayCommitteeMemberForm;
    this.initCommitteeStepper();
  }

  committeeUpdated(event): void {
    this.displayCommitteeMemberForm = false;
    this.loadOrganizationCommittee();
  }

  loadOrganization(): void {
    this.organizationService.getOrganization(this.organizationId).subscribe(this.organizationResponse.bind(this));
  }

  private organizationResponse(response: HttpResponse<any>): void {
    const organizationGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (organizationGeneralResponse === RestGeneralResponse.ok) {
      this.organization = response.body;
      this.unavailable = false;
    } else {
      this.unavailable = true;
    }
  }

  loadOrganizationCommittee(): void {
    this.organizationService.getOrganizationCommittee(this.organizationId).subscribe(this.commonResponse.bind(this));
  }

  private commonResponse(response: HttpResponse<any>): void {
    const commonGeneralResponse = this.organizationService.getStatusCodeToGeneralResponse(response.status);
    if (commonGeneralResponse === RestGeneralResponse.ok) {
      this.committee = response.body;
      this.committeeExists = true;
      this.unavailable = false;
    } else {
      this.unavailable = true;
    }
    this.committeeLoadEvent.emit(this.committee);
  }

  private initCommitteeStepper(): void {
    const committee = new Committee();
    committee.committeeId = this.committee.committeeId.toString();
    committee.email = this.committee.email.toString();
    committee.phone = this.committee.phone.toString();
    this.committeeStepperInit = new CommitteeStepperInit(committee, this.organizationId);
  }

  getUnavailableText(): string {
    return GeneralComponentTitles.UNAVAILABLE;
  }
}
