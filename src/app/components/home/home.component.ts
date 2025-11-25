import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BannerComponent } from './banner/banner.component';
import { FeaturesComponent } from './features/features.component';
import { BenefitsComponent } from './benefits/benefits.component';
import { CtaComponent } from './cta/cta.component';
import { SignupComponent } from './signup/signup.component';
import { LoginModalComponent } from '../login-modal/login-modal.component';
import { ModalService } from '../../services/modal.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    BannerComponent,
    FeaturesComponent,
    BenefitsComponent,
    CtaComponent,
    SignupComponent,
    LoginModalComponent
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  showLoginModal = false;
  private modalSubscription?: Subscription;

  constructor(private modalService: ModalService) {}

  ngOnInit(): void {
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