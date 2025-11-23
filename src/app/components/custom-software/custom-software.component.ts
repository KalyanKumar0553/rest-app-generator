import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AnimationService } from '../../services/animation.service';

interface FeatureCard {
  id: string;
  title: string;
  description: string;
  iconClass: string;
}

interface ProcessStep {
  id: string;
  title: string;
  description: string;
  image: string;
}

@Component({
  selector: 'app-custom-software',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './custom-software.component.html',
  styleUrls: ['./custom-software.component.css']
})
export class CustomSoftwareComponent implements AfterViewInit {

  constructor(
    private animationService: AnimationService,
    private router: Router
  ) {}

  ngAfterViewInit(): void {
    // Setup scroll animations after view initialization
    this.animationService.setupScrollAnimations();
  }

  // FAQ state management
  expandedFaq: string | null = null;

  /**
   * Toggle FAQ item expansion
   */
  toggleFaq(faqId: string): void {
    this.expandedFaq = this.expandedFaq === faqId ? null : faqId;
  }

  /**
   * Check if FAQ item is expanded
   */
  isFaqExpanded(faqId: string): boolean {
    return this.expandedFaq === faqId;
  }

  /**
   * Navigate to intake form
   */
  navigateToIntake(): void {
    this.router.navigate(['/intake']);
  }
  pageData = {
    title: 'WHY CHOOSE QUADPROSOL ?', // Keep for fallback, but now using HTML structure
    description: `Enterprise challenges require enterprise-level thinking. At QuadProSol, we combine deep technical expertise, 
    proven enterprise experience, and a results-driven mindset to become a trusted partner—not just a service provider.`,
    ctaText: 'SEE WHAT QUADPROSOL CAN DO FOR YOU',
    ctaIcon: 'bi bi-arrow-right'
  };

  processData = {
    title: 'OUR ENTERPRISE DELIVERY FRAMEWORK',
    description: `From early-stage planning to post-launch optimization, we guide you through every phase of the software journey. Our structured, transparent process ensures every solution we build is aligned with your goals, scalable across teams, and built to last.`
  };

  features: FeatureCard[] = [
    {
      id: 'expert-talent',
      title: 'Expert Talent',
      description: `We hire with purpose—through a rigorous, multi-stage process that ensures every 
      team member brings both technical excellence and strong communication skills.`,
      iconClass: 'bi bi-people'
    },
    {
      id: 'enterprise-mindset',
      title: 'Enterprise Mindset',
      description: `With years of experience working in IT Industry, we understand your workflows and 
      tailor ours to integrate smoothly with your enterprise ecosystem.`,
      iconClass: 'bi bi-building'
    },
    {
      id: 'outcome-focused',
      title: 'Outcome Focused',
      description: `We don't just complete tasks—we dive deep to understand your goals and deliver solutions 
      that move the needle for your business.`,
      iconClass: 'bi bi-bullseye'
    },
    {
      id: 'built-for-impact',
      title: 'Built for Impact',
      description: `Our expertise runs deep, and so do our processes. We're a good fit for companies looking for a 
      thoughtful, strategic, and long-term tech partner.`,
      iconClass: 'bi bi-lightning'
    }
  ];

  processSteps: ProcessStep[] = [
    {
      id: 'discovery',
      title: 'Discovery & Ideation',
      description: `We start by understanding your business, users, and challenges. Through stakeholder interviews and workshops, we shape the product vision, identify opportunities, and define success metrics.`,
      image: 'https://images.pexels.com/photos/3184465/pexels-photo-3184465.jpeg'
    },
    {
      id: 'analysis',
      title: 'Technical & Business Analysis',
      description: `Our team conducts a deep dive into technical requirements, business constraints, and integration needs. This phase includes risk assessments, solution architecture drafts, and infrastructure planning.`,
      image: 'https://images.pexels.com/photos/3184639/pexels-photo-3184639.jpeg'
    },
    {
      id: 'design',
      title: 'UX/UI Design',
      description: `We translate strategy into intuitive user flows and high-fidelity prototypes—balancing enterprise-grade functionality with clean, user-friendly design that supports real adoption.`,
      image: 'https://images.pexels.com/photos/196644/pexels-photo-196644.jpeg'
    },
    {
      id: 'architecture',
      title: 'Architecture & Roadmapping',
      description: `We define the technical architecture, break down the solution into milestones, and prepare the delivery roadmap—ensuring scalability, flexibility, and alignment with your internal systems.`,
      image: 'https://images.pexels.com/photos/270348/pexels-photo-270348.jpeg'
    },
    {
      id: 'development',
      title: 'Agile Development',
      description: `Our engineers build in sprints, ensuring transparency, speed, and adaptability. We focus on quality, performance, and security—shipping features that bring real value with every release.`,
      image: 'https://images.pexels.com/photos/574071/pexels-photo-574071.jpeg'
    },
    {
      id: 'testing',
      title: 'Testing & Validation',
      description: `We run comprehensive functional, integration, security, and performance testing to ensure your platform works flawlessly in real-world enterprise environments.`,
      image: 'https://images.pexels.com/photos/3184292/pexels-photo-3184292.jpeg'
    },
    {
      id: 'deployment',
      title: 'Deployment & Launch',
      description: `We support smooth go-lives across cloud or on-prem environments, prepare infrastructure, train internal teams, and ensure every system is ready for production use.`,
      image: 'https://images.pexels.com/photos/607812/pexels-photo-607812.jpeg'
    },
    {
      id: 'support',
      title: 'Post-Launch Support',
      description: `Our job doesn't end at launch. We provide proactive monitoring, rapid issue resolution, and long-term support to keep your system stable, secure, and evolving.`,
      image: 'https://images.pexels.com/photos/3184360/pexels-photo-3184360.jpeg'
    }
  ];

  faqData = {
    title: 'F.A.Q.',
    subtitle: 'Do you have additional questions?'
  };

  faqs = [
    {
      id: 'enterprise-app-company',
      question: 'What Is an Enterprise App Development Company?',
      answer: `An enterprise app development company specializes in creating large-scale, complex software solutions for businesses. We focus on building robust, secure, and scalable applications that can handle high volumes of users and data while integrating seamlessly with existing enterprise systems.`
    },
    {
      id: 'development-process',
      question: 'What Is the Enterprise Application Development Process?',
      answer: `Our enterprise development process includes discovery and planning, technical analysis, architecture design, agile development, comprehensive testing, deployment, and ongoing support. We follow industry best practices to ensure your application meets enterprise standards for security, performance, and scalability.`
    },
    {
      id: 'software-solutions',
      question: 'What Enterprise Software Solution Do You Offer?',
      answer: `We offer custom enterprise software development, cloud solutions, legacy system modernization, API development and integration, mobile enterprise applications, data analytics platforms, and enterprise resource planning (ERP) systems tailored to your specific business needs.`
    },
    {
      id: 'how-developers-work',
      question: 'How Do Enterprise Software Developers Work for a Company?',
      answer: `Our enterprise developers work as an extension of your team, following your processes and methodologies. We provide dedicated teams, regular communication, transparent reporting, and flexible engagement models to ensure seamless collaboration and successful project delivery.`
    }
  ];
}