import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Coupon } from '../models/coupon.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CouponService {
  private http = inject(HttpClient);

  getCouponsByBusiness(businessId: number): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(
      `${environment.apiUrl}/coupon/business/${businessId}`
    );
  }

  redeemCoupon(couponId: number): Observable<Coupon> {
    return this.http.put<Coupon>(
      `${environment.apiUrl}/coupon/${couponId}/redeem`,
      {}
    );
  }
}
