import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { AiLabsJobStatus, AiLabsService } from '../../../../services/ai-labs.service';
import { ToastService } from '../../../../services/toast.service';
import { resolveProjectGenerationRoute } from '../../../project-generation/utils/project-generation-route.utils';

@Component({
  selector: 'app-ai-labs-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule],
  templateUrl: './ai-labs-panel.component.html',
  styleUrls: ['./ai-labs-panel.component.css']
})
export class AiLabsPanelComponent implements OnDestroy {
  aiPrompt = '';
  isStartingAiJob = false;
  aiJob: AiLabsJobStatus | null = null;
  aiJobColumns: string[] = ['label', 'status', 'message', 'updatedAt'];
  private aiJobEventsSource: EventSource | null = null;

  constructor(
    private router: Router,
    private aiLabsService: AiLabsService,
    private toastService: ToastService
  ) {}

  ngOnDestroy(): void {
    this.closeAiJobEvents();
  }

  startAiProjectGeneration(): void {
    const prompt = this.aiPrompt.trim();
    if (!prompt) {
      this.toastService.error('Enter an idea before generating a project.');
      return;
    }
    this.isStartingAiJob = true;
    this.aiJob = null;
    this.closeAiJobEvents();
    this.aiLabsService.generateProject(prompt).subscribe({
      next: (response) => {
        this.isStartingAiJob = false;
        this.subscribeToAiJob(response.jobId);
      },
      error: (error) => {
        this.isStartingAiJob = false;
        this.toastService.error(error?.message || 'Failed to start AI project generation.');
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(',', '');
  }

  private subscribeToAiJob(jobId: string): void {
    this.aiLabsService.getJob(jobId).subscribe({
      next: (job) => {
        this.aiJob = job;
        if (job.status === 'FAILED') {
          this.toastService.error(job.errorMessage || 'AI project generation failed.');
          this.closeAiJobEvents();
        }
      },
      error: () => {
        this.toastService.error('Failed to load AI Labs job status.');
      }
    });

    const source = new EventSource(this.aiLabsService.getJobEventsUrl(jobId), { withCredentials: true });
    source.addEventListener('status', (event: MessageEvent) => {
      try {
        const payload = JSON.parse(event.data) as AiLabsJobStatus;
        this.aiJob = payload;
        if (payload.status === 'COMPLETED' && payload.projectId) {
          this.closeAiJobEvents();
          this.toastService.success('AI project generated successfully.');
          const route = resolveProjectGenerationRoute(payload.generator);
          this.router.navigate([route], { queryParams: { projectId: payload.projectId } });
          return;
        }
        if (payload.status === 'FAILED') {
          this.toastService.error(payload.errorMessage || 'Error while generating the Project. Please try again');
          this.closeAiJobEvents();
        }
      } catch {
        this.toastService.error('Error while generating the Project. Please try again');
        this.closeAiJobEvents();
      }
    });
    source.onerror = () => {
      if (this.aiJob?.status !== 'COMPLETED' && this.aiJob?.status !== 'FAILED') {
        this.toastService.error('AI Labs status stream disconnected.');
      }
      this.closeAiJobEvents();
    };
    this.aiJobEventsSource = source;
  }

  private closeAiJobEvents(): void {
    this.aiJobEventsSource?.close();
    this.aiJobEventsSource = null;
  }
}
