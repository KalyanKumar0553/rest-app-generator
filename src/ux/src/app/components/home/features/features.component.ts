import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BannerComponent } from '../banner/banner.component';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule, BannerComponent],
  templateUrl: './features.component.html',
  styleUrls: ['./features.component.css']
})
export class FeaturesComponent implements OnInit, OnDestroy {
  currentIndex = 0;
  private autoSlideTimer?: ReturnType<typeof setInterval>;

  features = [
    {
      number: '1.',
      title: 'Launch from the hero workflow',
      description: 'Use the same banner-section flow you had before, now as a dedicated slide in the carousel.',
      visual: 'banner',
      reverse: false
    },
    {
      number: '2.',
      title: 'Project and database settings',
      description: 'Select the package name, the Java Version, the Maven or Gradle. Add the name of your first database model. Create and manage multiple entities and define relationships.',
      visual: 'mockup',
      image: 'assets/images/home-carousel/slide-2-mockup-content.png',
      reverse: false
    },
    {
      number: '3.',
      title: 'Define your database schema',
      description: 'Create your entities and databases as simple, fast and managed as you want. Add fields, REST API or a complete CRUD for the entity. Define all model properties and relations required for later application building.',
      visual: 'schema',
      image: 'assets/images/home-carousel/slide-3-visual-mockup.png',
      reverse: true
    },
    {
      number: '4.',
      title: 'Explore and download your code',
      description: 'When you are done and happy with the result, download the complete package. Just extract it and import the project to your IDE for further testing and work.',
      visual: 'code',
      image: 'assets/images/home-carousel/slide-4-code-preview.png',
      reverse: false
    }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.startAutoSlide();
  }

  ngOnDestroy(): void {
    this.stopAutoSlide();
  }

  nextSlide(): void {
    this.currentIndex = (this.currentIndex + 1) % this.features.length;
    this.restartAutoSlide();
  }

  previousSlide(): void {
    this.currentIndex = (this.currentIndex - 1 + this.features.length) % this.features.length;
    this.restartAutoSlide();
  }

  goToSlide(index: number): void {
    this.currentIndex = index;
    this.restartAutoSlide();
  }

  startProject(): void {
    this.router.navigate(['/project-generation']);
  }

  onVisualImageError(event: Event): void {
    const image = event.target as HTMLImageElement | null;
    if (!image) {
      return;
    }
    image.style.display = 'none';
  }

  pauseAutoSlide(): void {
    this.stopAutoSlide();
  }

  resumeAutoSlide(): void {
    this.restartAutoSlide();
  }

  restartAutoSlide(): void {
    this.stopAutoSlide();
    this.startAutoSlide();
  }

  private startAutoSlide(): void {
    this.autoSlideTimer = setInterval(() => this.nextSlide(), 5000);
  }

  private stopAutoSlide(): void {
    if (!this.autoSlideTimer) {
      return;
    }
    clearInterval(this.autoSlideTimer);
    this.autoSlideTimer = undefined;
  }
}
