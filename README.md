## <img src="https://github.com/user-attachments/assets/b08b4b25-d8b4-496e-a42b-170903b08d86" width="40px"> FNIRS-Based Neurofeeddback System Using Prefrontal Asymmetry <img src="https://github.com/user-attachments/assets/b08b4b25-d8b4-496e-a42b-170903b08d86" width="40px">

![](https://img.shields.io/badge/Apache2-red)
![](https://img.shields.io/badge/AndroidStudio-green)
![](https://img.shields.io/badge/Java-blue)
![](https://img.shields.io/badge/Matlab-blue)

## Introduction
Functionl Near-Infrared Spectroscopy (fNIRS) is noninvasive neuroimaging technique that measure hemodynamic signals related to functional activation in the brain. It is used in reseach in various fields such as neuroscience, psychology, and developmental scinece. <br>
<br>
One of the promising application of fNIRS utilizing is neurofeedback (NF). NF is a technology that promotes self-adjustment of brain activity by analyzing the measured brain activity in real time and providing feedback to the participants. <br>
<br>
The asymmetry in prefrontal cortex (PFC) activation has been noted as a potential feedback biomarkers (Li et al., 2019; Baik et al., 2019; Slutter et al., 2021). The NF system that provide feedback the asymmetry in PFC activaiton have already been developed (Aranyi et al., 2016). However, it is relied on expensive measurement device and video device, which may be an obstacle to practical application and dissemination. Therefore, we aimed to develop a low-cost and simple fNIRS-based NF system that provides feedback of the asymmetry in PFC activation. <br>
<br>
Our system is characterized by a 2-channel wearable device, a robust asymmetry calculation algorithm against noise, auditory feedback system that can be used in various situation. <br>

## For who want to use this NF App
In order to conduct a placebo-controlled experiment for you, we developed two applications: an app that provides accurate neurofeedback (NF App) and an app that provides false neurofeedback (Placebo App). The APK files for each app can be downloaded from the links below. <br>
- NF App: [this releases page](https://github.com/Ryosuke-123/fNIRS_LateralityIndex_NeurofeedbackSystem/releases/tag/v1.0.0). <br>
- Placebo App: [this releases page](https://github.com/Ryosuke-123/fNIRS_LateralityIndex_NeurofeedbackSystem/releases/tag/v1.0.0). <br>

### How to Use
#### 1. Bluetooth pairing with HOT-2000
- HOT-2000 is attached to the head. <br>
- Turn on the HOT-2000 power supply. <br>
- Establish Bluetooth pairing with the HOT-2000 from the Android device setting. <br>

#### 2. Install the App
- **Download the APK file** for the desired app from the links above. <br>
- **Transfer the APK file** to your Android device if downloaded on a computer. <br>
- **Open the APK file** to your Android device to start the installation process. <br>
- **Follow the on-screen instructions** to complete the installation. <br>

#### 3. Launch the App
- **Find the app icon** on your device's home screen or app drawer. <br>
- **Tap the icon** to open the app.

#### 4. Navigate Through the App
The app consists of 4 main screens. Here's a brief overview of each: <br>
<p align="center">
    <img src="https://github.com/user-attachments/assets/e05f45ad-77f2-41e0-8b1d-6501188fd3c0" width="600px">
</p>

##### Screen 1: Start Screen
Tap anywhere on the screen. <br>

##### Screen 2: Experiment Setting Screen
After setting the "Restの時間" to 30, Baseline

##### Screen 3: Device Connecting Screen

##### Screen 4: Start Experiment Screen

## For who want to re-analyze our NF training Data
In order to allow you to re-analyze the data obtained from our NF training experiment, we have shared the data in public repository (OpenNeuro). These data and our analysis scripts can be downloaded from the links below. <br>
- NF Training Data: [this releases page](). <br>
- Analysis Scripts: [this releases page](). <br>

## For who want to change our NF App
In order to change our NF app, you need to purchase "ExBrainSdkAndroidLibrary" provided by NeU Inc. The company's website can be accessed at the following link. <br>
- [Neu Inc](https://neu-brains.net/). <br>

## Publications

## Libraries and Licenses
This projects uses the following libraries. Please ensure that you comply with their respective licenses: <br>

- [Android Support Libraries](https://developer.android.com/topic/libraries/support-library?hl=en) - Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). <br>
- [SteamSupport (android-retrofuture)](https://sourceforge.net/projects/streamsupport/) - Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). <br>
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). <br>
- [StateProgressBar](https://github.com/kofigyan/StateProgressBar) - Licensed under the [Apache License, Verision 2.0](https://www.apache.org/licenses/LICENSE-2.0). <br>
- [DataViz](https://github.com/povilaskarvelis/DataViz) - Licensed under the [MIT License](https://opensource.org/license/MIT). <br>

## License
This project is licensed by: <br>
- Apache License, Version 2.0 <br>

## Contributors & How to Contact
For any inquiries or feedback, please contact: <br>
- Lab Member: [Ryosuke Hiyama](), mf23095@shibaura-it.ac.jp <br>
- Lab Leader: [Hiroki Sato](), hiroki@shibaura-it.ac.jp <br>
