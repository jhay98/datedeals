import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { DealService } from '../../../core/services/deal.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { DealRequest } from '../../../core/models/deal.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-deal-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './deal-create.component.html',
  styleUrls: ['./deal-create.component.scss']
})
export class DealCreateComponent implements OnInit {
  private dealService = inject(DealService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

  businesses: Business[] = [];
  isLoadingBusinesses = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  deal: DealRequest = {
    code: '',
    title: '',
    htmlVoucherTemplate: null,
    expiryDate: null,
    lifetimeDays: null,
    commissionPercentage: null,
    businessId: 0
  };

  ngOnInit(): void {
    this.loadBusinesses();
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

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.deal.code || !this.deal.title || !this.deal.businessId) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    this.isSubmitting = true;

    // Prepare the deal object
    const dealToSubmit: DealRequest = {
      ...this.deal,
      expiryDate: this.deal.expiryDate || null,
      lifetimeDays: this.deal.lifetimeDays || null,
      commissionPercentage: this.deal.commissionPercentage || null,
      htmlVoucherTemplate: this.deal.htmlVoucherTemplate || null
    };

    this.dealService.createDeal(dealToSubmit).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Deal created successfully!';
        // Reset form
        this.deal = {
          code: '',
          title: '',
          htmlVoucherTemplate: null,
          expiryDate: null,
          lifetimeDays: null,
          commissionPercentage: null,
          businessId: 0
        };
        // Navigate to deals list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/deals']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to create deal';
        console.error('Error creating deal:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
