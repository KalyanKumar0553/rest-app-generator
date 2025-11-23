import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ContactIcon {
  id: string;
  name: string;
  icon: string;
  url: string;
  backgroundColor: string;
  hoverColor: string;
}

@Component({
  selector: 'app-floating-contact',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './floating-contact.component.html',
  styleUrls: ['./floating-contact.component.css']
})
export class FloatingContactComponent {

  contactIcons: ContactIcon[] = [
    {
      id: 'facebook',
      name: 'Facebook',
      icon: 'bi bi-facebook',
      url: 'https://facebook.com',
      backgroundColor: '#3b5998',
      hoverColor: '#2d4373'
    },
    {
      id: 'instagram',
      name: 'Instagram',
      icon: 'bi bi-instagram',
      url: 'https://instagram.com',
      backgroundColor: '#e4405f',
      hoverColor: '#c13584'
    },
    {
      id: 'twitter',
      name: 'Twitter/X',
      icon: 'bi bi-twitter-x',
      url: 'https://twitter.com',
      backgroundColor: '#000000',
      hoverColor: '#1a1a1a'
    },
    {
      id: 'linkedin',
      name: 'LinkedIn',
      icon: 'bi bi-linkedin',
      url: 'https://linkedin.com',
      backgroundColor: '#0077b5',
      hoverColor: '#005885'
    }
  ];

  /**
   * Open social media link in new tab
   */
  openLink(url: string): void {
    window.open(url, '_blank', 'noopener,noreferrer');
  }
}