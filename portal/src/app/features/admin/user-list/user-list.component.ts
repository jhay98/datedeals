import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);
  private router = inject(Router);

  Math = Math;

  businesses: Business[] = [];
  users: User[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingUsers = false;
  errorMessage = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  pageSizeOptions = [10, 25, 50, 100];

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

  onBusinessChange(): void {
    this.errorMessage = '';
    this.currentPage = 0;
    if (this.selectedBusinessId) {
      this.loadUsers();
    } else {
      this.users = [];
      this.totalElements = 0;
      this.totalPages = 0;
    }
  }

  loadUsers(): void {
    if (!this.selectedBusinessId) return;
    
    this.isLoadingUsers = true;
    this.userService.getUsersByBusinessIdPaginated(
      this.selectedBusinessId,
      this.currentPage,
      this.pageSize,
      'userId',
      'ASC'
    ).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.pageNumber;
        this.isLoadingUsers = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load users';
        this.isLoadingUsers = false;
        console.error('Error loading users:', error);
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadUsers();
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

  editUser(userId: number): void {
    this.router.navigate(['/admin/users/edit', userId]);
  }

  deleteUser(userId: number): void {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      this.userService.deleteUser(userId).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete user';
          console.error('Error deleting user:', error);
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
