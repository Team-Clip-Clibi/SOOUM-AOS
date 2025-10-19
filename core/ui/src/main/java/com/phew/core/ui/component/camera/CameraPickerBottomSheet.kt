package com.phew.core.ui.component.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core_design.BottomSheetComponent
import com.phew.core_design.BottomSheetItem
import com.phew.core_design.R

@Composable
fun CameraPickerBottomSheet(
    visible: Boolean,
    onActionSelected: (CameraPickerAction) -> Unit,
    onDismiss: () -> Unit,
    albumTextRes: Int = R.string.camera_picker_album,
    cameraTextRes: Int = R.string.camera_picker_camera
) {
    if (!visible) return

    BottomSheetComponent.BottomSheet(
        data = arrayListOf(
            BottomSheetItem(
                id = CameraPickerAction.Album.ordinal,
                title = stringResource(id = albumTextRes)
            ),
            BottomSheetItem(
                id = CameraPickerAction.Camera.ordinal,
                title = stringResource(id = cameraTextRes)
            )
        ),
        onItemClick = { id ->
            when (id) {
                CameraPickerAction.Album.ordinal -> onActionSelected(CameraPickerAction.Album)
                CameraPickerAction.Camera.ordinal -> onActionSelected(CameraPickerAction.Camera)
            }
        },
        onDismiss = onDismiss
    )
}
