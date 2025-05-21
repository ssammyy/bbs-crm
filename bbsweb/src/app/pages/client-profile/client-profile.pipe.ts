import { Pipe, PipeTransform } from '@angular/core';
import { Files } from '../data/clietDTOs';

@Pipe({
  name: 'filterNonKyc',
  standalone: true
})
export class FilterNonKycPipe implements PipeTransform {
  transform(files: Files[], kycDocuments: { fileType: string, label: string, required: boolean }[]): Files[] {
    const kycFileTypes = kycDocuments.map(doc => doc.fileType);
    return files.filter(file => !kycFileTypes.includes(file.fileType));
  }
}
