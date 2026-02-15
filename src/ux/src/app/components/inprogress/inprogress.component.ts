import { Component } from '@angular/core';
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
  readonly quote = 'Small progress each day compounds into big outcomes.';
}
