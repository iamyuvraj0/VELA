/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamyuvraj0.vela.innertube.YouTube
import com.iamyuvraj0.vela.innertube.models.AlbumItem
import com.iamyuvraj0.vela.db.MusicDatabase
import com.iamyuvraj0.vela.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object Content : AlbumUiState

    data object Empty : AlbumUiState

    data class Error(
        val isNotFound: Boolean = false,
    ) : AlbumUiState
}

private sealed interface FetchState {
    data object Pending : FetchState
    data object Success : FetchState
    data class Failed(val isNotFound: Boolean = false) : FetchState
}

@HiltViewModel
class AlbumViewModel
@Inject
constructor(
    private val database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val albumId = savedStateHandle.get<String>("albumId")!!
    val playlistId = MutableStateFlow("")
    val albumWithSongs =
        database
            .albumWithSongs(albumId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    var otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())

    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Pending)

    val uiState: StateFlow<AlbumUiState> =
        combine(albumWithSongs, _fetchState) { data, fetch ->
            when {
                data != null && data.songs.isNotEmpty() -> AlbumUiState.Content
                fetch is FetchState.Pending -> AlbumUiState.Loading
                fetch is FetchState.Failed && data == null -> AlbumUiState.Error(fetch.isNotFound)
                fetch is FetchState.Success && data != null && data.songs.isEmpty() -> AlbumUiState.Empty
                fetch is FetchState.Failed && data != null && data.songs.isNotEmpty() -> AlbumUiState.Content
                else -> AlbumUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AlbumUiState.Loading)

    init {
        retry()
    }

    fun retry() {
        viewModelScope.launch {
            _fetchState.value = FetchState.Pending
            val album = database.album(albumId).first()
            YouTube
                .album(albumId)
                .onSuccess {
                    playlistId.value = it.album.playlistId
                    otherVersions.value = it.otherVersions
                    database.withTransaction {
                        if (album == null) {
                            insert(it)
                        } else {
                            update(album.album, it, album.artists)
                        }
                    }
                    _fetchState.value = FetchState.Success
                }.onFailure {
                    reportException(it)
                    val isNotFound = it.message?.contains("NOT_FOUND") == true
                    if (isNotFound) {
                        database.query {
                            album?.album?.let(::delete)
                        }
                    }
                    _fetchState.value = FetchState.Failed(isNotFound = isNotFound)
                }
        }
    }
}
