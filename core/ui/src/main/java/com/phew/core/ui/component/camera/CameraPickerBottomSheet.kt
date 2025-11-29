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
    useDefaultText: Boolean = false
) {
    if (!visible) return
    val item = arrayListOf(
        BottomSheetItem(
            id = CameraPickerAction.Album.ordinal,
            title = stringResource(id = R.string.camera_picker_album)
        ),
        BottomSheetItem(
            id = CameraPickerAction.Camera.ordinal,
            title = stringResource(id = R.string.camera_picker_camera)
        )
    ).apply {
        if (useDefaultText) {
            add(
                BottomSheetItem(
                    id = CameraPickerAction.Default.ordinal,
                    title = stringResource(R.string.camera_picker_default)
                )
            )
        }
    }
    BottomSheetComponent.BottomSheet(
        data =  item
        ,
        onItemClick = { id ->
            when (id) {
                CameraPickerAction.Album.ordinal -> onActionSelected(CameraPickerAction.Album)
                CameraPickerAction.Camera.ordinal -> onActionSelected(CameraPickerAction.Camera)
                CameraPickerAction.Default.ordinal -> onActionSelected(CameraPickerAction.Default)
            }
        },
        onDismiss = onDismiss
    )
}
