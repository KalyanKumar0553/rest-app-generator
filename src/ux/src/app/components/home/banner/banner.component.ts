import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { StartProjectDialogComponent } from '../../start-project-dialog/start-project-dialog.component';
import { resolveProjectGenerationRoute } from '../../../modules/project-generation/utils/project-generation-route.utils';

@Component({
  selector: 'app-banner',
  standalone: true,
  imports: [CommonModule, StartProjectDialogComponent],
  templateUrl: './banner.component.html',
  styleUrls: ['./banner.component.css']
})
export class BannerComponent {
  showStartProjectDialog = false;

  constructor(private router: Router) {}

  startProject(): void {
    this.showStartProjectDialog = true;
  }

  proceedToProject(language: 'java' | 'node' | 'python'): void {
    this.showStartProjectDialog = false;
    this.router.navigate([resolveProjectGenerationRoute(language)]);
  }

  cancelStartProject(): void {
    this.showStartProjectDialog = false;
  }

  onDecorativeImageError(event: Event): void {
    const image = event.target as HTMLImageElement | null;
    if (!image) {
      return;
    }
    image.style.display = 'none';
  }
}
