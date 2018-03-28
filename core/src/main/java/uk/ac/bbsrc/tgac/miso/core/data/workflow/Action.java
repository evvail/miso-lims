package uk.ac.bbsrc.tgac.miso.core.data.workflow;

public class Action {
  private Object entity;

  private Command command;

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public Object getEntity() {
    return entity;
  }

  public void setEntity(Object entity) {
    this.entity = entity;
  }

  public enum Command {
    SAVE
  }
}
