import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { BusinessRequest } from '../../../core/models/business.model';

@Component({
  selector: 'app-business-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './business-edit.component.html',
  styleUrls: ['./business-edit.component.scss']
})
export class BusinessEditComponent implements OnInit {
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  businessId: number = 0;
  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  business: BusinessRequest = {
    businessName: '',
    contactEmail: '',
    contactPhone: '',
    address: '',
    description: ''
  };

  ngOnInit(): void {
    this.businessId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBusiness();
  }

  loadBusiness(): void {
    this.businessService.getBusinessById(this.businessId).subscribe({
      next: (data) => {
        this.business = {
          businessName: data.businessName,
          contactEmail: data.contactEmail,
          contactPhone: data.contactPhone,
          address: data.address,
          description: data.description
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load business details';
        this.isLoading = false;
        console.error('Error loading business:', error);
      }
    });
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.business.businessName || !this.business.contactEmail || 
        !this.business.contactPhone || !this.business.address || 
        !this.business.description) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    // Basic email validation
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.business.contactEmail)) {
      this.errorMessage = 'Please enter a valid email address';
      return;
    }

    this.isSubmitting = true;

    this.businessService.updateBusiness(this.businessId, this.business).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Business updated successfully!';
        // Navigate to businesses list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/businesses']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to update business';
        console.error('Error updating business:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
