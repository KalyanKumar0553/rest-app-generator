import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeroSectionComponent } from '../hero-section/hero-section.component';
import { FeatureSectionComponent } from '../feature-section/feature-section.component';
import { BenefitsSectionComponent } from '../benefits-section/benefits-section.component';
import { CtaSectionComponent } from '../cta-section/cta-section.component';
import { SignupSectionComponent } from '../signup-section/signup-section.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    HeroSectionComponent,
    FeatureSectionComponent,
    BenefitsSectionComponent,
    CtaSectionComponent,
    SignupSectionComponent
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  constructor() {}
}