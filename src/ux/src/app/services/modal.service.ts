import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  private showLoginModalSubject = new BehaviorSubject<boolean>(false);
  public showLoginModal$: Observable<boolean> = this.showLoginModalSubject.asObservable();

  openLoginModal(): void {
    this.showLoginModalSubject.next(true);
  }

  closeLoginModal(): void {
    this.showLoginModalSubject.next(false);
  }
}
