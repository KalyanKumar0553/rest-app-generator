import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface OauthProgressState {
  visible: boolean;
  title: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class OauthProgressService {
  private readonly stateSubject = new BehaviorSubject<OauthProgressState>({
    visible: false,
    title: 'Request in progress',
    message: 'Request in progress.'
  });

  readonly state$ = this.stateSubject.asObservable();

  show(title: string, message: string = 'Request in progress.'): void {
    this.stateSubject.next({
      visible: true,
      title,
      message
    });
  }

  hide(): void {
    this.stateSubject.next({
      visible: false,
      title: 'Request in progress',
      message: 'Request in progress.'
    });
  }
}
