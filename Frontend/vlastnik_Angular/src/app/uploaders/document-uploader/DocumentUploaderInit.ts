import {ActorType} from '../../general/enum/types/ActorType';
import {DocumentType} from '../../general/enum/types/DocumentType';
import {Crud} from '../../general/enum/types/Crud';

export class DocumentUploaderInit {
  crud: Crud;
  actorType: ActorType;
  documentType: DocumentType;
  detailId: string;
  organizationId: string;
  flatIdentifier: string;

  constructor(crud: Crud, messageType: DocumentType, actorType: ActorType) {
    this.crud = crud;
    this.documentType = messageType;
    this.actorType = actorType;
  }
}
