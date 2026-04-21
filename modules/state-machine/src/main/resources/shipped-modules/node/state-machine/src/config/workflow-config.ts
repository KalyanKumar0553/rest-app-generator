export type WorkflowModuleConfig = {
  workflowName?: string;
  states?: Array<{
    id: string;
    label: string;
  }>;
};

export const resolveWorkflowConfig = (config: Record<string, unknown>): Required<WorkflowModuleConfig> => ({
  workflowName: typeof config.workflowName === 'string' && config.workflowName.trim()
    ? config.workflowName.trim()
    : 'default-workflow',
  states: Array.isArray(config.states) && config.states.length
    ? config.states.map((state) => ({
        id: String((state as Record<string, unknown>).id ?? 'state'),
        label: String((state as Record<string, unknown>).label ?? 'State')
      }))
    : [
        { id: 'draft', label: 'Draft' },
        { id: 'in_review', label: 'In Review' },
        { id: 'published', label: 'Published' }
      ]
});
