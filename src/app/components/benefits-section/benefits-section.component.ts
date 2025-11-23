import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-benefits-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './benefits-section.component.html',
  styleUrls: ['./benefits-section.component.css']
})
export class BenefitsSectionComponent {
  benefits = [
    {
      icon: '⏱',
      title: 'Save tons of time',
      description: 'Generate working code of Java RESTful or persistent APIs and download that JAR - All managed from production.'
    },
    {
      icon: '📋',
      title: 'Follow best practices',
      description: 'The generated code is complete throughout all Adaptor for React. Just import to your IDE all projects & use.'
    },
    {
      icon: '🔧',
      title: 'Keep code minimalistic',
      description: 'Only generates the code beginning through the entire production of your project and download production tools.'
    },
    {
      icon: '🚀',
      title: 'Stronger Spring Initializer',
      description: 'Create powerful & feature-rich apps beyond the standard Spring Initializer capabilities.'
    },
    {
      icon: '✏',
      title: 'Make updates with ease',
      description: 'Get updates for your project in the downloads section updated tools and apply new features.'
    },
    {
      icon: '🎯',
      title: 'Put DevOps in focus',
      description: 'Customise the generated code to the fully-featured side Tutorial just leverage each feature!'
    }
  ];
}
