/*
 * VELA (2026)
 * VELA third-party attribution retained in THIRD_PARTY_NOTICES.md
 * Licensed under GPL-3.0. See THIRD_PARTY_NOTICES.md for third-party attribution.
 */



package com.iamyuvraj0.vela.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamyuvraj0.vela.innertube.YouTube
import com.iamyuvraj0.vela.innertube.models.SearchSuggestions
import com.iamyuvraj0.vela.innertube.models.YTItem
import com.iamyuvraj0.vela.innertube.models.filterExplicit
import com.iamyuvraj0.vela.innertube.models.filterVideo
import com.iamyuvraj0.vela.constants.HideExplicitKey
import com.iamyuvraj0.vela.constants.HideVideoKey
import com.iamyuvraj0.vela.db.MusicDatabase
import com.iamyuvraj0.vela.db.entities.SearchHistory
import com.iamyuvraj0.vela.utils.dataStore
import com.iamyuvraj0.vela.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        // History flow: updates immediately from DB
        viewModelScope.launch {
            query
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        database.searchHistory().map { history ->
                            SearchSuggestionViewState(history = history)
                        }
                    } else {
                        database
                            .searchHistory(query)
                            .map { it.take(3) }
                            .map { history ->
                                SearchSuggestionViewState(history = history)
                            }
                    }
                }.collect {
                    _viewState.value = it
                }
        }

        // Suggestions flow: fetches from network independently
        viewModelScope.launch {
            query
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        flowOf<SearchSuggestions?>(null)
                    } else {
                        flow<SearchSuggestions?> {
                            emit(null) // clear stale suggestions immediately
                            emit(YouTube.searchSuggestions(query).getOrNull())
                        }
                    }
                }.collect { result ->
                    val history = _viewState.value.history
                    _viewState.value = _viewState.value.copy(
                        suggestions = result
                            ?.queries
                            ?.filter { s -> history.none { it.query == s } }
                            .orEmpty(),
                        items = result
                            ?.recommendedItems
                            ?.filterExplicit(context.dataStore.get(HideExplicitKey, false))
                            ?.filterVideo(context.dataStore.get(HideVideoKey, false))
                            .orEmpty(),
                    )
                }
        }
    }
}

data class SearchSuggestionViewState(
    val history: List<SearchHistory> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)