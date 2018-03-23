package uk.ac.bbsrc.tgac.miso.service;

import java.io.IOException;
import java.util.List;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;

public interface WorkflowManager {
  Workflow beginWorkflow(String workflowName) throws IOException;

  Workflow processInput(Workflow workflow, String input) throws IOException;

  Workflow processInput(Workflow workflow, int stepNumber, String input) throws IOException;

  Workflow cancelInput(Workflow workflow) throws IOException;

  Workflow loadProgress(long id);

  List<Workflow> listUserWorkflows() throws IOException;

  void execute(Workflow workflow);
}
