package uk.ac.bbsrc.tgac.miso.service.impl;

import static uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow.WorkflowName;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing;

import uk.ac.bbsrc.tgac.miso.core.data.Barcodable;
import uk.ac.bbsrc.tgac.miso.core.data.Barcodable.EntityType;
import uk.ac.bbsrc.tgac.miso.core.data.impl.view.BarcodableView;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.Progress;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep.InputType;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.IntegerProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.LoadSequencerWorkflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.ProgressImpl;
import uk.ac.bbsrc.tgac.miso.core.store.ProgressStore;
import uk.ac.bbsrc.tgac.miso.service.BarcodableViewService;
import uk.ac.bbsrc.tgac.miso.service.WorkflowManager;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationError;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationException;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationResult;
import uk.ac.bbsrc.tgac.miso.service.security.AuthorizationManager;

@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultWorkflowManager implements WorkflowManager {
  @Autowired
  AuthorizationManager authorizationManager;

  @Autowired
  ProgressStore progressStore;

  @Autowired
  BarcodableViewService barcodableViewService;

  @Override
  public Workflow beginWorkflow(String workflowName) {
    return makeWorkflow(workflowName);
  }

  private Workflow makeWorkflow(String workflowName) {
    return makeWorkflow(WorkflowName.valueOf(workflowName));
  }

  private Workflow makeWorkflow(WorkflowName workflowName) {
    Workflow workflow;

    switch (workflowName) {
    case LOADSEQUENCER:
      workflow = new LoadSequencerWorkflow();
      break;
    default:
      throw new IllegalArgumentException("Unknown workflowName");
    }

    workflow.setProgress(makeProgress(workflowName));
    return workflow;
  }

  private Workflow makeWorkflow(Progress progress) {
    return makeWorkflow(progress.getWorkflowName());
  }

  private Progress makeProgress(WorkflowName workflowName) {
    Progress progress = new ProgressImpl();
    progress.setWorkflowName(workflowName);
    return progress;
  }

  @Override
  public Workflow processInput(Workflow workflow, String input) throws IOException {
    workflow.processInput(makeProgressStep(workflow.getNextStep().getDataTypes(), input));
    saveProgress(workflow.getProgress());
    return workflow;
  }

  @Override
  public Workflow processInput(Workflow workflow, int stepNumber, String input) throws IOException {
    workflow.processInput(stepNumber, makeProgressStep(workflow.getNextStep().getDataTypes(), input));
    saveProgress(workflow.getProgress());
    return workflow;
  }

  private ProgressStep makeProgressStep(Set<InputType> inputTypes, String input) {
    ValidationResult validationResult = new ValidationResult();
    if (containsBarcodable(inputTypes)) {
      List<BarcodableView> views = barcodableViewService.searchByBarcode(input, extractEntityTypes(inputTypes));
      if (views.size() > 0) {
        validationResult.addError(new ValidationError("Found entities with duplicate barcodes"));
        validationResult.throwIfInvalid();
      } else if (views.size() == 1) {
        return makeProgressStep(views.get(1));
      } else {
        if (!containsPrimitive(inputTypes)) {
          validationResult.addError(new ValidationError("Barcode not found"));
          validationResult.throwIfInvalid();
        }
      }
    } else if (containsEntity(inputTypes)) {
      // todo
    } else {
      makeProgressStep(getPrimitiveType(inputTypes), input);
    }
    // todo
    return null;
  }

  private ProgressStep makeProgressStep(InputType inputType, String input) {
    // todo
    return null;
  }

  private InputType getPrimitiveType(Set<InputType> inputTypes) {
    // todo
    return null;
  }

  private boolean containsEntity(Set<InputType> inputTypes) {
    // todo
    return false;
  }

  private boolean containsPrimitive(Set<InputType> inputTypes) {
    // todo
    return false;
  }

  private ProgressStep makeProgressStep(BarcodableView barcodableView) {
    // todo
    return null;
  }

  private InputType entityTypeToInputType(EntityType targetType) {
    // todo
    return null;
  }

  private Collection<EntityType> extractEntityTypes(Set<InputType> inputTypes) {
    // todo
    return null;
  }

  private boolean containsBarcodable(Set<InputType> inputTypes) {
    // todo
    return false;
  }

  @Override
  public Workflow cancelInput(Workflow workflow) throws IOException {
    workflow.cancelInput();
    saveProgress(workflow.getProgress());
    return workflow;
  }

  private void saveProgress(Progress progress) throws IOException {
    authorizationManager.throwIfNotOwner(progress.getUser());
    progress.setUser(authorizationManager.getCurrentUser());
    if (progress.getCreationTime() == null) {
      progress.setCreationTime(new Date());
    }
    progress.setLastModified(new Date());
    progressStore.save(progress);
  }

  @Override
  public Workflow loadProgress(long id) throws IOException {
    Progress progress = progressStore.get(id);
    authorizationManager.throwIfNotOwner(progress.getUser());
    Workflow workflow = makeWorkflow(progress.getWorkflowName());
    workflow.setProgress(progress);
    return workflow;
  }

  @Override
  public List<Workflow> listUserWorkflows() throws IOException {
    return progressStore.listByUserId(authorizationManager.getCurrentUser().getUserId()).stream().map(this::makeWorkflow)
        .collect(Collectors.toList());
  }

  @Override
  public void execute(Workflow workflow) {
    // todo
  }
}
