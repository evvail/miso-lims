package uk.ac.bbsrc.tgac.miso.service.impl;

import java.util.List;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;
import uk.ac.bbsrc.tgac.miso.service.WorkflowManager;

public class DefaultWorkflowManager implements WorkflowManager {
  @Override
  public Workflow beginWorkflow(String workflowName) {
    return null;
  }

  @Override
  public Workflow processInput(Workflow workflow, String input) {
    return null;
  }

  @Override
  public Workflow processInput(Workflow workflow, int stepNumber, String input) {
    return null;
  }

  @Override
  public Workflow cancelInput(Workflow workflow) {
    return null;
  }

  @Override
  public Workflow loadProgress(long id) {
    return null;
  }

  @Override
  public List<Workflow> listUserWorkflows() {
    return null;
  }

  @Override
  public void execute(Workflow workflow) {

  }
}
