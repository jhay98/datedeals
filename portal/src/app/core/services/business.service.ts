import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Business } from '../models/business.model';
import { environment } from '../../../environments/environment';

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
