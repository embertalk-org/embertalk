package edu.kit.tm.ps.embertalk.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.preference.PreferenceManager
import edu.kit.tm.ps.embertalk.ui.contacts.ContactsViewModel
import edu.kit.tm.ps.embertalk.ui.message_view.MessageViewModel
import edu.kit.tm.ps.embertalk.ui.qr_code.QrCodeViewModel
import edu.kit.tm.ps.embertalk.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MessageViewModel(emberTalkApplication().container.contactManager, emberTalkApplication().container.messageManager)
        }
        initializer {
            ContactsViewModel(emberTalkApplication().container.cryptoService, emberTalkApplication().container.contactManager)
        }
        initializer {
            SettingsViewModel(PreferenceManager.getDefaultSharedPreferences(emberTalkApplication()), emberTalkApplication().container.cryptoService)
        }
        initializer {
            QrCodeViewModel(emberTalkApplication().container.cryptoService)
        }
    }
}

fun CreationExtras.emberTalkApplication(): EmberTalkApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EmberTalkApplication)