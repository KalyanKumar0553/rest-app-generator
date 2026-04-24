export type WorkflowModuleConfig = {
  workflowName?: string;
  initialState?: string;
  states?: Array<{
    id: string;
    label: string;
  }>;
  transitions?: Array<{
    from: string;
    event: string;
    to: string;
  }>;
  allowSelfTransitions?: boolean;
};

export const resolveWorkflowConfig = (config: Record<string, unknown>): Required<WorkflowModuleConfig> => ({
  workflowName: typeof config.workflowName === 'string' && config.workflowName.trim()
    ? config.workflowName.trim()
    : 'default-workflow',
  initialState: typeof config.initialState === 'string' && config.initialState.trim()
    ? config.initialState.trim()
    : 'draft',
  states: Array.isArray(config.states) && config.states.length
    ? config.states.map((state) => ({
        id: String((state as Record<string, unknown>).id ?? 'state'),
        label: String((state as Record<string, unknown>).label ?? 'State')
      }))
    : [
        { id: 'draft', label: 'Draft' },
        { id: 'in_review', label: 'In Review' },
        { id: 'published', label: 'Published' }
      ],
  transitions: Array.isArray(config.transitions) && config.transitions.length
    ? config.transitions.map((transition) => ({
        from: String((transition as Record<string, unknown>).from ?? 'draft'),
        event: String((transition as Record<string, unknown>).event ?? 'advance'),
        to: String((transition as Record<string, unknown>).to ?? 'in_review')
      }))
    : [
        { from: 'draft', event: 'submit', to: 'in_review' },
        { from: 'in_review', event: 'approve', to: 'published' },
        { from: 'in_review', event: 'reject', to: 'draft' }
      ],
  allowSelfTransitions: typeof config.allowSelfTransitions === 'boolean' ? config.allowSelfTransitions : false
});
