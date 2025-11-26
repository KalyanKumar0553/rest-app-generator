import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LocalStorageService } from '../../services/local-storage.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  userEmail: string = '';

  constructor(
    private router: Router,
    private localStorageService: LocalStorageService
  ) {}

  ngOnInit(): void {
    const userDetails = this.localStorageService.get('userDetails');
    if (userDetails && userDetails.email) {
      this.userEmail = userDetails.email;
    }
  }

  logout(): void {
    this.localStorageService.clear();
    this.router.navigate(['/']);
  }

  navigateToAccount(): void {
    console.log('Navigate to Account');
  }

  navigateToPlan(): void {
    console.log('Navigate to Plan');
  }
}
