package com.example.composevisibilitytracker

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize

/**
* `onVisibleChange` fires events when the visibility of an item changes.
*
* The event includes information such as the bounds of the visible item, and the fraction of the
* item that's visible. It'll only fire once for when the item enters the screen, and on exit, using
* the bounds within the current window to determine if an item is visible or not.
*
* _Limitations_:
*    - This modifier has no way to detect if there is something on top of it - obscuring (ie z-index checking).
*    - Keyboard visibility, if an item is below the keyboard, this modifier will still track
*    the item as visible, it is recommended that you ensure you are using
*    android:windowSoftInputMode="adjustResize" to ensure the whole window is resized and then
*    items will not report themselves as being visible.
*
* See VisibilityTrackingSample.kt for example usage.
*
* @param visibleEvent function that will run when the item becomes visible or invisible.
*/
fun Modifier.onVisibilityChanged(visibleEvent: (VisibleEvent) -> Unit) =
   this.then(VisibilityAwareModifierElement(visibleEvent))


/**
* `onVisiblePositionChanged` fires events when the visibility of an item changes. It include the same information as `onVisibleChange`, with additional information such as
* a trigger on each position change.
*
* The event includes information such as the bounds of the visible item, and the fraction of the
* item that's visible. It'll trigger when the item enters the screen, and on exit, using
* the bounds within the current window to determine if an item is visible or not, as well as for each position change of the item on screen.
*
* WARNING: This function is invoked often if items are changing frequently (e.g. in a scrolling list), it should be use sparingly. Prefer `onVisibleChange` over this function.
*
* _Limitations_:
*    - This modifier has no way to detect if there is something on top of it - obscuring (ie z-index checking).
*    - Keyboard visibility, if an item is below the keyboard, this modifier will still track
*    the item as visible, it is recommended that you ensure you are using
*    android:windowSoftInputMode="adjustResize" to ensure the whole window is resized and then
*    items will not report themselves as being visible.
*
* See VisibilityTrackingSample.kt for example usage.
*
* @param visibleEvent function that will run when the item becomes visible or invisible.
*/
fun Modifier.onVisiblePositionChanged(visibleEvent: (VisiblePositionChangedEvent) -> Unit) =
   this.then(VisiblePositionChangedModifierElement(visibleEvent))


sealed class VisibleEvent {
   data class Visible(
       val visibleRect: Rect,
       val size: IntSize,
       val fractionVisibleWidth: Float,
       val fractionVisibleHeight: Float
   ) : VisibleEvent()


   data object Invisible : VisibleEvent()
}


sealed class VisiblePositionChangedEvent {
   data class Visible(
       val visibleRect: Rect,
       val size: IntSize,
       val fractionVisibleWidth: Float,
       val fractionVisibleHeight: Float
   ) : VisiblePositionChangedEvent()


   data object Invisible : VisiblePositionChangedEvent()


   data class OnPositionChanged(
       val visibleRect: Rect,
       val size: IntSize,
       val fractionVisibleWidth: Float,
       val fractionVisibleHeight: Float
   ): VisiblePositionChangedEvent()
}


private class VisiblePositionChangedModifierNode(var visibleEventCallback: (VisiblePositionChangedEvent) -> Unit) : GlobalPositionAwareModifierNode, Modifier.Node() {
   private var currentlyVisible = false
   private var visibleBounds = Rect.Zero
   override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
       val bounds = coordinates.boundsInWindow()
       val visible = isAttached && bounds.width > 0 && bounds.height > 0


       if (currentlyVisible != visible || bounds != visibleBounds) {
           val size = coordinates.size
           val fractionVisibleWidth = bounds.width / size.width.toFloat()
           val fractionVisibleHeight = bounds.height / size.height.toFloat()
           if (currentlyVisible == visible) {
               visibleEventCallback(
                   VisiblePositionChangedEvent.OnPositionChanged(
                       bounds,
                       size,
                       fractionVisibleWidth,
                       fractionVisibleHeight
                   )
               )
           } else {
               if (visible) {
                   visibleEventCallback(
                       VisiblePositionChangedEvent.Visible(
                           bounds,
                           size,
                           fractionVisibleWidth,
                           fractionVisibleHeight
                       )
                   )
               } else {
                   visibleEventCallback(VisiblePositionChangedEvent.Invisible)
               }
           }
           currentlyVisible = visible
           visibleBounds = bounds
       }
   }


   override fun onDetach() {
       super.onDetach()
       visibleEventCallback(VisiblePositionChangedEvent.Invisible)
       currentlyVisible = false
   }
}


private data class VisiblePositionChangedModifierElement(val visibleEventCallback: (VisiblePositionChangedEvent) -> Unit) :
   ModifierNodeElement<VisiblePositionChangedModifierNode>() {
   override fun create() = VisiblePositionChangedModifierNode(visibleEventCallback)


   override fun update(node: VisiblePositionChangedModifierNode) {
       node.visibleEventCallback = visibleEventCallback
   }


   override fun InspectorInfo.inspectableProperties() {
       name = "visibility aware"
       properties["visibleEventCallback"] = visibleEventCallback
   }
}




private class VisibilityAwareModifierNode(var visibleEventCallback: (VisibleEvent) -> Unit) :
   GlobalPositionAwareModifierNode, Modifier.Node() {


   private var currentlyVisible = false
   override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
       val bounds = coordinates.boundsInWindow()
       val visible = isAttached && bounds.width > 0 && bounds.height > 0


       if (currentlyVisible != visible) {
           val size = coordinates.size
           val fractionVisibleWidth = bounds.width / size.width.toFloat()
           val fractionVisibleHeight = bounds.height / size.height.toFloat()
           if (visible) {
               visibleEventCallback(
                   VisibleEvent.Visible(
                       bounds,
                       size,
                       fractionVisibleWidth,
                       fractionVisibleHeight
                   )
               )
           } else {
               visibleEventCallback(VisibleEvent.Invisible)
           }


           currentlyVisible = visible
       }
   }


   override fun onDetach() {
       super.onDetach()
       visibleEventCallback(VisibleEvent.Invisible)
       currentlyVisible = false
   }


}


private data class VisibilityAwareModifierElement(val visibleEventCallback: (VisibleEvent) -> Unit) :
   ModifierNodeElement<VisibilityAwareModifierNode>() {
   override fun create() = VisibilityAwareModifierNode(visibleEventCallback)


   override fun update(node: VisibilityAwareModifierNode) {
       node.visibleEventCallback = visibleEventCallback
   }


   override fun InspectorInfo.inspectableProperties() {
       name = "visibility aware"
       properties["visibleEventCallback"] = visibleEventCallback
   }
}
