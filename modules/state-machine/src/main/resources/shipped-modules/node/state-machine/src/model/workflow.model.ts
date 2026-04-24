export type WorkflowStateModel = {
  id: string;
  label: string;
};

export type WorkflowTransitionModel = {
  from: string;
  event: string;
  to: string;
};

export type WorkflowInstanceModel = {
  entityId: string;
  currentState: string;
  history: string[];
};
