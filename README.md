# ALEX: Mobile App

Мобильное приложение для управления умной колонкой ALEX.

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## О проекте

ALEX Mobile App — это Android-приложение на Kotlin, созданное для первичной настройки и управления умной колонкой ALEX на базе микроконтроллера ESP32.

В отличие от коммерческих аналогов (Google Home, Яндекс), где логика скрыта от пользователя, ALEX — это открытая экосистема. Приложение выступает связующим звеном между пользователем и сервером.

Главная задача приложения — передать настройки Wi-Fi колонке по Bluetooth (BLE), так как у самой колонки нет экрана и клавиатуры.

---

## Особенности

- BLE-сканирование: Поиск колонки в радиусе действия по префиксу `ALEX-Setup`.
- Передача данных: Отправка SSID и пароля Wi-Fi через GATT-характеристики.
- Автоматическое сканирование сетей: Встроенный `WifiScanner` с запросом разрешений.
- Синхронизация с сервером:
  - Получение профиля (`/users/me`)
  - Загрузка и очистка истории запросов (`/users/me/history`)
  - Проверка статуса сервера (`/health`)
- Интерфейс: Material Design, темная тема, анимации элементов.

---

## Стек технологий

| Компонент | Технология |
| :--- | :--- |
| Язык | Kotlin |
| UI | XML Layouts, Material Components, ViewBinding |
| Архитектура | MVVM (частично), Repository |
| Асинхронность | Kotlin Coroutines, Flow |
| Сеть | Retrofit 2, OkHttp, Gson |
| Bluetooth | Android BLE API (BluetoothGatt) |
| Хранение | DataStore Preferences |
| Минимальный SDK | API 26 (Android 8.0) |

---

## Установка и запуск

Требования:
- Android Studio (последняя версия)
- JDK 11+
- Телефон или эмулятор с Android 8.0+

Шаги:

1. Клонируй репозиторий:
   ```bash
   git clone https://github.com/QwKent/MobileAlex.git
   cd MobileAlex
   ```
2. Открой проект в Android Studio:
`File -> Open -> выбери папку MobileAlex`
3. Настрой сервер:
   В файле `app/src/main/java/com/example/alex/api/ApiClient.kt` измени IP-адрес на свой:
   ```kotlin
   const val BASE_URL = "http://127.0.0.1:8000/"
   ```
4. Собери и запусти:
   Нажми кнопку Run или выполни в терминале:
   ```bash
   ./gradlew assembleDebug
   ```

---

# Лицензия
## Этот проект распространяется под лицензией MIT. Подробности в файле LICENSE.


## Файл 2: `LICENSE`

```text
MIT License

Copyright (c) 2026 QwKent

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
