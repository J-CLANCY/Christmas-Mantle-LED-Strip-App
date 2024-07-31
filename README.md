# Christmas Mantle LED Strip App

This repository contains the Android Studio project for a christmas mantle LED strip app I made to control a series of WS2812B RGB LEDs, which were installed underneath the mantle of my fireplace. The WS2812B LED strip was connected to an Arduino Nano, which controlled the LED strip with the [FastLED](https://fastled.io/) library. A [HC-05 bluetooth module](https://howtomechatronics.com/tutorials/arduino/arduino-and-hc-05-bluetooth-module-tutorial/) is used to provide connectivity between the Arduino Nano and the app. The app allows the user to either: set the colour of the strip with a colour wheel, or choose a special colour palette that will "twinkle" the LED strip in various ways i.e. fire, snow etc.

<TO-DO - Insert screenshots of app >

## Project Structure

```
├── ""app"" => Contains the source code for the app.
├── ""arduino"" => Contains the Arduino code for the Arduino Nano in this project.
├── ""gradle"" => Contains the gradle wrapper for the app.
├── ""build.gradle"" => Top level build configuration file for the gradle.
├── ""gradle.properties"" => Project-wide gradle configuration.
├── ""gradlew"" => Gradle start up script for UniX.
├── ""gradew.bat"" => Gradle startup script for Windows.
├── ""local.properties" => Automatically generated file.
├── ""settings.gradle" => User-defined global variables for gradle.
```
