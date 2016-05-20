(ns rnntest.utils
  (:require-macros [natal-shell.dimensions :as dim])
  (:require [rnntest.colors :refer [colors]]
            [medley.core :as m]
            [camel-snake-kebab.core :as cs :include-macros true]
            [clojure.walk :as w]))

(def js->cljk #(js->clj % :keywordize-keys true))
(def e-style-sheet (aget (js/require "react-native-extended-stylesheet") "default"))

(defn build-stylesheet
  ([] (build-stylesheet {}))
  ([vals]
   (.build e-style-sheet (clj->js vals))))

(def window (js->cljk (dim/get "window")))

(build-stylesheet
  {:screenWidth  (:width window)
   :screenHeight (:height window)})


(defn function? [x]
  (or (= js/Function (type x))
      (implements? cljs.core/IFn x)))

(defn apply-if [pred f x & args]
  (let [pred (if (function? pred) pred (constantly pred))]
    (if (pred x)
      (apply f x args)
      x)))

(defn obj->hash-map [obj]
  (let [ks (js/Object.keys obj)]
    (reduce #(assoc %1 (keyword %2) (js->clj (aget obj %2) :keywordize-keys true)) {} ks)))

(defn create-stylesheet [styles]
  (-> (m/map-vals #(apply-if map? (partial m/map-keys cs/->camelCase) %) styles)
      clj->js
      (->> (.create e-style-sheet))
      obj->hash-map
      ))
(def color colors)

(defn clear-console! []
  (.log js/console "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")  ; console.clear() somehow doesn't work in RN
  (when-let [clear (aget js/console "clear")]
    (clear)))

(defn walk-keys
  [f m]
  (let [f (fn [[k v]] [(f k) v])]
    ;; only apply to maps
    (w/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn walk-camelize-keys [m]
  (walk-keys (partial apply-if
                      #(or (string? %)
                           (symbol? %)
                           (keyword? %)) cs/->camelCase) m))

(def clj->camel->js (comp clj->js walk-camelize-keys))

