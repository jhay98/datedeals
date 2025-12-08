import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRequest } from '../../../core/models/user.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  private userService = inject(UserService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  userId: number = 0;
  businesses: Business[] = [];
  isLoading = true;
  isLoadingBusinesses = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  confirmPassword = '';

  user: UserRequest & { password?: string } = {
    username: '',
    password: '',
    role: 'BUSINESS',
    businessId: undefined,
    enabled: true
  };

  ngOnInit(): void {
    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBusinesses();
    this.loadUser();
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

  loadUser(): void {
    this.userService.getUserById(this.userId).subscribe({
      next: (data) => {
        this.user = {
          username: data.username,
          password: '', // Keep empty for editing
          role: data.role,
          businessId: data.business?.businessId,
          enabled: data.enabled
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load user details';
        this.isLoading = false;
        console.error('Error loading user:', error);
      }
    });
  }

  onRoleChange(): void {
    // Clear business selection if role is ADMIN
    if (this.user.role === 'ADMIN') {
      this.user.businessId = undefined;
    }
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.user.username) {
      this.errorMessage = 'Username is required';
      return;
    }

    if (this.user.password && this.user.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    if (this.user.role === 'BUSINESS' && !this.user.businessId) {
      this.errorMessage = 'Please select a business for BUSINESS role users';
      return;
    }

    this.isSubmitting = true;

    // Prepare the user object - only include password if it was changed
    const userToSubmit: any = {
      username: this.user.username,
      role: this.user.role,
      businessId: this.user.role === 'BUSINESS' ? this.user.businessId : undefined,
      enabled: this.user.enabled
    };

    // Only include password if it was changed
    if (this.user.password && this.user.password.trim()) {
      userToSubmit.password = this.user.password;
    }

    this.userService.updateUser(this.userId, userToSubmit).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'User updated successfully!';
        // Navigate to users list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/users']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to update user';
        console.error('Error updating user:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
