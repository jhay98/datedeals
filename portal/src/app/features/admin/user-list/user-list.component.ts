import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
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

  businesses: Business[] = [];
  users: User[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingUsers = false;
  errorMessage = '';

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
    if (this.selectedBusinessId) {
      this.loadUsers();
    } else {
      this.users = [];
    }
  }

  loadUsers(): void {
    if (!this.selectedBusinessId) return;
    
    this.isLoadingUsers = true;
    this.userService.getUsersByBusinessId(this.selectedBusinessId).subscribe({
      next: (data) => {
        this.users = data;
        this.isLoadingUsers = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load users';
        this.isLoadingUsers = false;
        console.error('Error loading users:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
