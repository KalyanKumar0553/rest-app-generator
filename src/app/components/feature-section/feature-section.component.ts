import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-feature-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feature-section.component.html',
  styleUrls: ['./feature-section.component.css']
})
export class FeatureSectionComponent {
  features = [
    {
      number: '1.',
      title: 'Project and database settings',
      description: 'Select the package name, the Java Version, the Maven or Gradle. Add the name of your first database model. Create and manage multiple entities and define relationships.',
      visual: 'mockup',
      reverse: false,
      hasButton: false
    },
    {
      number: '2.',
      title: 'Define your database schema',
      description: 'Create your entities and databases as simple, fast and managed as you want. Add fields, REST API or a complete CRUD for the entity. Define all model properties and relations required for later application building.',
      visual: 'schema',
      reverse: true,
      hasButton: false
    },
    {
      number: '3.',
      title: 'Explore and download your code',
      description: 'When you are done and happy with the result, download the complete package. Just extract it and import the project to your IDE for further testing and work.',
      visual: 'code',
      reverse: false,
      hasButton: true,
      buttonText: 'Start Project',
      buttonType: 'primary'
    },
    {
      number: '4.',
      title: 'Unlock advanced features',
      description: 'Bring your full production app online always Upgrade to Premium and unlock additional features, get access to live servers generator, microservice support, test. Develop your application with the technical API integration.',
      visual: 'icon',
      reverse: true,
      hasButton: true,
      buttonText: 'Get Project',
      buttonType: 'secondary'
    }
  ];
}
