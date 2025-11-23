import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent {

  constructor(private router: Router) {}

  /**
   * About section data
   */
  aboutData = {
    sectionTitle: 'EMPOWERING YOUR DIGITAL JOURNEY',
    title: 'Innovative IT solutions for growth',
    description: `At QuadProSol, we specialize in delivering cutting-edge 
    technology solutions tailored to your unique business needs. Our team of 
    experts is dedicated to empowering organizations by optimizing their digital 
    landscape, enhancing productivity, and driving growth. We bring a wealth of 
    experience and passion to every project, ensuring that our clients not only 
    meet their goals but exceed them. Partner with us to unlock your potential 
    and transform your IT capabilities today!`,
    ctaText: 'Get in touch',
    image: 'https://images.pexels.com/photos/3184639/pexels-photo-3184639.jpeg'
  };

  /**
   * Navigate to intake form when Get in touch button is clicked
   */
  navigateToIntake(): void {
    this.router.navigate(['/intake']);
  }
}