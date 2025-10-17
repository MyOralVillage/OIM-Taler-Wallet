# Transaction Database

This library provides an experimental implementation of an SQLite database for 
transaction histories. It acts as an extension to the [taler-kotlin-android library].
In order to preserve taler-koltin-android functionalities,
Time.kt, Amount.kt, and Currency.kt are implemented here but shadowed in the original library.

Warning: If you use this library and need bar code scanning, please target at least SDK version 24
         or set `coreLibraryDesugaringEnabled` to `true` in `android.compileOptions`.
