import {ActorType} from '../../general/enum/types/ActorType';

export class CategoryTableInit {
  organizationId: string;
  actorType: ActorType;

  constructor(organizationId: string, actorType: ActorType) {
    this.organizationId = organizationId;
    this.actorType = actorType;
  }
}
