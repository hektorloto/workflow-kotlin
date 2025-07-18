package com.squareup.sample.hellobackbutton

import android.os.Parcelable
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.Finished
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.Rendering
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State.Quitting
import com.squareup.sample.hellobackbutton.AreYouSureWorkflow.State.Running
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.AndroidScreen
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ScreenViewFactory
import com.squareup.workflow1.ui.ScreenViewFactory.Companion.map
import com.squareup.workflow1.ui.Unwrappable
import com.squareup.workflow1.ui.navigation.AlertOverlay
import com.squareup.workflow1.ui.navigation.AlertOverlay.Button.NEGATIVE
import com.squareup.workflow1.ui.navigation.AlertOverlay.Button.POSITIVE
import com.squareup.workflow1.ui.navigation.AlertOverlay.Event.ButtonClicked
import com.squareup.workflow1.ui.navigation.AlertOverlay.Event.Canceled
import com.squareup.workflow1.ui.navigation.BackButtonScreen
import com.squareup.workflow1.ui.navigation.BodyAndOverlaysScreen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

/**
 * Wraps [HelloBackButtonWorkflow] to (sometimes) pop a confirmation alert when the back
 * button is pressed.
 */
object AreYouSureWorkflow : StatefulWorkflow<Unit, State, Finished, Rendering>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): State = snapshot?.toParcelable() ?: Running

  class Rendering(
    private val base: Screen,
    private val alert: AlertOverlay? = null
  ) : AndroidScreen<Rendering>, Unwrappable {
    override val viewFactory: ScreenViewFactory<Rendering> = map { newRendering ->
      BodyAndOverlaysScreen(newRendering.base, listOfNotNull(newRendering.alert))
    }

    /**
     * For nicer logging. See the call to [unwrap][com.squareup.workflow1.ui.unwrap]
     * in [HelloBackButtonActivity].
     */
    override val unwrapped: Any
      get() = alert ?: base
  }

  @Parcelize
  enum class State : Parcelable {
    Running,
    Quitting
  }

  object Finished

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext<Unit, State, Finished>
  ): Rendering {
    val ableBakerCharlie = context.renderChild(HelloBackButtonWorkflow, Unit) { noAction() }

    return when (renderState) {
      Running -> {
        Rendering(
          BackButtonScreen(ableBakerCharlie) {
            // While we always provide a back button handler, by default the view code
            // associated with BackButtonScreen ignores ours if the view created for the
            // wrapped rendering sets a handler of its own. (Set BackButtonScreen.shadow
            // to change this precedence.)
            context.actionSink.send(maybeQuit)
          }
        )
      }
      Quitting -> {
        val alert = AlertOverlay(
          buttons = mapOf(
            POSITIVE to "I'm Positive",
            NEGATIVE to "Negatory"
          ),
          message = "Are you sure you want to do this thing?",
          onEvent = { alertEvent ->
            context.actionSink.send(
              when (alertEvent) {
                is ButtonClicked -> when (alertEvent.button) {
                  POSITIVE -> confirmQuit
                  else -> cancelQuit
                }
                Canceled -> cancelQuit
              }
            )
          }
        )

        Rendering(ableBakerCharlie, alert)
      }
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()

  private val maybeQuit = action("maybeQuit") { state = Quitting }
  private val confirmQuit = action("confirmQuit") { setOutput(Finished) }
  private val cancelQuit = action("cancelQuit") { state = Running }
}
