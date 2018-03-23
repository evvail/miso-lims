package uk.ac.bbsrc.tgac.miso.core.data.workflow.impl;

import static uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow.WorkflowName.LOADSEQUENCER;

import java.util.List;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.AbstractWorkflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.WorkflowStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.WorkflowStepPrompt;

public class LoadSequencerWorkflow extends AbstractWorkflow {
  @Override
  protected List<WorkflowStep> getCompletedSteps() {
    return null;
  }

  @Override
  protected WorkflowName getWorkflowName() {
    return LOADSEQUENCER;
  }

  @Override
  public WorkflowStepPrompt getNextStep() {
    return null;
  }

  @Override
  public WorkflowStepPrompt getStep(int stepNumber) {
    return null;
  }

  @Override
  public boolean isComplete() {
    return false;
  }

  @Override
  public void processInput(ProgressStep step) {

  }

  @Override
  public void processInput(int stepNumber, ProgressStep step) {

  }

  @Override
  public void cancelInput() {

  }
}
