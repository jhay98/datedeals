export interface DealRequest {
  code: string;
  title: string;
  htmlVoucherTemplate: string | null;
  expiryDate: string | null;
  lifetimeDays: number | null;
  commissionPercentage: number | null;
  businessId: number;
}

export interface Deal {
  dealId: number;
  code: string;
  title: string;
  htmlVoucherTemplate: string | null;
  expiryDate: string | null;
  lifetimeDays: number | null;
  commissionPercentage: number | null;
  business: {
    businessId: number;
    businessName: string;
    contactEmail: string;
    contactPhone: string;
    address: string;
    description: string;
  };
}
