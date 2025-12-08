import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
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
  private router = inject(Router);

  Math = Math;

  businesses: Business[] = [];
  deals: Deal[] = [];
  selectedBusinessId: number | null = null;
  isLoadingBusinesses = false;
  isLoadingDeals = false;
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
      this.loadDeals();
    } else {
      this.deals = [];
      this.totalElements = 0;
      this.totalPages = 0;
    }
  }

  loadDeals(): void {
    if (!this.selectedBusinessId) return;
    
    this.isLoadingDeals = true;
    this.dealService.getDealsByBusinessIdPaginated(
      this.selectedBusinessId,
      this.currentPage,
      this.pageSize,
      'dealId',
      'ASC'
    ).subscribe({
      next: (response) => {
        this.deals = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.pageNumber;
        this.isLoadingDeals = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load deals';
        this.isLoadingDeals = false;
        console.error('Error loading deals:', error);
      }
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadDeals();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadDeals();
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

  editDeal(dealId: number): void {
    this.router.navigate(['/admin/deals/edit', dealId]);
  }

  deleteDeal(dealId: number): void {
    if (confirm('Are you sure you want to delete this deal? This action cannot be undone.')) {
      this.dealService.deleteDeal(dealId).subscribe({
        next: () => {
          this.loadDeals();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete deal';
          console.error('Error deleting deal:', error);
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
