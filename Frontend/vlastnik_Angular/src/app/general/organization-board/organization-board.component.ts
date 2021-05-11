import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DocumentTableInit} from '../../tables/document-table/DocumentTableInit';
import {CommitteeInit} from '../committee/CommitteeInit';
import {PersonTableInit} from '../../tables/person-table/PersonTableInit';
import {PersonType} from '../enum/types/PersonType';
import {ActorType} from '../enum/types/ActorType';
import {OrganizationBoardInit} from './OrganizationBoardInit';
import {DocumentType} from '../enum/types/DocumentType';

@Component({
  selector: 'app-organization-board',
  templateUrl: './organization-board.component.html',
  styleUrls: ['./organization-board.component.css']
})
export class OrganizationBoardComponent implements OnInit {

  organizationBoardInit: OrganizationBoardInit;

  flatResidentView: boolean;
  organizationMemberView: boolean;

  messageTableInit: DocumentTableInit;
  memberTableInit: PersonTableInit;

  committeeInit: CommitteeInit;
  committeeExists: boolean;

  displayMember: boolean;

  constructor() {
  }

  @Input()
  set initComponent(organizationBoardInit: OrganizationBoardInit) {
    this.organizationBoardInit = organizationBoardInit;
    this.initAll();
  }

  @Output() afterDone = new EventEmitter<any>();

  ngOnInit(): void {
  }

  initAll(): void {
    if (this.organizationBoardInit.actorType === ActorType.FlatResident) {
      this.flatResidentView = true;
      this.committeeInit = new CommitteeInit(ActorType.FlatResident, this.organizationBoardInit.organizationId);
      this.initOrganizationFilesTable(DocumentType.Home, ActorType.FlatResident);
    }

    if (this.organizationBoardInit.actorType === ActorType.OrganizationMember) {
      this.organizationMemberView = true;
      this.committeeInit = new CommitteeInit(ActorType.OrganizationMember, this.organizationBoardInit.organizationId);
      this.initOrganizationFilesTable(DocumentType.Home, ActorType.OrganizationMember);
    }
  }

  displayOrganizationMembers(value): void {
    this.displayMember = value;
  }

  committeeLoaded(event): void {
    if (event != null) {
      this.committeeExists = true;
      if (this.flatResidentView) {
        this.initMemberTable(PersonType.Member, ActorType.FlatResident, event.committeeId);
      }
      if (this.organizationMemberView) {
        this.initMemberTable(PersonType.Member, ActorType.OrganizationMember, event.committeeId);
      }
    } else {
      this.committeeExists = false;
    }
  }

  initOrganizationFilesTable(messageType: DocumentType, actorType: ActorType): void {
    this.messageTableInit = new DocumentTableInit(messageType, actorType);
    this.messageTableInit.organizationId = this.organizationBoardInit.organizationId;
  }

  initMemberTable(personType: PersonType, actorType: ActorType, committeeId: string): void {
    this.memberTableInit = new PersonTableInit(personType, actorType);
    this.memberTableInit.organizationId = this.organizationBoardInit.organizationId;
    this.memberTableInit.committeeId = committeeId;
  }
}
