(ns rnntest.shared.ui
  (:require [reagent.core :as r]
            [print.foo :as pf :include-macros true]))

(set! js/window.React (js/require "react-native"))

(def text (r/adapt-react-class (.-Text js/React)))
(def view (r/adapt-react-class (.-View js/React)))
(def scroll-view (r/adapt-react-class (.-ScrollView js/React)))
(def image (r/adapt-react-class (.-Image js/React)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js/React)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity js/React)))
(def list-view (r/adapt-react-class (.-ListView js/React)))
(def activity-indicator-ios (r/adapt-react-class (.-ActivityIndicatorIOS js/React)))
(def list-item (r/adapt-react-class (js/require "react-native-listitem")))
(def image-progress (r/adapt-react-class (js/require "react-native-image-progress")))
(def LinkingIOS (.-LinkingIOS js/React))

(defn alert [title]
  (.alert (.-Alert js/React) title))

(defn ^:private set-static-props! [component statics]
  (let [c (r/reactify-component component)]
    (doseq [field statics]
      (aset component (name (key field)) (clj->js (val field))))
    component))

(defn open-url [url]
  (.openURL LinkingIOS url))

(defn create-class-with-statics [spec]
  (-> (r/create-class spec)
      r/reactify-component
      (set-static-props! (:statics spec))))