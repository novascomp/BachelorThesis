import {PersonType} from '../../general/enum/types/PersonType';
import {ActorType} from '../../general/enum/types/ActorType';

export class PersonTableInit {
  actorType: ActorType;
  personType: PersonType;
  flatId: string;
  detailId: string;
  organizationId: string;
  committeeId: string;

  constructor(personType: PersonType, actorType: ActorType) {
    this.personType = personType;
    this.actorType = actorType;
  }
}
