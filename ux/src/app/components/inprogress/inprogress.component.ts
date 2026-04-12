import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-inprogress',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './inprogress.component.html',
  styleUrls: ['./inprogress.component.css']
})
export class InprogressComponent {
  @Input() mode: 'page' | 'loading' = 'page';
  @Input() loadingTitle = 'Loading';
  @Input() loadingMessage = 'Preparing your workspace...';
  @Input() loadingHint = 'Please wait while we set things up.';

  readonly quote = 'Small progress each day compounds into big outcomes.';
}
