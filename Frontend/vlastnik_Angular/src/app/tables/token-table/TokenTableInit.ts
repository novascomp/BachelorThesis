import {ActorType} from '../../general/enum/types/ActorType';
import {TokenType} from '../../general/enum/types/TokenType';

export class TokenTableInit {
  actorType: ActorType;
  tokenType: TokenType;
  organizationId: string;
  flatId: string;

  constructor(tokenType: TokenType, actorType: ActorType,  organizationId: string) {
    this.tokenType = tokenType;
    this.actorType = actorType;
    this.organizationId = organizationId;
  }
}
