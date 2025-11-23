import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface Service {
  id: string;
  title: string;
  description: string;
  image: string;
  iconClass: string;
}

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent {

  constructor(private router: Router) {}

  sectionData = {
    label: 'INNOVATIVE IT SOLUTIONS',
    title: 'Empowering your digital journey.',
  };

  /**
   * Services offered by QuadProSol
   */
  services: Service[] = [
    {
      id: 'custom-software',
      title: 'Custom software development',
      description: 'Tailored software solutions for unique business needs.',
      image: 'https://images.pexels.com/photos/3184465/pexels-photo-3184465.jpeg',
      iconClass: 'bi bi-laptop'
    },
    {
      id: 'cloud-solutions',
      title: 'Cloud solutions',
      description: 'Efficient and scalable cloud services for businesses.',
      image: 'https://images.pexels.com/photos/2156881/pexels-photo-2156881.jpeg',
      iconClass: 'bi bi-cloud'
    },
    {
      id: 'seo-optimization',
      title: 'SEO optimization',
      description: 'Boost your online visibility with expert SEO strategies.',
      image: 'https://images.pexels.com/photos/270637/pexels-photo-270637.jpeg',
      iconClass: 'bi bi-graph-up-arrow'
    }
  ];

  /**
   * Handle service card click
   */
  onServiceClick(serviceId: string): void {
    switch (serviceId) {
      case 'custom-software':
        this.router.navigate(['/custom-software']);
        break;
      // Add more cases for other services as needed
      default:
        console.log(`Service ${serviceId} clicked - no specific route defined yet`);
    }
  }
}