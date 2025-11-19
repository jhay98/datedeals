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

  businesses: Business[] = [];
  coupons: Coupon[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingCoupons = false;
  errorMessage = '';

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
    if (this.selectedBusinessId) {
      this.loadCoupons();
    } else {
      this.coupons = [];
    }
  }

  loadCoupons(): void {
    if (!this.selectedBusinessId) {
      return;
    }

    this.isLoadingCoupons = true;
    this.errorMessage = '';

    this.couponService.getCouponsByBusiness(this.selectedBusinessId).subscribe({
      next: (data) => {
        this.coupons = data;
        this.isLoadingCoupons = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load coupons';
        this.isLoadingCoupons = false;
        console.error('Error loading coupons:', error);
      }
    });
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
