import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AnimationService {
  private observedElements = new Set<Element>();
  private hasInitialAnimationRun = false;

  constructor() {
    if (typeof window !== 'undefined') {
      this.initIntersectionObserver();
    }
  }

  /**
   * Initialize intersection observer for animation triggers
   */
  private initIntersectionObserver(): void {
    const options = {
      root: null,
      rootMargin: '0px 0px -10% 0px', // Trigger when element is 10% into viewport
      threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        // Only animate if this is the initial load and element hasn't been animated yet
        if (entry.isIntersecting && 
            !this.observedElements.has(entry.target) && 
            !this.hasInitialAnimationRun) {
          
          entry.target.classList.add('animate-in');
          this.observedElements.add(entry.target);
        }
      });
      
      // Mark initial animation as complete after first intersection check
      if (!this.hasInitialAnimationRun) {
        setTimeout(() => {
          this.hasInitialAnimationRun = true;
        }, 100);
      }
    }, options);

    // Observe all elements with animate-on-scroll class
    setTimeout(() => {
      const elements = document.querySelectorAll('.animate-on-scroll');
      elements.forEach(el => observer.observe(el));
    }, 100);
  }

  /**
   * Add animation classes to elements that should animate on scroll
   * This method now only runs animations on initial load
   */
  setupScrollAnimations(): void {
    if (typeof window !== 'undefined') {
      // Reset the animation state for new component
      this.hasInitialAnimationRun = false;
      this.observedElements.clear();
      
      setTimeout(() => {
        const elements = document.querySelectorAll('.animate-on-scroll');
        const observer = new IntersectionObserver((entries) => {
          entries.forEach(entry => {
            // Only animate on initial load
            if (entry.isIntersecting && 
                !this.observedElements.has(entry.target) && 
                !this.hasInitialAnimationRun) {
              
              entry.target.classList.add('animate-in');
              this.observedElements.add(entry.target);
            }
          });
          
          // Mark initial animation as complete
          if (!this.hasInitialAnimationRun) {
            setTimeout(() => {
              this.hasInitialAnimationRun = true;
            }, 100);
          }
        }, { 
          threshold: 0.1,
          rootMargin: '0px 0px -10% 0px'
        });

        elements.forEach(el => observer.observe(el));
      }, 100);
    }
  }

  /**
   * Reset animation state (useful for route changes)
   */
  resetAnimationState(): void {
    this.hasInitialAnimationRun = false;
    this.observedElements.clear();
  }
}