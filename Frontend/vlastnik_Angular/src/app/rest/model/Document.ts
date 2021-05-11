import {FileContent} from './FileContent';
import {LightweightComponent} from './LightweightComponent';
import {Share} from './Share';
import {General} from './General';

export class Document {
  fileId: string;
  heading: string;
  body: string;
  categoryComponentLink: string;
  priorityComponentLink: string;
  contentLink: string;
  detailId: string;
  general: General;

  categories: LightweightComponent[];
  fileContents: FileContent[];
  shares: Share[];
}
