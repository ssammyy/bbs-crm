export interface KycDocument {
  label: string;
  fileType: string;
}

export const KYC_DOCUMENTS: KycDocument[] = [
  { label: 'Title Deed', fileType: 'TITLE_DEED' },
  { label: 'Deed Plan', fileType: 'DEED_PLAN' },
  { label: 'KRA Pin', fileType: 'KRA_PIN' },
  { label: 'Land Rates', fileType: 'LAND_RATES' },
  { label: 'ID', fileType: 'ID' },
  { label: 'Survey Map', fileType: 'SURVEY_MAP' }
];
