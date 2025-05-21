import { PreliminaryType } from '../data/clietDTOs';

export interface InvoiceItem {
    description: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    isPreliminary?: boolean;
    preliminaryType?: PreliminaryType | null; // Add this
}


export enum InvoiceType {
    PROFORMA = 'PROFORMA',
    SITE_VISIT = 'SITE_VISIT',
    ARCHITECTURAL_DRAWINGS = 'ARCHITECTURAL_DRAWINGS',
    BOQ = 'BOQ',
    OPEN = 'OPEN',
    PRELIMINARY = 'PRELIMINARY'
}
