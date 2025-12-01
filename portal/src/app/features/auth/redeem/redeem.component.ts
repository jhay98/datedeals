import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CouponService } from '../../../core/services/coupon.service';
import { Coupon } from '../../../core/models/coupon.model';

@Component({
  selector: 'app-redeem',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './redeem.component.html',
  styleUrls: ['./redeem.component.scss']
})
export class RedeemComponent implements OnInit {
  private couponService = inject(CouponService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  couponCode = '';
  coupon: Coupon | null = null;
  errorMessage = '';
  successMessage = '';
  isLoading = false;
  isRedeeming = false;

  ngOnInit(): void {
    this.couponCode = this.route.snapshot.paramMap.get('couponCode') || '';
    if (this.couponCode) {
      this.loadCoupon();
    } else {
      this.errorMessage = 'Invalid coupon code';
    }
  }

  loadCoupon(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.couponService.getCouponByCode(this.couponCode).subscribe({
      next: (coupon) => {
        this.isLoading = false;
        this.coupon = coupon;
        
        // Check if already redeemed
        if (coupon.redeemed) {
          this.errorMessage = 'This coupon has already been redeemed';
        }
        
        // Check if expired
        if (coupon.expireDate && new Date(coupon.expireDate) < new Date()) {
          this.errorMessage = 'This coupon has expired';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Coupon not found or invalid';
      }
    });
  }

  onConfirmRedeem(): void {
    if (!this.coupon || this.coupon.redeemed) {
      return;
    }

    this.isRedeeming = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.couponService.redeemCouponByCode(this.couponCode).subscribe({
      next: (redeemedCoupon) => {
        this.isRedeeming = false;
        this.coupon = redeemedCoupon;
        this.successMessage = 'Coupon redeemed successfully!';
      },
      error: (error) => {
        this.isRedeeming = false;
        this.errorMessage = error.error?.message || 'Failed to redeem coupon. Please try again.';
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/']);
  }
}
