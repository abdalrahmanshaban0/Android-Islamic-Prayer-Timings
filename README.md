# 🕌 Islamic Prayer Timings for Android
[![API](https://img.shields.io/badge/API-27%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=27)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-orange.svg)](https://www.gnu.org/licenses/gpl-3.0)

An Android application that displays Islamic prayer times with a persistent notification showing the **next prayer name, time, and countdown**. Built with Kotlin, this app offers **24-hour / 12-hour format switching**, **Arabic & English language support**, and runs a foreground service to keep you updated throughout the day.

## 📱 Features

- ✅ Prayer times fetched from [Aladhan API](https://aladhan.com/prayer-times-api)
- 🔔 Sticky notification with:
  - Next prayer name
  - Time remaining countdown
  - All prayer timings in the notification inbox
- 🌍 Arabic and English UI support (auto based on system language)
- 🕒 Time format switch (24-hour or AM/PM)
- 🗣 Adhan recited by **Mansour Al-Zaharani**
- ⚙️ Foreground service updates the notification even after reboot (TODO)
- 🔁 Automatic update every 24 hours via `WorkManager` (TODO)

<div align="center">
  <img src="https://github.com/user-attachments/assets/70f7cf8f-089d-435e-b588-de1394b72084" width="33%" />
  <img src="https://github.com/user-attachments/assets/2df1b858-c7a2-4076-919b-a7d7086b78af" width="33%" />
  <img src="https://github.com/user-attachments/assets/061c4175-13d9-4d57-83d7-46e91e4ae8e9" width="33%" />
</div>
