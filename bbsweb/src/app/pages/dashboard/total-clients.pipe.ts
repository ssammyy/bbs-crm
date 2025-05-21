// total-clients.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'totalClients',
    standalone: true
})
export class TotalClientsPipe implements PipeTransform {
    transform(distribution: { [key: string]: number } | null | undefined): number {
        if (!distribution) return 0;
        return Object.values(distribution).reduce((sum, count) => sum + (count || 0), 0);
    }
}
