export interface Notification {
    id: number;
    title: string;
    message: string;
    isRead: boolean;
    type: string;
    relatedEntityId?: number;
    relatedEntityType?: string;
    createdAt: string;
    updatedAt: string;
} 