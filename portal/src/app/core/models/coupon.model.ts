export interface Coupon {
  couponId: number;
  couponCode: string;
  purchasePrice: number;
  valuePrice: number;
  issueDate: string;
  redeemDate: string | null;
  expireDate: string;
  redeemed: boolean;
  dealTitle: string;
  dealId: number;
  businessName: string;
  businessId: number;
}
