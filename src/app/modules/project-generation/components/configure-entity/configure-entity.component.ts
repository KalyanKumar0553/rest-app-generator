import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatButtonModule } from '@angular/material/button';
import { ModalComponent } from '../../../../components/modal/modal.component';
import { GeneralSettings, LombokSettings } from '../add-entity/entity-settings.model';

@Component({
  selector: 'app-configure-entity',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatButtonModule,
    ModalComponent
  ],
  templateUrl: './configure-entity.component.html',
  styleUrls: ['./configure-entity.component.css']
})
export class ConfigureEntityComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() useLombok = false;
  @Input() lombokSettings: LombokSettings = this.defaultLombokSettings();
  @Input() generalSettings: GeneralSettings = this.defaultGeneralSettings();
  @Output() save = new EventEmitter<{ useLombok: boolean; lombokSettings: LombokSettings; generalSettings: GeneralSettings }>();
  @Output() cancel = new EventEmitter<void>();

  localUseLombok = false;
  localLombokSettings: LombokSettings = this.defaultLombokSettings();
  localGeneralSettings: GeneralSettings = this.defaultGeneralSettings();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      this.syncLocalState();
    }
    if (changes['useLombok'] && !this.isOpen) {
      this.localUseLombok = this.useLombok;
    }
  }

  onSave(): void {
    this.save.emit({
      useLombok: this.localUseLombok,
      lombokSettings: { ...this.localLombokSettings },
      generalSettings: { ...this.localGeneralSettings }
    });
  }

  onCancel(): void {
    this.cancel.emit();
    this.syncLocalState();
  }

  private syncLocalState(): void {
    this.localUseLombok = this.useLombok;
    this.localLombokSettings = { ...this.lombokSettings };
    this.localGeneralSettings = { ...this.generalSettings };
  }

  private defaultLombokSettings(): LombokSettings {
    return {
      generateBuilder: false,
      generateToString: false,
      generateEqualsAndHashCode: false
    };
  }

  private defaultGeneralSettings(): GeneralSettings {
    return {
      softDelete: false,
      auditing: false,
      makeImmutable: false,
      naturalIdCache: false
    };
  }
}
