package uk.ac.bbsrc.tgac.miso.service.impl;

import static uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep.FactoryType;
import static uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow.WorkflowName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

import uk.ac.bbsrc.tgac.miso.core.data.Barcodable.EntityType;
import uk.ac.bbsrc.tgac.miso.core.data.impl.view.BarcodableView;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.Progress;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep.InputType;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.Workflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.IntegerProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.LoadSequencerWorkflow;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.PoolProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.ProgressImpl;
import uk.ac.bbsrc.tgac.miso.core.store.ProgressStore;
import uk.ac.bbsrc.tgac.miso.service.BarcodableViewService;
import uk.ac.bbsrc.tgac.miso.service.WorkflowManager;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationError;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationException;
import uk.ac.bbsrc.tgac.miso.service.security.AuthorizationManager;

@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultWorkflowManager implements WorkflowManager {
  @Autowired
  private BarcodableViewService barcodableViewService;

  @Autowired
  private AuthorizationManager authorizationManager;

  @Autowired
  private ProgressStore progressStore;

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

  @VisibleForTesting
  protected ProgressStep makeProgressStep(Set<InputType> inputTypes, String input) throws IOException {
    List<FactoryType> factoryTypes = new ArrayList<>(getFactoryTypes(inputTypes));
    Collections.sort(factoryTypes);

    for (FactoryType factoryType : factoryTypes) {
      ProgressStep step = makeFactory(factoryType, inputTypes).create(input);
      if (step != null) {
        return step;
      }
    }

    throw new ValidationException(Collections.singletonList(new ValidationError("Could not construct ProgressStep")));
  }

  private ProgressStepFactory makeFactory(FactoryType factoryType, Set<InputType> inputTypes) {
    switch (factoryType) {
    case BARCODABLE:
      return new BarcodableProgressStepFactory(inputTypes);
    default:
      return new IntegerProgressStepFactory();
    }
  }

  private Set<FactoryType> getFactoryTypes(Set<InputType> inputTypes) {
    return inputTypes.stream().map(InputType::getFactoryType).collect(Collectors.toSet());
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

  private interface ProgressStepFactory {
    ProgressStep create(String input) throws IOException;
  }

  private class BarcodableProgressStepFactory implements ProgressStepFactory {
    private final Set<InputType> inputTypes;

    BarcodableProgressStepFactory(Set<InputType> inputTypes) {
      this.inputTypes = inputTypes;
    }

    @Override
    public ProgressStep create(String input) throws IOException {
      List<BarcodableView> views = barcodableViewService.searchByBarcode(input, getEntityTypes());
      if (views.size() == 0) {
        return null;
      } else if (views.size() == 1) {
        return makeProgressStep(views.get(0));
      } else {
        throw new ValidationException(Collections.singletonList(new ValidationError("Duplicate barcodes found")));
      }
    }

    private ProgressStep makeProgressStep(BarcodableView view) throws IOException {
      switch (view.getId().getTargetType()) {
      default:
        PoolProgressStep step = new PoolProgressStep();
        step.setInput(barcodableViewService.getEntity(view));
        return step;
      }
    }

    private List<EntityType> getEntityTypes() {
      return this.inputTypes.stream().map(InputType::toEntityType).filter(Objects::nonNull).collect(Collectors.toList());
    }
  }

  private class IntegerProgressStepFactory implements ProgressStepFactory {
    @Override
    public ProgressStep create(String input) {
      IntegerProgressStep step = new IntegerProgressStep();
      step.setInput(Integer.parseInt(input));
      return step;
    }
  }
}
