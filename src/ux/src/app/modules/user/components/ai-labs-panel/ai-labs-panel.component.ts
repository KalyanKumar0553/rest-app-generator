import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AiLabsAvailability, AiLabsJobStatus, AiLabsService } from '../../../../services/ai-labs.service';
import { NoDataStateComponent } from '../../../../components/shared/no-data-state/no-data-state.component';
import { ToastService } from '../../../../services/toast.service';
import { resolveProjectGenerationRoute } from '../../../project-generation/utils/project-generation-route.utils';

@Component({
  selector: 'app-ai-labs-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatTableModule, MatTooltipModule, NoDataStateComponent],
  templateUrl: './ai-labs-panel.component.html',
  styleUrls: ['./ai-labs-panel.component.css']
})
export class AiLabsPanelComponent implements OnDestroy {
  aiPrompt = '';
  readonly aiLabsHelpText = 'Turn a product idea into a saved project draft using AI-assisted project planning.';
  isAiLabsEnabled = true;
  aiLabsAvailability: AiLabsAvailability = {
    enabled: true,
    usedCount: 0,
    usageLimit: null,
    remainingCount: null,
    limitReached: false
  };
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

  ngOnInit(): void {
    this.aiLabsService.getAvailability().subscribe({
      next: (response) => {
        this.aiLabsAvailability = {
          enabled: !!response?.enabled,
          usedCount: Number(response?.usedCount ?? 0),
          usageLimit: response?.usageLimit ?? null,
          remainingCount: response?.remainingCount ?? null,
          limitReached: !!response?.limitReached
        };
        this.isAiLabsEnabled = this.aiLabsAvailability.enabled;
      },
      error: () => {
        this.isAiLabsEnabled = false;
        this.aiLabsAvailability = {
          enabled: false,
          usedCount: 0,
          usageLimit: null,
          remainingCount: null,
          limitReached: false
        };
      }
    });
  }

  startAiProjectGeneration(): void {
    if (!this.isAiLabsEnabled) {
      this.toastService.error('AI Labs is not configured for this environment.');
      return;
    }
    if (this.aiLabsAvailability.limitReached) {
      this.toastService.error(this.limitReachedMessage);
      return;
    }
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
        this.applyUsageConsumption();
        this.subscribeToAiJob(response.jobId);
      },
      error: (error) => {
        this.isStartingAiJob = false;
        if (error?.message === this.limitReachedMessage) {
          this.aiLabsAvailability = {
            ...this.aiLabsAvailability,
            limitReached: true,
            remainingCount: 0
          };
        }
        this.toastService.error(error?.message || 'Failed to start AI project generation.');
      }
    });
  }

  get canGenerateCode(): boolean {
    return this.isAiLabsEnabled && !this.aiLabsAvailability.limitReached && !this.isStartingAiJob;
  }

  get usageSummaryText(): string {
    if (!this.isAiLabsEnabled) {
      return 'AI Labs is currently unavailable.';
    }
    if (this.aiLabsAvailability.usageLimit == null) {
      return `${this.aiLabsAvailability.usedCount} AI-assisted generations used.`;
    }
    return `${this.aiLabsAvailability.usedCount} of ${this.aiLabsAvailability.usageLimit} uses consumed.`;
  }

  get usagePillText(): string {
    if (this.aiLabsAvailability.usageLimit == null) {
      return 'Unlimited';
    }
    return `${this.aiLabsAvailability.remainingCount ?? 0} left`;
  }

  get limitReachedMessage(): string {
    return 'Limit reached please subscribe for more Usage';
  }

  get limitReachedHelpText(): string {
    return 'Your AI Labs usage limit has been reached. Upgrade or subscribe for more usage.';
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

  private applyUsageConsumption(): void {
    const usageLimit = this.aiLabsAvailability.usageLimit;
    const usedCount = this.aiLabsAvailability.usedCount + 1;
    const remainingCount = usageLimit == null ? null : Math.max((this.aiLabsAvailability.remainingCount ?? usageLimit) - 1, 0);
    this.aiLabsAvailability = {
      ...this.aiLabsAvailability,
      usedCount,
      remainingCount,
      limitReached: usageLimit == null ? false : remainingCount === 0
    };
  }
}
