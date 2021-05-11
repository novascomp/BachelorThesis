import {Committee} from '../../rest/model/Committee';

export class CommitteeStepperInit {
  organizationId: string;
  committee: Committee;

  constructor(committee: Committee, organizationId: string) {
    this.committee = committee;
    this.organizationId = organizationId;
  }
}
