/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamyuvraj0.vela.innertube.YouTube
import com.iamyuvraj0.vela.innertube.pages.BrowseResult
import com.iamyuvraj0.vela.constants.HideExplicitKey
import com.iamyuvraj0.vela.constants.HideVideoKey
import com.iamyuvraj0.vela.utils.dataStore
import com.iamyuvraj0.vela.utils.get
import com.iamyuvraj0.vela.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YouTubeBrowseViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val browseId = savedStateHandle.get<String>("browseId")!!
    private val params = savedStateHandle.get<String>("params")

    val result = MutableStateFlow<BrowseResult?>(null)

    init {
        viewModelScope.launch {
            YouTube
                .browse(browseId, params)
                .onSuccess {
                    val hideVideo = context.dataStore.get(HideVideoKey, false)
                    result.value = it.filterExplicit(context.dataStore.get(HideExplicitKey, false)).filterVideo(hideVideo)
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
