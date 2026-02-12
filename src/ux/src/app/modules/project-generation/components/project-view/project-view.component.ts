import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import JSZip from 'jszip';

interface ZipTreeNode {
  name: string;
  path: string;
  isDirectory: boolean;
  depth: number;
  expanded: boolean;
  children: ZipTreeNode[];
}

@Component({
  selector: 'app-project-view',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.css']
})
export class ProjectViewComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() isSyncing = false;
  @Input() zipBlob: Blob | null = null;
  @Input() zipFileName = 'project.zip';
  @Output() close = new EventEmitter<void>();
  @Output() reload = new EventEmitter<void>();

  zipTree: ZipTreeNode[] = [];
  visibleNodes: ZipTreeNode[] = [];
  selectedFilePath = '';
  selectedFileName = '';
  selectedFileContent = '';
  selectedFileError = '';
  selectedFileLineNumbers: number[] = [];
  editorScrollTop = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['isOpen'] || changes['zipBlob']) && this.isOpen && this.zipBlob) {
      this.prepareTree();
    }
  }

  onClose(): void {
    this.close.emit();
  }

  onReload(): void {
    if (this.isSyncing) {
      return;
    }
    this.reload.emit();
  }

  toggleNode(node: ZipTreeNode): void {
    if (!node.isDirectory) {
      return;
    }
    node.expanded = !node.expanded;
    this.rebuildVisibleNodes();
  }

  async selectFile(node: ZipTreeNode): Promise<void> {
    if (node.isDirectory || !this.zipBlob) {
      return;
    }

    this.selectedFilePath = node.path;
    this.selectedFileName = node.name;
    this.selectedFileContent = '';
    this.selectedFileError = '';
    this.editorScrollTop = 0;
    this.updateLineNumbers();

    try {
      const zip = await JSZip.loadAsync(this.zipBlob);
      const file = zip.file(node.path);
      if (!file) {
        this.selectedFileError = 'Unable to load selected file.';
        return;
      }

      if (!this.isTextFile(node.path)) {
        this.selectedFileError = 'Binary file preview is not available.';
        return;
      }

      this.selectedFileContent = await file.async('string');
      this.updateLineNumbers();
    } catch {
      this.selectedFileError = 'Failed to read file content.';
    }
  }

  downloadZip(): void {
    if (!this.zipBlob) {
      return;
    }
    const url = window.URL.createObjectURL(this.zipBlob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = this.zipFileName;
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    window.URL.revokeObjectURL(url);
  }

  onEditorScroll(event: Event): void {
    const target = event.target as HTMLTextAreaElement | null;
    this.editorScrollTop = target ? target.scrollTop : 0;
  }

  private async prepareTree(): Promise<void> {
    if (!this.zipBlob) {
      return;
    }

    this.selectedFileName = '';
    this.selectedFilePath = '';
    this.selectedFileContent = '';
    this.selectedFileError = '';
    this.editorScrollTop = 0;
    this.updateLineNumbers();

    try {
      const zip = await JSZip.loadAsync(this.zipBlob);
      this.zipTree = this.buildZipTree(zip);
      this.rebuildVisibleNodes();

      const firstFile = this.visibleNodes.find(node => !node.isDirectory);
      if (firstFile) {
        await this.selectFile(firstFile);
      } else {
        this.selectedFileError = 'No files found in zip.';
      }
    } catch {
      this.zipTree = [];
      this.visibleNodes = [];
      this.selectedFileError = 'Failed to parse zip content.';
      this.updateLineNumbers();
    }
  }

  private buildZipTree(zip: JSZip): ZipTreeNode[] {
    const root: ZipTreeNode = {
      name: this.zipFileName,
      path: '',
      isDirectory: true,
      depth: 0,
      expanded: true,
      children: []
    };

    Object.keys(zip.files).forEach((filePath) => {
      const file = zip.files[filePath];
      const normalized = file.name.endsWith('/') ? file.name.slice(0, -1) : file.name;
      if (!normalized) {
        return;
      }

      const segments = normalized.split('/').filter(Boolean);
      let current = root;
      let currentPath = '';

      segments.forEach((segment, index) => {
        currentPath = currentPath ? `${currentPath}/${segment}` : segment;
        const isLast = index === segments.length - 1;
        const shouldBeDirectory = !isLast || file.dir;

        let child = current.children.find(item => item.name === segment && item.path === currentPath);
        if (!child) {
          child = {
            name: segment,
            path: currentPath,
            isDirectory: shouldBeDirectory,
            depth: current.depth + 1,
            expanded: current.depth < 1,
            children: []
          };
          current.children.push(child);
        } else if (shouldBeDirectory) {
          child.isDirectory = true;
        }
        current = child;
      });
    });

    const sortTree = (nodes: ZipTreeNode[]) => {
      nodes.sort((left, right) => {
        if (left.isDirectory !== right.isDirectory) {
          return left.isDirectory ? -1 : 1;
        }
        return left.name.localeCompare(right.name);
      });
      nodes.forEach(node => sortTree(node.children));
    };
    sortTree(root.children);

    return root.children;
  }

  private rebuildVisibleNodes(): void {
    const visible: ZipTreeNode[] = [];
    const walk = (nodes: ZipTreeNode[]) => {
      nodes.forEach((node) => {
        visible.push(node);
        if (node.isDirectory && node.expanded) {
          walk(node.children);
        }
      });
    };
    walk(this.zipTree);
    this.visibleNodes = visible;
  }

  private isTextFile(path: string): boolean {
    const extension = (path.split('.').pop() ?? '').toLowerCase();
    const textExtensions = new Set([
      'java', 'kt', 'groovy', 'gradle', 'xml', 'yml', 'yaml', 'properties',
      'md', 'txt', 'sql', 'json', 'ts', 'js', 'html', 'css', 'scss', 'gitignore',
      'bat', 'sh'
    ]);
    if (path.endsWith('.gitignore') || path.endsWith('.env')) {
      return true;
    }
    return textExtensions.has(extension);
  }

  private updateLineNumbers(): void {
    const lineCount = this.selectedFileContent ? this.selectedFileContent.split('\n').length : 0;
    this.selectedFileLineNumbers = Array.from({ length: lineCount }, (_, i) => i + 1);
  }
}
