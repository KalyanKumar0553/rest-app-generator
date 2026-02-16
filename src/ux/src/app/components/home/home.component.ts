import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeaturesComponent } from './features/features.component';
import { BenefitsComponent } from './benefits/benefits.component';
import { SignupComponent } from './signup/signup.component';
import { LoginModalComponent } from '../../modules/auth/components/login-modal/login-modal.component';
import { ModalService } from '../../services/modal.service';
import { VisitTrackingService } from '../../services/visit-tracking.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    FeaturesComponent,
    BenefitsComponent,
    SignupComponent,
    LoginModalComponent
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  showLoginModal = false;
  private modalSubscription?: Subscription;

  constructor(
    private modalService: ModalService,
    private visitTrackingService: VisitTrackingService
  ) {}

  ngOnInit(): void {
    this.visitTrackingService.trackHomeVisit().subscribe({
      error: () => {
        // Tracking must be non-blocking.
      }
    });

    this.modalSubscription = this.modalService.showLoginModal$.subscribe(
      (show) => {
        this.showLoginModal = show;
        if (show) {
          document.body.style.overflow = 'hidden';
        } else {
          document.body.style.overflow = '';
        }
      }
    );
  }

  ngOnDestroy(): void {
    if (this.modalSubscription) {
      this.modalSubscription.unsubscribe();
    }
    document.body.style.overflow = '';
  }

  closeLoginModal(): void {
    this.modalService.closeLoginModal();
  }
}
