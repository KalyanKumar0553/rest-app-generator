import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthResponse, AuthService } from '../../../../services/auth.service';
import { ToastService } from '../../../../services/toast.service';
import { OauthProgressService } from '../../../../services/oauth-progress.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: ``
})
export class OauthCallbackComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService,
    private oauthProgressService: OauthProgressService
  ) {}

  ngOnInit(): void {
    this.oauthProgressService.show('Completing sign-in', 'Request in progress.');

    this.route.queryParamMap.subscribe((params) => {
      const error = params.get('error');
      if (error) {
        this.oauthProgressService.hide();
        this.toastService.error(decodeURIComponent(error));
        this.router.navigate(['/'], { replaceUrl: true });
        return;
      }

      const accessToken = params.get('accessToken') ?? undefined;
      const refreshToken = params.get('refreshToken') ?? undefined;
      const email = params.get('email') ?? undefined;

      if (!accessToken || !email) {
        this.oauthProgressService.hide();
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
      this.router.navigate(['/user/dashboard'], { replaceUrl: true }).then(() => {
        this.oauthProgressService.hide();
        this.toastService.success(response.message || 'Login successful!');
      });
    });
  }
}
