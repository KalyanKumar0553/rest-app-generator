import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthResponse, AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="oauth-callback">
      <p class="oauth-callback__message">Completing sign-in...</p>
    </section>
  `,
  styles: [`
    .oauth-callback {
      min-height: 40vh;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--theme-modal-body-text);
    }

    .oauth-callback__message {
      margin: 0;
      font-size: 1rem;
      color: var(--theme-neutral-text);
    }
  `]
})
export class OauthCallbackComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const error = params.get('error');
      if (error) {
        this.toastService.error(decodeURIComponent(error));
        this.router.navigate(['/'], { replaceUrl: true });
        return;
      }

      const accessToken = params.get('accessToken') ?? undefined;
      const refreshToken = params.get('refreshToken') ?? undefined;
      const email = params.get('email') ?? undefined;

      if (!accessToken || !email) {
        this.toastService.error('OAuth sign-in response is incomplete.');
        this.router.navigate(['/'], { replaceUrl: true });
        return;
      }

      const response: AuthResponse = {
        accessToken,
        refreshToken,
        user: {
          id: params.get('userId') ?? undefined,
          email,
          name: params.get('name') ?? undefined,
          role: params.get('role') ?? undefined
        },
        message: 'Login successful!'
      };

      this.authService.completeExternalAuth(response);
      this.toastService.success(response.message || 'Login successful!');
      this.router.navigate(['/user/dashboard'], { replaceUrl: true });
    });
  }
}
