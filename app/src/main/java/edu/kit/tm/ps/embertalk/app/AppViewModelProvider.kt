package edu.kit.tm.ps.embertalk.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import edu.kit.tm.ps.embertalk.ui.message_view.MessageViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MessageViewModel(emberTalkApplication().container.messageManager)
        }
    }
}

fun CreationExtras.emberTalkApplication(): EmberTalkApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EmberTalkApplication)