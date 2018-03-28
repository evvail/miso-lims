package uk.ac.bbsrc.tgac.miso.service;

import java.io.IOException;
import java.util.List;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;

public interface WorkflowManager {
  /**
   * @param workflowName String representation of a WorkflowName
   */
  Workflow beginWorkflow(String workflowName) throws IOException;

  /**
   * Similar to processInput(Workflow, int, String), but for workflow's current step
   */
  Workflow processInput(Workflow workflow, String input) throws IOException;

  /**
   * Interpret input as part of the step identified by stepNumber.
   * Delegate input processing to workflow
   * @param input user input
   * @param stepNumber 0-based index into workflow steps
   * @return updated Workflow
   */
  Workflow processInput(Workflow workflow, int stepNumber, String input) throws IOException;

  /**
   * Remove the latest step from workflow and any effects it caused
   * @return update Workflow
   */
  Workflow cancelInput(Workflow workflow) throws IOException;

  Workflow loadProgress(long id) throws IOException;

  /**
   * @return Workflows owned by the current User
   */
  List<Workflow> listUserWorkflows() throws IOException;

  /**
   * todo
   */
  void execute(Workflow workflow);
}
