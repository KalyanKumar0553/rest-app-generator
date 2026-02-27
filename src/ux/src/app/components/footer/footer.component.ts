import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Theme, ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnDestroy {
  
  currentYear = new Date().getFullYear();
  currentTheme: Theme = 'light';
  private themeSubscription?: Subscription;
  
  footerData = {
    companyName: 'QuadProSol',
    links: [
      { text: 'Schedule Appointment', href: '#contact' },
      { text: 'Complete Intake', href: '#services' }
    ],
    websiteCredit: 'B2B'
  };

  constructor(
    private router: Router,
    private themeService: ThemeService
  ) {
    this.currentTheme = this.themeService.getCurrentTheme();
    this.themeSubscription = this.themeService.theme$.subscribe((theme) => {
      this.currentTheme = theme;
    });
  }

  /**
   * Handle footer link clicks
   */
  onLinkClick(event: Event, link: any): void {
    event.preventDefault();
    
    if (link.text === 'Complete Intake') {
      this.router.navigate(['/intake']);
    } else if (link.text === 'Schedule Appointment') {
      this.router.navigate(['/scheduling']);
    }
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  ngOnDestroy(): void {
    this.themeSubscription?.unsubscribe();
  }
}
