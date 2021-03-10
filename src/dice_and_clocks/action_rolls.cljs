(ns dice-and-clocks.action-rolls)

(def positions ["Controlled" "Risky" "Desparate"])

(def effects ["Great" "Standard" "Limited"])

(def combinations (for [p positions e effects] {:position p :effect e}))


; combinations