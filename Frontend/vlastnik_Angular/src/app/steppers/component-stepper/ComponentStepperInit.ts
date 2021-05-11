import {Crud} from '../../general/enum/types/Crud';

export class ComponentStepperInit {
  crud: Crud;
  organizationId: string;

  constructor(crud: Crud, organizationId: string) {
    this.crud = crud;
    this.organizationId = organizationId;
  }
}
