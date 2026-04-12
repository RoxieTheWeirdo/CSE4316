package com.example.nutritionalappplanner.page

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class PantryUiState {
    object Loading : PantryUiState()
    data class Loaded(val items: List<PantryItemUi>) : PantryUiState()
    data class Error(val message: String) : PantryUiState()
}

class PantryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listener: ListenerRegistration? = null

    private val _state = MutableStateFlow<PantryUiState>(PantryUiState.Loading)
    val state: StateFlow<PantryUiState> = _state

    private var currentFilter: String? = null // null = All

    fun startListening(filterStorage: String? = null) {
        currentFilter = filterStorage
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = PantryUiState.Error("Not signed in.")
            return
        }

        _state.value = PantryUiState.Loading

        // Remove old listener if switching filters
        listener?.remove()

        var query = db.collection("users")
            .document(uid)
            .collection("pantry_items")
            .orderBy("createdAt")

        if (!filterStorage.isNullOrBlank()) {
            query = query.whereEqualTo("storage", filterStorage)
        }

        listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _state.value = PantryUiState.Error(e.message ?: "Firestore error")
                return@addSnapshotListener
            }

            val docs = snapshot?.documents ?: emptyList()
            val items = docs.mapNotNull { doc ->
                val item = doc.toObject(PantryItem::class.java) ?: return@mapNotNull null
                PantryItemUi(docId = doc.id, item = item)
            }

            _state.value = PantryUiState.Loaded(items)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
