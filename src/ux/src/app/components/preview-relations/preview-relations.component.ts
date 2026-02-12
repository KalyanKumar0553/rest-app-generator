import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import cytoscape, { Core, ElementDefinition } from 'cytoscape';

interface PreviewRelation {
  sourceEntity: string;
  targetEntity: string;
  relationType?: string;
}

@Component({
  selector: 'app-preview-relations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './preview-relations.component.html',
  styleUrls: ['./preview-relations.component.css']
})
export class PreviewRelationsComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() relations: PreviewRelation[] = [];
  @Input() emptyMessage = 'No relations available to preview.';
  @Input() isOpen = false;

  @ViewChild('cyContainer') cyContainer?: ElementRef<HTMLDivElement>;

  private cy: Core | null = null;
  private viewReady = false;

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.renderGraph();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.viewReady) {
      return;
    }

    if (changes['isOpen']?.currentValue) {
      // Render after modal becomes visible so Cytoscape can measure container size.
      requestAnimationFrame(() => this.renderGraph());
      return;
    }

    if (changes['relations'] && this.isOpen) {
      this.renderGraph();
    }
  }

  ngOnDestroy(): void {
    this.destroyGraph();
  }

  get hasRelations(): boolean {
    return this.relations.length > 0;
  }

  private renderGraph(): void {
    if (!this.cyContainer?.nativeElement) {
      return;
    }

    if (!this.hasRelations) {
      this.destroyGraph();
      return;
    }

    const elements = this.buildElements(this.relations);
    const styles = this.resolveGraphTheme();

    this.destroyGraph();
    this.cy = cytoscape({
      container: this.cyContainer.nativeElement,
      elements,
      style: [
        {
          selector: 'node',
          style: {
            shape: 'round-rectangle',
            label: 'data(label)',
            'text-wrap': 'ellipsis',
            'text-max-width': '148px',
            'text-halign': 'center',
            'text-valign': 'center',
            'background-color': styles.nodeBg,
            'border-color': styles.nodeBorder,
            'border-width': 1,
            color: styles.nodeText,
            'font-size': 14,
            'font-weight': 600,
            width: 180,
            height: 72
          }
        },
        {
          selector: 'edge',
          style: {
            width: 2,
            'line-color': styles.edgeColor,
            'target-arrow-color': styles.edgeColor,
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            label: 'data(label)',
            'font-size': 12,
            'font-weight': 600,
            color: styles.edgeText,
            'text-background-color': styles.edgeLabelBg,
            'text-background-opacity': 0.95,
            'text-background-padding': '3px'
          }
        }
      ],
      layout: {
        name: 'breadthfirst',
        directed: true,
        spacingFactor: 1.25,
        padding: 30,
        animate: false
      }
    });

    this.cy.fit(undefined, 30);
  }

  private buildElements(relations: PreviewRelation[]): ElementDefinition[] {
    const entitySet = new Set<string>();
    for (const relation of relations) {
      if (relation.sourceEntity) {
        entitySet.add(relation.sourceEntity);
      }
      if (relation.targetEntity) {
        entitySet.add(relation.targetEntity);
      }
    }

    const nodes: ElementDefinition[] = Array.from(entitySet).map(entity => ({
      data: {
        id: entity,
        label: entity
      }
    }));

    const edges: ElementDefinition[] = relations
      .filter(relation => relation.sourceEntity && relation.targetEntity)
      .map((relation, index) => ({
        data: {
          id: `${relation.sourceEntity}-${relation.targetEntity}-${index}`,
          source: relation.sourceEntity,
          target: relation.targetEntity,
          label: this.getRelationEdgeLabel(relation.relationType)
        }
      }));

    return [...nodes, ...edges];
  }

  private getRelationEdgeLabel(relationType: string | undefined): string {
    switch (relationType) {
      case 'OneToOne':
        return '1-1';
      case 'OneToMany':
        return '1-*';
      case 'ManyToOne':
        return '*-1';
      case 'ManyToMany':
        return '*-*';
      default:
        return relationType?.trim() || '';
    }
  }

  private destroyGraph(): void {
    if (this.cy) {
      this.cy.destroy();
      this.cy = null;
    }
  }

  private resolveGraphTheme(): {
    nodeBg: string;
    nodeBorder: string;
    nodeText: string;
    edgeColor: string;
    edgeText: string;
    edgeLabelBg: string;
  } {
    const css = getComputedStyle(document.documentElement);
    return {
      nodeBg: this.readCssVar(css, '--color-background-secondary', '#e7eff1'),
      nodeBorder: this.readCssVar(css, '--color-text-secondary', '#4f5f66'),
      nodeText: this.readCssVar(css, '--color-text-primary', '#2f3b41'),
      edgeColor: this.readCssVar(css, '--color-text-secondary', '#7a8f97'),
      edgeText: this.readCssVar(css, '--neutral-700', '#334155'),
      edgeLabelBg: this.readCssVar(css, '--color-background-white', '#ffffff')
    };
  }

  private readCssVar(css: CSSStyleDeclaration, name: string, fallback: string): string {
    const value = css.getPropertyValue(name).trim();
    return value || fallback;
  }
}
