package uk.ac.bbsrc.tgac.miso.core.data.workflow;

import java.io.Serializable;

import uk.ac.bbsrc.tgac.miso.core.data.Barcodable;
import uk.ac.bbsrc.tgac.miso.core.data.Barcodable.EntityType;

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
      public FactoryType getFactoryType() {
        return FactoryType.BARCODABLE;
      }

      @Override
      public EntityType toEntityType() {
        return EntityType.POOL;
      }
    },
    INTEGER {
      @Override
      public FactoryType getFactoryType() {
        return FactoryType.INTEGER;
      }

      @Override
      public EntityType toEntityType() {
        return null;
      }
    };

    public abstract FactoryType getFactoryType();

    public abstract EntityType toEntityType();
  }

  enum FactoryType {
    BARCODABLE, INTEGER
  }
}
