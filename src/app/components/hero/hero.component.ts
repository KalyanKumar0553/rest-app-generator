import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollService } from '../../services/scroll.service';

interface CarouselSlide {
  id: number;
  title: string;
  subtitle: string;
  image: string;
  alt: string;
}

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hero.component.html',
  styleUrls: ['./hero.component.css']
})
export class HeroComponent implements OnInit {
  currentSlide = 0;
  autoSlideInterval: any;

  /**
   * Carousel slides data
   */
  slides: CarouselSlide[] = [
    {
      id: 1,
      title: 'Transform your IT solutions',
      subtitle: 'Delivering excellence in custom software development',
      image: 'https://images.pexels.com/photos/3184465/pexels-photo-3184465.jpeg',
      alt: 'Software development team working on computers'
    },
    {
      id: 2,
      title: 'Cloud solutions for modern business',
      subtitle: 'Scalable and secure cloud infrastructure services',
      image: 'https://images.pexels.com/photos/2156881/pexels-photo-2156881.jpeg',
      alt: 'Cloud computing and data visualization'
    },
    {
      id: 3,
      title: 'Expert IT consulting services',
      subtitle: 'Strategic guidance to optimize your technology',
      image: 'https://images.pexels.com/photos/3184292/pexels-photo-3184292.jpeg',
      alt: 'IT professionals in consultation meeting'
    }
  ];

  constructor(private scrollService: ScrollService) {}

  ngOnInit(): void {
    // Start auto-slide functionality
    this.startAutoSlide();
  }

  ngOnDestroy(): void {
    // Clean up interval
    if (this.autoSlideInterval) {
      clearInterval(this.autoSlideInterval);
    }
  }

  /**
   * Start automatic slide transition
   */
  startAutoSlide(): void {
    this.autoSlideInterval = setInterval(() => {
      this.nextSlide();
    }, 5000); // Change slide every 5 seconds
  }

  /**
   * Stop automatic slide transition
   */
  stopAutoSlide(): void {
    if (this.autoSlideInterval) {
      clearInterval(this.autoSlideInterval);
    }
  }

  /**
   * Go to next slide
   */
  nextSlide(): void {
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
  }

  /**
   * Go to previous slide
   */
  prevSlide(): void {
    this.currentSlide = this.currentSlide === 0 ? this.slides.length - 1 : this.currentSlide - 1;
  }

  /**
   * Go to specific slide
   */
  goToSlide(index: number): void {
    this.currentSlide = index;
  }

  /**
   * Get current slide data
   */
  getCurrentSlide(): CarouselSlide {
    return this.slides[this.currentSlide];
  }

  /**
   * Scroll to services section when CTA button is clicked
   */
  scrollToServices(): void {
    this.scrollService.scrollToElement('services');
  }
}