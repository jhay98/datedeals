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
}
