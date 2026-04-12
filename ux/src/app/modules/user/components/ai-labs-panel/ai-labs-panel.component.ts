import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AiLabsAvailability, AiLabsJobStatus, AiLabsJobSummary, AiLabsService } from '../../../../services/ai-labs.service';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { NoDataStateComponent } from '../../../../components/shared/no-data-state/no-data-state.component';
import { ToastService } from '../../../../services/toast.service';
import { resolveProjectGenerationRoute } from '../../../project-generation/utils/project-generation-route.utils';

@Component({
  selector: 'app-ai-labs-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatExpansionModule, MatIconModule, MatTableModule, MatTooltipModule, ModalComponent, NoDataStateComponent],
  templateUrl: './ai-labs-panel.component.html',
  styleUrls: ['./ai-labs-panel.component.css']
})
export class AiLabsPanelComponent implements OnInit, OnDestroy {
  aiPrompt = '';
  readonly aiLabsHelpText = 'Turn a product idea into a saved project draft using AI-assisted project planning.';
  readonly ideationHelpText = 'Describe the product idea, target users, and core capabilities you want BootRid to turn into a saved project draft.';
  readonly historyHelpText = 'Review every AI Labs generation, inspect its backend stages, and reopen details when you need to troubleshoot or confirm what was created.';
  isAiLabsEnabled = true;
  aiLabsAvailability: AiLabsAvailability = {
    enabled: true,
    usedCount: 0,
    usageLimit: null,
    remainingCount: null,
    limitReached: false
  };
  isStartingAiJob = false;
  isLoadingHistory = false;
  aiJob: AiLabsJobStatus | null = null;
  aiHistory: AiLabsJobSummary[] = [];
  aiJobColumns: string[] = ['label', 'status', 'message', 'updatedAt'];
  aiHistoryColumns: string[] = ['jobId', 'generatedOn', 'generatedBy', 'status', 'action'];
  isJobDetailsModalOpen = false;
  selectedHistoryJob: AiLabsJobStatus | null = null;
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
        if (this.isAiLabsEnabled) {
          this.loadHistory();
        }
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
        this.loadHistory();
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

  openJobDetails(jobId: string): void {
    this.aiLabsService.getJob(jobId).subscribe({
      next: (job) => {
        this.selectedHistoryJob = job;
        this.isJobDetailsModalOpen = true;
      },
      error: () => {
        this.toastService.error('Failed to load AI Labs job details.');
      }
    });
  }

  closeJobDetails(): void {
    this.isJobDetailsModalOpen = false;
    this.selectedHistoryJob = null;
  }

  refreshHistory(): void {
    this.loadHistory(true);
  }

  trackByJobId(_: number, job: AiLabsJobSummary): string {
    return job.jobId;
  }

  get hasHistory(): boolean {
    return this.aiHistory.length > 0;
  }

  get activeJobSummaryText(): string {
    if (!this.aiJob) {
      return 'No AI generation is running right now.';
    }
    return this.aiJob.status === 'COMPLETED'
      ? 'Latest generation completed and the saved project is ready.'
      : this.aiJob.status === 'FAILED'
        ? 'Latest generation stopped before the project could be completed.'
        : 'Latest generation is still in progress.';
  }

  private subscribeToAiJob(jobId: string): void {
    this.aiLabsService.getJob(jobId).subscribe({
      next: (job) => {
        this.aiJob = job;
        this.syncHistoryItem(job);
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
        this.syncHistoryItem(payload);
        if (payload.status === 'COMPLETED' && payload.projectId) {
          this.loadHistory();
          this.closeAiJobEvents();
          this.toastService.success('AI project generated successfully.');
          const route = resolveProjectGenerationRoute(payload.generator);
          this.router.navigate([route], { queryParams: { projectId: payload.projectId } });
          return;
        }
        if (payload.status === 'FAILED') {
          this.loadHistory();
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

  private loadHistory(showToast = false): void {
    this.isLoadingHistory = true;
    this.aiLabsService.listJobs().subscribe({
      next: (jobs) => {
        this.aiHistory = jobs ?? [];
        this.isLoadingHistory = false;
        if (showToast) {
          this.toastService.success('AI Labs history refreshed.');
        }
      },
      error: () => {
        this.isLoadingHistory = false;
        this.toastService.error('Failed to load AI Labs generation history.');
      }
    });
  }

  private syncHistoryItem(job: AiLabsJobStatus): void {
    const existingIndex = this.aiHistory.findIndex((item) => item.jobId === job.jobId);
    const summary: AiLabsJobSummary = {
      jobId: job.jobId,
      generatedBy: 'You',
      status: job.status,
      generator: job.generator,
      projectId: job.projectId,
      generatedOn: job.createdAt,
      updatedAt: job.updatedAt
    };
    if (existingIndex === -1) {
      this.aiHistory = [summary, ...this.aiHistory];
      return;
    }
    const nextHistory = [...this.aiHistory];
    nextHistory[existingIndex] = {
      ...nextHistory[existingIndex],
      ...summary
    };
    this.aiHistory = nextHistory;
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
