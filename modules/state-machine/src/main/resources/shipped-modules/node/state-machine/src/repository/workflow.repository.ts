import { prisma } from '../../../../src/lib/prisma';

import type { WorkflowInstanceModel, WorkflowStateModel, WorkflowTransitionModel } from '../model/workflow.model';

export class WorkflowRepository {
  async seedWorkflow(workflowName: string, states: WorkflowStateModel[]): Promise<void> {
    for (const state of states) {
      await prisma.workflowState.upsert({
        where: {
          workflowName_stateId: {
            workflowName,
            stateId: state.id
          }
        },
        update: {
          label: state.label
        },
        create: {
          workflowName,
          stateId: state.id,
          label: state.label
        }
      });
    }
  }

  async seedTransitions(workflowName: string, transitions: WorkflowTransitionModel[]): Promise<void> {
    for (const transition of transitions) {
      await prisma.workflowTransition.upsert({
        where: {
          workflowName_fromState_eventName: {
            workflowName,
            fromState: transition.from,
            eventName: transition.event
          }
        },
        update: {
          toState: transition.to
        },
        create: {
          workflowName,
          fromState: transition.from,
          eventName: transition.event,
          toState: transition.to
        }
      });
    }
  }

  async summary(workflowName: string): Promise<{ workflowName: string; states: WorkflowStateModel[]; transitions: WorkflowTransitionModel[] }> {
    const states = await prisma.workflowState.findMany({
      where: {
        workflowName
      },
      orderBy: {
        createdAt: 'asc'
      }
    });
    const transitions = await prisma.workflowTransition.findMany({
      where: {
        workflowName
      },
      orderBy: {
        createdAt: 'asc'
      }
    });
    return {
      workflowName,
      states: states.map((state) => ({
        id: state.stateId,
        label: state.label
      })),
      transitions: transitions.map((transition) => ({
        from: transition.fromState,
        event: transition.eventName,
        to: transition.toState
      }))
    };
  }

  async createInstance(workflowName: string, entityId: string, initialState: string): Promise<WorkflowInstanceModel> {
    const saved = await prisma.workflowInstance.upsert({
      where: {
        workflowName_entityId: {
          workflowName,
          entityId
        }
      },
      update: {
        currentState: initialState
      },
      create: {
        workflowName,
        entityId,
        currentState: initialState
      }
    });
    return {
      entityId: saved.entityId,
      currentState: saved.currentState,
      history: [saved.currentState]
    };
  }

  async transition(workflowName: string, entityId: string, eventName: string): Promise<WorkflowInstanceModel> {
    const instance = await prisma.workflowInstance.findUnique({
      where: {
        workflowName_entityId: {
          workflowName,
          entityId
        }
      }
    });
    if (!instance) {
      throw new Error(`Workflow instance ${entityId} not found.`);
    }
    const transition = await prisma.workflowTransition.findUnique({
      where: {
        workflowName_fromState_eventName: {
          workflowName,
          fromState: instance.currentState,
          eventName
        }
      }
    });
    if (!transition) {
      throw new Error(`Transition ${eventName} is not allowed from ${instance.currentState}.`);
    }
    const saved = await prisma.workflowInstance.update({
      where: {
        workflowName_entityId: {
          workflowName,
          entityId
        }
      },
      data: {
        currentState: transition.toState
      }
    });
    return {
      entityId: saved.entityId,
      currentState: saved.currentState,
      history: [instance.currentState, saved.currentState]
    };
  }
}
