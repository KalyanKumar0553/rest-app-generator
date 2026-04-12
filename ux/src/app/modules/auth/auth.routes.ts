import { Routes } from '@angular/router';
import { OauthCallbackComponent } from './components/oauth-callback/oauth-callback.component';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'oauth/callback',
        component: OauthCallbackComponent
      }
    ]
  }
];
