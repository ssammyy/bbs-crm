export interface Privilege {
    id: number;
    name: string;
    createdOn: string;
    createdBy: string;
    updatedOn: string;
    updatedBy: string;
}

export interface Role {
    id: number;
    name: string;
    privileges: Privilege[];
}
