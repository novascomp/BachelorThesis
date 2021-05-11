import { MatPaginatorIntl } from '@angular/material/paginator';

//https://stackoverflow.com/questions/47593692/how-to-translate-mat-paginator-in-angular-4

const czechRangeLabel = (page: number, pageSize: number, length: number) => {
  if (length == 0 || pageSize == 0) { return `0 z ${length}`; }

  length = Math.max(length, 0);

  const startIndex = page * pageSize;

  // If the start index exceeds the list length, do not try and fix the end index to the end.
  const endIndex = startIndex < length ?
    Math.min(startIndex + pageSize, length) :
    startIndex + pageSize;

  return `${startIndex + 1} - ${endIndex} z ${length}`;
}


export function getCzechPaginatorIntl() {
  const paginatorIntl = new MatPaginatorIntl();

  paginatorIntl.itemsPerPageLabel = 'Počet prvků:';
  paginatorIntl.nextPageLabel = 'Další strana';
  paginatorIntl.previousPageLabel = 'Předchozí strana';
  paginatorIntl.getRangeLabel = czechRangeLabel;

  return paginatorIntl;
}
