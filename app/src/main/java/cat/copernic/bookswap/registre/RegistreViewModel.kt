package cat.copernic.bookswap.registre

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.actionCodeSettings

class RegistreViewModel: ViewModel() {

    init{
        val actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://www.bookswap-7d63c.firebaseapp.com"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "cat.copernic.bookswap",
                true, /* installIfNotAvailable */
                "21" /* minimumVersion */)
        }

    }
}