import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-business-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './business-list.component.html',
  styleUrls: ['./business-list.component.scss']
})
export class BusinessListComponent implements OnInit {
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

  businesses: Business[] = [];
  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadBusinesses();
  }

  loadBusinesses(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.businessService.getAllBusinesses().subscribe({
      next: (data) => {
        this.businesses = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load businesses';
        this.isLoading = false;
        console.error('Error loading businesses:', error);
      }
    });
  }

  viewCoupons(businessId: number): void {
    this.router.navigate(['/admin/coupons'], { queryParams: { businessId } });
  }

  logout(): void {
    this.authService.logout();
  }
}
