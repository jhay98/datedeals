export interface Business {
  businessId: number;
  businessName: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  description: string;
}

export interface BusinessRequest {
  businessName: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  description: string;
}
