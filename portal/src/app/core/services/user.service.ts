import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, UserRequest } from '../models/user.model';

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

  getUsersByBusinessId(businessId: number): Observable<User[]> {
    return this.http.get<User[]>(
      `${environment.apiUrl}/user/business/${businessId}`
    );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(
      `${environment.apiUrl}/user/${id}`
    );
  }

  createUser(user: UserRequest): Observable<User> {
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
