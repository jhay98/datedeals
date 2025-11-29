import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRequest } from '../../../core/models/user.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss']
})
export class UserCreateComponent implements OnInit {
  private userService = inject(UserService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

  businesses: Business[] = [];
  isLoadingBusinesses = false;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  user: UserRequest = {
    username: '',
    password: '',
    role: 'BUSINESS',
    businessId: undefined,
    enabled: true
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

  onRoleChange(): void {
    // Clear business selection if role is ADMIN
    if (this.user.role === 'ADMIN') {
      this.user.businessId = undefined;
    }
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.user.username || !this.user.password) {
      this.errorMessage = 'Username and password are required';
      return;
    }

    if (this.user.role === 'BUSINESS' && !this.user.businessId) {
      this.errorMessage = 'Please select a business for BUSINESS role users';
      return;
    }

    this.isSubmitting = true;

    // Prepare the user object
    const userToSubmit: UserRequest = {
      username: this.user.username,
      password: this.user.password,
      role: this.user.role,
      businessId: this.user.role === 'BUSINESS' ? this.user.businessId : undefined,
      enabled: this.user.enabled
    };

    this.userService.createUser(userToSubmit).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'User created successfully!';
        // Reset form
        this.user = {
          username: '',
          password: '',
          role: 'BUSINESS',
          businessId: undefined,
          enabled: true
        };
        // Navigate to users list after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/admin/users']);
        }, 2000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to create user';
        console.error('Error creating user:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
