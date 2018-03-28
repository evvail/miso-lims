package uk.ac.bbsrc.tgac.miso.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;

import uk.ac.bbsrc.tgac.miso.core.data.Barcodable.EntityType;
import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.view.BarcodableView;
import uk.ac.bbsrc.tgac.miso.core.data.impl.view.BarcodableView.BarcodableId;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.ProgressStep.InputType;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.IntegerProgressStep;
import uk.ac.bbsrc.tgac.miso.core.data.workflow.impl.PoolProgressStep;
import uk.ac.bbsrc.tgac.miso.service.BarcodableViewService;
import uk.ac.bbsrc.tgac.miso.service.exception.ValidationException;

public class DefaultWorkflowManagerTest {
  private static final String INTEGER_INPUT = "1";
  private static final String POOL_BARCODE = "123456789";
  private static final long POOL_ID = 1;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private BarcodableViewService barcodableViewService;

  @InjectMocks
  private DefaultWorkflowManager sut;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testNoInputTypesGivesError() throws IOException {
    exception.expect(ValidationException.class);
    sut.makeProgressStep(Sets.newHashSet(), "input");
  }

  @Test
  public void testIntegerInputType() throws IOException {
    IntegerProgressStep step = (IntegerProgressStep) sut.makeProgressStep(Sets.newHashSet(InputType.INTEGER), INTEGER_INPUT);
    assertEquals(1, step.getInput());
  }

  @Test
  public void testPoolInputType() throws IOException {
    Pool pool = new PoolImpl();
    pool.setId(POOL_ID);

    BarcodableView view = new BarcodableView();
    BarcodableId id = new BarcodableId();
    id.setTargetType(EntityType.POOL);
    view.setId(id);
    Mockito.when(barcodableViewService.getEntity(view)).thenReturn(pool);

    Mockito.when(barcodableViewService.searchByBarcode(POOL_BARCODE, Arrays.asList(EntityType.POOL)))
        .thenReturn(Collections.singletonList(view));

    assertEquals(POOL_ID, ((PoolProgressStep) sut.makeProgressStep(Sets.newHashSet(InputType.POOL), POOL_BARCODE)).getInput().getId());
  }

  @Test
  public void testFallbackToIntegerProgressStep() throws IOException {
    Pool pool = new PoolImpl();
    pool.setId(POOL_ID);

    BarcodableView view = new BarcodableView();
    BarcodableId id = new BarcodableId();
    id.setTargetType(EntityType.POOL);
    view.setId(id);
    Mockito.when(barcodableViewService.getEntity(view)).thenReturn(pool);

    Mockito.when(barcodableViewService.searchByBarcode(POOL_BARCODE, Arrays.asList(EntityType.POOL)))
        .thenReturn(Collections.singletonList(view));
    assertEquals(Integer.parseInt(INTEGER_INPUT),
        ((IntegerProgressStep) sut.makeProgressStep(Sets.newHashSet(InputType.INTEGER, InputType.POOL), INTEGER_INPUT)).getInput());
  }
}
