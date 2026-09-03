/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamyuvraj0.vela.innertube.YouTube
import com.iamyuvraj0.vela.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.iamyuvraj0.vela.innertube.models.filterExplicit
import com.iamyuvraj0.vela.innertube.models.filterVideo
import com.iamyuvraj0.vela.innertube.pages.SearchSummaryPage
import com.iamyuvraj0.vela.constants.HideExplicitKey
import com.iamyuvraj0.vela.constants.HideVideoKey
import com.iamyuvraj0.vela.models.ItemsPage
import com.iamyuvraj0.vela.utils.dataStore
import com.iamyuvraj0.vela.utils.get
import com.iamyuvraj0.vela.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSearchViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query = savedStateHandle.get<String>("query") ?: ""
    val filter = MutableStateFlow<YouTube.SearchFilter?>(null)
    var summaryPage by mutableStateOf<SearchSummaryPage?>(null)
    val viewStateMap = mutableStateMapOf<String, ItemsPage?>()

    init {
        viewModelScope.launch {
            filter.collect { filter ->
                if (query.isNotBlank()) {
                    if (filter == null) {
                        if (summaryPage == null) {
                            YouTube
                                .searchSummary(query)
                                .onSuccess {
                                    summaryPage = it.filterExplicit(context.dataStore.get(HideExplicitKey, false)).filterVideo(context.dataStore.get(HideVideoKey, false))
                                }.onFailure {
                                    reportException(it)
                                }
                        }
                        if (viewStateMap[FILTER_SONG.value] == null) {
                            loadSearchPage(FILTER_SONG)
                        }
                    } else {
                        if (viewStateMap[filter.value] == null) {
                            loadSearchPage(filter)
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        val filter = filter.value ?: FILTER_SONG
        viewModelScope.launch {
            val viewState = viewStateMap[filter.value] ?: return@launch
            val continuation = viewState.continuation
            if (continuation != null) {
                val searchResult =
                    YouTube.searchContinuation(continuation).getOrNull() ?: return@launch
                viewStateMap[filter.value] = ItemsPage(
                    (viewState.items + searchResult.items)
                        .distinctBy { it.id }
                        .filterExplicit(context.dataStore.get(HideExplicitKey, false))
                        .filterVideo(context.dataStore.get(HideVideoKey, false)),
                    searchResult.continuation
                )
            }
        }
    }

    private suspend fun loadSearchPage(filter: YouTube.SearchFilter) {
        YouTube
            .search(query, filter)
            .onSuccess { result ->
                viewStateMap[filter.value] =
                    ItemsPage(
                        result.items
                            .distinctBy { it.id }
                            .filterExplicit(context.dataStore.get(HideExplicitKey, false))
                            .filterVideo(context.dataStore.get(HideVideoKey, false)),
                        result.continuation,
                    )
            }.onFailure {
                reportException(it)
            }
    }
}
