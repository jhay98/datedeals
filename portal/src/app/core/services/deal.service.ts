import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DealRequest } from '../models/deal.model';
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
}
