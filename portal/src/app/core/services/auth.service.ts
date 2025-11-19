import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/login.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);
  
  private currentUserSubject = new BehaviorSubject<LoginResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    // Load user from localStorage on service initialization (only in browser)
    if (this.isBrowser) {
      const token = this.getToken();
      if (token) {
        const user = this.getUserFromToken(token);
        this.currentUserSubject.next(user);
      }
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${environment.apiUrl}/auth/login`,
      credentials
    ).pipe(
      tap(response => {
        if (this.isBrowser) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role);
          if (response.businessId) {
            localStorage.setItem('businessId', response.businessId.toString());
          }
          if (response.businessName) {
            localStorage.setItem('businessName', response.businessName);
          }
        }
        this.currentUserSubject.next(response);
      })
    );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('businessId');
      localStorage.removeItem('businessName');
    }
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('token') : null;
  }

  getRole(): string | null {
    return this.isBrowser ? localStorage.getItem('role') : null;
  }

  getBusinessId(): number | null {
    if (!this.isBrowser) return null;
    const businessId = localStorage.getItem('businessId');
    return businessId ? parseInt(businessId, 10) : null;
  }

  getBusinessName(): string | null {
    return this.isBrowser ? localStorage.getItem('businessName') : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isBusiness(): boolean {
    return this.getRole() === 'BUSINESS';
  }

  private getUserFromToken(token: string): LoginResponse {
    return {
      token,
      role: this.getRole() || '',
      businessId: this.getBusinessId(),
      businessName: this.getBusinessName()
    };
  }
}
