import {ActorType} from '../../general/enum/types/ActorType';
import {DocumentType} from '../../general/enum/types/DocumentType';

export class DocumentTableInit {
  actorType: ActorType;
  documentType: DocumentType;
  detailId: string;
  flatId: string;
  organizationId: string;
  flatIdentifier: string;

  constructor(messageType: DocumentType, actorType: ActorType) {
    this.documentType = messageType;
    this.actorType = actorType;
  }
}
