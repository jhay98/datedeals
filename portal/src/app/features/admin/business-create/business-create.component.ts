import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { BusinessRequest } from '../../../core/models/business.model';

@Component({
  selector: 'app-business-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './business-create.component.html',
  styleUrls: ['./business-create.component.scss']
})
export class BusinessCreateComponent {
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

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

    this.businessService.createBusiness(this.business).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'Business created successfully!';
        // Reset form
        this.business = {
          businessName: '',
          contactEmail: '',
          contactPhone: '',
          address: '',
          description: ''
        };
        // Navigate to businesses list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/businesses']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to create business';
        console.error('Error creating business:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
