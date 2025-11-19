import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CouponService } from '../../../core/services/coupon.service';
import { AuthService } from '../../../core/services/auth.service';
import { Coupon } from '../../../core/models/coupon.model';

@Component({
  selector: 'app-business-coupons',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './coupons.component.html',
  styleUrls: ['./coupons.component.scss']
})
export class BusinessCouponsComponent implements OnInit {
  private couponService = inject(CouponService);
  private authService = inject(AuthService);
  private router = inject(Router);

  coupons: Coupon[] = [];
  isLoading = false;
  errorMessage = '';
  businessName = '';

  ngOnInit(): void {
    this.businessName = this.authService.getBusinessName() || 'Unknown Business';
    this.loadCoupons();
  }

  loadCoupons(): void {
    const businessId = this.authService.getBusinessId();
    if (!businessId) {
      this.errorMessage = 'Business ID not found';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.couponService.getCouponsByBusiness(businessId).subscribe({
      next: (data) => {
        this.coupons = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load coupons';
        this.isLoading = false;
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
        // Update the coupon in the list
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
