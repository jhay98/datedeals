import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DealRequest, Deal } from '../models/deal.model';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page-response.model';

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

  getDealsByBusinessIdPaginated(
    businessId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'dealId',
    sortDirection: string = 'ASC'
  ): Observable<PageResponse<Deal>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<PageResponse<Deal>>(
      `${environment.apiUrl}/deal/business/${businessId}/paginated`,
      { params }
    );
  }

  getDealById(id: number): Observable<Deal> {
    return this.http.get<Deal>(
      `${environment.apiUrl}/deal/${id}`
    );
  }

  updateDeal(id: number, deal: DealRequest): Observable<Deal> {
    return this.http.put<Deal>(
      `${environment.apiUrl}/deal/${id}`,
      deal
    );
  }

  deleteDeal(id: number): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/deal/${id}`
    );
  }
}
