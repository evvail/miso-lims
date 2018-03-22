package uk.ac.bbsrc.tgac.miso.service;

import java.util.List;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;

public interface WorkflowManager {
  Workflow beginWorkflow(String workflowName);

  Workflow processInput(Workflow workflow, String input);

  Workflow processInput(Workflow workflow, int stepNumber, String input);

  Workflow cancelInput(Workflow workflow);

  Workflow loadProgress(long id);

  List<Workflow> listUserWorkflows();

  void execute(Workflow workflow);
}
