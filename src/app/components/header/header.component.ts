import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ScrollService } from '../../services/scroll.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  activeSection = 'home';
  isMenuOpen = false;
  isTransitioning = false;
  currentRoute = '';
  private subscription: Subscription = new Subscription();

  navigationItems = [
    { id: 'home', label: 'Home' },
    { id: 'about', label: 'About' },
    { id: 'services', label: 'Services' },
    { id: 'testimonials', label: 'Testimonials' }
  ];

  constructor(
    private scrollService: ScrollService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Track current route
    this.subscription.add(
      this.router.events.pipe(
        filter(event => event instanceof NavigationEnd)
      ).subscribe((event: NavigationEnd) => {
        this.currentRoute = event.url;
      })
    );
    
    // Subscribe to active section changes
    this.subscription.add(
      this.scrollService.activeSection$.subscribe(section => {
        this.activeSection = section;
      })
    );
    
    // Initialize scroll listener after view is ready
    setTimeout(() => {
      this.initializeScrollDetection();
    }, 1000);
  }

  /**
   * Initialize scroll detection for active section highlighting
   */
  private initializeScrollDetection(): void {
    // Force initial active section detection
    const ionContent = document.querySelector('ion-content');
    if (ionContent) {
      ionContent.getScrollElement().then(scrollElement => {
        // Trigger initial scroll detection
        const event = new Event('scroll');
        scrollElement.dispatchEvent(event);
      });
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Navigate to a specific section
   * @param sectionId - The ID of the section to navigate to
   */
  navigateToSection(sectionId: string): void {
    // If not on home page, navigate to home first
    if (this.currentRoute !== '/') {
      this.router.navigate(['/']).then(() => {
        setTimeout(() => {
          this.scrollService.scrollToElement(sectionId);
        }, 100);
      });
      return;
    }
    
    // Close mobile menu first
    this.isMenuOpen = false;
    
    // Small delay to ensure menu closes before scrolling
    setTimeout(() => {
      this.scrollService.scrollToElement(sectionId);
    }, 100);
  }

  /**
   * Handle click events for navigation
   */
  onNavClick(event: Event, sectionId: string): void {
    event.preventDefault();
    event.stopPropagation();
    this.navigateToSection(sectionId);
    this.isMenuOpen = false;
  }

  /**
   * Navigate to home page
   */
  public navigateToHome(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/']);
  }

  /**
   * Toggle mobile menu
   */
  toggleMenu(): void {
    if (this.isMenuOpen) {
      // Closing menu
      this.isTransitioning = true;
      setTimeout(() => {
        this.isMenuOpen = false;
        this.isTransitioning = false;
      }, 300);
    } else {
      // Opening menu
      this.isMenuOpen = true;
      this.isTransitioning = true;
      setTimeout(() => {
        this.isTransitioning = false;
      }, 300);
    }
  }

  /**
   * Close mobile menu when clicking outside
   */
  closeMenu(): void {
    this.isMenuOpen = false;
  }

  /**
   * Navigate to intake form page
   */
  navigateToIntake(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/scheduling']);
  }
}