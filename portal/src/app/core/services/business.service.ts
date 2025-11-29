import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Business } from '../models/business.model';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class BusinessService {
  private http = inject(HttpClient);

  getAllBusinesses(): Observable<Business[]> {
    return this.http.get<Business[]>(
      `${environment.apiUrl}/business/all`
    );
  }

  getAllBusinessesPaginated(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'businessId',
    sortDirection: string = 'ASC'
  ): Observable<PageResponse<Business>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);

    return this.http.get<PageResponse<Business>>(
      `${environment.apiUrl}/business/all/paginated`,
      { params }
    );
  }

  getBusinessById(id: number): Observable<Business> {
    return this.http.get<Business>(
      `${environment.apiUrl}/business/${id}`
    );
  }

  createBusiness(business: Omit<Business, 'businessId'>): Observable<Business> {
    return this.http.post<Business>(
      `${environment.apiUrl}/business`,
      business
    );
  }

  updateBusiness(id: number, business: Partial<Business>): Observable<Business> {
    return this.http.put<Business>(
      `${environment.apiUrl}/business/${id}`,
      business
    );
  }

  deleteBusiness(id: number): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/business/${id}`
    );
  }
}
