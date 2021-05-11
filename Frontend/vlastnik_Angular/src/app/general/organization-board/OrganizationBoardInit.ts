import {ActorType} from '../enum/types/ActorType';

export class OrganizationBoardInit {
  actorType: ActorType;
  organizationId: string;

  constructor(actorType: ActorType, organizationId: string) {
    this.actorType = actorType;
    this.organizationId = organizationId;
  }
}
