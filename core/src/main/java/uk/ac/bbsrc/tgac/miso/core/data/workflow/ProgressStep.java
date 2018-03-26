package uk.ac.bbsrc.tgac.miso.core.data.workflow;

import java.io.Serializable;

import uk.ac.bbsrc.tgac.miso.core.data.Barcodable;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.IntegerProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.PoolProgressStep;

/**
 * Holds the data for a single workflow step Each input should have its own step
 */
public interface ProgressStep extends Serializable, Comparable<ProgressStep> {
  Progress getProgress();

  void setProgress(Progress progress);

  int getStepNumber();

  void setStepNumber(int stepNumber);

  /**
   * Part of the Visitor Pattern to use WorkflowStep to validate ProgressStep All implementations of this method should call
   * {@code visitor.processInput(this)}
   * 
   * @param visitor
   *          WorkflowStep used to validate {@code this}
   */
  void accept(WorkflowStep visitor);

  enum InputType {
    POOL {
      @Override
      public ProgressStep createStep() {
        return new PoolProgressStep();
      }

      @Override
      public boolean isBarcodable() {
        return true;
      }
    },
    INTEGER {
      @Override
      public ProgressStep createStep() {
        return new IntegerProgressStep();
      }

      @Override
      public boolean isBarcodable() {
        return false;
      }
    };

    public abstract ProgressStep createStep();

    public abstract boolean isBarcodable();
  }
}
