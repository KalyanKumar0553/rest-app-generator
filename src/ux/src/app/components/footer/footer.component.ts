import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  
  currentYear = new Date().getFullYear();
  
  footerData = {
    companyName: 'QuadProSol',
    links: [
      { text: 'Schedule Appointment', href: '#contact' },
      { text: 'Complete Intake', href: '#services' }
    ],
    websiteCredit: 'B2B'
  };

  constructor(
    private router: Router
  ) {
    // Theme toggle disabled for now
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

}