<!-- SPDX-License-Identifier: MIT -->

![MBCommonKit](logo.jpg "Banner")

![License](https://img.shields.io/badge/License-MIT-green)
![Platform](https://img.shields.io/badge/Platforms-Android-blue)
![Version](https://img.shields.io/badge/Azure%20Artifacts-2.0.3-orange)

## Requirements
* __Minimum Android SDK:__ MBCommonKit requires a minimum API level of 19. 
* __Compile Android SDK:__ MBCommonKit requires you to compile against minimum API level 27.

## Intended Usage

This module contains a fascade implementation for the usage of the `KeyStore` API, convient APIs for `SharedPreferences` and 
general utility methods.  

### KeyStore
Use the class `Crypto` for encryption operations.  
```kotlin
val alias = "my.crypto.alias"
val crypto = Crypto(context)

// Generate a new key for the alias if it does not exist.
if (!crypto.keyExists(alias)) {
    crypto.generateKey(alias)
}

// Encrypt strings.
val encryptedText = crypto.encrypt(alias, "my_plain_text")

// Decrypt strings.
crypto.decrypt(alias, encryptedText)
```

### SharedPreferences
You can use the convient implementations for the usage of `SharedPreferences`.  
```kotlin
val preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
val simplePref: Preference<Boolean> = preferences.booleanPreference("key", false)

val prefValue = simplePref.get()    // prefValue contains the default value "false"
simplePref.set(true)
val newPrefValue = simplePref.get() // newPrefValue now contains "true"

simplePref.observe(object : PreferenceObserver<Boolean> {
    override fun onChanged(newValue: Boolean) {
        // The preferences value changed.
        // Stop observing.
        simplePref.stopObserving(this)
    }
}
```

## Installation

Add the following maven url to your project `build.gradle`:  
```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url 'https://pkgs.dev.azure.com/daimler-ris/sdk/_packaging/release/maven/v1'
        }
    }
}
```

Add to your app's `build.gradle`:  
```gradle
implementation "com.daimler.mm:MBCommonKit:$mb_common_kit_version"
```

## Contributing

We welcome any contributions.
If you want to contribute to this project, please read the [contributing guide](https://github.com/Daimler/MBSDK-CommonKit-Android/blob/master/CONTRIBUTING.md).

## Code of Conduct

Please read our [Code of Conduct](https://github.com/Daimler/daimler-foss/blob/master/CODE_OF_CONDUCT.md) as it is our base for interaction.

## License

This project is licensed under the [MIT LICENSE](https://github.com/Daimler/MBSDK-CommonKit-Android/blob/master/LICENSE).

## Provider Information

Please visit <https://mbition.io/en/home/index.html> for information on the provider.

Notice: Before you use the program in productive use, please take all necessary precautions,
e.g. testing and verifying the program with regard to your specific use.
The program was tested solely for our own use cases, which might differ from yours.
