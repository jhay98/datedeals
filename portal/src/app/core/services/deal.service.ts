import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DealRequest, Deal } from '../models/deal.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DealService {
  private http = inject(HttpClient);

  createDeal(deal: DealRequest): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/deal`,
      deal
    );
  }

  getAllDeals(): Observable<Deal[]> {
    return this.http.get<Deal[]>(
      `${environment.apiUrl}/deal/all`
    );
  }

  getDealsByBusinessId(businessId: number): Observable<Deal[]> {
    return this.http.get<Deal[]>(
      `${environment.apiUrl}/deal/business/${businessId}`
    );
  }
}
