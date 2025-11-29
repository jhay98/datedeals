import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CouponService } from '../../../core/services/coupon.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Coupon } from '../../../core/models/coupon.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-admin-coupons',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './coupons.component.html',
  styleUrls: ['./coupons.component.scss']
})
export class AdminCouponsComponent implements OnInit {
  private couponService = inject(CouponService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  Math = Math;

  businesses: Business[] = [];
  coupons: Coupon[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingCoupons = false;
  errorMessage = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 25, 50, 100];

  ngOnInit(): void {
    this.loadBusinesses();
    
    // Check if businessId is in query params
    this.route.queryParams.subscribe(params => {
      if (params['businessId']) {
        this.selectedBusinessId = parseInt(params['businessId'], 10);
        this.loadCoupons();
      }
    });
  }

  loadBusinesses(): void {
    this.isLoadingBusinesses = true;
    this.businessService.getAllBusinesses().subscribe({
      next: (data) => {
        this.businesses = data;
        this.isLoadingBusinesses = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load businesses';
        this.isLoadingBusinesses = false;
        console.error('Error loading businesses:', error);
      }
    });
  }

  onBusinessChange(): void {
    this.currentPage = 0;
    if (this.selectedBusinessId) {
      this.loadCoupons();
    } else {
      this.coupons = [];
      this.totalElements = 0;
      this.totalPages = 0;
    }
  }

  loadCoupons(): void {
    if (!this.selectedBusinessId) {
      return;
    }

    this.isLoadingCoupons = true;
    this.errorMessage = '';

    this.couponService.getCouponsByBusinessPaginated(
      this.selectedBusinessId,
      this.currentPage,
      this.pageSize,
      'couponId',
      'DESC'
    ).subscribe({
      next: (response) => {
        this.coupons = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.pageNumber;
        this.isLoadingCoupons = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load coupons';
        this.isLoadingCoupons = false;
        console.error('Error loading coupons:', error);
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCoupons();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadCoupons();
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

  redeemCoupon(coupon: Coupon): void {
    if (coupon.redeemed) {
      return;
    }

    if (!confirm(`Are you sure you want to redeem coupon ${coupon.couponCode}?`)) {
      return;
    }

    this.couponService.redeemCoupon(coupon.couponId).subscribe({
      next: (updatedCoupon) => {
        const index = this.coupons.findIndex(c => c.couponId === updatedCoupon.couponId);
        if (index !== -1) {
          this.coupons[index] = updatedCoupon;
        }
      },
      error: (error) => {
        alert('Failed to redeem coupon');
        console.error('Error redeeming coupon:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
