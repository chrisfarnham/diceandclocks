(ns dice-and-clocks.i18n
  "A hand-rolled translation dictionary, not a library -- most of what
  needs translating here (SRD rule text, the intro page's credits) is
  hiccup with embedded [:b]/[:a] spans, not flat interpolatable strings,
  which template-string-oriented i18n libraries fight rather than help.
  Pure namespace: no re-frame/DOM/localStorage dependency, so it's usable
  from action-rolls.cljs (which has none today) without dragging any in.")

(def default-locale "en")
(def supported-locales #{"en" "ru"})

;; Russian plural forms depend on the last one/two digits of the count
;; (1 -> singular, 2-4 -> paucal, 0/5-20/25-30... -> plural), unlike
;; English's simple singular/plural split -- see `:dice/count-suffix`
;; below for the one place this app needs it.
(defn- ru-plural [n one few many]
  (let [n10 (mod n 10)
        n100 (mod n 100)]
    (cond
      (and (= n10 1) (not= n100 11)) one
      (and (<= 2 n10 4) (not (<= 12 n100 14))) few
      :else many)))

;; -- SRD result text -----------------------------------------------------
;;
;; English text and bold-emphasis spans copied verbatim from the Blades
;; in the Dark SRD, "ACTION ROLL" section (Blades-in-the-Dark-SRD.md,
;; the CONTROLLED/RISKY/DESPERATE subsections spanning lines 398-427);
;; see action_rolls.cljs's result-description for the lookup this feeds.
;; Russian entries are a from-scratch translation (not an official SRD
;; translation) preserving the same bold-span terms and paragraph count
;; per tier -- worth a native-speaker pass before this is considered
;; final/polished, same as the rest of the "ru" table below.
(def ^:private descriptions
  {"en"
   {:critical [[:p "Critical: You do it with " [:b "increased effect"] "."]]
    :controlled [[:p "You do it."]
                 [:p "You hesitate. Withdraw and try a different approach, or else do it with a"
                  " minor consequence: a " [:b "minor complication"] " occurs, you have "
                  [:b "reduced effect"] ", you suffer " [:b "lesser harm"]
                  ", you end up in a " [:b "risky"] " position."]
                 [:p "You falter. Press on by seizing a " [:b "risky"]
                  " opportunity, or withdraw and try a different approach."]]
    :risky [[:p "You do it."]
            [:p "You do it, but there's a consequence: you suffer " [:b "harm"] ", a "
             [:b "complication"] " occurs, you have " [:b "reduced effect"]
             ", you end up in a " [:b "desperate"] " position."]
            [:p "Things go badly. You suffer " [:b "harm"] ", a " [:b "complication"]
             " occurs, you end up in a " [:b "desperate"] " position, you "
             [:b "lose this opportunity"] "."]]
    :desperate [[:p "You do it."]
                [:p "You do it, but there's a consequence: you suffer " [:b "severe harm"]
                 ", a " [:b "serious complication"] " occurs, you have " [:b "reduced effect"] "."]
                [:p "It's the worst outcome. You suffer " [:b "severe harm"] ", a "
                 [:b "serious complication"] " occurs, you " [:b "lose this opportunity"]
                 " for action."]]}
   "ru"
   {:critical [[:p "Критический успех: вы делаете это с " [:b "усиленным эффектом"] "."]]
    :controlled [[:p "У вас получается."]
                 [:p "Вы колеблетесь. Отступите и попробуйте другой подход, или действуйте"
                  " с небольшим последствием: происходит " [:b "небольшое осложнение"] ", эффект "
                  [:b "снижен"] ", вы получаете " [:b "меньший урон"]
                  ", вы оказываетесь в " [:b "рискованной"] " позиции."]
                 [:p "Вы дрогнули. Продолжайте, воспользовавшись " [:b "рискованной"]
                  " возможностью, либо отступите и попробуйте другой подход."]]
    :risky [[:p "У вас получается."]
            [:p "У вас получается, но есть последствие: вы получаете " [:b "урон"] ", происходит "
             [:b "осложнение"] ", эффект " [:b "снижен"]
             ", вы оказываетесь в " [:b "отчаянной"] " позиции."]
            [:p "Всё идёт плохо. Вы получаете " [:b "урон"] ", происходит " [:b "осложнение"]
             ", вы оказываетесь в " [:b "отчаянной"] " позиции, вы "
             [:b "упускаете эту возможность"] "."]]
    :desperate [[:p "У вас получается."]
                [:p "У вас получается, но есть последствие: вы получаете " [:b "тяжёлый урон"]
                 ", происходит " [:b "серьёзное осложнение"] ", эффект " [:b "снижен"] "."]
                [:p "Это худший исход. Вы получаете " [:b "тяжёлый урон"] ", происходит "
                 [:b "серьёзное осложнение"] ", вы " [:b "упускаете эту возможность"]
                 " для действия."]]}})

(defn description [locale key tier]
  (or (get-in descriptions [locale key tier])
      (get-in descriptions [default-locale key tier])))

;; -- position/effect display labels --------------------------------------
;;
;; The canonical English values in action-rolls.cljs's `positions`/
;; `effects` vectors are never translated -- they're persisted to
;; Firebase and used as a lookup key (`(keyword (string/lower-case
;; position))`). These tables translate only the *displayed* label,
;; keyed by that canonical English value, so existing/future records
;; round-trip correctly regardless of which locale wrote or reads them.
(def ^:private position-label
  {"en" {"Controlled" "Controlled" "Risky" "Risky" "Desperate" "Desperate"}
   "ru" {"Controlled" "Контролируемая" "Risky" "Рискованная" "Desperate" "Отчаянная"}})

(def ^:private effect-label
  {"en" {"Great" "Great" "Standard" "Standard" "Limited" "Limited"}
   "ru" {"Great" "Высокий" "Standard" "Стандартный" "Limited" "Ограниченный"}})

(defn translate-position [position locale]
  (get-in position-label [locale position] position))

(defn translate-effect [effect locale]
  (get-in effect-label [locale effect] effect))

;; -- general UI dictionary -------------------------------------------------

(def ^:private dictionary
  {"en"
   {:ui/start "Start"
    :ui/channel-secret-hint "Your channel name is a shared secret for your group."
    :ui/channel-name-placeholder "Channel Name"
    :ui/user-name-placeholder "User Name"
    :ui/join "Join"
    :ui/restore "Restore"
    :ui/critical-banner "Critical!"
    :ui/roll-caption-placeholder "Roll caption"
    :ui/roll "Roll"
    :ui/message-placeholder "Message"
    :ui/send "Send"
    :ui/clock-caption-placeholder "Clock caption"
    :ui/theme-toggle-title "Toggle the color scheme for everyone in this channel"
    :ui/locale-toggle-title "Switch language"
    :ui/copy-address-hint "Copy and share this address "
    :ui/loading "Loading..."

    :format/quote (fn [s] (str "\"" s "\""))
    :dice/count-suffix (fn [size] (str " (" size " dice)"))

    :clock-event/created "created a new clock"
    :clock-event/advanced "advanced a clock"
    :clock-event/rolled-back "rolled back a clock"
    :clock-event/deleted (fn [sender] (str sender " deleted a clock."))
    :theme/switched-prefix (fn [theme-label] (str "switched the color scheme to " theme-label))

    :intro/description "Clocks and Dice is an assistant (dice roller, chat, and clock tracker)\n        for Evil Hat Productions' Blades in the Dark RPG."
    :intro/usage-warning "This site tends to work poorly in private browsing modes. You'll also want to allowlist\n     this site for your ad-blocker. Your channel name isn't a password or a security boundary —\n     it's just how you and your friends find the same game session instead of a stranger's.\n     Pick something specific enough that no one stumbles onto it by guessing, and share it only\n     with the people joining your game. This site is intended for casual use; please don't\n     share sensitive information here."
    :intro/copyright "Copyright 2021"
    :intro/donate "Donate"
    :intro/source-code-prefix "Source code available at "
    :intro/mit-license-suffix " under the MIT License"
    :intro/thanks-heading "Thanks to:"
    :intro/credit-skyjedi-link "Star Wars RPG game manager"
    :intro/credit-skyjedi-prefix "SkyJedi's "
    :intro/credit-skyjedi-suffix " for inspiration"
    :intro/credit-acegiak-link "acegiak at itch.io"
    :intro/credit-acegiak-suffix " for the cool clock images"
    :intro/credit-widd-prefix "Henry Widd's blog post, \""
    :intro/credit-widd-link "Wrapper-free Firebase with Clojurescript's Re-Frame"
    :intro/credit-widd-suffix "\" for technical inspiration"}

   "ru"
   {:ui/start "Начать"
    :ui/channel-secret-hint "Название канала — это общий секрет для вашей группы."
    :ui/channel-name-placeholder "Название канала"
    :ui/user-name-placeholder "Имя пользователя"
    :ui/join "Присоединиться"
    :ui/restore "Восстановить"
    :ui/critical-banner "Критический успех!"
    :ui/roll-caption-placeholder "Подпись броска"
    :ui/roll "Бросить"
    :ui/message-placeholder "Сообщение"
    :ui/send "Отправить"
    :ui/clock-caption-placeholder "Подпись часов"
    :ui/theme-toggle-title "Переключить цветовую схему для всех в этом канале"
    :ui/locale-toggle-title "Сменить язык"
    :ui/copy-address-hint "Скопируйте и поделитесь этим адресом "
    :ui/loading "Загрузка..."

    :format/quote (fn [s] (str "«" s "»"))
    :dice/count-suffix (fn [size] (str " (" size " " (ru-plural size "кубик" "кубика" "кубиков") ")"))

    ;; Present tense throughout this group of entries is a deliberate
    ;; choice, not a narrative-voice preference: Russian past-tense verbs
    ;; are gender-inflected (создал/создала) and there's no gender field
    ;; on a player name to agree with. Present tense doesn't inflect for
    ;; gender, so "Иван создаёт новые часы" is correct regardless of who
    ;; Иван is -- sidesteps the problem instead of guessing.
    :clock-event/created "создаёт новые часы"
    :clock-event/advanced "продвигает часы"
    :clock-event/rolled-back "отматывает часы назад"
    :clock-event/deleted (fn [sender] (str sender " удаляет часы."))
    :theme/switched-prefix (fn [theme-label] (str "меняет цветовую схему на " theme-label))

    :intro/description "Clocks and Dice — это помощник (бросок костей, чат и трекер часов)\n        для настольной ролевой игры Blades in the Dark от Evil Hat Productions."
    :intro/usage-warning "Этот сайт часто плохо работает в режиме приватного просмотра. Также стоит добавить\n     сайт в исключения вашего блокировщика рекламы. Название канала — это не пароль и не элемент\n     безопасности, а просто способ для вас и ваших друзей найти одну и ту же игровую сессию, а не\n     сессию незнакомцев. Выберите что-то достаточно специфичное, чтобы никто не наткнулся на него\n     случайно, и делитесь названием только с участниками вашей игры. Сайт предназначен для\n     непринуждённого использования — пожалуйста, не делитесь здесь конфиденциальной информацией."
    :intro/copyright "© 2021"
    :intro/donate "Поддержать"
    :intro/source-code-prefix "Исходный код доступен на "
    :intro/mit-license-suffix " по лицензии MIT"
    :intro/thanks-heading "Благодарности:"
    :intro/credit-skyjedi-link "менеджер игр Star Wars RPG"
    :intro/credit-skyjedi-prefix ""
    :intro/credit-skyjedi-suffix " от SkyJedi — за вдохновение"
    :intro/credit-acegiak-link "acegiak на itch.io"
    :intro/credit-acegiak-suffix " — за классные изображения часов"
    :intro/credit-widd-prefix "Запись в блоге Генри Уидда «"
    :intro/credit-widd-link "Wrapper-free Firebase with Clojurescript's Re-Frame"
    :intro/credit-widd-suffix "» — за техническое вдохновение"}})

(defn t
  "Look up `key` in `locale`'s table, falling back to English on any miss
  (unknown key or unsupported locale), then to `(str key)` as a
  last-resort visible marker -- never throws, never renders blank, so
  partial Russian coverage (or a future English-only key nobody's
  translated yet) never breaks the UI."
  [key locale]
  (or (get-in dictionary [locale key])
      (get-in dictionary [default-locale key])
      (str key)))

(defn tf
  "Like `t`, for function-valued entries -- looks up `key`, then applies
  it to `args`."
  [key locale & args]
  (if-let [f (or (get-in dictionary [locale key])
                  (get-in dictionary [default-locale key]))]
    (apply f args)
    (str key)))
