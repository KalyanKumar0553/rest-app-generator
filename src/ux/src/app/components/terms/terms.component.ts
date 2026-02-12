import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { APP_SETTINGS } from '../../settings/app-settings';

@Component({
  selector: 'app-terms',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.css']
})
export class TermsComponent {
  readonly appSettings = APP_SETTINGS;

  constructor(private router: Router) {}

  goBack(): void {
    this.router.navigate(['/']);
  }
}
