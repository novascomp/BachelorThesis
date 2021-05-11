import {Crud} from '../../general/enum/types/Crud';
import {PersonType} from '../../general/enum/types/PersonType';
import {Person} from '../../general/model/Person';

export class PersonStepperInit {
  crud: Crud;
  personType: PersonType;
  person: Person;
  detailId: string;
  organizationId: string;
  committeeId: string;

  constructor(crud: Crud, personType: PersonType, person: Person) {
    this.crud = crud;
    this.personType = personType;
    this.person = person;
  }
}
