import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Testimonial {
  id: string;
  name: string;
  position: string;
  company: string;
  content: string;
  avatar: string;
}

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonials.component.html',
  styleUrls: ['./testimonials.component.css']
})
export class TestimonialsComponent {

  sectionData = {
    label: 'WHAT OUR CLIENTS SAY',
    title: 'Hear from our satisfied customers'
  };

  /**
   * Client testimonials
   */
  testimonials: Testimonial[] = [
    {
      id: 'testimonial-1',
      name: 'Brandon Vega',
      position: 'CEO',
      company: 'Tech Innovations Ltd',
      content: `QuadProSol transformed our IT infrastructure, making us more efficient and secure. Their attention to detail and commitment to customer service set them apart from others in the industry.`,
      avatar: 'https://images.pexels.com/photos/2182970/pexels-photo-2182970.jpeg'
    },
    {
      id: 'testimonial-2',
      name: 'Chris Wei',
      position: 'CTO',
      company: 'Future Ventures Inc.',
      content: `Working with QuadProSol was a game-changer for our company. Their cloud solutions helped us streamline processes and significantly reduce costs.`,
      avatar: 'https://images.pexels.com/photos/2379004/pexels-photo-2379004.jpeg'
    },
    {
      id: 'testimonial-3',
      name: 'Karen Weiss',
      position: 'Director',
      company: 'Growth Partners LLC',
      content: `The team at QuadProSol delivers results. Their expertise and consultation allowed us to focus on growth and innovation.`,
      avatar: 'https://images.pexels.com/photos/3785079/pexels-photo-3785079.jpeg'
    }
  ];
}