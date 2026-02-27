import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Subscription } from 'rxjs';

interface DocSection {
  key: string;
  title: string;
  subtitle: string;
  points: string[];
}

@Component({
  selector: 'app-documentation',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, MatExpansionModule],
  templateUrl: './documentation.component.html',
  styleUrls: ['./documentation.component.css']
})
export class DocumentationComponent implements OnInit, OnDestroy {
  readonly sections: DocSection[] = [
    {
      key: 'general',
      title: 'General Tab',
      subtitle: 'Project backgrounds and setup essentials',
      points: [
        'Define project identity like group, artifact, build tool, and generator language.',
        'Set database type first (SQL, NoSQL, None), then choose engine-specific options.',
        'Enable OpenAPI, actuator, and API configuration from developer preferences.',
        'Use profiles and deployment options to tailor generated scaffolding.'
      ]
    },
    {
      key: 'actuator',
      title: 'Actuator Tab',
      subtitle: 'Configure runtime management endpoints',
      points: [
        'Choose which actuator endpoint groups should be exposed for each profile.',
        'Tune management visibility for health, metrics, info, and operational endpoints.',
        'Keep production-safe exposure defaults and relax only where required.',
        'Use this section to align observability endpoints with deployment needs.'
      ]
    },
    {
      key: 'entities',
      title: 'Entities Tab',
      subtitle: 'Design models and persistence schema',
      points: [
        'Create entities with fields, constraints, and optional REST/controller linkage.',
        'Add relations between entities with compatible field types.',
        'Configure class methods and additional model options from reusable selectors.',
        'Use quick actions on cards to edit, inspect, and delete entities.'
      ]
    },
    {
      key: 'dataObjects',
      title: 'Data Objects Tab',
      subtitle: 'Build DTOs, enums, and reusable contracts',
      points: [
        'Define request/response DTOs with constraints and class method options.',
        'Manage enums and reuse them in entity and DTO fields.',
        'Switch to mapper sub-tab to configure source-to-target field mappings.',
        'Preview compact cards and open detailed views for full field lists.'
      ]
    },
    {
      key: 'mappers',
      title: 'Mappers',
      subtitle: 'Map DTOs and entities safely',
      points: [
        'Select source and destination models and map compatible fields.',
        'Auto-map matching field names, then fine-tune with row-level controls.',
        'Save mapper specs into YAML and generate mapper classes in backend.',
        'Use mapping cards for quick edit/delete workflows.'
      ]
    },
    {
      key: 'controllers',
      title: 'Controllers Tab',
      subtitle: 'Configure API behavior and endpoint controls',
      points: [
        'Create reusable REST configurations and map them to entities when needed.',
        'Define endpoint selection, request/response contracts, pagination, and docs.',
        'Validate required fields before save and navigate to failing tab automatically.',
        'Manage multiple API configs from a table with edit/delete actions.'
      ]
    },
    {
      key: 'explore',
      title: 'Explore Tab',
      subtitle: 'Inspect generated project content',
      points: [
        'Open generated zip content directly in explorer view.',
        'Refresh synced output after generation runs.',
        'Explore availability follows zip-cache and generation-progress rules.',
        'Use this view for fast validation before downloading artifacts.'
      ]
    }
  ];

  activeKey = this.sections[0].key;
  private queryParamsSubscription: Subscription | null = null;

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.queryParamsSubscription = this.route.queryParamMap.subscribe((params) => {
      const section = this.resolveSectionKey(params.get('section'));
      if (section) {
        this.activeKey = section;
      }
    });
  }

  ngOnDestroy(): void {
    this.queryParamsSubscription?.unsubscribe();
    this.queryParamsSubscription = null;
  }

  get activeSection(): DocSection {
    return this.sections.find((section) => section.key === this.activeKey) ?? this.sections[0];
  }

  selectSection(key: string): void {
    this.activeKey = key;
  }

  startProject(): void {
    this.router.navigate(['/project-generation']);
  }

  private resolveSectionKey(rawSection: string | null): string | null {
    if (!rawSection) {
      return null;
    }

    const normalized = String(rawSection).trim();
    const aliases: Record<string, string> = {
      'data-objects': 'dataObjects',
      'data_objects': 'dataObjects'
    };
    const resolved = aliases[normalized] ?? normalized;
    const isValid = this.sections.some((section) => section.key === resolved);
    return isValid ? resolved : null;
  }
}
