;
; I will probably make this into a library
;
(ns rnntest.ios.components.navigation
  (:refer-clojure :exclude [pop!])
  (:require-macros [print.foo :as pf])
  (:require [reagent.core :as r]
            [rnntest.utils :as u]
            [schema.core :as s :include-macros true]
            [camel-snake-kebab.core :as cs :include-macros true]))

(def *registered-screens* (atom {}))
(def *nav-state-for-next-mount* (atom nil))

(def Navigation (aget (js/require "react-native-navigation") "Navigation"))

(s/defschema NavScreenConfig
  {(s/optional-key :screen)      s/Keyword
   (s/optional-key :title)       s/Str
   (s/optional-key :screen-type) (s/enum :screen :modal :light-box)
   s/Any                         s/Any})

(s/defschema NavigationStack [NavScreenConfig])

(s/defschema Drawer {(s/optional-key :side)     (s/enum :left :right)
                     (s/optional-key :to)       (s/enum :open :closed)
                     (s/optional-key :animated) s/Bool})

(s/defschema Drawers {(s/enum :left :right) Drawer})

(s/defschema NavigationScreen
  {:component             s/Any
   :nav-config            NavScreenConfig
   :on-navigator-event-fn s/Any})

(s/defschema Navigator (s/cond-pre s/Keyword s/Any) #_(partial instance? js/Object))

(s/defschema NavState
  {:nav-stack                NavigationStack
   (s/optional-key :drawers) Drawers})

(def app-default-config
  {:navigator-style {:nav-bar-blur       true
                     ;:nav-bar-translucent true
                     :draw-under-nav-bar true}})

(declare apply-nav-state!)

(defn set-on-navigator-event! [navigator callback]
  (.setOnNavigatorEvent navigator (comp (partial callback @*registered-screens* navigator) u/js->cljk)))

(defn register-component! [name component]
  (.registerComponent Navigation name (fn [] component)))

(defn ^:private wrap-screen [component screen-name on-navigator-event-fn]
  (r/create-class
    {:component-will-mount
     (fn [this]
       (let [props (u/obj->hash-map this)
             navigator (get-in props [:props :navigator])]
         (swap! *registered-screens* assoc-in [screen-name :navigator] navigator)
         (when @*nav-state-for-next-mount*
           (apply-nav-state! navigator @*nav-state-for-next-mount*)
           (reset! *nav-state-for-next-mount* nil))
         (set-on-navigator-event! navigator on-navigator-event-fn)))
     :reagent-render
     (fn [props]
       [component props])}))

(defn register-reagent-component!
  ([screen-name component] (register-reagent-component! screen-name component nil))
  ([screen-name component config]
   (let [component (r/reactify-component component)]
     (when config
       (doseq [field (-> (merge app-default-config config)
                         u/walk-camelize-keys)]
         (aset component (name (key field)) (clj->js (val field)))))
     (register-component! (name screen-name) component))))

(s/defn register-screen! [screen :- NavigationScreen]
  (let [config (:nav-config screen)
        scr (:screen config)
        component (-> (:component screen)
                      (wrap-screen scr (:on-navigator-event-fn screen)))]
    (swap! *registered-screens* assoc-in [scr :config] config)
    (register-reagent-component! scr component config)))

(defn screen-config [id]
  (get-in @*registered-screens* [id :config]))

(defn screen-navigator [id]
  (get-in @*registered-screens* [id :navigator]))

(s/defn action! [action-name :- s/Keyword
                 navigator :- Navigator
                 config :- (s/maybe NavScreenConfig)
                 & args]
  (let [navigator (if (keyword? navigator)
                    (screen-navigator navigator)
                    navigator)
        f (aget navigator (cs/->camelCaseString action-name))
        config (if (seq config) config {})]
    (.apply f navigator (clj->js (concat [(u/clj->camel->js config)] args)))))

(defn start-tab-based-app! [config]
  (.startTabBasedApp Navigation (u/clj->camel->js config)))

(defn start-single-screen-app! [config]
  (.startSingleScreenApp Navigation (u/clj->camel->js config)))

(defn push! [navigator screen]
  (action! :push navigator screen))

(defn pop! [navigator screen]
  (action! :pop navigator screen))

(defn reset-to! [navigator config]
  (action! :reset-to navigator config))

(defn show-modal! [navigator config]
  (action! :show-modal navigator config))

(defn dismiss-modal! [navigator config]
  (action! :dismiss-modal navigator config))

(defn dismiss-all-modals! [navigator config]
  (action! :dismiss-all-modals navigator config))

(defn show-light-box! [navigator config]
  (action! :show-light-box navigator config))

(defn dismiss-light-box! [navigator config]
  (action! :dismiss-light-box navigator config))

(defn handle-deep-link! [navigator config]
  (action! :handle-deep-link navigator config))

(defn set-buttons! [navigator config]
  (action! :set-buttons navigator config))

(defn set-title! [navigator config]
  (action! :set-title navigator config))

(defn toggle-drawer! [navigator config]
  (action! :toggle-drawer navigator config))

(defn toggle-tabs! [navigator config]
  (action! :toggle-tabs navigator config))

(defn set-tab-badge! [navigator config]
  (action! :set-tab-badge navigator config))

(defn switch-to-tab! [navigator config]
  (action! :switch-to-tab navigator config))

(defn toggle-nav-bar! [navigator config]
  (action! :toggle-nav-bar navigator config))

(s/defn apply-nav-state-on-next-mount!
  ([nav-state] (apply-nav-state-on-next-mount! nav-state false))
  ([{:keys [nav-stack drawers]} :- NavState apply-last?]
    (when (seq nav-stack)
      (swap! *nav-state-for-next-mount* assoc :nav-stack
             (u/apply-if (not apply-last?) butlast nav-stack)))
    (when drawers
      (swap! *nav-state-for-next-mount* assoc :drawers drawers))))

(defn screen-type->action [type add?]
  (let [f (if add? first second)]
    (f (case type
         :modal [:show-modal :dismiss-modal]
         :light-box [:show-light-box :dismiss-light-box]
         [:push :pop]))))

(s/defn with-no-anim [item :- NavScreenConfig]
  (case (:screen-type item)
    :modal (assoc item :animation-type :none)
    :light-box (assoc item :animated false)
    item))


(s/defn push-screen!
  ([navigator item] (push-screen! navigator item false))
  ([navigator :- Navigator item :- NavScreenConfig no-anim?]
    (action! (screen-type->action (:screen-type (pf/look item)) true)
             navigator
             (u/apply-if no-anim? with-no-anim item))))

(s/defn pop-screen!
  ([navigator item] (pop-screen! navigator item false))
  ([navigator :- Navigator item :- NavScreenConfig no-anim?]
    (action! (screen-type->action item false)
             navigator
             (u/apply-if no-anim? with-no-anim item))))

(s/defn apply-nav-state! [navigator :- Navigator nav-state :- NavState]
  (doseq [item (:nav-stack nav-state)]
    (push-screen! navigator item true))
  (doseq [drawer (:drawers nav-state)]
    (toggle-drawer! navigator (merge (val drawer) {:animated false
                                                   :side     (key drawer)}))))



