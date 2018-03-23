package uk.ac.bbsrc.tgac.miso.service.impl;

import static uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow.WorkflowName;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.core.data.workflow.Progress;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.LoadSequencerWorkflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.ProgressImpl;
import uk.ac.bbsrc.tgac.miso.core.store.ProgressStore;
import uk.ac.bbsrc.tgac.miso.service.WorkflowManager;
import uk.ac.bbsrc.tgac.miso.service.security.AuthorizationManager;

@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultWorkflowManager implements WorkflowManager {
  @Autowired
  AuthorizationManager authorizationManager;

  @Autowired
  ProgressStore progressStore;

  @Override
  public Workflow beginWorkflow(String workflowName) {
    return makeWorkflow(workflowName);
  }

  private Workflow makeWorkflow(String workflowNameString) {
    Workflow workflow;
    WorkflowName workflowName = WorkflowName.valueOf(workflowNameString);

    switch (workflowName) {
    case LOADSEQUENCER:
      workflow = new LoadSequencerWorkflow();
      break;
    default:
      throw new IllegalArgumentException("Unknown workflowName");
    }

    workflow.setProgress(makeProgress(workflowName, getCurrentUser()));
    return workflow;
  }

  private User getCurrentUser() {
    // todo
    return null;
  }

  private Progress makeProgress(WorkflowName workflowName, User user) {
    Progress progress = new ProgressImpl();
    progress.setWorkflowName(workflowName);
    progress.setUser(user);
    // todo: set other fields
    return progress;
  }

  @Override
  public Workflow processInput(Workflow workflow, String input) {
    // todo authentication
    return null;
  }

  @Override
  public Workflow processInput(Workflow workflow, int stepNumber, String input) {
    // todo authentication
    return null;
  }

  @Override
  public Workflow cancelInput(Workflow workflow) {
    // todo authentication
    workflow.cancelInput();
    progressStore.save(workflow.getProgress());
    return workflow;
  }

  private void saveProgress(Progress progress) throws IOException {
    // todo: throw if wrong user
    progress.setUser(authorizationManager.getCurrentUser());
  }

  @Override
  public Workflow loadProgress(long id) {
    Progress progress = progressStore.get(id);
    Workflow workflow = makeWorkflow(progress.getWorkflowName().toString());
    workflow.setProgress(progress);
    // todo
    return null;
  }

  @Override
  public List<Workflow> listUserWorkflows() {
    // todo
    return null;
  }

  @Override
  public void execute(Workflow workflow) {
    // todo
  }
}
