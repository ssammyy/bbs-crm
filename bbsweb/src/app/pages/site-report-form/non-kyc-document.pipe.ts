import { Pipe, PipeTransform } from '@angular/core';
import { Files } from '../data/clietDTOs';
import { KYC_DOCUMENTS } from './kyc-documents';

@Pipe({
  name: 'nonKycDocument',
  standalone: true
})
export class NonKycDocumentPipe implements PipeTransform {
  transform(files: Files[]): Files[] {
    const kycFileTypes = KYC_DOCUMENTS.map(doc => doc.fileType);
    return files.filter(file => !kycFileTypes.includes(file.fileType));
  }
}
