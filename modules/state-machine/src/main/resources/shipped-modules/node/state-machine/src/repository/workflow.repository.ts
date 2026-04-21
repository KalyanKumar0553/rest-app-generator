import { prisma } from '../../../../src/lib/prisma';

import type { WorkflowStateModel } from '../model/workflow.model';

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

  async summary(workflowName: string): Promise<{ workflowName: string; states: WorkflowStateModel[] }> {
    const states = await prisma.workflowState.findMany({
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
      }))
    };
  }
}
