import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-business-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './business-list.component.html',
  styleUrls: ['./business-list.component.scss']
})
export class BusinessListComponent implements OnInit {
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

  Math = Math;

  businesses: Business[] = [];
  isLoading = false;
  errorMessage = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 25, 50, 100];

  ngOnInit(): void {
    this.loadBusinesses();
  }

  loadBusinesses(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.businessService.getAllBusinessesPaginated(
      this.currentPage,
      this.pageSize,
      'businessId',
      'ASC'
    ).subscribe({
      next: (response) => {
        this.businesses = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.pageNumber;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load businesses';
        this.isLoading = false;
        console.error('Error loading businesses:', error);
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadBusinesses();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadBusinesses();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.onPageChange(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.onPageChange(this.currentPage + 1);
    }
  }

  viewCoupons(businessId: number): void {
    this.router.navigate(['/admin/coupons'], { queryParams: { businessId } });
  }

  editBusiness(businessId: number): void {
    this.router.navigate(['/admin/businesses/edit', businessId]);
  }

  deleteBusiness(businessId: number): void {
    if (confirm('Are you sure you want to delete this business? This action cannot be undone.')) {
      this.businessService.deleteBusiness(businessId).subscribe({
        next: () => {
          this.loadBusinesses();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete business';
          console.error('Error deleting business:', error);
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
