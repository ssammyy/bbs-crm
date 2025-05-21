export interface Receipt {
    id: number;
    invoiceId: number;
    invoiceNumber: string;
    clientName: string;
    paymentDate: string;
    paymentMethod: string;
    amountPaid: number;
    transactionId?: string;
    createdAt: string;
    invoiceReconciled?: boolean;
}
