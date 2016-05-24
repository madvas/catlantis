(ns env.ios.main
  (:require [catlantis.ios.core :as core]))

(enable-console-print!)
(aset js/console "disableYellowBox" true)
(core/init)


