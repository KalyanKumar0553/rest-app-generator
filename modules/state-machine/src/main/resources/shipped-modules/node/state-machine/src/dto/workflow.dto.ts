export type WorkflowStateDto = {
  id: string;
  label: string;
};

export type WorkflowSummaryDto = {
  workflowName: string;
  states: WorkflowStateDto[];
};
