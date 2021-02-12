(ns diceandclocks.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [diceandclocks.core-test]))

(doo-tests 'diceandclocks.core-test)
