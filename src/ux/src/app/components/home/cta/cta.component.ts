import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { APP_SETTINGS } from '../../../settings/app-settings';

@Component({
  selector: 'app-cta',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cta.component.html',
  styleUrls: ['./cta.component.css']
})
export class CtaComponent {
  readonly appSettings = APP_SETTINGS;
}
