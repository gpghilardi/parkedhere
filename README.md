# parkedhere

[![standard-readme compliant](https://img.shields.io/badge/standard--readme-OK-green.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Simple Kotlin/Jetpack Composed-based WearOS (Android) application.

Purpose: quickly store a GPS location and navigate to it via Google Maps.

If you, like me, need something to remember where you parked your car and quickly navigate to it, this is your app.

This app serves two distinct purposes:
- educational: this app allows me to experiment with Kotlin/Jetpack Compose on my WearOS smartwatch
- practical: it gives me a fast way to get back to my car's parking directly from my smartwatch

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Maintainers](#maintainers)
- [Contributing](#contributing)
- [License](#license)

## Install

Download the app code, open it in Android Studio, build and install it on your device (a real smartwatch or an emulated one).

## Usage

Open the app on your smartwatch.

![Screenshot](docs/images/parkedhere_icon.png)

Two buttons, nothing more, nothing less:
- "Set position": reads the current position ans stores it in app's own data store
- "Navigate": opens Google Maps and sets it to navigate to the previously stored position

Stored location data (latitude and longitude scalar values) is persisted, this way we don't lose the stored location data in the unlucky event the app crashes or it's killed by the os for whatever reason.

Please note the app does nothing without an explicit input from the user, i.e. pressing one of the two buttons.

![Screenshot](docs/images/parkedhere_buttons.png)

## Maintainers

[@gpghilardi](https://github.com/gpghilardi)

## Contributing

PRs are accepted.

## License

MIT © 2023 gpghilardi
