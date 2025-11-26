import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DealService } from '../../../core/services/deal.service';
import { BusinessService } from '../../../core/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Deal } from '../../../core/models/deal.model';
import { Business } from '../../../core/models/business.model';

@Component({
  selector: 'app-deal-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './deal-list.component.html',
  styleUrls: ['./deal-list.component.scss']
})
export class DealListComponent implements OnInit {
  private dealService = inject(DealService);
  private businessService = inject(BusinessService);
  private authService = inject(AuthService);

  businesses: Business[] = [];
  deals: Deal[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingDeals = false;
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
      this.loadDeals();
    } else {
      this.deals = [];
    }
  }

  loadDeals(): void {
    if (!this.selectedBusinessId) return;
    
    this.isLoadingDeals = true;
    this.dealService.getDealsByBusinessId(this.selectedBusinessId).subscribe({
      next: (data) => {
        this.deals = data;
        this.isLoadingDeals = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load deals';
        this.isLoadingDeals = false;
        console.error('Error loading deals:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
