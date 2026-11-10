# Clipboard Screen (Fabric, Minecraft 26.3)

Скриншот на **F2** сразу попадает в буфер обмена — можно вставить в Discord/Telegram через Ctrl+V.

## Возможности
- Копирование скриншота в буфер обмена (как изображение PNG).
- Уведомление в чате: «Скриншот скопирован в буфер обмена» (или сообщение об ошибке).
- Настройки (через Mod Menu → Clipboard Screen → ⚙): вкл/выкл копирование, вкл/выкл уведомления.
  Без Mod Menu — файл `config/clipboardscreen.json`.
- Языки: English, Español, Русский, Қазақша, Українська, Беларуская.

## Требования
Minecraft 26.3, Fabric Loader ≥ 0.19.5, Java 25. Fabric API не нужен. Mod Menu — по желанию.

## Как работает буфер обмена
| ОС | Основной способ | Запасной |
|---|---|---|
| Windows | Java AWT — мгновенно | PowerShell |
| Linux | `wl-copy` (Wayland) / `xclip` (X11) — картинка остаётся в буфере и после закрытия игры | Java AWT — работает без установленных утилит |
| macOS | osascript | — |

В буфер кладётся сразу два формата: исходный PNG (точные пиксели — для Discord, браузеров) и обычное изображение (для Paint, Word и старых программ).

Flatpak-лаунчер (Prism на Bazzite и т.п.): песочница не видит `wl-copy` хоста. Мод пробует
`flatpak-spawn --host`, для этого лаунчеру нужен доступ:
`flatpak override --user --talk-name=org.freedesktop.Flatpak org.prismlauncher.PrismLauncher`

## Сборка
```
./gradlew build        # Windows: gradlew.bat build
```
Готовый jar: `build/libs/clipboard-screen-1.0.0+mc26.3.jar`.
Или залей проект на GitHub — workflow в `.github/workflows/build.yml` соберёт jar сам (вкладка Actions → Artifacts).
