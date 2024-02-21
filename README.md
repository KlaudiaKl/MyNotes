# MyNotes 
This is a note taking application that saves the notes in the cloud using MongoDB DeviceSync and Firebase.

## Features
* Google One-Tap Authentication
* Easy note adding
* Font size selection for users with impaired vision
* Translated to Polish and English
* Note categorisation into folders
* Sorting the notes by date
* Dark and light mode
* Sharing notes
* Moving notes to different folders/categories
* Created with Kotlin, MaterialDesign 3 and Jetpack Compose

## Usage
Requires an Android device with Android 8.0 operating system or newer
RequiresApi(Build.VERSION_CODES.O)

Requires an internet connection to log in
Users can still write and read notes with no internet, they will be automatically saved to the cloud once the internet connection is back.
Repositories are injected using dagger hilt.
