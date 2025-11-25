import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  userId?: number;
  username: string;
  password?: string;
  role: 'ADMIN' | 'BUSINESS';
  business?: {
    businessId: number;
    businessName: string;
  };
  enabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(
      `${environment.apiUrl}/user/all`
    );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(
      `${environment.apiUrl}/user/${id}`
    );
  }

  createUser(user: Omit<User, 'userId'>): Observable<User> {
    return this.http.post<User>(
      `${environment.apiUrl}/user`,
      user
    );
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(
      `${environment.apiUrl}/user/${id}`,
      user
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/user/${id}`
    );
  }
}
