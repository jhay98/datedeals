import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Coupon } from '../models/coupon.model';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page-response.model';

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

  getCouponsByBusinessPaginated(
    businessId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'couponId',
    sortDirection: string = 'DESC'
  ): Observable<PageResponse<Coupon>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<PageResponse<Coupon>>(
      `${environment.apiUrl}/coupon/business/${businessId}/paginated`,
      { params }
    );
  }

  redeemCoupon(couponId: number): Observable<Coupon> {
    return this.http.put<Coupon>(
      `${environment.apiUrl}/coupon/${couponId}/redeem`,
      {}
    );
  }
}
