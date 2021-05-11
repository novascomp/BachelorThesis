import {TokenType} from '../../general/enum/types/TokenType';

export class TokenPinInit {
  tokenType: TokenType;

  constructor(tokenType: TokenType) {
    this.tokenType = tokenType;
  }
}
