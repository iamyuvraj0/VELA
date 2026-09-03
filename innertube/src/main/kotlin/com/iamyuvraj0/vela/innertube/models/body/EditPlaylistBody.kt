/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.innertube.models.body

import com.iamyuvraj0.vela.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class EditPlaylistBody(
    val context: Context,
    val playlistId: String,
    val actions: List<Action>
)

@Serializable
sealed class Action {
    @Serializable
    data class AddVideoAction(
        val action: String = "ACTION_ADD_VIDEO",
        val addedVideoId: String
    ) : Action()

    @Serializable
    data class AddPlaylistAction(
        val action: String = "ACTION_ADD_PLAYLIST",
        val addedFullListId: String
    ) : Action()

    @Serializable
    data class MoveVideoAction(
        val action: String = "ACTION_MOVE_VIDEO_BEFORE",
        val setVideoId: String,
        val movedSetVideoIdSuccessor: String?
    ) : Action()

    @Serializable
    data class RemoveVideoAction(
        val action: String = "ACTION_REMOVE_VIDEO",
        val setVideoId: String,
        val removedVideoId: String
    ) : Action()

    @Serializable
    data class RenamePlaylistAction(
        val action: String = "ACTION_SET_PLAYLIST_NAME",
        val playlistName: String
    ) : Action()
}
