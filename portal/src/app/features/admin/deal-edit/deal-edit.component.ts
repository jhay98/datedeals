import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { DealService } from '../../../core/services/deal.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { DealRequest } from '../../../core/models/deal.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-deal-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './deal-edit.component.html',
  styleUrls: ['./deal-edit.component.scss']
})
export class DealEditComponent implements OnInit {
  private dealService = inject(DealService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  dealId: number = 0;
  businesses: Business[] = [];
  isLoading = true;
  isLoadingBusinesses = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  expiryType: 'none' | 'date' | 'lifetime' = 'none';

  deal: DealRequest = {
    code: '',
    title: '',
    htmlVoucherTemplate: null,
    expiryDate: null,
    lifetimeDays: null,
    commissionPercentage: null,
    businessId: 0
  };

  templateVariablesHelp = `Available variables:
    {{couponCode}} - Coupon code
    {{dealTitle}} - Deal title
    {{businessName}} - Business name
    {{businessAddress}} - Business address
    {{businessEmail}} - Business email
    {{businessPhone}} - Business phone
    {{purchasePrice}} - Purchase price (formatted)
    {{valuePrice}} - Value price (formatted)
    {{issueDate}} - Issue date
    {{expireDate}} - Expiry date
    {{qrCode}} - QR code image`;
  
  templatePlaceholder = `Example:
<div>
  <h1>{{dealTitle}}</h1>
  <p>Code: {{couponCode}}</p>
  <p>Business: {{businessName}}</p>
  <p>Expires: {{expireDate}}</p>
  <img src='{{qrCode}}' />
</div>`;

  ngOnInit(): void {
    this.dealId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBusinesses();
    this.loadDeal();
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

  loadDeal(): void {
    this.dealService.getDealById(this.dealId).subscribe({
      next: (data) => {
        this.deal = {
          code: data.code,
          title: data.title,
          htmlVoucherTemplate: data.htmlVoucherTemplate,
          expiryDate: data.expiryDate,
          lifetimeDays: data.lifetimeDays,
          commissionPercentage: data.commissionPercentage,
          businessId: data.business.businessId
        };
        // Set expiryType based on existing data
        if (data.expiryDate) {
          this.expiryType = 'date';
        } else if (data.lifetimeDays) {
          this.expiryType = 'lifetime';
        } else {
          this.expiryType = 'none';
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load deal details';
        this.isLoading = false;
        console.error('Error loading deal:', error);
      }
    });
  }

  onExpiryTypeChange(): void {
    // Clear both fields when switching types
    this.deal.expiryDate = null;
    this.deal.lifetimeDays = null;
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
      expiryDate: this.expiryType === 'date' ? this.deal.expiryDate : null,
      lifetimeDays: this.expiryType === 'lifetime' ? this.deal.lifetimeDays : null,
      commissionPercentage: this.deal.commissionPercentage || null,
      htmlVoucherTemplate: this.deal.htmlVoucherTemplate || null
    };

    this.dealService.updateDeal(this.dealId, dealToSubmit).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Deal updated successfully!';
        // Navigate to deals list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/deals']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to update deal';
        console.error('Error updating deal:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
