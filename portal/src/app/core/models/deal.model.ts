export interface DealRequest {
  code: string;
  title: string;
  htmlVoucherTemplate: string | null;
  expiryDate: string | null;
  lifetimeDays: number | null;
  commissionPercentage: number | null;
  businessId: number;
}
