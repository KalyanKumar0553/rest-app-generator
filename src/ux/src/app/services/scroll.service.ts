import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ScrollService {
  private activeSection = new BehaviorSubject<string>('home');
  public activeSection$ = this.activeSection.asObservable();
  private isScrolling = false;

  constructor() {
    // Listen for scroll events to update active section
    if (typeof window !== 'undefined') {
      this.setupScrollListener();
    }
  }

  /**
   * Setup scroll listener for both window and ion-content
   */
  private setupScrollListener(): void {
    // Listen to window scroll as fallback
    window.addEventListener('scroll', () => this.onScroll(), { passive: true });
    
    // Setup ion-content scroll listener
    setTimeout(() => {
      const ionContent = document.querySelector('ion-content');
      if (ionContent) {
        ionContent.getScrollElement().then(scrollElement => {
          scrollElement.addEventListener('scroll', () => this.onScroll(), { passive: true });
        });
      }
    }, 1000);
  }

  /**
   * Smooth scroll to a specific element by ID
   * @param elementId - The ID of the element to scroll to
   */
  scrollToElement(elementId: string): void {
    if (typeof window !== 'undefined') {
      this.isScrolling = true;
      
      // Use multiple attempts to find the element
      let attempts = 0;
      const maxAttempts = 10;
      
      const scrollToSection = () => {
        const element = document.getElementById(elementId);
        const ionContent = document.querySelector('ion-content');
        
        if (element) {
          const headerOffset = 80;
          
          if (ionContent) {
            // For Ionic content, use scrollToPoint
            const elementTop = element.offsetTop - headerOffset;
            ionContent.scrollToPoint(0, elementTop, 500);
            
            // Set active section immediately and stop auto-detection temporarily
            this.setActiveSection(elementId);
            setTimeout(() => {
              this.isScrolling = false;
            }, 600);
          } else {
            // Fallback to regular scroll
            const elementPosition = element.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
            
            window.scrollTo({
              top: offsetPosition,
              behavior: 'smooth'
            });
            
            // Set active section immediately and stop auto-detection temporarily
            this.setActiveSection(elementId);
            setTimeout(() => {
              this.isScrolling = false;
            }, 600);
          }
        } else if (attempts < maxAttempts) {
          attempts++;
          setTimeout(scrollToSection, 100);
        }
      };
      
      scrollToSection();
    }
  }

  /**
   * Set the active section manually
   * @param section - The section ID to set as active
   */
  setActiveSection(section: string): void {
    this.activeSection.next(section);
  }

  /**
   * Handle scroll events to determine active section
   */
  private onScroll(): void {
    // Don't update active section while programmatically scrolling
    if (this.isScrolling) {
      return;
    }
    
    if (typeof window !== 'undefined') {
      const sections = ['home', 'about', 'services', 'testimonials', 'contact'];
      const ionContent = document.querySelector('ion-content');
      
      if (ionContent) {
        // Get scroll position from ion-content
        ionContent.getScrollElement().then(scrollElement => {
          const scrollPosition = scrollElement.scrollTop + 150;
          this.updateActiveSection(sections, scrollPosition);
        });
      } else {
        // Fallback to window scroll
        const scrollPosition = window.scrollY + 150;
        this.updateActiveSection(sections, scrollPosition);
      }
    }
  }

  private updateActiveSection(sections: string[], scrollPosition: number): void {
    let currentSection = 'home';
    
    for (const section of sections) {
      const element = document.getElementById(section);
      if (element) {
        const elementTop = element.offsetTop;
        const elementHeight = element.offsetHeight;
        const elementBottom = elementTop + elementHeight;

        // Check if section is in viewport
        if (scrollPosition >= elementTop - 200) {
          currentSection = section;
        }
      }
    }
    
    // Only update if the section has actually changed
    if (this.activeSection.value !== currentSection) {
      this.setActiveSection(currentSection);
    }
  }
}